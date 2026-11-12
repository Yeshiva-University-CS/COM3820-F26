#!/usr/bin/env python3
"""
labutil.py: helpers you can use in lab.py. You do not need to change this file.

  list_files(root)                 every file under a directory, recursively
  file_size(path)                  size in bytes
  count_file(path)                 word counts of a whole file: {word: count}
  count_text(text)                 word counts of a string
  read_range(path, start, end)     the text of bytes [start, end) of a file, moved
                                   to word boundaries so that no word is cut in two
                                   or counted twice (see below)
  merge_counts(into, part)         add the counts in `part` into `into`
"""

import os
import re
from typing import Dict, List

Counts = Dict[str, int]
_WORD = re.compile(r"\b[a-zA-Z]{3,}\b")


def list_files(root: str) -> List[str]:
    return sorted(os.path.join(d, n) for d, _, names in os.walk(root) for n in names)


def file_size(path: str) -> int:
    return os.path.getsize(path)


def count_text(text: str) -> Counts:
    """Words are runs of 3 or more letters, lowercased."""
    counts: Counts = {}
    for w in _WORD.findall(text.lower()):
        counts[w] = counts.get(w, 0) + 1
    return counts


def count_file(path: str) -> Counts:
    with open(path, encoding="utf-8", errors="replace") as f:
        return count_text(f.read())


def merge_counts(into: Counts, part: Counts) -> Counts:
    for w, c in part.items():
        into[w] = into.get(w, 0) + c
    return into


def _word_byte(b: int) -> bool:
    return (48 <= b <= 57) or (65 <= b <= 90) or (97 <= b <= 122) or b == 95


def read_range(path: str, start: int, end: int) -> str:
    """
    The text of bytes [start, end) of a file. A word that straddles `start`
    is left to the previous range; a word that straddles `end` is read to its
    finish. So if you cut a file into consecutive ranges (0, a), (a, b), ...,
    (z, size), every word is counted exactly once, and merging the counts of
    all the ranges gives exactly the counts of the whole file.
    """
    with open(path, "rb") as f:
        if start > 0:
            f.seek(start - 1)
            prev, cur = f.read(1), f.read(1)
            if prev and cur and _word_byte(prev[0]) and _word_byte(cur[0]):
                start += 1
                while True:
                    b = f.read(1)
                    if not b or not _word_byte(b[0]):
                        break
                    start += 1
        if start >= end:
            return ""
        f.seek(start)
        data = f.read(end - start)
        if data and _word_byte(data[-1]):
            while True:
                b = f.read(1)
                if not b or not _word_byte(b[0]):
                    break
                data += b
    return data.decode("utf-8", errors="replace")
