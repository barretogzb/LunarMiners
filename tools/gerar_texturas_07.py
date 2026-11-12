"""Texturas da 0.7: resina e ovos da colmeia, Núcleo da Rainha, Cuspidor e Rainha.

Uso: python3 tools/gerar_texturas_07.py
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gerar_texturas as gt  # noqa: E402
import gerar_texturas_04 as t4  # noqa: E402

salvar = t4.salvar
RESINA = [(36, 20, 44), (52, 28, 62), (68, 38, 80), (84, 50, 96)]


def resina(brilho=False, seed=701):
    img = gt.base_pedra(RESINA, seed)
    px = img.load()
    rnd = random.Random(seed + 1)
    # veios orgânicos
    x, y = rnd.randrange(16), 0
    for _ in range(22):
        px[x % 16, y % 16] = ((150, 255, 120) if brilho else (100, 60, 120)) + (255,)
        x += rnd.choice([-1, 0, 1])
        y += 1 if rnd.random() < 0.7 else 0
    if brilho:
        for _ in range(6):
            px[rnd.randrange(16), rnd.randrange(16)] = (200, 255, 170, 255)
    return img


def ovo(topo=False):
    img = Image.new("RGBA", (16, 16), (60, 70, 50, 255))
    px = img.load()
    rnd = random.Random(710 + topo)
    for y in range(16):
        for x in range(16):
            v = rnd.randint(-8, 8)
            px[x, y] = (70 + v, 84 + v, 56 + v, 255)
    d = ImageDraw.Draw(img)
    if topo:
        d.line([(4, 8), (8, 6), (12, 9)], fill=(160, 255, 120, 255))
        d.line([(8, 6), (8, 2)], fill=(160, 255, 120, 255))
    else:
        for y in (3, 8, 13):
            d.line([(0, y), (15, y + rnd.randint(-1, 1))], fill=(44, 52, 36, 255))
        d.point((5, 6), fill=(150, 240, 110, 255))
        d.point((11, 11), fill=(150, 240, 110, 255))
    return img


def nucleo():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([2, 2, 13, 13], fill=(70, 20, 100, 255), outline=(30, 8, 46, 255))
    d.ellipse([5, 5, 10, 10], fill=(190, 80, 255, 255))
    d.ellipse([6, 6, 8, 8], fill=(255, 220, 255, 255))
    for a in range(0, 360, 60):
        r = math.radians(a)
        d.point((7.5 + math.cos(r) * 6, 7.5 + math.sin(r) * 6), fill=(150, 255, 120, 255))
    return img


def humanoide(pele, corpo, olho, seed, coroa=False, sacos=False):
    """Pele 64x64 no layout de zumbi (só a metade de cima é usada)."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = img.load()
    rnd = random.Random(seed)

    def preencher(x0, y0, x1, y1, pal):
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = pal[rnd.randrange(len(pal))] + (255,)

    preencher(0, 0, 32, 16, pele)
    preencher(16, 16, 40, 32, corpo)
    preencher(40, 16, 56, 32, pele)
    preencher(0, 16, 16, 32, corpo)
    for (x, y) in [(8, 10), (9, 10), (8, 11), (9, 11), (10, 11), (9, 12), (10, 12),
                   (14, 10), (15, 10), (13, 11), (14, 11), (15, 11), (13, 12), (14, 12)]:
        px[x, y] = (8, 6, 10, 255)
    px[9, 10] = olho + (255,)
    px[14, 10] = olho + (255,)
    if coroa:
        # cristas na cabeça (topo 8..16,0..8 e frente da testa)
        for x in range(8, 16):
            px[x, 8] = (200, 90, 255, 255)
            if x % 2 == 0:
                px[x, 0] = (230, 140, 255, 255)
                px[x, 1] = (200, 90, 255, 255)
        for x in range(16, 24):
            px[x, 3] = (200, 90, 255, 255)
    if sacos:
        # bolsas de ácido no peito
        for (x, y) in [(21, 22), (22, 22), (21, 23), (22, 23), (25, 22), (26, 22), (25, 23), (26, 23)]:
            px[x, y] = (200, 255, 80, 255)
    # boca
    for x in range(11, 13):
        px[x, 14] = (30, 10, 20, 255)
    return img


def main():
    salvar(resina(False, 701), "block/alien_resin.png")
    salvar(resina(True, 702), "block/alien_resin_glow.png")
    salvar(ovo(False), "block/alien_egg.png")
    salvar(ovo(True), "block/alien_egg_top.png")
    salvar(nucleo(), "item/queen_core.png")
    salvar(humanoide([(150, 170, 70), (170, 190, 80), (130, 150, 60)], [(70, 40, 90), (84, 50, 104), (60, 32, 78)],
                     (220, 255, 90), 720, sacos=True), "entity/alien_spitter.png")
    salvar(humanoide([(50, 26, 70), (62, 32, 86), (40, 20, 58)], [(30, 16, 44), (40, 20, 56), (24, 12, 34)],
                     (240, 120, 255), 721, coroa=True), "entity/alien_queen.png")
    print("texturas 0.7 geradas")


if __name__ == "__main__":
    main()
