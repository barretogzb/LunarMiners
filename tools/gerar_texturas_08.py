"""Texturas da 0.8: foguete brasileiro, kit e combustível.

Uso: python3 tools/gerar_texturas_08.py
"""
import os
import random
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gerar_texturas_04 as t4  # noqa: E402

salvar = t4.salvar


def foguete():
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    rnd = random.Random(801)
    d = ImageDraw.Draw(img)

    def area(u, v, w, h, dd, cor, amp=5):
        for y in range(v, v + dd + h):
            for x in range(u, u + 2 * (dd + w)):
                if 0 <= x < 256 and 0 <= y < 256:
                    k = rnd.randint(-amp, amp)
                    img.putpixel((x, y), tuple(max(0, min(255, c + k)) for c in cor) + (255,))

    def faixas(u, v, w, h, dd):
        # faixas verde e amarela ao redor do corpo + janela da cápsula na frente
        lado_y0 = v + dd
        largura = 2 * (dd + w)
        for y, cor in [(lado_y0 + 10, (0, 151, 57)), (lado_y0 + 13, (254, 221, 0)), (lado_y0 + 60, (0, 151, 57)),
                       (lado_y0 + 63, (254, 221, 0))]:
            d.rectangle([u, y, u + largura - 1, y + 2], fill=cor + (255,))
        fx = u + dd + w // 2
        d.ellipse([fx - 4, lado_y0 + 2, fx + 3, lado_y0 + 9], fill=(40, 100, 180, 255), outline=(60, 64, 76, 255))
        d.point((fx - 2, lado_y0 + 4), fill=(200, 230, 255, 255))
        # "BR" em pixels
        for (x, y) in [(0, 0), (0, 1), (0, 2), (0, 3), (0, 4), (1, 0), (2, 1), (1, 2), (2, 3), (1, 4),
                       (4, 0), (4, 1), (4, 2), (4, 3), (4, 4), (5, 0), (6, 1), (5, 2), (6, 3), (6, 4)]:
            img.putpixel((fx - 3 + x, lado_y0 + 30 + y), (0, 60, 140, 255))

    branco = (236, 238, 244)
    area(0, 0, 20, 76, 12, branco)
    faixas(0, 0, 20, 76, 12)
    area(64, 0, 12, 76, 20, branco)
    faixas(64, 0, 12, 76, 20)
    area(0, 96, 17, 76, 17, (226, 228, 236))
    faixas(0, 96, 17, 76, 17)
    area(128, 0, 14, 10, 14, (230, 232, 240))
    area(128, 24, 8, 8, 8, (220, 60, 50))
    area(128, 40, 3, 6, 3, (200, 206, 218))
    area(128, 60, 2, 22, 10, (0, 120, 50))
    area(160, 60, 10, 22, 2, (0, 120, 50))
    area(128, 100, 14, 4, 14, (50, 52, 60), 8)
    return img


def icone_foguete():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 0), (10, 3), (10, 11), (6, 11), (6, 3)], fill=(236, 238, 244, 255), outline=(120, 126, 140, 255))
    d.polygon([(8, 0), (9, 2), (7, 2)], fill=(220, 60, 50, 255))
    d.ellipse([7, 4, 9, 6], fill=(40, 100, 180, 255))
    d.line([(6, 8), (10, 8)], fill=(0, 151, 57, 255))
    d.line([(6, 9), (10, 9)], fill=(254, 221, 0, 255))
    d.polygon([(6, 9), (4, 13), (6, 12)], fill=(0, 120, 50, 255))
    d.polygon([(10, 9), (12, 13), (10, 12)], fill=(0, 120, 50, 255))
    d.line([(7, 12), (9, 12)], fill=(60, 62, 70, 255))
    d.point((8, 14), fill=(255, 160, 40, 255))
    d.point((8, 15), fill=(255, 230, 120, 255))
    return img


def icone_combustivel():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([4, 2, 11, 14], radius=2, fill=(200, 206, 218, 255), outline=(100, 106, 120, 255))
    d.rectangle([5, 6, 10, 12], fill=(60, 220, 200, 255))
    d.line([(5, 8), (10, 8)], fill=(200, 255, 245, 255))
    d.rectangle([6, 0, 9, 2], fill=(120, 126, 140, 255))
    return img


def main():
    salvar(foguete(), "entity/rocket.png")
    salvar(icone_foguete(), "item/rocket_kit.png")
    salvar(icone_combustivel(), "item/rocket_fuel.png")
    print("texturas 0.8 geradas")


if __name__ == "__main__":
    main()
