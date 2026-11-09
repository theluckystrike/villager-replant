#!/usr/bin/env python3
"""Draws the 512 px icon: three crop rows on farmland (carrot, wheat, beetroot) and a replant arrow.
Usage: draw-icon.py <out.png>   (also writes <out>-128.png)"""
import sys
from PIL import Image, ImageDraw

out = sys.argv[1]
S = 512
im = Image.new('RGBA', (S, S), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.rounded_rectangle([0, 0, S - 1, S - 1], 96, fill=(34, 52, 30))
# farmland rows
for i, (crop, leaf) in enumerate([((235, 125, 35), (90, 170, 60)), ((222, 190, 80), (170, 160, 60)), ((170, 40, 60), (80, 150, 70))]):
    y = 120 + i * 110
    d.rounded_rectangle([60, y + 40, 452, y + 80], 12, fill=(92, 60, 36))
    for k in range(5):
        x = 92 + k * 80
        d.polygon([(x - 6, y + 44), (x + 6, y + 44), (x + 2, y - 4), (x - 2, y - 4)], fill=leaf)
        d.polygon([(x, y + 44), (x - 22, y + 4), (x - 14, y)], fill=leaf)
        d.polygon([(x, y + 44), (x + 22, y + 4), (x + 14, y)], fill=leaf)
        d.ellipse([x - 13, y + 22, x + 13, y + 48], fill=crop)
# replant arrow badge
bx, by, br = 392, 392, 92
d.ellipse([bx - br, by - br, bx + br, by + br], fill=(245, 200, 60), outline=(34, 52, 30), width=10)
d.arc([bx - 52, by - 52, bx + 52, by + 52], 30, 320, fill=(34, 52, 30), width=18)
d.polygon([(bx + 50, by - 54), (bx + 66, by - 4), (bx + 18, by - 14)], fill=(34, 52, 30))
im.save(out)
im.resize((128, 128), Image.LANCZOS).save(out.replace('.png', '-128.png'))
