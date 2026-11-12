"""Gera as texturas do LunarMiners (mod + texture pack) em pixel art.

Rodar a partir da raiz do repositório:  python3 tools/gerar_texturas.py
Cada textura usa uma semente fixa, então o resultado é sempre o mesmo.
"""
import math
import os
import random

from PIL import Image, ImageDraw, ImageFilter

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MOD = os.path.join(RAIZ, "src/main/resources/assets/lunarminers/textures")
PACK = os.path.join(RAIZ, "resourcepack")
PACK_TEX = os.path.join(PACK, "assets/minecraft/textures")

# Paletas
NIOBIO = [(40, 30, 88), (72, 64, 170), (104, 120, 222), (150, 176, 255), (214, 228, 255)]
PEDRA = [(98, 98, 102), (112, 112, 116), (124, 124, 128), (138, 138, 142)]
ARDOSIA = [(52, 52, 60), (62, 62, 70), (72, 72, 80), (84, 84, 92)]
REGOLITO = [(126, 126, 132), (142, 142, 148), (158, 158, 164), (174, 174, 180), (190, 190, 196)]
BASALTO = [(40, 42, 50), (50, 52, 60), (60, 62, 72), (72, 74, 84)]


def salvar(img, *caminho):
    p = os.path.join(*caminho)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


def ruido(paleta, semente, tam=16):
    rnd = random.Random(semente)
    img = Image.new("RGBA", (tam, tam))
    px = img.load()
    for y in range(tam):
        for x in range(tam):
            px[x, y] = paleta[rnd.randrange(len(paleta))] + (255,)
    return img


def suavizar(img, paleta, semente):
    """Agrupa os pixels em manchas para não parecer chiado de TV."""
    rnd = random.Random(semente)
    px = img.load()
    w, h = img.size
    for _ in range(w * h // 3):
        x, y = rnd.randrange(w), rnd.randrange(h)
        dx, dy = rnd.choice([(1, 0), (0, 1), (-1, 0), (0, -1)])
        px[(x + dx) % w, (y + dy) % h] = px[x, y]
    return img


def base_pedra(paleta, semente):
    return suavizar(ruido(paleta, semente), paleta, semente + 1)


def cratera(px, cx, cy, r, paleta, tam=16):
    escuro, claro = paleta[0], paleta[-1]
    for y in range(cy - r - 1, cy + r + 2):
        for x in range(cx - r - 1, cx + r + 2):
            d = math.hypot(x - cx, y - cy)
            xx, yy = x % tam, y % tam
            if d <= r - 0.5:
                px[xx, yy] = (escuro if (x - cx) + (y - cy) < 0 else paleta[1]) + (255,)
            elif d <= r + 0.6 and (x - cx) + (y - cy) > 0:
                px[xx, yy] = claro + (255,)


def minerio(base_paleta, semente):
    img = base_pedra(base_paleta, semente)
    px = img.load()
    rnd = random.Random(semente + 7)
    veios = [(3, 3), (10, 2), (6, 8), (12, 10), (2, 12), (9, 13)]
    for cx, cy in veios:
        forma = rnd.choice([[(0, 0), (1, 0), (0, 1)], [(0, 0), (1, 0), (1, 1), (0, 1)], [(0, 0), (1, 1), (1, 0)]])
        for i, (dx, dy) in enumerate(forma):
            cor = NIOBIO[3] if i == 0 else NIOBIO[rnd.choice([1, 2, 2])]
            px[(cx + dx) % 16, (cy + dy) % 16] = cor + (255,)
        px[(cx + forma[-1][0] + 1) % 16, (cy + forma[-1][1]) % 16] = NIOBIO[0] + (255,)
    return img


def bloco_metal():
    img = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], fill=NIOBIO[2])
    d.rectangle([1, 1, 14, 14], fill=NIOBIO[1])
    d.rectangle([2, 2, 13, 13], fill=NIOBIO[2])
    # placas e rebites
    d.line([(2, 7), (13, 7)], fill=NIOBIO[1])
    d.line([(2, 8), (13, 8)], fill=NIOBIO[3])
    for x, y in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        d.point((x, y), fill=NIOBIO[4])
        d.point((x + 1, y + 1), fill=NIOBIO[0])
    d.line([(0, 0), (15, 0)], fill=NIOBIO[3])
    d.line([(0, 0), (0, 15)], fill=NIOBIO[3])
    d.line([(0, 15), (15, 15)], fill=NIOBIO[0])
    d.line([(15, 0), (15, 15)], fill=NIOBIO[0])
    d.line([(4, 4), (6, 4)], fill=NIOBIO[4])
    return img


def lingote():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    # lingote em perspectiva, diagonal
    for i in range(10):
        for j in range(4):
            x, y = 2 + i + j, 9 - i // 2 + j
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = NIOBIO[2 if j < 2 else 1] + (255,)
    for i in range(10):
        x, y = 2 + i, 9 - i // 2
        px[x, y] = NIOBIO[4 if i % 3 == 0 else 3] + (255,)
        px[x + 3, y + 3] = NIOBIO[0] + (255,)
    for j in range(4):
        px[2 + j, 9 + j] = NIOBIO[3] + (255,)
        px[11 + j, 5 + j] = NIOBIO[0] + (255,)
    return img


def bruto():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = random.Random(42)
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot((x - 7.5) / 6.0, (y - 8.0) / 5.0) + rnd.uniform(-0.15, 0.15)
            if d < 1.0:
                if d > 0.82:
                    c = NIOBIO[0]
                elif (x - 7.5) + (y - 8) < -4:
                    c = NIOBIO[3]
                else:
                    c = NIOBIO[rnd.choice([1, 1, 2, 2, 3])]
                px[x, y] = c + (255,)
    for x, y in [(5, 5), (6, 5), (9, 7), (4, 8)]:
        px[x, y] = NIOBIO[4] + (255,)
    return img


def regolito(semente, crateras=True):
    img = base_pedra(REGOLITO, semente)
    if crateras:
        px = img.load()
        rnd = random.Random(semente + 3)
        for _ in range(3):
            cratera(px, rnd.randrange(16), rnd.randrange(16), rnd.choice([1, 2, 2]), REGOLITO)
    return img


def pedregulho(paleta, semente):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    rnd = random.Random(semente)
    # pedras arredondadas com rejunte escuro
    for y in range(16):
        for x in range(16):
            px[x, y] = paleta[0] + (255,)
    for _ in range(14):
        cx, cy, r = rnd.randrange(16), rnd.randrange(16), rnd.uniform(2.0, 3.4)
        for y in range(16):
            for x in range(16):
                dx = min(abs(x - cx), 16 - abs(x - cx))
                dy = min(abs(y - cy), 16 - abs(y - cy))
                d = math.hypot(dx, dy)
                if d < r - 0.6:
                    lado = (x - cx) + (y - cy)
                    c = paleta[-1] if lado < -1 else paleta[len(paleta) // 2] if lado < 1 else paleta[1]
                    px[x, y] = c + (255,)
    return img


# ---------- panorama espacial do menu ----------

def ceu(tam, semente, nebulosa=None):
    rnd = random.Random(semente)
    img = Image.new("RGB", (tam, tam), (4, 6, 16))
    px = img.load()
    if nebulosa:
        neb = Image.new("RGB", (tam, tam), (0, 0, 0))
        d = ImageDraw.Draw(neb)
        for _ in range(40):
            x, y = rnd.randrange(tam), int(rnd.gauss(nebulosa[1] * tam, tam * 0.08))
            r = rnd.randrange(tam // 12, tam // 5)
            cor = rnd.choice([(40, 20, 70), (20, 30, 80), (60, 25, 60)])
            d.ellipse([x - r, y - r, x + r, y + r], fill=cor)
        neb = neb.filter(ImageFilter.GaussianBlur(tam // 14))
        # some nas bordas laterais para não criar emenda entre as faces do cubo
        borda = Image.new("L", (tam, tam), 0)
        db = ImageDraw.Draw(borda)
        for x in range(tam):
            t = min(x, tam - 1 - x) / (tam * 0.3)
            db.line([(x, 0), (x, tam)], fill=int(140 * min(1.0, t)))
        img.paste(Image.blend(img, neb, 1.0), (0, 0), borda)
        px = img.load()
    for _ in range(tam * tam // 260):
        x, y = rnd.randrange(tam), rnd.randrange(tam)
        b = rnd.choice([120, 150, 180, 220, 255])
        tint = rnd.choice([(1, 1, 1), (0.8, 0.9, 1), (1, 0.95, 0.8)])
        px[x, y] = tuple(int(b * t) for t in tint)
        if b == 255 and rnd.random() < 0.3:
            for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                if 0 <= x + dx < tam and 0 <= y + dy < tam:
                    px[x + dx, y + dy] = (120, 130, 160)
    return img


def chao_lunar(img, horizonte, semente):
    tam = img.size[0]
    rnd = random.Random(semente)
    d = ImageDraw.Draw(img)
    h0 = int(tam * horizonte)
    for y in range(h0, tam):
        t = (y - h0) / max(1, tam - h0)
        c = int(70 + 70 * t)
        d.line([(0, y), (tam, y)], fill=(c, c, c + 4))
    for _ in range(60):
        y = rnd.randrange(h0 + 4, tam)
        t = (y - h0) / (tam - h0)
        rx = int(4 + 40 * t * rnd.uniform(0.5, 1.4))
        ry = max(1, int(rx * (0.15 + 0.25 * t)))
        x = rnd.randrange(tam)
        c = int(55 + 60 * t)
        d.ellipse([x - rx, y - ry, x + rx, y + ry], fill=(c, c, c + 4))
        d.arc([x - rx, y - ry, x + rx, y + ry], 0, 180, fill=(c + 40, c + 40, c + 44), width=max(1, ry // 3))
    return img


def terra(img, cx, cy, r):
    d = ImageDraw.Draw(img)
    for i in range(6, 0, -1):
        d.ellipse([cx - r - i * 3, cy - r - i * 3, cx + r + i * 3, cy + r + i * 3], fill=(10 + i * 4, 20 + i * 6, 50 + i * 12))
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(30, 90, 190))
    rnd = random.Random(9)
    # continentes (com um "Brasil" em destaque à direita)
    for x0, y0, rr, cor in [(-0.35, -0.2, 0.3, (60, 150, 70)), (0.3, 0.15, 0.32, (70, 160, 60)), (0.15, 0.45, 0.2, (80, 150, 60)), (-0.2, 0.4, 0.15, (110, 140, 70))]:
        R = int(r * rr)
        X, Y = cx + int(r * x0), cy + int(r * y0)
        d.ellipse([X - R, Y - R, X + R, Y + int(R * 1.3)], fill=cor)
    for _ in range(14):
        a = rnd.uniform(0, 2 * math.pi)
        rr = rnd.uniform(0, r * 0.85)
        X, Y = cx + int(math.cos(a) * rr), cy + int(math.sin(a) * rr)
        d.ellipse([X - r // 6, Y - r // 20, X + r // 6, Y + r // 20], fill=(235, 240, 250))
    # sombra (lado noturno)
    sombra = Image.new("L", img.size, 0)
    ds = ImageDraw.Draw(sombra)
    ds.ellipse([cx - r + r // 3, cy - r - r // 4, cx + r + r, cy + r + r // 4], fill=170)
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).ellipse([cx - r, cy - r, cx + r, cy + r], fill=255)
    from PIL import ImageChops
    sombra = ImageChops.multiply(sombra, mask).filter(ImageFilter.GaussianBlur(r // 6))
    img.paste((5, 8, 20), (0, 0), sombra)
    return img


def panorama():
    tam = 1024
    horizonte = 0.56
    for i in range(4):
        img = ceu(tam, 100 + i, nebulosa=(0, 0.25) if i in (1, 2) else None)
        if i == 0:
            terra(img, int(tam * 0.68), int(tam * 0.26), tam // 9)
        chao_lunar(img, horizonte, 200 + i)
        salvar(img, PACK_TEX, "gui/title/background", f"panorama_{i}.png")
    salvar(ceu(tam, 104), PACK_TEX, "gui/title/background", "panorama_4.png")
    baixo = Image.new("RGB", (tam, tam), (140, 140, 144))
    chao_lunar(baixo, 0.0, 205)
    salvar(baixo, PACK_TEX, "gui/title/background", "panorama_5.png")


def icone_pack():
    img = ceu(128, 7)
    terra(img, 84, 40, 22)
    chao_lunar(img, 0.68, 8)
    salvar(img, PACK, "pack.png")


MAR = [(58, 60, 68), (68, 70, 78), (78, 80, 88), (90, 92, 100)]


def alien_textura():
    """Pele 64x64 no formato do zumbi: verde-acinzentado, olhos pretos enormes."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = img.load()
    rnd = random.Random(77)
    pele = [(126, 180, 112), (140, 196, 124), (114, 166, 102), (152, 206, 136)]
    traje = [(46, 54, 74), (56, 66, 88), (40, 46, 64)]
    def preencher(x0, y0, x1, y1, paleta):
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = paleta[rnd.randrange(len(paleta))] + (255,)
    preencher(0, 0, 32, 16, pele)        # cabeça
    preencher(16, 16, 40, 32, traje)     # tronco (traje escuro)
    preencher(40, 16, 56, 32, pele)      # braços
    preencher(0, 16, 16, 32, traje)      # pernas
    # faixa luminosa no peito
    for x in range(20, 28):
        px[x, 22] = (90, 230, 210, 255)
    # olhos grandes e amendoados ocupando a face (8..15, 8..15)
    olho_esq = [(8, 10), (9, 10), (8, 11), (9, 11), (10, 11), (9, 12), (10, 12)]
    olho_dir = [(14, 10), (15, 10), (13, 11), (14, 11), (15, 11), (13, 12), (14, 12)]
    for x, y in olho_esq + olho_dir:
        px[x, y] = (8, 8, 12, 255)
    px[9, 10] = (80, 240, 220, 255)
    px[14, 10] = (80, 240, 220, 255)
    px[11, 14] = (84, 124, 74, 255)
    px[12, 14] = (84, 124, 74, 255)
    return img


def gosma():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    rnd = random.Random(5)
    verde = [(80, 200, 90), (110, 230, 110), (60, 160, 70), (170, 255, 160)]
    for y in range(16):
        for x in range(16):
            d = math.hypot((x - 7.5) / 5.5, (y - 9) / 4.5) + rnd.uniform(-0.12, 0.12)
            if d < 1.0:
                px[x, y] = (verde[2] if d > 0.8 else verde[rnd.choice([0, 0, 1])]) + (235,)
    for x, y in [(6, 6), (7, 6), (5, 7)]:
        px[x, y] = verde[3] + (255,)
    for x, y in [(7, 3), (7, 4), (8, 4)]:
        px[x, y] = verde[1] + (230,)
    return img


def estrelas():
    tam = 256
    rnd = random.Random(31)
    img = Image.new("RGBA", (tam, tam), (2, 3, 10, 255))
    px = img.load()
    for _ in range(260):
        x, y = rnd.randrange(tam), rnd.randrange(tam)
        b = rnd.choice([110, 140, 180, 220, 255])
        tint = rnd.choice([(1, 1, 1), (0.8, 0.9, 1), (1, 0.92, 0.8)])
        px[x, y] = tuple(int(b * t) for t in tint) + (255,)
        if b == 255 and rnd.random() < 0.25:
            for dx, dy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                px[(x + dx) % tam, (y + dy) % tam] = (110, 120, 150, 255)
    return img


def terra_ceu():
    tam = 128
    img = Image.new("RGBA", (tam, tam), (0, 0, 0, 0))
    base = Image.new("RGB", (tam, tam), (0, 0, 0))
    terra(base, tam // 2, tam // 2, tam // 2 - 8)
    mask = Image.new("L", (tam, tam), 0)
    ImageDraw.Draw(mask).ellipse([4, 4, tam - 5, tam - 5], fill=255)
    img.paste(base, (0, 0), mask)
    return img


def main():
    # Mod: nióbio
    salvar(minerio(PEDRA, 11), MOD, "block/niobium_ore.png")
    salvar(minerio(ARDOSIA, 12), MOD, "block/deepslate_niobium_ore.png")
    salvar(bloco_metal(), MOD, "block/niobium_block.png")
    salvar(lingote(), MOD, "item/niobium_ingot.png")
    salvar(bruto(), MOD, "item/raw_niobium.png")
    # Mod: Lua
    salvar(regolito(21), MOD, "block/moon_regolith.png")
    salvar(suavizar(ruido(MAR, 25), MAR, 26), MOD, "block/moon_mare_regolith.png")
    salvar(base_pedra(REGOLITO[:4], 27), MOD, "block/moon_rock.png")
    salvar(pedregulho(REGOLITO, 22), MOD, "block/moon_cobblestone.png")
    salvar(base_pedra(BASALTO, 23), MOD, "block/moon_deep_rock.png")
    salvar(minerio(REGOLITO[:4], 28), MOD, "block/moon_niobium_ore.png")
    ferro = base_pedra(REGOLITO[:4], 29)
    fp = ferro.load()
    for x, y in [(3, 4), (4, 4), (10, 3), (11, 4), (6, 9), (7, 9), (12, 11), (2, 12), (3, 13)]:
        fp[x, y] = (216, 175, 147, 255)
        fp[(x + 1) % 16, (y + 1) % 16] = (150, 106, 80, 255)
    salvar(ferro, MOD, "block/moon_iron_ore.png")
    salvar(alien_textura(), MOD, "entity/alien.png")
    salvar(gosma(), MOD, "item/alien_goo.png")
    salvar(estrelas(), MOD, "environment/stars.png")
    salvar(terra_ceu(), MOD, "environment/earth.png")
    # Texture pack: menu espacial
    panorama()
    icone_pack()
    print("texturas geradas")


if __name__ == "__main__":
    main()
