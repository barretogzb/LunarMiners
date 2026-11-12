"""Texturas da 0.6: grande painel solar, torre de comunicação, meteorito, scanner, jetpack e o rover grande.

Uso: python3 tools/gerar_texturas_06.py
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
ruido_px = t4.ruido_px
AMARELO = (250, 200, 40)


def celula_grande():
    img = Image.new("RGBA", (64, 64), (18, 36, 88, 255))
    d = ImageDraw.Draw(img)
    for i in range(0, 64, 8):
        d.line([(i, 0), (i, 63)], fill=(140, 150, 172, 255))
        d.line([(0, i), (63, i)], fill=(140, 150, 172, 255))
    rnd = random.Random(601)
    for _ in range(60):
        x, y = rnd.randrange(64), rnd.randrange(64)
        if x % 8 and y % 8:
            d.point((x, y), fill=(70, 110, 200, 255))
    for i in range(10):
        d.point((4 + i, 18 - i), fill=(170, 210, 255, 255))
    d.rectangle([0, 0, 63, 63], outline=(200, 206, 218, 255))
    return img


def meteorito():
    img = gt.base_pedra([(30, 26, 30), (44, 38, 42), (58, 50, 52), (72, 62, 60)], 602)
    px = img.load()
    rnd = random.Random(603)
    for _ in range(12):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = rnd.choice([(255, 140, 40), (255, 200, 80), (220, 90, 30)]) + (255,)
    for x, y in [(4, 4), (11, 9), (6, 12)]:
        px[x, y] = (255, 240, 180, 255)
    return img


def fragmento():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(3, 10), (5, 4), (10, 2), (13, 6), (12, 12), (7, 14)], fill=(52, 44, 48, 255), outline=(30, 24, 28, 255))
    for x, y in [(6, 7), (9, 5), (10, 10), (7, 11)]:
        d.point((x, y), fill=(255, 150, 50, 255))
    d.point((8, 8), fill=(255, 230, 160, 255))
    return img


def scanner():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([3, 1, 12, 14], radius=2, fill=(190, 196, 210, 255), outline=(100, 106, 120, 255))
    d.rectangle([4, 3, 11, 9], fill=(16, 40, 30, 255))
    d.arc([5, 4, 10, 9], 180, 360, fill=(80, 240, 140, 255))
    d.point((7, 6), fill=(200, 255, 220, 255))
    d.point((9, 5), fill=(255, 140, 50, 255))
    for x in (5, 8, 11):
        d.point((x, 12), fill=(40, 120, 220, 255))
    d.line([(10, 0), (12, 0)], fill=(150, 156, 170, 255))
    return img


def jetpack():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for x in (3, 9):
        d.rounded_rectangle([x, 2, x + 4, 11], radius=2, fill=(220, 226, 236, 255), outline=(110, 116, 130, 255))
        d.rectangle([x + 1, 11, x + 3, 12], fill=(70, 76, 90, 255))
        d.point((x + 2, 13), fill=(255, 160, 40, 255))
        d.point((x + 2, 14), fill=(255, 220, 120, 255))
    d.rectangle([7, 4, 9, 8], fill=(150, 156, 170, 255))
    d.point((5, 5), fill=(250, 140, 40, 255))
    d.point((11, 5), fill=(250, 140, 40, 255))
    return img


def farol_vermelho():
    img = Image.new("RGBA", (16, 16), (240, 60, 50, 255))
    return ruido_px(img, 12, 604)


# ---------------- rover grande (256x256, mesmo layout do RoverModel) ----------------

def rover():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    rnd = random.Random(620)
    d = ImageDraw.Draw(img)

    def area(u, v, w, h, dd, cor, amp=6):
        for y in range(v, v + dd + h):
            for x in range(u, u + 2 * (dd + w)):
                if 0 <= x < 256 and 0 <= y < 256:
                    k = rnd.randint(-amp, amp)
                    img.putpixel((x, y), tuple(max(0, min(255, c + k)) for c in cor) + (255,))

    branco = (228, 232, 240)
    metal = (150, 156, 172)
    escuro = (46, 50, 60)
    dourado = (214, 170, 70)
    # chassi 36x4x64 (com faixa amarela/preta nas laterais e folha dourada embaixo)
    area(0, 0, 36, 4, 64, metal)
    for x in range(0, 200, 4):
        c = AMARELO if (x // 4) % 2 == 0 else (30, 30, 36)
        d.line([(x, 64), (x + 3, 64)], fill=c + (255,))
        d.line([(x, 65), (x + 3, 65)], fill=c + (255,))
    d.rectangle([164, 0, 199, 63], fill=dourado + (255,))  # parte de baixo (face down)
    # para-choque
    area(0, 70, 32, 3, 2, escuro)
    # assentos (azul) e encostos
    area(0, 80, 12, 6, 12, (40, 110, 210))
    area(50, 80, 12, 14, 2, (40, 110, 210))
    # painel de controle com telas
    area(0, 100, 16, 16, 4, (70, 76, 92))
    d.rectangle([5, 106, 18, 112], fill=(20, 40, 60, 255))
    d.line([(6, 108), (14, 108)], fill=(80, 240, 200, 255))
    d.line([(6, 110), (11, 110)], fill=(250, 170, 40, 255))
    d.point((16, 107), fill=(240, 60, 50, 255))
    # colunas
    area(80, 80, 2, 36, 2, branco)
    # teto de painéis solares 40x2x42: face de cima em (u+d .. u+d+w, v .. v+d)
    area(0, 124, 40, 2, 42, (20, 40, 92), 8)
    for x in range(42, 82, 5):
        d.line([(x, 124), (x, 165)], fill=(150, 160, 180, 255))
    for y in range(124, 166, 5):
        d.line([(42, y), (81, y)], fill=(150, 160, 180, 255))
    # bandeira do Brasil na lateral do teto
    d.rectangle([4, 166, 16, 167], fill=(0, 151, 57, 255))
    # bagageiro
    area(0, 170, 32, 8, 12, (200, 206, 218))
    d.line([(0, 182), (87, 182)], fill=(250, 140, 40, 255))
    for x in range(14, 46, 8):
        d.rectangle([x, 172, x + 5, 180], outline=(120, 126, 140, 255))
    # antena e parabólica
    area(200, 0, 2, 54, 2, (200, 206, 218))
    area(204, 60, 12, 1, 12, (236, 240, 248))
    d.ellipse([207, 61, 221, 72], outline=(160, 166, 180, 255))
    # faróis
    area(210, 80, 6, 4, 2, (255, 250, 220), 3)
    # rodas: malha metálica com garras
    for (v, w_, h_, dd) in [(190, 8, 18, 12), (190, 8, 12, 18), (190, 8, 16, 16)]:
        pass
    area(100, 190, 8, 18, 12, escuro, 8)
    area(140, 190, 8, 12, 18, escuro, 8)
    area(192, 190, 8, 16, 16, escuro, 8)
    for u0, w0 in [(100, 40), (140, 52), (192, 48)]:
        for x in range(u0, u0 + w0, 3):
            d.line([(x, 190), (x, 221)], fill=(80, 86, 100, 255))
    return img


def main():
    salvar(celula_grande(), "block/solar_array_cell.png")
    salvar(meteorito(), "block/meteorite_ore.png")
    salvar(farol_vermelho(), "block/beacon_red.png")
    salvar(fragmento(), "item/meteorite_fragment.png")
    salvar(scanner(), "item/ore_scanner.png")
    salvar(jetpack(), "item/jetpack_module.png")
    salvar(rover(), "entity/rover.png")
    print("texturas 0.6 geradas")


if __name__ == "__main__":
    main()
