# Row purity from a real-run survey: share of planted plots whose crop matches the crop the row started with.
import csv, sys, collections
ROW = {0: 'carrots', 1: 'carrots', 2: 'potatoes', 3: 'potatoes', 4: 'wheat', 5: 'beetroots', 6: 'beetroots', 7: 'wheat', 8: 'wheat'}
for path in sys.argv[1:]:
    rows = list(csv.DictReader(open(path)))
    print('==', path)
    for s in sorted({r['sample'] for r in rows}):
        cells = {(int(r['x']), int(r['z'])): r['block'] for r in rows if r['sample'] == s}
        planted = {k: v for k, v in cells.items() if v != 'air' and not (k == (4, 4))}
        ok = sum(1 for (x, z), v in planted.items() if v == ROW[z])
        empty = sum(1 for k, v in cells.items() if v == 'air' and k != (4, 4))
        wrong = collections.Counter(f'{ROW[z]}->{v}' for (x, z), v in planted.items() if v != ROW[z])
        print(f'  sample {s}: plots {len(cells)}, planted {len(planted)}, right crop {ok} ({100*ok/max(1,len(planted)):.0f}%), empty {empty}, wrong {dict(wrong)}')
