#!/usr/bin/env python3
"""Create the bundled task data from the BSD-licensed Sailing reference data.

Usage:
  python tools/import_reference_tasks.py SeaChartTask.java chartables.tsv output.tsv
"""

import csv
import re
import sys
from pathlib import Path


ENTRY = re.compile(
    r"^\s*TASK_(?P<id>\d+)\((?P<id_again>\d+),\s*"
    r"SeaChartTaskType\.(?P<type>[A-Z_]+),\s*"
    r"VarbitID\.(?P<varbit>[A-Z0-9_]+),.*?"
    r"new WorldPoint\((?P<x>\d+),\s*(?P<y>\d+),\s*(?P<plane>\d+)\),\s*"
    r".*?,\s*(?P<level>\d+)\),$"
)

TYPE_PREFIXES = {
    "GENERIC": "SAILING_CHARTING_GENERIC_",
    "SPYGLASS": "SAILING_CHARTING_SPYGLASS_",
    "CURRENT_DUCK": "SAILING_CHARTING_CURRENT_DUCK_",
    "DRINK_CRATE": "SAILING_CHARTING_DRINK_CRATE_",
    "MERMAID_GUIDE": "SAILING_CHARTING_MERMAID_GUIDE_",
    "WEATHER": "SAILING_CHARTING_WEATHER_TROLL_",
}

TYPE_LABELS = {
    "GENERIC": "Charting",
    "SPYGLASS": "Spyglass",
    "CURRENT_DUCK": "Current duck",
    "DRINK_CRATE": "Drink crate",
    "MERMAID_GUIDE": "Mermaid guide",
    "WEATHER": "Weather station",
}


def title_case(identifier: str) -> str:
    words = []
    for word in identifier.split("_"):
        if word.isdigit() or word in {"KGP", "VS", "HQ"}:
            words.append(word)
        elif word == "UNKAH":
            words.append("Unkah")
        else:
            words.append(word.capitalize())
    return " ".join(words)


def display_name(task_type: str, varbit_name: str) -> str:
    prefix = TYPE_PREFIXES[task_type]
    descriptor = varbit_name.removeprefix(prefix).removesuffix("_COMPLETE")
    return f"{TYPE_LABELS[task_type]}: {title_case(descriptor)}"


def load_varbits(path: Path) -> dict[int, int]:
    result = {}
    with path.open(encoding="utf-8", newline="") as source:
        for row in csv.DictReader(source, delimiter="\t"):
            result[int(row["taskid"])] = int(row["varb"])
    return result


def main() -> int:
    if len(sys.argv) != 4:
        print(__doc__.strip(), file=sys.stderr)
        return 2

    enum_path, chartables_path, output_path = map(Path, sys.argv[1:])
    varbits = load_varbits(chartables_path)
    tasks = []

    for line in enum_path.read_text(encoding="utf-8").splitlines():
        match = ENTRY.match(line)
        if not match:
            continue
        values = match.groupdict()
        task_id = int(values["id"])
        if task_id != int(values["id_again"]):
            raise ValueError(f"Mismatched task id in {line}")
        if task_id not in varbits:
            raise ValueError(f"No numeric completion varbit for task {task_id}")

        tasks.append(
            [
                task_id,
                display_name(values["type"], values["varbit"]),
                values["type"],
                varbits[task_id],
                values["x"],
                values["y"],
                values["plane"],
                values["level"],
            ]
        )

    if not tasks:
        raise ValueError("No task entries were found in the reference enum")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8", newline="") as output:
        writer = csv.writer(output, delimiter="\t", lineterminator="\n")
        writer.writerow(["id", "title", "type", "completionVarbit", "x", "y", "plane", "level"])
        writer.writerows(tasks)

    print(f"Wrote {len(tasks)} task records to {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

