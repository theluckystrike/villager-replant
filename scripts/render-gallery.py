#!/usr/bin/env python3
"""Renders the gallery images from real-run surveys.
Usage: render-gallery.py <vanilla survey.csv> <mod survey.csv> <minutes per sample> <outdir>"""
import csv, sys
from PIL import Image, ImageDraw, ImageFont

van, mod, step, out = sys.argv[1], sys.argv[2], float(sys.argv[3]), sys.argv[4]
FONT = '/System/Library/Fonts/Menlo.ttc'
SURF, INK, INK2, GRID = (26, 26, 25), (240, 239, 236), (195, 194, 183), (60, 60, 58)
VAN, MOD = (217, 89, 38), (57, 135, 229)
ROW = {0: 'carrots', 1: 'carrots', 2: 'potatoes', 3: 'potatoes', 4: 'wheat', 5: 'beetroots', 6: 'beetroots', 7: 'wheat', 8: 'wheat'}
CROP = {'carrots': ((235, 125, 35), 'C'), 'potatoes': ((196, 160, 92), 'P'), 'wheat': ((224, 205, 80), 'W'),
        'beetroots': ((178, 48, 72), 'B'), 'air': ((92, 60, 36), '')}


def font(n):
    return ImageFont.truetype(FONT, n)


def load(p):
    rows = list(csv.DictReader(open(p)))
    samples = sorted({int(r['sample']) for r in rows})
    out = {}
    for s in samples:
        out[s] = {(int(r['x']), int(r['z'])): r['block'] for r in rows if int(r['sample']) == s}
    return out


def purity(cells):
    """Plots (of 80) that grow the crop their row started with. Empty plots count as not right."""
    return sum(1 for (x, z), v in cells.items() if (x, z) != (4, 4) and v == ROW[z])


V, M = load(van), load(mod)


def chart(path):
    W, H = 1600, 900
    im = Image.new('RGB', (W, H), SURF)
    d = ImageDraw.Draw(im)
    d.text((60, 40), 'Plots (of 80) that grow the crop their row started with', font=font(36), fill=INK)
    d.text((60, 92), 'Fabric 26.3 dedicated server, 2 farmer villagers, 80 plots in 4 crop kinds, same world both runs', font=font(22), fill=INK2)
    lx = 60
    for label, col in (('Vanilla', VAN), ('With Villager Replant', MOD)):
        d.rounded_rectangle([lx, 140, lx + 22, 162], 4, fill=col)
        d.text((lx + 32, 137), label, font=font(22), fill=INK)
        lx += 32 + d.textlength(label, font=font(22)) + 40
    left, right, top, bottom = 160, W - 380, 210, H - 140
    for p in range(0, 81, 20):
        y = bottom - (bottom - top) * p / 80
        d.line([(left, y), (right, y)], fill=GRID, width=1)
        d.text((left - 20, y - 12), f'{p}', font=font(20), fill=INK2, anchor='ra')
    xs = sorted(V)
    X = lambda s: left + (right - left) * s / max(xs)
    for s in xs:
        d.text((X(s), bottom + 18), f'{s * step:g} min', font=font(20), fill=INK2, anchor='ma')
    for series, col, name in ((V, VAN, 'Vanilla'), (M, MOD, 'Villager Replant')):
        pts = [(X(s), bottom - (bottom - top) * purity(series[s]) / 80) for s in xs]
        d.line(pts, fill=col, width=4, joint='curve')
        for x, y in pts:
            d.ellipse([x - 7, y - 7, x + 7, y + 7], fill=col, outline=SURF, width=2)
        x, y = pts[-1]
        d.text((x + 18, y - 14), f'{name}  {purity(series[xs[-1]])}', font=font(22), fill=INK)
    d.text((60, H - 62), 'Stress test: 3x game speed, crops regrow about 13x faster than normal. Late in the run the farmers had no', font=font(18), fill=INK2)
    d.text((60, H - 38), 'wheat or beetroot seeds left, so the mod kept those plots empty instead of planting carrots or potatoes there.', font=font(18), fill=INK2)
    im.save(path)


def farms(path, last=None):
    W, H = 1600, 900
    im = Image.new('RGB', (W, H), SURF)
    d = ImageDraw.Draw(im)
    last = max(V) if last is None else last
    d.text((60, 40), f'The same farm after {last * step:g} minutes, seen from above', font=font(36), fill=INK)
    d.text((60, 92), 'Rows started as carrots, potatoes, wheat, beetroots, wheat (top to bottom). Water in the middle.', font=font(22), fill=INK2)
    cell = 64
    for ox, title, data in ((140, 'Vanilla', V[last]), (880, 'With Villager Replant', M[last])):
        d.text((ox, 160), f'{title}: {purity(data)} of 80 right', font=font(26), fill=INK)
        for (x, z), b in data.items():
            px, py = ox + x * cell, 210 + z * cell
            if (x, z) == (4, 4):
                d.rounded_rectangle([px + 2, py + 2, px + cell - 2, py + cell - 2], 6, fill=(50, 100, 200))
                continue
            col, letter = CROP.get(b, ((80, 80, 80), '?'))
            d.rounded_rectangle([px + 2, py + 2, px + cell - 2, py + cell - 2], 6, fill=col)
            if letter:
                d.text((px + cell / 2, py + cell / 2), letter, font=font(26), fill=(20, 20, 20), anchor='mm')
            if b != 'air' and b != ROW[z]:
                d.rounded_rectangle([px + 2, py + 2, px + cell - 2, py + cell - 2], 6, outline=(240, 239, 236), width=4)
    lx = 140
    for name in ('carrots', 'potatoes', 'wheat', 'beetroots', 'air'):
        col, letter = CROP[name]
        d.rounded_rectangle([lx, 820, lx + 28, 848], 5, fill=col)
        d.text((lx + 14, 834), letter, font=font(18), fill=(20, 20, 20), anchor='mm')
        label = 'empty farmland' if name == 'air' else name
        d.text((lx + 38, 822), label, font=font(20), fill=INK)
        lx += 60 + d.textlength(label, font=font(20)) + 30
    d.text((lx + 10, 822), 'White frame = wrong crop', font=font(20), fill=INK2)
    im.save(path)


chart(out + '/row-purity.png')
farms(out + '/farm-top-down.png')
farms(out + '/farm-top-down-4min.png', 2)
print('wrote', out)
