#!/usr/bin/env python3
"""
T-01 (D27): ONE heavy test run at a time per machine (ported from DarkBlade tools/suite_lock.py, CI-14).

    python tools/suite_lock.py run -- ./mvnw -B verify -Pfull
                                    waits for the lock (a 15 s loop inside, one line when it starts waiting), runs the
                                    command, releases the lock and exits with the command's code
    python tools/suite_lock.py status
                                    who holds the lock, since when and with what command
    python tools/suite_lock.py release [--force]
                                    removes a lock this worktree holds (--force: whoever holds it)

- The lock is the folder <lock dir>/suite.lock, made with an atomic mkdir; owner.json inside says the worktree, the
  branch, the command, the process and its heartbeat. The lock dir is FUTTOBORU_LOCK_DIR, else <temp>/futtoboru-locks
  (one per user and machine: every worktree and agent of the machine shares it).
- A lock is stale, and the next waiter takes it over, when its heartbeat is 20 minutes old (--expire-min) or its
  process is gone. While the command runs the heartbeat is refreshed every minute: a long run keeps the lock, a killed
  agent loses it. Never write a wait loop of your own: this one waits inside and expires by itself.
- Every Maven verify of the fast/full tier and every smoke run goes through it (tools/test_level.py does this);
  tools/test-changed (a few -Dtest classes while coding) does not.
- Every run appends one JSON line to <lock dir>/history.jsonl (wait and run seconds, branch, command).
- Re-entrant: the command runs with FUTTOBORU_LOCK_<NAME>=<token> in its environment, so a nested
  `suite_lock.py run` (e.g. `suite_lock.py run -- python tools/test_level.py --run`, which locks its Maven run itself)
  sees that its parent holds the lock and runs at once instead of waiting for itself until --wait-min.
- Exit code: the command's; 75 when the lock could not be taken within --wait-min (60).
"""
import argparse
import datetime
import json
import os
import shutil
import socket
import subprocess
import sys
import tempfile
import threading
import time
import uuid

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOCK_DIR = os.environ.get("FUTTOBORU_LOCK_DIR", os.path.join(tempfile.gettempdir(), "futtoboru-locks"))
OWNER = "owner.json"
HISTORY = "history.jsonl"
EXPIRE_MIN = 20
WAIT_MIN = 60
POLL_S = 15
HEARTBEAT_S = 60
NOT_TAKEN = 75
# the environment variable that tells a nested run its parent holds the lock (with the lock's token)
HELD_ENV = "FUTTOBORU_LOCK_%s"
# a process created this long after the lock was taken holds a reused pid, not the lock
REUSE_SLACK_S = 5


def lock_path(name):
    return os.path.join(LOCK_DIR, name + ".lock")


def held_env(name):
    return HELD_ENV % "".join(c if c.isalnum() else "_" for c in name.upper())


def now():
    return time.time()


def stamp(seconds):
    return datetime.datetime.fromtimestamp(seconds).strftime("%H:%M:%S")


def branch():
    try:
        out = subprocess.run(["git", "-C", ROOT, "branch", "--show-current"], capture_output=True, text=True)
        return out.stdout.strip() or "(detached)"
    except OSError:
        return "?"


def read_owner(path):
    """owner.json of a lock, or None (a bare mkdir, or the owner is still writing it)"""
    try:
        with open(os.path.join(path, OWNER), encoding="utf-8") as handle:
            return json.load(handle)
    except (OSError, ValueError):
        return None


def write_owner(path, owner):
    target = os.path.join(path, OWNER)
    with open(target + ".tmp", "w", encoding="utf-8") as handle:
        json.dump(owner, handle, indent=1)
    os.replace(target + ".tmp", target)


def process_created(pid):
    """When the process was created (epoch seconds), False when there is none, None when unknown"""
    pid = int(pid)
    if os.name == "nt":
        import ctypes
        from ctypes import wintypes
        kernel = ctypes.WinDLL("kernel32", use_last_error=True)
        kernel.OpenProcess.restype = wintypes.HANDLE
        kernel.OpenProcess.argtypes = (wintypes.DWORD, wintypes.BOOL, wintypes.DWORD)
        kernel.GetExitCodeProcess.argtypes = (wintypes.HANDLE, ctypes.POINTER(wintypes.DWORD))
        kernel.GetProcessTimes.argtypes = (wintypes.HANDLE,) + (ctypes.POINTER(wintypes.FILETIME),) * 4
        kernel.CloseHandle.argtypes = (wintypes.HANDLE,)
        handle = kernel.OpenProcess(0x1000, False, pid)  # PROCESS_QUERY_LIMITED_INFORMATION
        if not handle:
            return None if ctypes.get_last_error() == 5 else False  # access denied: it exists
        try:
            code = wintypes.DWORD()
            if kernel.GetExitCodeProcess(handle, ctypes.byref(code)) and code.value != 259:  # not STILL_ACTIVE
                return False
            times = [wintypes.FILETIME() for _ in range(4)]
            if not kernel.GetProcessTimes(handle, *[ctypes.byref(t) for t in times]):
                return None
            ticks = (times[0].dwHighDateTime << 32) | times[0].dwLowDateTime  # 100 ns since 1601
            return ticks / 1e7 - 11644473600
        finally:
            kernel.CloseHandle(handle)
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    except OSError:
        return None
    try:
        with open("/proc/%d/stat" % pid) as handle:
            ticks = int(handle.read().rsplit(")", 1)[1].split()[19])
        with open("/proc/stat") as handle:
            boot = next(int(line.split()[1]) for line in handle if line.startswith("btime "))
        return boot + ticks / os.sysconf("SC_CLK_TCK")
    except (OSError, ValueError, IndexError, StopIteration):
        return None


def alive(pid, host, started=None):
    """False only when the process is surely gone (same host; no such pid, or the pid was reused after the lock)"""
    if not pid or host != socket.gethostname():
        return True
    try:
        created = process_created(pid)
    except (OSError, ValueError, AttributeError):
        return True
    if created is False:
        return False
    return not (created is not None and started and created > started + REUSE_SLACK_S)


def heartbeat_of(path, owner):
    if owner and owner.get("heartbeat"):
        return owner["heartbeat"]
    try:
        return os.path.getmtime(path)
    except OSError:
        return now()


def stale(path, owner, expire_min):
    """Why the lock is stale, or None"""
    age = now() - heartbeat_of(path, owner)
    if age > expire_min * 60:
        return "heartbeat %d min old" % (age // 60)
    if owner and not alive(owner.get("pid"), owner.get("host"), owner.get("started")):
        return "process %s is gone" % owner.get("pid")
    return None


def describe(path, owner):
    if not owner:
        return "someone without owner.json since %s" % stamp(heartbeat_of(path, owner))
    return "%s (%s) since %s, heartbeat %s: %s" % (owner.get("worktree"), owner.get("branch"), stamp(owner["started"]),
                                                  stamp(owner["heartbeat"]), owner.get("command"))


def take_over(path, reason, owner=None):
    """Moves a stale lock aside (atomic: of two waiters only one manages) and deletes it"""
    aside = "%s.stale-%s" % (path, uuid.uuid4().hex[:8])
    try:
        os.rename(path, aside)
    except FileNotFoundError:
        return True
    except OSError:
        return False
    print("suite_lock: taking over a stale lock of %s (%s)" % (owner.get("worktree") if owner else "?", reason),
          flush=True)
    shutil.rmtree(aside, ignore_errors=True)
    return True


def acquire(name, command, wait_min, expire_min):
    """The owner dict once the lock is ours, or None after wait_min"""
    os.makedirs(LOCK_DIR, exist_ok=True)
    path = lock_path(name)
    token = uuid.uuid4().hex
    deadline = now() + wait_min * 60
    started_waiting = None
    while True:
        try:
            os.mkdir(path)
        except FileExistsError:
            pass
        else:
            owner = {"token": token, "worktree": ROOT, "branch": branch(), "command": command, "pid": os.getpid(),
                     "host": socket.gethostname(), "started": now(), "heartbeat": now()}
            write_owner(path, owner)
            if started_waiting:
                print("suite_lock: %s taken after %d s of waiting" % (name, now() - started_waiting), flush=True)
            return owner
        owner = read_owner(path)
        reason = stale(path, owner, expire_min)
        if reason and take_over(path, reason, owner):
            continue
        if started_waiting is None:
            started_waiting = now()
            print("suite_lock: %s is held by %s; waiting (up to %d min)" % (name, describe(path, owner), wait_min),
                  flush=True)
        if now() > deadline:
            print("suite_lock: %s not taken in %d min; held by %s" % (name, wait_min, describe(path, owner)), flush=True)
            return None
        time.sleep(POLL_S)


def ours(path, owner):
    held = read_owner(path)
    return held is not None and held.get("token") == owner["token"]


def beat(name, owner, stop):
    path = lock_path(name)
    while not stop.wait(HEARTBEAT_S):
        if not ours(path, owner):
            print("suite_lock: WARNING: %s was taken over while the command ran" % name, flush=True)
            return
        owner["heartbeat"] = now()
        try:
            write_owner(path, owner)
        except OSError:
            pass


def log_history(record):
    try:
        with open(os.path.join(LOCK_DIR, HISTORY), "a", encoding="utf-8") as handle:
            handle.write(json.dumps(record, separators=(",", ":")) + "\n")
    except OSError as e:
        print("suite_lock: WARNING: could not write %s (%s)" % (HISTORY, e), flush=True)


class NotTaken(Exception):
    pass


class Held:
    """with Held("suite", "<command>"): the lock, its heartbeat while the block runs, and its release"""

    def __init__(self, name, command, wait_min=WAIT_MIN, expire_min=EXPIRE_MIN):
        self.name, self.command, self.wait_min, self.expire_min = name, command, wait_min, expire_min
        self.owner = None
        self.exit = None
        self.requested = None
        self.nested = False
        self.stop = threading.Event()

    def __enter__(self):
        self.requested = now()
        inherited = os.environ.get(held_env(self.name))
        held = read_owner(lock_path(self.name)) if inherited else None
        if held is not None and held.get("token") == inherited:
            # our parent process holds the lock (and keeps its heartbeat): run inside it, release nothing
            self.nested, self.owner = True, held
            return self
        self.owner = acquire(self.name, self.command, self.wait_min, self.expire_min)
        if self.owner is None:
            raise NotTaken(self.name)
        threading.Thread(target=beat, args=(self.name, self.owner, self.stop), daemon=True).start()
        return self

    def child_env(self):
        """The environment for the command: ours plus the token, so a nested run does not wait for us"""
        return dict(os.environ, **{held_env(self.name): self.owner["token"]})

    def __exit__(self, *exc):
        if self.nested:
            return
        self.stop.set()
        if ours(lock_path(self.name), self.owner):
            shutil.rmtree(lock_path(self.name), ignore_errors=True)
        acquired = self.owner["started"]
        log_history({"lock": self.name, "host": socket.gethostname(), "worktree": ROOT, "branch": self.owner["branch"],
                     "command": self.command[:120], "wait_s": round(acquired - self.requested),
                     "run_s": round(now() - acquired), "exit": self.exit if self.exit is not None else 1,
                     "at": datetime.datetime.now().isoformat(timespec="seconds")})


def runnable(command):
    """mvnw.cmd / a .bat needs an absolute path to run through cmd on Windows"""
    first = command[0]
    if os.path.isfile(first) and not os.path.isabs(first):
        return [os.path.abspath(first)] + command[1:]
    return command


def run_locked(command, lock="suite", wait_min=WAIT_MIN, expire_min=EXPIRE_MIN, cwd=ROOT):
    """Runs the command (a list) under the lock; @return its exit code, or NOT_TAKEN"""
    try:
        with Held(lock, " ".join(command), wait_min, expire_min) as held:
            started = now()
            try:
                code = subprocess.call(runnable(command), cwd=cwd, env=held.child_env(),
                                       shell=os.name == "nt" and command[0].lower().endswith((".bat", ".cmd")))
            except KeyboardInterrupt:
                code = 130
            held.exit = code
    except NotTaken:
        return NOT_TAKEN
    if not held.nested:
        print("suite_lock: %s released after %d s (exit %d)" % (lock, now() - started, code), flush=True)
    return code


def status(_):
    found = False
    if os.path.isdir(LOCK_DIR):
        for entry in sorted(os.listdir(LOCK_DIR)):
            if entry.endswith(".lock"):
                found = True
                path = os.path.join(LOCK_DIR, entry)
                owner = read_owner(path)
                reason = stale(path, owner, EXPIRE_MIN)
                print("%s: %s%s" % (entry[:-5], describe(path, owner), " [stale: %s]" % reason if reason else ""))
    if not found:
        print("no lock held in %s" % LOCK_DIR)
    return 0


def release_command(args):
    path = lock_path(args.lock)
    if not os.path.exists(path):
        print("%s is not held" % args.lock)
        return 0
    owner = read_owner(path)
    if not args.force and (not owner or os.path.normcase(owner.get("worktree", "")) != os.path.normcase(ROOT)):
        print("%s is held by %s, not by this worktree (--force to remove it anyway)" % (args.lock, describe(path, owner)))
        return 1
    shutil.rmtree(path, ignore_errors=True)
    print("%s released" % args.lock)
    return 0


def main():
    parser = argparse.ArgumentParser(description="One heavy test run at a time per machine (T-01)")
    commands = parser.add_subparsers(dest="action", required=True)
    running = commands.add_parser("run", help="wait for the lock, run the command, release it")
    running.add_argument("--lock", default="suite")
    running.add_argument("--wait-min", type=float, default=WAIT_MIN)
    running.add_argument("--expire-min", type=float, default=EXPIRE_MIN)
    running.add_argument("command", nargs=argparse.REMAINDER)
    commands.add_parser("status", help="who holds the lock")
    releasing = commands.add_parser("release", help="remove a lock this worktree holds")
    releasing.add_argument("--lock", default="suite")
    releasing.add_argument("--force", action="store_true")
    args = parser.parse_args()
    if args.action == "run":
        command = args.command[1:] if args.command and args.command[0] == "--" else args.command
        if not command:
            print("suite_lock: nothing to run (python tools/suite_lock.py run -- <command>)")
            return 2
        return run_locked(command, args.lock, args.wait_min, args.expire_min, cwd=os.getcwd())
    if args.action == "status":
        return status(args)
    return release_command(args)


if __name__ == "__main__":
    sys.exit(main())
