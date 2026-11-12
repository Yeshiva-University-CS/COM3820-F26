#!/usr/bin/env python3
"""
lab.py: your code. Each function takes a list of file paths and a number of
workers, and returns {path: counts} for every path, where counts is the
{word: count} map of that file (labutil.count_file gives it for one file).

Helpers are in labutil.py. run.py times and checks each function.
"""

from concurrent.futures import InterpreterPoolExecutor, ProcessPoolExecutor, ThreadPoolExecutor, as_completed
from typing import Dict, List

from labutil import count_file, count_text, file_size, merge_counts, read_range

Counts = Dict[str, int]


# Part 1 ------------------------------------------------------------------

def count_threads(paths: List[str], workers: int) -> Dict[str, Counts]:
    """A ThreadPoolExecutor with `workers` threads; one file per task."""
    raise NotImplementedError("Part 1: count_threads")


# Part 2 ------------------------------------------------------------------

def count_procs(paths: List[str], workers: int) -> Dict[str, Counts]:
    """A ProcessPoolExecutor with `workers` processes; one file per task."""
    raise NotImplementedError("Part 2: count_procs")


# Part 3 ------------------------------------------------------------------

def count_tasks(paths: List[str], workers: int, task_bytes: int) -> Dict[str, Counts]:
    """
    A ProcessPoolExecutor where each task is about `task_bytes` of text,
    whatever the file boundaries. How you build the tasks is up to you.
    """
    raise NotImplementedError("Part 3: count_tasks")
