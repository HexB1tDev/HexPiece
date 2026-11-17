"""tools/sword_check.py [regex,regex]: every sword animation through the pose check, every problem kind with its worst depth (2.3.2)."""
"""Per-move summary of the clearance check: every problem kind with its worst depth and ticks."""
import multiprocessing, os, re, sys, json
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sword_anims as sa
import sword_rig as rig
sa.SOLVE = False
sa.build()

def one(name):
    move = sa.MOVES[name]
    rig.set_sword(move.profile.key)
    return name, sa.clearance(move)

if __name__ == "__main__":
    names = sorted(sa.MOVES)
    if len(sys.argv) > 1:
        names = [n for n in names if any(re.fullmatch(p, n) for p in sys.argv[1].split(","))]
    with multiprocessing.get_context("fork").Pool(4) as pool:
        res = pool.map(one, names, chunksize=1)
    kinds = {}
    bad = 0
    out = {}
    for name, probs in res:
        if not probs:
            continue
        bad += 1
        per = {}
        for t, msg in probs:
            m = re.match(r"(.*?)(?: \(([-\d.]+)\))?$", msg)
            k, d = m.group(1), float(m.group(2)) if m.group(2) else 0.0
            per.setdefault(k, []).append((t, d))
        out[name] = per
        print(name)
        for k, lst in per.items():
            w = max(lst, key=lambda x: x[1])
            ticks = sorted(set(t for t, _ in lst))
            print(f"    {k}: n={len(lst)} worst {w[1]:.2f} at t{w[0]}  ticks {ticks[:12]}{'...' if len(ticks) > 12 else ''}")
            kinds.setdefault(k, []).append((name, w[1]))
    print(f"\n{bad}/{len(names)} moves with problems")
    for k, lst in sorted(kinds.items(), key=lambda kv: -len(kv[1])):
        ds = sorted(d for _, d in lst)
        print(f"  {k}: {len(lst)} moves, median {ds[len(ds)//2]:.2f}, worst {max(lst, key=lambda x: x[1])}")
    os.makedirs(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "build"), exist_ok=True)
    json.dump(out, open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "build", "sword_check.json"), "w"))
