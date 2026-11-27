# Privacy rules for this repository

This repository is **public**. Everything committed here, including its
history, can be read by anyone and cannot really be taken back.

## Never commit

- Credentials: tokens, passwords, API keys, private keys, `settings.xml`
  with server passwords, `.env` files.
- Internal network details: LAN IPs, host names, shared-folder paths.
- Personal data: personal email addresses, phone numbers, home paths
  (`C:\Users\<name>`), anything about other people.
- Third-party material we have no licence to redistribute: papers, books,
  art and concept images found online. Keep a link in a doc instead.
- Business-private notes (finances, contracts, private plans).

## Where private material goes

- Outside the clone, in `C:\git\futtoboru-private\` (backed up by you, never
  pushed). A file that is not inside the working tree cannot be pushed by
  accident; `.gitignore` is only the second line of defence.
- Secrets for builds go in Jenkins credentials (`withCredentials`) or in a
  file in your home folder, never in the Jenkinsfile or the pom.

## Setup on every machine (once per clone)

```
git config user.email "77251262+Geomancer86@users.noreply.github.com"
git config core.hooksPath tools/hooks
```

The pre-commit hook (`tools/hooks/pre-commit`) blocks commits that add
secrets, private IPs, personal emails, private or reference files, or that
use a non-noreply author email. If `gitleaks` is installed it also runs it.
Bypass only for a confirmed false positive: `git commit --no-verify`.

## Before each push

- `git diff --cached --stat` and add paths explicitly (`git add <paths>`),
  never `git add -A`.
- If a secret slips in: revoke it first, then remove it. Removing it from the
  latest commit does not remove it from history.
