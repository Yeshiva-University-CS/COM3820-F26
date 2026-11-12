#!/usr/bin/env python3
"""
run.py: times a strategy from lab.py on a dataset and checks its answer.

    uv run run.py DATASET --mode MODE [--workers LIST] [--task-kb LIST]

  DATASET    small or mixed (built by gen_small.py / gen_mixed.py)
  --mode     seq      one file after another (the baseline)
             threads  lab.count_threads   (Part 1)
             procs    lab.count_procs     (Part 2)
             tasks    lab.count_tasks     (Part 3)
  --workers  pool sizes to try, e.g. 1,2,4,8 (default: the number of CPUs)
  --task-kb  task sizes to try for --mode tasks, in KB, e.g. 64,1024,16384

The first run on a dataset times the sequential version and saves its answer
(data/<name>.expected.json); every later run is checked against it.
"""

import argparse
import hashlib
import json
import os
import sys
import time

from labutil import count_file, file_size, list_files

HERE = os.path.dirname(os.path.abspath(__file__))


def digest(counts):
    return hashlib.sha1(json.dumps(sorted(counts.items())).encode()).hexdigest()[:16]


def count_seq(paths, workers):
    return {p: count_file(p) for p in paths}


def expected_for(name, root, paths):
    path = os.path.join(HERE, "data", name + ".expected.json")
    if not os.path.exists(path):
        print(f"(first run on {name}: timing the sequential version ...)")
        t0 = time.perf_counter()
        result = count_seq(paths, 1)
        seconds = time.perf_counter() - t0
        with open(path, "w") as f:
            json.dump({"seq_seconds": seconds,
                       "files": {os.path.relpath(p, root): digest(c) for p, c in result.items()}}, f)
    with open(path) as f:
        return json.load(f)


def check(expected, root, paths, result):
    wrong = [p for p in paths if digest(result.get(p, {})) != expected["files"][os.path.relpath(p, root)]]
    return "OK" if not wrong else f"FAILED: {len(wrong)} file(s) wrong, e.g. {os.path.relpath(wrong[0], root)}"


def ints(text):
    return [int(x) for x in text.split(",") if x]


def main():
    ap = argparse.ArgumentParser(description="Time and check a strategy from lab.py.")
    ap.add_argument("dataset")
    ap.add_argument("--mode", default="seq", choices=["seq", "threads", "procs", "tasks"])
    ap.add_argument("--workers", default=str(os.cpu_count() or 1))
    ap.add_argument("--task-kb", default="1024")
    args = ap.parse_args()

    root = os.path.join(HERE, "data", args.dataset)
    if not os.path.isdir(root):
        sys.exit(f"no dataset {args.dataset}; run gen_small.py or gen_mixed.py first")
    paths = list_files(root)
    mb = sum(file_size(p) for p in paths) / 2**20
    expected = expected_for(args.dataset, root, paths)
    seq = expected["seq_seconds"]
    gil = sys._is_gil_enabled() if hasattr(sys, "_is_gil_enabled") else True
    print(f"{args.dataset}: {len(paths)} files, {mb:.0f} MB; sequential {seq:.2f} s; "
          f"{os.cpu_count()} CPUs; Python {sys.version.split()[0]} (GIL {'on' if gil else 'off'}); "
          f"mode {args.mode}")

    if args.mode == "seq":
        runs = [(1, None, count_seq)]
    else:
        import lab
        fn = {"threads": lab.count_threads, "procs": lab.count_procs}.get(args.mode)
        sizes = ints(args.task_kb) if args.mode == "tasks" else [None]
        runs = []
        for w in ints(args.workers):
            for kb in sizes:
                if args.mode == "tasks":
                    runs.append((w, kb, lambda p, w, kb=kb: lab.count_tasks(p, w, kb * 1024)))
                else:
                    runs.append((w, None, fn))

    print(f"{'workers':>8} {'task KB':>8} {'seconds':>8} {'speedup':>8}  check")
    for w, kb, fn in runs:
        t0 = time.perf_counter()
        result = fn(paths, w)
        secs = time.perf_counter() - t0
        print(f"{w:>8} {kb if kb else '':>8} {secs:>8.2f} {seq / secs:>7.1f}x  "
              f"{check(expected, root, paths, result)}")


if __name__ == "__main__":
    main()
