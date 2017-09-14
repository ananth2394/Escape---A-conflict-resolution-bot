import subprocess
import statistics
import os
import sys
from tqdm import tqdm

if len(sys.argv) is not 4:
    print("Usage: python3 %s player num_handles num_repetition" % sys.argv[0])
    sys.exit(0)

FNULL = open(os.devnull, "w")
player = sys.argv[1]
num_handles = int(sys.argv[2])
repetition = int(sys.argv[3])
results = []

for i in tqdm(range(repetition)):
    p = open("tmp.log", "w")
    subprocess.run(["java", "escape.sim.Simulator", "-p", player, "-d", str(num_handles)], stdout = p, stderr = FNULL)
    p.close
    with open("tmp.log", "rb") as log:
        log.seek(-2, 2)
        while log.read(1) != b"\n":
            log.seek(-2, 1)
        last = log.readline()
        parsed = [int(s) for s in last.split() if s.isdigit()]
        if (len(parsed) == 0):
            results.append(-1)
        results.extend(parsed)
        log.close()

print("Min, max: %d, %d" % (min(results), max(results)))
print("Median: %.2f" % statistics.median(results))
print("Average: %.2f" % statistics.mean(results))
print("Standard deviation: %.2f" % statistics.pstdev(results))
