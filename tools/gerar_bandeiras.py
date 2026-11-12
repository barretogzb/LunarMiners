"""Desenha as bandeiras reais (48x32 = 3x2 blocos) e corta em 6 pedaços de 16x16.

Uso: python3 tools/gerar_bandeiras.py
"""
import math
import os

from PIL import Image, ImageDraw

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEX = os.path.join(RAIZ, "src/main/resources/assets/lunarminers/textures")
E = 8  # desenha 8x maior e reduz, para ficar com bordas suaves
W, H = 48 * E, 32 * E


def estrela(d, cx, cy, r, cor, rot=-90):
    pts = []
    for i in range(10):
        ang = math.radians(rot + i * 36)
        rr = r if i % 2 == 0 else r * 0.4
        pts.append((cx + math.cos(ang) * rr, cy + math.sin(ang) * rr))
    d.polygon(pts, fill=cor)


def faixas_h(d, cores):
    n = len(cores)
    for i, c in enumerate(cores):
        d.rectangle([0, H * i // n, W, H * (i + 1) // n], fill=c)


def faixas_v(d, cores):
    n = len(cores)
    for i, c in enumerate(cores):
        d.rectangle([W * i // n, 0, W * (i + 1) // n, H], fill=c)


def brasil(d):
    d.rectangle([0, 0, W, H], fill=(0, 151, 57))
    m = 0.085
    d.polygon([(W * m, H / 2), (W / 2, H * m * 1.4), (W * (1 - m), H / 2), (W / 2, H * (1 - m * 1.4))], fill=(254, 221, 0))
    r = H * 0.27
    d.ellipse([W / 2 - r, H / 2 - r, W / 2 + r, H / 2 + r], fill=(1, 33, 105))
    # faixa branca curva
    d.arc([W / 2 - r * 2.2, H / 2 - r * 0.55, W / 2 + r * 2.0, H / 2 + r * 3.6], 236, 302, fill=(255, 255, 255), width=int(E * 1.3))
    for (dx, dy) in [(-0.4, 0.35), (-0.15, 0.55), (0.1, 0.4), (0.35, 0.25), (0.2, 0.7), (-0.35, 0.65), (0.45, 0.55), (0.6, -0.1)]:
        estrela(d, W / 2 + dx * r, H / 2 + dy * r * 0.9, E * 0.9, (255, 255, 255))


def eua(d):
    for i in range(13):
        d.rectangle([0, H * i / 13, W, H * (i + 1) / 13], fill=(178, 34, 52) if i % 2 == 0 else (255, 255, 255))
    cw, ch = W * 0.42, H * 7 / 13
    d.rectangle([0, 0, cw, ch], fill=(60, 59, 110))
    for linha in range(5):
        for col in range(6 if linha % 2 == 0 else 5):
            x = cw * (col + (0.5 if linha % 2 == 0 else 1.0)) / 6
            y = ch * (linha + 0.6) / 5.3
            estrela(d, x, y, E * 1.2, (255, 255, 255))


def china(d):
    d.rectangle([0, 0, W, H], fill=(222, 41, 16))
    estrela(d, W * 0.17, H * 0.27, H * 0.15, (255, 222, 0))
    for (x, y) in [(0.34, 0.1), (0.4, 0.2), (0.4, 0.34), (0.34, 0.44)]:
        estrela(d, W * x, H * y, H * 0.05, (255, 222, 0), rot=-60)


def russia(d):
    faixas_h(d, [(255, 255, 255), (0, 57, 166), (213, 43, 30)])


def india(d):
    faixas_h(d, [(255, 153, 51), (255, 255, 255), (19, 136, 8)])
    r = H / 6 * 0.9
    d.ellipse([W / 2 - r, H / 2 - r, W / 2 + r, H / 2 + r], outline=(0, 0, 128), width=int(E * 0.9))
    for i in range(24):
        a = math.radians(i * 15)
        d.line([(W / 2, H / 2), (W / 2 + math.cos(a) * r, H / 2 + math.sin(a) * r)], fill=(0, 0, 128), width=max(1, E // 4))


def japao(d):
    d.rectangle([0, 0, W, H], fill=(255, 255, 255))
    r = H * 0.3
    d.ellipse([W / 2 - r, H / 2 - r, W / 2 + r, H / 2 + r], fill=(188, 0, 45))


def franca(d):
    faixas_v(d, [(0, 35, 149), (255, 255, 255), (237, 41, 57)])


def italia(d):
    faixas_v(d, [(0, 146, 70), (255, 255, 255), (206, 43, 55)])


def alemanha(d):
    faixas_h(d, [(0, 0, 0), (221, 0, 0), (255, 206, 0)])


def argentina(d):
    faixas_h(d, [(116, 172, 223), (255, 255, 255), (116, 172, 223)])
    r = H * 0.09
    for i in range(16):
        a = math.radians(i * 22.5)
        d.line([(W / 2, H / 2), (W / 2 + math.cos(a) * r * 1.7, H / 2 + math.sin(a) * r * 1.7)], fill=(246, 180, 14), width=E)
    d.ellipse([W / 2 - r, H / 2 - r, W / 2 + r, H / 2 + r], fill=(246, 180, 14), outline=(133, 52, 11))


BANDEIRAS = {"brasil": brasil, "eua": eua, "china": china, "russia": russia, "india": india, "japao": japao,
             "franca": franca, "italia": italia, "alemanha": alemanha, "argentina": argentina}


def main():
    for nome, fn in BANDEIRAS.items():
        img = Image.new("RGB", (W, H))
        fn(ImageDraw.Draw(img))
        pequena = img.resize((48, 32), Image.LANCZOS).convert("RGBA")
        # leve sombreado de dobra para não parecer chapado
        px = pequena.load()
        for y in range(32):
            for x in range(48):
                r, g, b, a = px[x, y]
                f = 1.0 - 0.05 * (math.sin(x / 48 * math.pi * 3) * 0.5 + 0.5)
                px[x, y] = (int(r * f), int(g * f), int(b * f), 255)
        pasta = os.path.join(TEX, "block/flag")
        os.makedirs(pasta, exist_ok=True)
        for linha in range(2):
            for col in range(3):
                pequena.crop((col * 16, linha * 16, col * 16 + 16, linha * 16 + 16)).save(
                    os.path.join(pasta, f"{nome}_{col + linha * 3}.png"))
        icone = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        d = ImageDraw.Draw(icone)
        d.line([(2, 1), (2, 15)], fill=(200, 206, 216, 255), width=1)
        icone.paste(img.resize((12, 8), Image.LANCZOS).convert("RGBA"), (3, 2))
        pasta_i = os.path.join(TEX, "item/flag_kit")
        os.makedirs(pasta_i, exist_ok=True)
        icone.save(os.path.join(pasta_i, f"{nome}.png"))
    print("bandeiras geradas")


if __name__ == "__main__":
    main()
