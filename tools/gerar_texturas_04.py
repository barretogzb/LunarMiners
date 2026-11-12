"""Texturas da versão 0.4: minérios novos, metais, ferramentas, trajes, máquinas,
móveis, habitat e bandeiras reais dos países.

Uso: python3 tools/gerar_texturas_04.py
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gerar_texturas as gt  # noqa: E402

MOD = gt.MOD
REG = gt.REGOLITO[:4]
BAS = gt.BASALTO

TITANIO = [(52, 54, 68), (88, 92, 110), (132, 138, 158), (184, 190, 208), (232, 236, 246)]
ALUMINIO = [(110, 120, 132), (156, 166, 178), (196, 204, 214), (224, 230, 238), (248, 251, 255)]
NIOBIO = gt.NIOBIO
SILICIO = [(30, 34, 50), (58, 66, 92), (96, 110, 150), (150, 170, 215), (215, 228, 255)]
HELIO = [(16, 70, 76), (32, 150, 140), (90, 230, 210), (170, 255, 240), (235, 255, 252)]
GELO = [(120, 166, 214), (156, 196, 236), (192, 222, 250), (224, 240, 255), (250, 253, 255)]
CIANO = (80, 230, 225)


def salvar(img, rel):
    gt.salvar(img, MOD, rel)


def ruido_px(img, amp, seed):
    rnd = random.Random(seed)
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            v = rnd.randint(-amp, amp)
            px[x, y] = (max(0, min(255, r + v)), max(0, min(255, g + v)), max(0, min(255, b + v)), a)
    return img


def minerio(base, veio, seed, forma="veio"):
    img = gt.base_pedra(base, seed)
    px = img.load()
    rnd = random.Random(seed + 7)
    centros = [(3, 3), (10, 2), (6, 8), (12, 10), (2, 12), (9, 13)]
    for cx, cy in centros:
        if forma == "cristal":
            for i in range(3):
                px[cx % 16, (cy - i) % 16] = veio[2 + (i == 1)] + (255,)
            px[(cx + 1) % 16, (cy - 1) % 16] = veio[1] + (255,)
            px[cx % 16, (cy - 3) % 16] = veio[4] + (255,)
        else:
            pts = rnd.choice([[(0, 0), (1, 0), (0, 1)], [(0, 0), (1, 0), (1, 1), (0, 1)], [(0, 0), (1, 1), (1, 0)]])
            for i, (dx, dy) in enumerate(pts):
                px[(cx + dx) % 16, (cy + dy) % 16] = (veio[3] if i == 0 else veio[rnd.choice([1, 2, 2])]) + (255,)
            px[(cx + pts[-1][0] + 1) % 16, (cy + pts[-1][1]) % 16] = veio[0] + (255,)
    return img


def lingote(p):
    """Lingote em perspectiva: topo claro, frente média, lateral escura."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(4, 5), (12, 5), (14, 8), (2, 8)], fill=p[3] + (255,))       # topo
    d.polygon([(2, 8), (14, 8), (14, 11), (2, 11)], fill=p[2] + (255,))     # frente
    d.polygon([(1, 9), (2, 8), (2, 11), (1, 12)], fill=p[1] + (255,))
    d.line([(2, 11), (14, 11)], fill=p[0] + (255,))
    d.line([(14, 8), (14, 11)], fill=p[1] + (255,))
    d.line([(5, 6), (10, 6)], fill=p[4] + (255,))
    d.line([(3, 9), (6, 9)], fill=p[4] + (255,))
    return img


def bruto(p, seed):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rnd = random.Random(seed)
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot((x - 7.5) / 6.0, (y - 8.0) / 5.0) + rnd.uniform(-0.15, 0.15)
            if d < 1.0:
                c = p[0] if d > 0.82 else (p[3] if (x - 7.5) + (y - 8) < -4 else p[rnd.choice([1, 1, 2, 2, 3])])
                px[x, y] = c + (255,)
    for x, y in [(5, 5), (6, 5), (9, 7), (4, 8)]:
        px[x, y] = p[4] + (255,)
    return img


def cristal(p, seed):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rnd = random.Random(seed)
    for cx, cy, h, w in [(8, 13, 11, 3), (5, 13, 7, 2), (11, 13, 8, 2)]:
        topo = cy - h
        d.polygon([(cx - w, cy), (cx - w, topo + w), (cx, topo), (cx + w, topo + w), (cx + w, cy)], fill=p[2] + (255,))
        d.line([(cx, topo + 1), (cx, cy - 1)], fill=p[4] + (255,))
        d.line([(cx - w, topo + w), (cx - w, cy)], fill=p[1] + (255,))
        d.line([(cx + w, topo + w), (cx + w, cy)], fill=p[0] + (255,))
    return ruido_px(img, 6, rnd.randint(0, 999))


def haste():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for i in range(12):
        x, y = 2 + i, 13 - i
        px[x, y] = ALUMINIO[3] + (255,)
        px[x + 1, y] = ALUMINIO[1] + (255,) if x + 1 < 16 else (0, 0, 0, 0)
    px[2, 13] = ALUMINIO[0] + (255,)
    px[13, 2] = ALUMINIO[4] + (255,)
    return img


def fragmento_gelo():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(4, 12), (3, 7), (7, 3), (12, 4), (13, 9), (9, 13)], fill=GELO[2] + (230,), outline=GELO[0] + (255,))
    d.line([(6, 6), (10, 5)], fill=GELO[4] + (255,))
    d.line([(5, 9), (8, 8)], fill=GELO[3] + (255,))
    return img


# ---------------- ferramentas ----------------

def ferramenta(tipo, p):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cabo_ini, cabo_fim = (2, 13), (9, 6)
    if tipo == "sword":
        cabo_fim = (4, 11)
    n = cabo_fim[0] - cabo_ini[0]
    for i in range(n + 1):
        x, y = cabo_ini[0] + i, cabo_ini[1] - i
        px[x, y] = ALUMINIO[1] + (255,)
        if x + 1 < 16:
            px[x + 1, y] = ALUMINIO[3] + (255,)
    px[cabo_ini[0], cabo_ini[1]] = ALUMINIO[0] + (255,)

    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = c + (255,)

    if tipo == "pickaxe":
        arco = [(4, 3), (5, 2), (6, 1), (7, 1), (8, 1), (9, 1), (10, 2), (11, 3), (12, 4), (13, 5), (14, 6), (14, 7),
                (14, 8), (14, 9), (14, 10), (13, 11)]
        for i, (x, y) in enumerate(arco):
            put(x, y, p[3] if i % 4 else p[4])
            put(x - (1 if x > 9 else 0), y + 1, p[1])
        put(10, 5, p[2]); put(9, 6, p[2]); put(11, 4, p[2])
    elif tipo == "axe":
        for y in range(1, 8):
            for x in range(8, 15):
                if (x - 8) + (7 - y) >= 2 and not (x > 12 and y > 5):
                    put(x, y, p[2] if x < 13 else p[3])
        for y in range(1, 7):
            put(14, y, p[4])
        put(8, 6, p[1]); put(9, 7, p[1])
    elif tipo == "shovel":
        for y in range(1, 7):
            for x in range(9, 15):
                if abs((x - 12) + (y - 3.5)) + abs((x - 12) - (y - 3.5)) * 0.6 < 4:
                    put(x, y, p[2])
        put(12, 2, p[4]); put(13, 3, p[3]); put(11, 2, p[3])
        put(10, 5, p[1]); put(10, 6, p[1])
    elif tipo == "sword":
        for i in range(9):
            x, y = 5 + i, 10 - i
            put(x, y, p[3])
            put(x + 1, y, p[2])
            put(x, y - 1, p[4] if i % 3 == 0 else p[3])
        put(14, 1, p[4])
        for i in range(-2, 3):
            put(4 + i, 10 + i, p[1])
        put(2, 13, p[0])
    elif tipo == "hoe":
        for x in range(8, 14):
            put(x, 2, p[3])
            put(x, 3, p[1])
        put(13, 2, p[4]); put(13, 3, p[2]); put(13, 4, p[2])
        put(9, 4, p[2]); put(9, 5, p[1])
    return img


# ---------------- trajes ----------------

def icone_traje(peca, base, detalhe, visor=(40, 110, 200)):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    esc = tuple(int(c * 0.6) for c in base)
    if peca == "helmet":
        d.ellipse([2, 2, 13, 13], fill=base + (255,), outline=esc + (255,))
        d.rounded_rectangle([4, 5, 11, 10], radius=2, fill=visor + (255,))
        d.line([(5, 6), (7, 6)], fill=(200, 230, 255, 255))
        d.line([(2, 11), (13, 11)], fill=detalhe + (255,))
    elif peca == "chestplate":
        d.rounded_rectangle([3, 2, 12, 14], radius=3, fill=base + (255,), outline=esc + (255,))
        d.rectangle([0, 3, 3, 9], fill=base + (255,), outline=esc + (255,))
        d.rectangle([12, 3, 15, 9], fill=base + (255,), outline=esc + (255,))
        d.rectangle([6, 5, 9, 8], fill=(30, 40, 60, 255))
        d.point((7, 6), fill=CIANO + (255,))
        d.point((8, 7), fill=(240, 80, 60, 255))
        d.line([(3, 11), (12, 11)], fill=detalhe + (255,))
    elif peca == "leggings":
        d.rectangle([3, 2, 12, 5], fill=base + (255,), outline=esc + (255,))
        d.rectangle([3, 5, 7, 14], fill=base + (255,), outline=esc + (255,))
        d.rectangle([8, 5, 12, 14], fill=base + (255,), outline=esc + (255,))
        d.line([(3, 9), (7, 9)], fill=detalhe + (255,))
        d.line([(8, 9), (12, 9)], fill=detalhe + (255,))
    else:
        d.rectangle([2, 6, 6, 13], fill=base + (255,), outline=esc + (255,))
        d.rectangle([9, 6, 13, 13], fill=base + (255,), outline=esc + (255,))
        d.rectangle([1, 12, 7, 14], fill=(60, 64, 76, 255))
        d.rectangle([8, 12, 14, 14], fill=(60, 64, 76, 255))
        d.line([(2, 8), (6, 8)], fill=detalhe + (255,))
        d.line([(9, 8), (13, 8)], fill=detalhe + (255,))
    return ruido_px(img, 4, hash(peca) % 999)


def camada_traje(base, detalhe, camada, seed):
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    px = img.load()
    rnd = random.Random(seed)

    def preencher(x0, y0, x1, y1, cor, amp=5):
        for y in range(y0, y1):
            for x in range(x0, x1):
                v = rnd.randint(-amp, amp)
                px[x, y] = tuple(max(0, min(255, c + v)) for c in cor) + (255,)

    esc = tuple(int(c * 0.7) for c in base)
    if camada == 1:
        preencher(0, 0, 32, 16, base)                 # capacete
        preencher(8, 8, 16, 16, (34, 90, 170), 8)     # visor (frente)
        for x in range(9, 12):
            px[x, 9] = (190, 225, 255, 255)
        for x in range(8, 16):
            px[x, 15] = detalhe + (255,)
        preencher(16, 16, 40, 32, base)               # tronco
        preencher(20, 20, 28, 26, (40, 46, 62), 3)    # painel do peito
        px[22, 22] = CIANO + (255,)
        px[25, 23] = (240, 80, 60, 255)
        preencher(32, 20, 40, 32, esc)                # mochila de oxigênio nas costas
        for x in range(16, 40):
            px[x, 28] = detalhe + (255,)
        preencher(40, 16, 56, 32, base)               # braços
        for x in range(40, 56):
            px[x, 27] = detalhe + (255,)
        preencher(0, 16, 16, 32, base)                # botas
        preencher(0, 26, 16, 32, (64, 68, 80), 3)
    else:
        preencher(0, 16, 16, 32, base)                # pernas
        for x in range(0, 16):
            px[x, 22] = detalhe + (255,)
        preencher(16, 26, 40, 32, esc)                # cintura
    return img


# ---------------- máquinas e móveis ----------------

def painel(base, seed, borda=True):
    img = Image.new("RGBA", (16, 16), base + (255,))
    ruido_px(img, 4, seed)
    if borda:
        d = ImageDraw.Draw(img)
        esc = tuple(int(c * 0.65) for c in base)
        cla = tuple(min(255, c + 28) for c in base)
        d.line([(0, 0), (15, 0)], fill=cla + (255,))
        d.line([(0, 0), (0, 15)], fill=cla + (255,))
        d.line([(0, 15), (15, 15)], fill=esc + (255,))
        d.line([(15, 0), (15, 15)], fill=esc + (255,))
    return img


def habitat_wall():
    img = painel((222, 226, 234), 101)
    d = ImageDraw.Draw(img)
    d.line([(1, 7), (14, 7)], fill=(170, 176, 190, 255))
    d.line([(1, 8), (14, 8)], fill=(244, 246, 250, 255))
    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        d.point((x, y), fill=(150, 156, 170, 255))
    d.line([(3, 11), (12, 11)], fill=(250, 200, 50, 255))
    return img


def habitat_floor():
    img = painel((108, 116, 134), 102)
    d = ImageDraw.Draw(img)
    for i in range(2, 14, 3):
        d.line([(i, 2), (i + 2, 2)], fill=(84, 90, 106, 255))
        d.line([(i, 13), (i + 2, 13)], fill=(84, 90, 106, 255))
    d.rectangle([5, 5, 10, 10], outline=(90, 96, 112, 255))
    return img


def vidro_habitat():
    img = Image.new("RGBA", (16, 16), (130, 200, 255, 46))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(210, 216, 228, 255))
    d.point((0, 0), fill=(150, 156, 170, 255))
    d.point((15, 15), fill=(150, 156, 170, 255))
    for i in range(5):
        d.point((3 + i, 10 - i), fill=(240, 250, 255, 110))
    return img


def plataforma():
    img = painel((52, 56, 68), 103, borda=False)
    d = ImageDraw.Draw(img)
    for i in range(0, 16, 4):
        d.line([(i, 0), (i + 1, 0)], fill=(250, 200, 40, 255))
        d.line([(0, i), (0, i + 1)], fill=(250, 200, 40, 255))
    d.ellipse([4, 4, 11, 11], outline=(250, 200, 40, 255))
    return img


def bloco_metal(p, seed):
    img = painel(p[2], seed)
    d = ImageDraw.Draw(img)
    d.rectangle([3, 3, 12, 12], outline=p[1] + (255,))
    d.line([(4, 4), (11, 4)], fill=p[4] + (255,))
    return img


def gelo_bloco():
    img = gt.suavizar(gt.ruido(GELO[:4], 140), GELO[:4], 141)
    d = ImageDraw.Draw(img)
    d.line([(2, 12), (7, 6)], fill=GELO[4] + (255,))
    d.line([(9, 3), (13, 7)], fill=GELO[4] + (255,))
    return img


def bancada(face):
    if face == "top":
        img = painel((70, 80, 102), 110, borda=False)
        d = ImageDraw.Draw(img)
        d.ellipse([1, 1, 14, 14], outline=(150, 166, 196, 255))
        for i in (5, 10):
            d.line([(3, i), (12, i)], fill=(50, 58, 76, 255))
            d.line([(i, 3), (i, 12)], fill=(50, 58, 76, 255))
        d.point((4, 4), fill=CIANO + (255,))
        return img
    if face == "edge":
        img = painel((150, 162, 186), 111)
        ImageDraw.Draw(img).line([(0, 8), (15, 8)], fill=CIANO + (255,))
        return img
    img = painel((190, 198, 214), 112)
    d = ImageDraw.Draw(img)
    d.rectangle([4, 3, 11, 9], fill=(40, 50, 70, 255))
    d.line([(5, 5), (9, 5)], fill=CIANO + (255,))
    d.line([(5, 7), (10, 7)], fill=(90, 200, 120, 255))
    return img


def holo():
    img = Image.new("RGBA", (16, 16), (60, 220, 230, 90))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(120, 250, 255, 200))
    for y in (4, 7, 10):
        d.line([(3, y), (12 - (y % 3), y)], fill=(200, 255, 255, 220))
    return img


def forno(face, aceso=False):
    if face == "side":
        img = painel((196, 202, 214), 120)
        d = ImageDraw.Draw(img)
        for y in (4, 8, 12):
            d.line([(1, y), (14, y)], fill=(160, 166, 180, 255))
        d.line([(0, 2), (15, 2)], fill=(250, 170, 40, 255))
        return img
    img = painel((60, 64, 80), 121)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([2, 3, 13, 13], radius=3, fill=(20, 22, 30, 255), outline=(150, 156, 170, 255))
    if aceso:
        d.rounded_rectangle([4, 6, 11, 11], radius=2, fill=(255, 150, 40, 255))
        d.line([(5, 8), (10, 8)], fill=(255, 235, 140, 255))
    else:
        d.line([(4, 9), (11, 9)], fill=(60, 64, 80, 255))
    return img


def prato_solar():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            t = d / 8
            px[x, y] = (int(150 + 90 * (1 - t)), int(170 + 80 * (1 - t)), int(200 + 55 * (1 - t)), 255)
            if abs(d - 4) < 0.5 or abs(d - 7) < 0.5:
                px[x, y] = (110, 120, 140, 255)
    px[7, 7] = (255, 220, 120, 255)
    px[8, 8] = (255, 220, 120, 255)
    return img


def celula_solar():
    img = Image.new("RGBA", (16, 16), (20, 40, 90, 255))
    d = ImageDraw.Draw(img)
    for i in range(0, 16, 4):
        d.line([(i, 0), (i, 15)], fill=(150, 160, 180, 255))
        d.line([(0, i), (15, i)], fill=(150, 160, 180, 255))
    for x, y in [(1, 1), (5, 5), (9, 9), (13, 13)]:
        d.point((x, y), fill=(120, 170, 255, 255))
    return img


def main():
    salvar(minerio(REG, TITANIO, 201), "block/moon_titanium_ore.png")
    salvar(minerio(REG, ALUMINIO, 202), "block/moon_aluminum_ore.png")
    salvar(minerio(REG, SILICIO, 203, "cristal"), "block/moon_silicon_ore.png")
    salvar(minerio(BAS, HELIO, 204, "cristal"), "block/moon_helium3_ore.png")
    salvar(gelo_bloco(), "block/lunar_ice.png")
    salvar(bloco_metal(TITANIO, 205), "block/titanium_block.png")
    salvar(bloco_metal(ALUMINIO, 206), "block/aluminum_block.png")
    salvar(habitat_wall(), "block/habitat_wall.png")
    salvar(habitat_floor(), "block/habitat_floor.png")
    salvar(vidro_habitat(), "block/habitat_glass.png")
    salvar(plataforma(), "block/landing_pad.png")

    salvar(bancada("side"), "block/bench_side.png")
    salvar(bancada("edge"), "block/bench_edge.png")
    salvar(bancada("top"), "block/bench_top.png")
    salvar(holo(), "block/holo_screen.png")
    salvar(forno("side"), "block/solar_furnace_side.png")
    salvar(forno("front"), "block/solar_furnace_front.png")
    salvar(forno("front", True), "block/solar_furnace_front_on.png")
    salvar(prato_solar(), "block/solar_dish.png")
    crate = painel((214, 220, 230), 130)
    ImageDraw.Draw(crate).rectangle([6, 4, 9, 7], fill=(40, 120, 220, 255))
    salvar(crate, "block/crate_side.png")
    topo = painel((190, 196, 210), 131)
    ImageDraw.Draw(topo).ellipse([3, 3, 12, 12], outline=(150, 156, 170, 255))
    salvar(topo, "block/crate_top.png")
    salvar(painel((40, 120, 220), 132, borda=False), "block/crate_band.png")
    salvar(painel((176, 184, 200), 133, borda=False), "block/furniture_metal.png")
    dist = painel((230, 234, 240), 134)
    d = ImageDraw.Draw(dist)
    d.line([(0, 4), (15, 4)], fill=(40, 120, 220, 255))
    for x, y in [(5, 9), (6, 9), (4, 10), (7, 10), (5, 11), (6, 11), (9, 10), (10, 10), (10, 11), (9, 12), (10, 12)]:
        d.point((x, y), fill=(30, 60, 110, 255))
    salvar(dist, "block/distributor_side.png")
    top = Image.new("RGBA", (16, 16), (150, 156, 170, 255))
    d = ImageDraw.Draw(top)
    d.ellipse([1, 1, 14, 14], fill=(70, 76, 92, 255))
    for i in range(4):
        a = i * math.pi / 2
        d.line([(7.5, 7.5), (7.5 + math.cos(a) * 6, 7.5 + math.sin(a) * 6)], fill=(190, 196, 210, 255), width=2)
    salvar(top, "block/distributor_top.png")
    glow = Image.new("RGBA", (16, 16), (225, 250, 255, 255))
    ruido_px(glow, 6, 135)
    salvar(glow, "block/lamp_glow.png")
    salvar(painel((150, 156, 170), 136), "block/panel_edge.png")
    salvar(celula_solar(), "block/solar_cell.png")
    mesa = painel((236, 238, 244), 137, borda=False)
    ImageDraw.Draw(mesa).ellipse([2, 2, 13, 13], outline=(40, 120, 220, 255))
    salvar(mesa, "block/table_top.png")
    salvar(painel((40, 120, 220), 138, borda=False), "block/seat.png")
    mastro = Image.new("RGBA", (16, 16), (200, 206, 216, 255))
    ruido_px(mastro, 8, 139)
    salvar(mastro, "block/flag_pole.png")

    for nome, p, seed in [("titanium", TITANIO, 210), ("aluminum", ALUMINIO, 211)]:
        salvar(lingote(p), f"item/{nome}_ingot.png")
        salvar(bruto(p, seed), f"item/raw_{nome}.png")
    salvar(lingote(NIOBIO), "item/niobium_ingot.png")
    salvar(haste(), "item/aluminum_rod.png")
    salvar(cristal(SILICIO, 212), "item/silicon.png")
    salvar(cristal(HELIO, 213), "item/helium3_crystal.png")
    salvar(fragmento_gelo(), "item/ice_shard.png")
    for metal, p in [("aluminum", ALUMINIO), ("niobium", NIOBIO), ("titanium", TITANIO)]:
        for f in ["pickaxe", "axe", "shovel", "sword", "hoe"]:
            salvar(ferramenta(f, p), f"item/{metal}_{f}.png")

    trajes = {"space_suit": ((232, 234, 240), (250, 140, 40)), "titanium_suit": ((150, 156, 176), CIANO)}
    for traje, (base, det) in trajes.items():
        for peca in ["helmet", "chestplate", "leggings", "boots"]:
            salvar(icone_traje(peca, base, det), f"item/{traje}_{peca}.png")
        salvar(camada_traje(base, det, 1, 220), f"models/armor/{traje}_layer_1.png")
        salvar(camada_traje(base, det, 2, 221), f"models/armor/{traje}_layer_2.png")

    # módulos
    tanque = icone_traje("chestplate", (220, 224, 232), (40, 120, 220))
    salvar(modulo_icone((40, 120, 220), "O"), "item/o2_tank_module.png")
    salvar(modulo_icone((90, 220, 120), "V"), "item/night_vision_module.png")
    salvar(modulo_icone((250, 170, 40), "S"), "item/servo_module.png")
    salvar(modulo_icone((90, 230, 210), "P"), "item/thruster_module.png")
    del tanque

    import gerar_bandeiras
    gerar_bandeiras.main()
    print("texturas 0.4 geradas")


def modulo_icone(cor, letra):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([2, 3, 13, 12], radius=3, fill=(60, 66, 84, 255), outline=(170, 178, 196, 255))
    d.rounded_rectangle([4, 5, 11, 10], radius=2, fill=cor + (255,))
    pts = {"O": [(6, 6), (7, 6), (8, 6), (9, 6), (6, 9), (7, 9), (8, 9), (9, 9), (6, 7), (6, 8), (9, 7), (9, 8)],
           "V": [(6, 6), (6, 7), (7, 8), (8, 9), (9, 8), (10, 7), (10, 6)],
           "S": [(7, 6), (8, 6), (9, 6), (7, 7), (8, 8), (9, 9), (7, 9), (8, 9)],
           "P": [(7, 6), (8, 6), (9, 7), (8, 8), (7, 8), (7, 7), (7, 9)]}[letra]
    for p in pts:
        d.point(p, fill=(20, 24, 34, 255))
    for x in (4, 7, 10):
        d.line([(x, 13), (x, 14)], fill=(200, 170, 60, 255))
    return img


if __name__ == "__main__":
    main()
