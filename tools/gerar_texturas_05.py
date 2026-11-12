"""Texturas da versão 0.5: rede elétrica, reator, eletrolisador, eclusa, cápsula, bastão de luz e rover.

Uso: python3 tools/gerar_texturas_05.py
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gerar_texturas_04 as t4  # noqa: E402

painel = t4.painel
ruido_px = t4.ruido_px
salvar = t4.salvar
CIANO = t4.CIANO
AMARELO = (250, 200, 40)


def cabo():
    img = Image.new("RGBA", (16, 16), (60, 66, 84, 255))
    d = ImageDraw.Draw(img)
    for i in range(0, 16, 4):
        d.line([(i, 0), (i, 15)], fill=(90, 98, 120, 255))
    d.line([(0, 7), (15, 7)], fill=(250, 170, 40, 255))
    d.line([(0, 8), (15, 8)], fill=(200, 120, 30, 255))
    return ruido_px(img, 4, 501)


def bateria_lado():
    img = painel((44, 50, 66), 502)
    d = ImageDraw.Draw(img)
    for x in (4, 8, 12):
        d.line([(x, 2), (x, 13)], fill=(70, 78, 98, 255))
    d.rectangle([6, 0, 9, 1], fill=(220, 60, 50, 255))
    return img


def bateria_topo():
    img = painel((150, 156, 170), 503)
    d = ImageDraw.Draw(img)
    d.rectangle([3, 3, 6, 6], fill=(220, 60, 50, 255))
    d.rectangle([9, 9, 12, 12], fill=(40, 60, 90, 255))
    d.line([(4, 4), (5, 5)], fill=(255, 200, 200, 255))
    return img


def brilho(cor, seed, alfa=200):
    img = Image.new("RGBA", (16, 16), cor + (alfa,))
    return ruido_px(img, 10, seed)


def he3_lado():
    img = painel((120, 128, 146), 504)
    d = ImageDraw.Draw(img)
    d.line([(0, 3), (15, 3)], fill=AMARELO + (255,))
    d.line([(0, 12), (15, 12)], fill=AMARELO + (255,))
    for x in range(1, 15, 3):
        d.point((x, 3), fill=(30, 30, 36, 255))
        d.point((x, 12), fill=(30, 30, 36, 255))
    return img


def he3_frente(aceso):
    img = painel((60, 66, 84), 505)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([2, 2, 13, 13], radius=3, fill=(20, 24, 34, 255), outline=(150, 156, 170, 255))
    if aceso:
        d.ellipse([4, 4, 11, 11], fill=(90, 240, 220, 255))
        d.ellipse([6, 6, 9, 9], fill=(230, 255, 250, 255))
    else:
        d.ellipse([5, 5, 10, 10], outline=(70, 90, 100, 255))
    return img


def eletro_agua(aceso):
    img = Image.new("RGBA", (16, 16), (60, 130, 220, 170))
    d = ImageDraw.Draw(img)
    rnd = random.Random(506 + aceso)
    n = 14 if aceso else 3
    for _ in range(n):
        x, y = rnd.randrange(16), rnd.randrange(16)
        d.ellipse([x, y, x + 1, y + 1], fill=(220, 245, 255, 230))
    return img


def porta(topo):
    img = painel((206, 212, 224), 510 + topo)
    d = ImageDraw.Draw(img)
    d.line([(8, 0), (8, 15)], fill=(110, 118, 136, 255))
    if topo:
        d.rounded_rectangle([3, 3, 12, 11], radius=4, fill=(40, 90, 150, 255), outline=(110, 118, 136, 255))
        d.line([(5, 5), (8, 5)], fill=(200, 230, 255, 255))
        d.rectangle([6, 13, 9, 14], fill=CIANO + (255,))
    else:
        for x in range(0, 16):
            c = AMARELO if (x // 2) % 2 == 0 else (30, 30, 36)
            d.point((x, 13), fill=c + (255,))
            d.point((x, 14), fill=c + (255,))
        d.rectangle([11, 4, 13, 7], fill=(40, 46, 60, 255))
        d.point((12, 5), fill=(90, 230, 120, 255))
    return img


def aviso():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = (AMARELO if ((x + y) // 3) % 2 == 0 else (30, 30, 36)) + (255,)
    return img


def icone_porta():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([3, 0, 12, 15], fill=(206, 212, 224, 255), outline=(110, 118, 136, 255))
    d.line([(8, 0), (8, 15)], fill=(110, 118, 136, 255))
    d.rounded_rectangle([5, 2, 10, 6], radius=2, fill=(40, 90, 150, 255))
    for x in range(4, 12):
        d.point((x, 13), fill=(AMARELO if x % 2 == 0 else (30, 30, 36)) + (255,))
    return img


def icone_capsula():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([1, 6, 14, 12], radius=3, fill=(220, 226, 236, 255), outline=(120, 126, 140, 255))
    d.rounded_rectangle([3, 4, 12, 9], radius=3, fill=(130, 200, 255, 160), outline=(170, 180, 196, 255))
    d.rectangle([2, 12, 13, 13], fill=(150, 156, 170, 255))
    return img


def icone_bastao():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([7, 7, 8, 15], fill=(150, 156, 170, 255))
    d.rectangle([6, 2, 9, 7], fill=(225, 250, 255, 255))
    d.point((7, 3), fill=(255, 255, 255, 255))
    return img


def icone_rover():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 7, 14, 10], fill=(220, 226, 236, 255), outline=(120, 126, 140, 255))
    d.rectangle([4, 4, 10, 7], fill=(200, 206, 218, 255))
    d.rectangle([5, 5, 7, 6], fill=(60, 140, 220, 255))
    d.line([(3, 3), (11, 3)], fill=(30, 60, 140, 255))
    for x in (3, 8, 12):
        d.ellipse([x - 2, 10, x + 1, 13], fill=(50, 54, 64, 255))
    d.line([(12, 1), (12, 4)], fill=(150, 156, 170, 255))
    return img


def icone_racao():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([3, 2, 12, 14], radius=2, fill=(200, 206, 218, 255), outline=(120, 126, 140, 255))
    d.rectangle([4, 6, 11, 10], fill=(40, 120, 220, 255))
    d.line([(5, 8), (10, 8)], fill=(250, 200, 40, 255))
    d.line([(4, 3), (11, 3)], fill=(240, 244, 250, 255))
    return img


# ---------------- rover (128x128, mesmo layout do RoverModel) ----------------

def rover():
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    rnd = random.Random(520)

    def area(u, v, w, h, d, cor, amp=6, faces=None):
        # caixa: largura total 2*(d+w), altura d+h
        for y in range(v, v + d + h):
            for x in range(u, u + 2 * (d + w)):
                if 0 <= x < 128 and 0 <= y < 128:
                    k = rnd.randint(-amp, amp)
                    img.putpixel((x, y), tuple(max(0, min(255, c + k)) for c in cor) + (255,))
        return (u, v, w, h, d)

    branco = (226, 230, 238)
    cinza = (120, 126, 140)
    escuro = (50, 54, 64)
    d = ImageDraw.Draw(img)
    # chassi 22x4x34
    area(0, 0, 22, 4, 34, cinza)
    for x in range(0, 112, 6):
        d.line([(x, 34), (x + 2, 34)], fill=AMARELO + (255,))
    # para-choque 20x3x2
    area(0, 40, 20, 3, 2, escuro)
    # cabine 18x10x14 (frente = face norte: u+d .. u+d+w, v+d .. v+d+h)
    area(0, 46, 18, 10, 14, branco)
    d.line([(0, 66), (63, 66)], fill=(250, 140, 40, 255))
    # bandeira do Brasil na lateral da cabine (face leste: u .. u+d)
    d.rectangle([3, 61, 10, 66], fill=(0, 151, 57, 255))
    d.polygon([(4, 63.5), (6.5, 61.5), (9, 63.5), (6.5, 65.5)], fill=(254, 221, 0, 255))
    d.point((6, 63), fill=(1, 33, 105, 255))
    # vidro 16x7x1
    area(64, 46, 16, 7, 1, (60, 140, 220), 10)
    d.line([(66, 48), (72, 48)], fill=(200, 230, 255, 255))
    # teto solar 20x2x26: topo = u+d .. u+d+w, v .. v+d
    area(0, 72, 20, 2, 26, (24, 44, 96), 8)
    for x in range(26, 46, 4):
        d.line([(x, 72), (x, 97)], fill=(150, 160, 180, 255))
    for y in range(72, 98, 4):
        d.line([(26, y), (45, y)], fill=(150, 160, 180, 255))
    # carga 16x7x10
    area(0, 101, 16, 7, 10, (200, 206, 218))
    d.line([(0, 113), (51, 113)], fill=(40, 120, 220, 255))
    # antena
    area(114, 0, 1, 10, 1, (190, 196, 210))
    area(112, 40, 4, 2, 4, (220, 60, 50))
    # rodas
    for (v, w, h, dd) in [(60, 5, 8, 8), (76, 5, 6, 10), (92, 5, 10, 6)]:
        area(96, v, w, h, dd, escuro, 8)
        for y in range(v, v + dd + h, 2):
            d.point((96 + dd // 2, y), fill=(90, 96, 110, 255))
    return img


def main():
    salvar(cabo(), "block/cable.png")
    salvar(bateria_lado(), "block/battery_side.png")
    salvar(bateria_topo(), "block/battery_top.png")
    salvar(brilho((90, 240, 120), 507), "block/battery_glow.png")
    salvar(he3_lado(), "block/he3_side.png")
    salvar(brilho((40, 80, 90), 508, 230), "block/he3_core.png")
    salvar(brilho((90, 240, 220), 509, 230), "block/he3_core_on.png")
    salvar(he3_frente(False), "block/he3_front.png")
    salvar(he3_frente(True), "block/he3_front_on.png")
    salvar(eletro_agua(False), "block/electrolyzer_water.png")
    salvar(eletro_agua(True), "block/electrolyzer_water_on.png")
    salvar(brilho((70, 80, 96), 511, 255), "block/lamp_off.png")
    salvar(porta(False), "block/airlock_door_bottom.png")
    salvar(porta(True), "block/airlock_door_top.png")
    salvar(aviso(), "block/airlock_warning.png")
    salvar(icone_porta(), "item/airlock_door.png")
    salvar(icone_capsula(), "item/sleeping_pod.png")
    salvar(icone_bastao(), "item/light_stick.png")
    salvar(icone_rover(), "item/rover_kit.png")
    salvar(icone_racao(), "item/space_ration.png")
    salvar(rover(), "entity/rover.png")
    print("texturas 0.5 geradas")


if __name__ == "__main__":
    main()
