#!/usr/bin/env python3
"""
gen_mixed.py: the dataset for Part 3.

    uv run gen_mixed.py          # data/mixed: 10,000 files of 1-4 KB
                                 # plus 2 files of about 100 MB and 60 MB (about 185 MB)
"""

import os
import random
import shutil
import sys

from textgen import write_files, write_text_file

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data", "mixed")


def main():
    if os.path.isdir(OUT) and os.listdir(OUT) and "--force" not in sys.argv:
        print(f"{OUT} already exists (use --force to rebuild)")
        return
    shutil.rmtree(OUT, ignore_errors=True)
    rng = random.Random(2)
    tiny = [rng.randint(1_000, 4_000) for _ in range(10_000)]
    write_files(os.path.join(OUT, "notes"), tiny, rng, subdirs=50, prefix="note")
    big = [100_000_000, 60_000_000]
    for i, size in enumerate(big):
        write_text_file(os.path.join(OUT, "logs", f"log_{i}.txt"), size, rng)
    print(f"wrote {len(tiny)} tiny files and {len(big)} big ones, "
          f"{(sum(tiny) + sum(big)) / 2**20:.0f} MB, to data/mixed")


if __name__ == "__main__":
    main()
