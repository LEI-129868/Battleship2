import json
import sys
from collections import defaultdict

# ---- CONFIG (tune thresholds here) ----
BRAIN_METHOD_MIN_SIGNALS = 3

# Map Qodana inspections to Brain Method signals
SIGNAL_RULES = {
    "LOC": ["MethodLength", "LongMethod"],
    "CYCLO": ["CyclomaticComplexity", "OverlyComplexMethod"],
    "NESTING": ["NestedBlockDepth"],
    "VARIABLES": ["MethodWithTooManyParameters"]
}


def load_sarif(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def extract_method_key(result):
    """Group issues by method (file + region)."""
    loc = result["locations"][0]["physicalLocation"]
    file_uri = loc["artifactLocation"]["uri"]

    region = loc.get("region", {})
    start_line = region.get("startLine", 0)

    # Approximate method identity
    return f"{file_uri}:{start_line}"


def classify_signal(rule_id):
    for signal, rules in SIGNAL_RULES.items():
        if rule_id in rules:
            return signal
    return None


def detect_brain_methods(sarif):
    runs = sarif.get("runs", [])
    if not runs:
        return []

    results = runs[0].get("results", [])

    method_signals = defaultdict(set)

    for result in results:
        rule_id = result.get("ruleId", "")
        signal = classify_signal(rule_id)

        if not signal:
            continue

        method_key = extract_method_key(result)
        method_signals[method_key].add(signal)

    brain_methods = []

    for method, signals in method_signals.items():
        if len(signals) >= BRAIN_METHOD_MIN_SIGNALS:
            brain_methods.append((method, signals))

    return brain_methods


def create_brain_method_results(brain_methods):
    results = []

    for method, signals in brain_methods:
        file_uri, line = method.split(":")

        result = {
            "ruleId": "BrainMethodDetected",
            "level": "warning",
            "message": {
                "text": f"Brain Method detected (signals: {', '.join(signals)})"
            },
            "locations": [
                {
                    "physicalLocation": {
                        "artifactLocation": {"uri": file_uri},
                        "region": {"startLine": int(line)}
                    }
                }
            ]
        }

        results.append(result)

    return results


def main(input_path, output_path):
    sarif = load_sarif(input_path)

    brain_methods = detect_brain_methods(sarif)
    new_results = create_brain_method_results(brain_methods)

    # Inject new rule
    run = sarif["runs"][0]

    if "tool" in run and "driver" in run["tool"]:
        rules = run["tool"]["driver"].setdefault("rules", [])
        rules.append({
            "id": "BrainMethodDetected",
            "name": "Brain Method",
            "shortDescription": {
                "text": "Method matches Brain Method detection strategy"
            }
        })

    # Append results
    run["results"].extend(new_results)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(sarif, f, indent=2)

    print(f"Detected {len(brain_methods)} Brain Methods")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python brain_method_detector.py input.sarif output.sarif")
        sys.exit(1)

    main(sys.argv[1], sys.argv[2])
