#!/usr/bin/env python3
"""
textgen.py: shared by the dataset generators. Writes files of English-like
text (common words plus computing vocabulary) of a given size.
"""

import os
import random

WORDS = """the be to of and a in that have I it for not on with he as you do at time person year way day thing man world life hand part child eye woman place work week case point government company number group problem fact water room money story month said make know get see come think look want give use find tell ask work seem feel try leave call keep let begin help show hear play run move live good new first last long great little own other old right big high different small large next early young important few public bad same able local sure national social political data system program process memory thread file code algorithm function class object array string integer boolean loop condition error debug compile execute parallel concurrent sequential optimize performance benchmark compression encryption network server client database query index cache buffer queue stack tree graph information question service student school state family business issue area level change result door health body community name president university research book study course power report computer technology internet application software hardware""".split()


def write_text_file(path: str, size: int, rng: random.Random) -> None:
    """Write about `size` bytes of sentences to `path`."""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    parts, total = [], 0
    while total < size:
        words = rng.choices(WORDS, k=rng.randint(5, 20))
        sentence = " ".join(words).capitalize() + rng.choice(".....!?") + "\n"
        parts.append(sentence)
        total += len(sentence)
    with open(path, "w", encoding="ascii") as f:
        f.write("".join(parts))


def write_files(out_dir: str, sizes, rng: random.Random, subdirs: int = 1, prefix: str = "file") -> None:
    """Write one file per size, spread over `subdirs` subdirectories."""
    for i, size in enumerate(sizes):
        sub = f"d{i % subdirs:02d}" if subdirs > 1 else ""
        write_text_file(os.path.join(out_dir, sub, f"{prefix}_{i:05d}.txt"), size, rng)
