#!/usr/bin/env python3
"""
gen_small.py: the dataset for Parts 1 and 2.

    uv run gen_small.py          # data/small: 2,000 files of 10-100 KB (about 105 MB)
"""

import os
import random
import shutil
import sys

from textgen import write_files

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data", "small")


def main():
    if os.path.isdir(OUT) and os.listdir(OUT) and "--force" not in sys.argv:
        print(f"{OUT} already exists (use --force to rebuild)")
        return
    shutil.rmtree(OUT, ignore_errors=True)
    rng = random.Random(1)
    sizes = [rng.randint(10_000, 100_000) for _ in range(2000)]
    write_files(OUT, sizes, rng, subdirs=20)
    print(f"wrote {len(sizes)} files, {sum(sizes) / 2**20:.0f} MB, to data/small")


if __name__ == "__main__":
    main()
