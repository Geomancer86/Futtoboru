"""Write a 3x nearest-neighbour preview of one rendered ground (for eyeballing only, not committed output).

    python tools/art/preview.py <ground-id> <out.png>
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import ground_art as g  # noqa: E402


def main(gid: str, out: str, scale: int = 3) -> None:
    cv = g.render(g.load(g.DESCRIPTORS / f"{gid}.json"))
    big = g.Canvas(cv.w * scale, cv.h * scale)
    for y in range(big.h):
        for x in range(big.w):
            big.set(x, y, cv.get(x // scale, y // scale))
    Path(out).write_bytes(g.png_bytes(big))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
