"""Gera o visual lunar do jogo: logo, menus, inventários e blocos comuns retexturizados.

Os arquivos saem em src/main/resources/assets/minecraft/..., então valem sempre que o mod
estiver instalado (não precisa ativar texture pack).

As telas de inventário são recoloridas a partir das texturas originais 1.20.1 (só a cor muda;
o encaixe dos slots continua igual). Passe a pasta "assets/minecraft" original como argumento:
    python3 tools/gerar_visual.py /caminho/para/assets/minecraft
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(RAIZ, "src/main/resources/assets/minecraft")
FONTE = "/usr/share/fonts/truetype/google-fonts/Poppins-Bold.ttf"

# gradiente "aço lunar" (luminância -> cor)
ACO = [(0, (6, 10, 22)), (55, (22, 30, 50)), (100, (52, 66, 94)), (150, (104, 122, 152)),
       (198, (176, 190, 212)), (255, (232, 242, 255))]
CIANO = (80, 230, 225)


def salvar(img, *caminho):
    p = os.path.join(OUT, *caminho)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


def gradiente(lum, paradas=ACO):
    for (l0, c0), (l1, c1) in zip(paradas, paradas[1:]):
        if lum <= l1:
            t = (lum - l0) / max(1, l1 - l0)
            return tuple(int(a + (b - a) * t) for a, b in zip(c0, c1))
    return paradas[-1][1]


def recolorir(img, so_cinzas=True, preservar_quentes=True):
    img = img.convert("RGBA")
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            sat = max(r, g, b) - min(r, g, b)
            if preservar_quentes and r > 170 and r - b > 70 and sat > 70:
                continue  # fogo, brilho, setas coloridas
            if so_cinzas and sat > 28:
                continue
            lum = int(0.299 * r + 0.587 * g + 0.114 * b)
            px[x, y] = gradiente(lum) + (a,)
    return img


# ---------------- Logo ----------------

def logo():
    W, H = 1024, 256
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    texto = "LUNARMINERS"
    fonte = ImageFont.truetype(FONTE, 132)
    esp = 6
    larguras = [fonte.getbbox(c)[2] - fonte.getbbox(c)[0] for c in texto]
    total = sum(larguras) + esp * (len(texto) - 1)
    escala = min(1.0, 980 / total)
    if escala < 1:
        fonte = ImageFont.truetype(FONTE, int(132 * escala))
        larguras = [fonte.getbbox(c)[2] - fonte.getbbox(c)[0] for c in texto]
        total = sum(larguras) + esp * (len(texto) - 1)
    mascara = Image.new("L", (W, H), 0)
    d = ImageDraw.Draw(mascara)
    x = (W - total) // 2
    topo = 34
    for c, lw in zip(texto, larguras):
        bb = fonte.getbbox(c)
        d.text((x - bb[0], topo - bb[1] + 10), c, font=fonte, fill=255)
        x += lw + esp
    caixa = mascara.getbbox()

    # contorno escuro grosso + brilho ciano
    contorno = mascara.filter(ImageFilter.MaxFilter(13))
    brilho = mascara.filter(ImageFilter.MaxFilter(21)).filter(ImageFilter.GaussianBlur(8))
    camada = Image.new("RGBA", (W, H), CIANO + (0,))
    camada.putalpha(brilho.point(lambda v: int(v * 0.55)))
    img.alpha_composite(camada)
    escuro = Image.new("RGBA", (W, H), (8, 12, 28, 255))
    escuro.putalpha(contorno)
    img.alpha_composite(escuro)

    # preenchimento cromado: branco em cima, aço azulado embaixo, linha de horizonte
    grad = Image.new("RGBA", (W, H))
    gp = grad.load()
    y0, y1 = caixa[1], caixa[3]
    for y in range(H):
        t = min(1, max(0, (y - y0) / max(1, y1 - y0)))
        if t < 0.48:
            c = tuple(int(a + (b - a) * (t / 0.48)) for a, b in zip((255, 255, 255), (196, 210, 232)))
        elif t < 0.52:
            c = (120, 140, 175)
        else:
            k = (t - 0.52) / 0.48
            c = tuple(int(a + (b - a) * k) for a, b in zip((150, 170, 205), (70, 92, 135)))
        for x in range(W):
            gp[x, y] = c + (255,)
    grad.putalpha(mascara)
    img.alpha_composite(grad)

    # anel orbital atravessando o logo
    anel = Image.new("L", (W * 2, H * 2), 0)
    da = ImageDraw.Draw(anel)
    da.ellipse([60, 190, W * 2 - 60, 350], outline=255, width=7)
    anel = anel.resize((W, H), Image.LANCZOS)
    # some na frente das letras na metade de cima do anel (passa por trás)
    frente = Image.new("L", (W, H), 0)
    ImageDraw.Draw(frente).rectangle([0, 135, W, H], fill=255)
    anel_frente = Image.composite(anel, Image.new("L", (W, H), 0), frente)
    cor_anel = Image.new("RGBA", (W, H), (250, 210, 60, 255))
    cor_anel.putalpha(anel_frente)
    img.alpha_composite(cor_anel)
    # estrelinha
    ds = ImageDraw.Draw(img)
    sx, sy = caixa[2] - 18, caixa[1] - 2
    for r, a in [(14, 60), (8, 140), (3, 255)]:
        ds.polygon([(sx, sy - r * 2), (sx + r // 2, sy), (sx, sy + r * 2), (sx - r // 2, sy)], fill=(255, 255, 255, a))
        ds.polygon([(sx - r * 2, sy), (sx, sy - r // 2), (sx + r * 2, sy), (sx, sy + r // 2)], fill=(255, 255, 255, a))
    return img


def edicao():
    W, H = 512, 64
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    fonte = ImageFont.truetype(FONTE, 34)
    texto = "MISSÃO LUNAR BRASILEIRA"
    bb = fonte.getbbox(texto)
    tw = bb[2] - bb[0]
    if tw > W - 8:
        fonte = ImageFont.truetype(FONTE, int(34 * (W - 8) / tw))
        bb = fonte.getbbox(texto)
        tw = bb[2] - bb[0]
    m = Image.new("L", (W, H), 0)
    ImageDraw.Draw(m).text(((W - tw) // 2 - bb[0], 6 - bb[1] + 4), texto, font=fonte, fill=255)
    sombra = Image.new("RGBA", (W, H), (8, 12, 28, 255))
    sombra.putalpha(m.filter(ImageFilter.MaxFilter(5)))
    img.alpha_composite(sombra)
    cor = Image.new("RGBA", (W, H), (252, 214, 70, 255))
    cor.putalpha(m)
    img.alpha_composite(cor)
    return img


SPLASHES = """Agora com 100% menos oxigênio!
Feito no Brasil, minerado na Lua!
Nióbio é o novo diamante!
A Terra é azul!
Um pequeno passo para o jogador...
Cuidado com os alienígenas!
Não esqueça o capacete!
Gravidade? Que gravidade?
Base Cruzeiro do Sul na escuta!
Sem chuva há 4 bilhões de anos!
Regolito por todo lado!
Crateras grátis!
Câmbio, desligo!
Houston, temos um bloco!
Plante batatas no espaço!
Mais cratera que estrada!
Céu preto, ideias brilhantes!
Próxima parada: Terra!
Energia solar inclusa!
O lado escuro não é tão escuro!
Brasil acima da atmosfera!
Ordem e Progresso... e Mineração!
Pule mais alto!
Agora com alienígenas!
Hélio-3 em breve!
Tanques cheios, missão segura!
Feito com café brasileiro!
Lua cheia de possibilidades!
Sem nuvens para atrapalhar!
Contagem regressiva: 3, 2, 1...
""".strip().splitlines()


# ---------------- Botões e fundos ----------------

def botao(w, h, estado):
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    if estado == "desligado":
        topo, base, borda = (26, 30, 40), (18, 20, 28), (50, 56, 68)
    elif estado == "foco":
        topo, base, borda = (40, 78, 112), (22, 44, 70), CIANO
    else:
        topo, base, borda = (44, 56, 84), (24, 30, 48), (98, 116, 150)
    for y in range(h):
        t = y / max(1, h - 1)
        c = tuple(int(a + (b - a) * t) for a, b in zip(topo, base))
        d.line([(0, y), (w - 1, y)], fill=c + (255,))
    d.rectangle([0, 0, w - 1, h - 1], outline=borda + (255,))
    d.line([(1, 1), (w - 2, 1)], fill=tuple(min(255, c + 40) for c in topo) + (255,))
    for x, y in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]:
        img.putpixel((x, y), (0, 0, 0, 0))
    if estado == "foco":
        d.line([(2, h - 2), (w - 3, h - 2)], fill=CIANO + (160,))
    return img


def widgets(original):
    img = recolorir(original.copy(), so_cinzas=True)
    barra = recolorir(original.crop((0, 0, 182, 46)), so_cinzas=False, preservar_quentes=False)
    img.paste(barra, (0, 0))
    img.paste(botao(200, 20, "desligado"), (0, 46))
    img.paste(botao(200, 20, "normal"), (0, 66))
    img.paste(botao(200, 20, "foco"), (0, 86))
    return img


def fundo_painel(escuro=True):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    rnd = random.Random(3 if escuro else 4)
    base = (14, 18, 32) if escuro else (30, 36, 54)
    for y in range(16):
        for x in range(16):
            v = rnd.randint(-3, 3)
            px[x, y] = tuple(max(0, c + v) for c in base) + (255,)
    for x in range(16):
        px[x, 0] = tuple(c + 14 for c in base) + (255,)
    for y in range(16):
        px[0, y] = tuple(c + 10 for c in base) + (255,)
    px[3, 5] = (120, 140, 180, 255)
    px[11, 12] = (90, 110, 150, 255)
    return img


# ---------------- Blocos comuns ----------------

def painel_metal(base=(196, 204, 218), seed=1):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            v = rnd.randint(-4, 4)
            px[x, y] = tuple(max(0, min(255, c + v)) for c in base) + (255,)
    escuro = tuple(int(c * 0.62) for c in base) + (255,)
    claro = tuple(min(255, c + 30) for c in base) + (255,)
    for i in range(16):
        px[i, 0] = claro
        px[0, i] = claro
        px[i, 15] = escuro
        px[15, i] = escuro
    return img


def bloco_ferro():
    img = painel_metal((214, 220, 230), 5)
    d = ImageDraw.Draw(img)
    d.line([(1, 8), (14, 8)], fill=(150, 158, 172, 255))
    d.line([(1, 9), (14, 9)], fill=(238, 242, 250, 255))
    d.line([(1, 12), (14, 12)], fill=(40, 120, 220, 255))
    for x, y in [(2, 2), (13, 2), (2, 5), (13, 5)]:
        d.point((x, y), fill=(130, 138, 152, 255))
    return img


def vidro():
    img = Image.new("RGBA", (16, 16), (120, 190, 255, 38))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(92, 104, 128, 255))
    d.line([(1, 1), (14, 1)], fill=(170, 182, 204, 255))
    d.line([(1, 1), (1, 14)], fill=(150, 162, 186, 255))
    for x, y in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        d.point((x, y), fill=(60, 68, 88, 255))
    # reflexo diagonal
    for i in range(4):
        d.point((4 + i, 9 - i), fill=(230, 245, 255, 120))
        d.point((5 + i, 9 - i), fill=(230, 245, 255, 70))
    return img


def porta(topo):
    img = painel_metal((176, 186, 204), 11 if topo else 12)
    d = ImageDraw.Draw(img)
    d.line([(8, 0), (8, 15)], fill=(70, 80, 100, 255))
    if topo:
        d.rounded_rectangle([3, 3, 12, 11], radius=3, fill=(0, 0, 0, 0), outline=(70, 80, 100, 255))
        d.point((10, 5), fill=(0, 0, 0, 0))
    else:
        for x in range(1, 15):
            cor = (240, 200, 40, 255) if (x // 2) % 2 == 0 else (30, 30, 36, 255)
            d.point((x, 13), fill=cor)
            d.point((x, 14), fill=cor)
        d.rectangle([11, 4, 12, 6], fill=CIANO + (255,))
    return img


def porta_item():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 1, 11, 14], fill=(176, 186, 204, 255), outline=(70, 80, 100, 255))
    d.rounded_rectangle([5, 3, 10, 7], radius=1, fill=(80, 160, 220, 255))
    d.line([(5, 12), (10, 12)], fill=(240, 200, 40, 255))
    d.point((9, 9), fill=CIANO + (255,))
    return img


def tocha():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y in range(6, 16):
        for x in (7, 8):
            if y <= 7:
                px[x, y] = (230, 255, 255, 255) if y == 6 else (120, 240, 240, 255)
            else:
                px[x, y] = (70, 80, 100, 255) if x == 7 else (110, 122, 146, 255)
    px[7, 10] = CIANO + (255,)
    return img


def lampada_led():
    img = painel_metal((60, 68, 88), 21)
    d = ImageDraw.Draw(img)
    d.rectangle([2, 2, 13, 13], fill=(200, 250, 255, 255))
    d.rectangle([3, 3, 12, 12], fill=(240, 255, 255, 255))
    for i in (5, 10):
        d.line([(2, i), (13, i)], fill=(170, 230, 240, 255))
        d.line([(i, 2), (i, 13)], fill=(170, 230, 240, 255))
    return img


def bancada(face):
    """Bancada de montagem: tampa com grade de ferramentas e laterais metálicas."""
    if face == "top":
        img = painel_metal((92, 104, 128), 31)
        d = ImageDraw.Draw(img)
        for i in (5, 10):
            d.line([(1, i), (14, i)], fill=(60, 70, 90, 255))
            d.line([(i, 1), (i, 14)], fill=(60, 70, 90, 255))
        d.rectangle([2, 2, 3, 3], fill=CIANO + (255,))
        return img
    img = painel_metal((150, 162, 184), 32 if face == "front" else 33)
    d = ImageDraw.Draw(img)
    d.rectangle([2, 3, 13, 9], fill=(30, 40, 60, 255))
    if face == "front":
        d.line([(4, 5), (8, 5)], fill=CIANO + (255,))
        d.line([(4, 7), (11, 7)], fill=(90, 200, 120, 255))
    else:
        d.line([(4, 6), (11, 6)], fill=(70, 90, 120, 255))
    d.rectangle([3, 11, 12, 13], fill=(110, 122, 146, 255))
    return img


def fornalha(face, acesa=False, alto=False):
    base = (126, 136, 156) if not alto else (96, 104, 124)
    if face == "top":
        img = painel_metal(base, 41 + alto)
        d = ImageDraw.Draw(img)
        d.ellipse([4, 4, 11, 11], fill=(50, 56, 72, 255), outline=(170, 180, 200, 255))
        return img
    img = painel_metal(base, 43 + alto)
    d = ImageDraw.Draw(img)
    if face == "front":
        d.rounded_rectangle([3, 6, 12, 13], radius=3, fill=(20, 22, 30, 255), outline=(60, 66, 84, 255))
        if acesa:
            d.rounded_rectangle([4, 8, 11, 12], radius=2, fill=(255, 140, 40, 255))
            d.line([(5, 10), (10, 10)], fill=(255, 230, 120, 255))
        d.rectangle([3, 2, 12, 3], fill=(40, 120, 220, 255) if not alto else (220, 120, 40, 255))
    else:
        for y in (4, 7, 10):
            d.line([(3, y), (12, y)], fill=(80, 88, 108, 255))
    return img


def bau(original):
    """Baú vira caixa de carga: recolore toda a madeira para metal e destaca a trava."""
    img = original.convert("RGBA")
    px = img.load()
    for y in range(64):
        for x in range(64):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = int(0.299 * r + 0.587 * g + 0.114 * b)
            if x < 6 and y < 5:
                px[x, y] = tuple(int(c * (0.4 + lum / 255 * 0.8)) for c in CIANO) + (a,)
            else:
                px[x, y] = gradiente(min(255, int(lum * 1.35))) + (a,)
    return img


def main():
    if len(sys.argv) < 2:
        print("uso: python3 tools/gerar_visual.py /caminho/assets/minecraft")
        sys.exit(1)
    van = sys.argv[1]
    T = "textures"

    salvar(logo(), T, "gui/title/minecraft.png")
    salvar(edicao(), T, "gui/title/edition.png")
    os.makedirs(os.path.join(OUT, "texts"), exist_ok=True)
    with open(os.path.join(OUT, "texts/splashes.txt"), "w", encoding="utf-8") as f:
        f.write("\n".join(SPLASHES) + "\n")

    salvar(widgets(Image.open(os.path.join(van, T, "gui/widgets.png"))), T, "gui/widgets.png")
    salvar(fundo_painel(True), T, "gui/options_background.png")
    salvar(fundo_painel(False), T, "gui/light_dirt_background.png")
    for nome in ["slider.png", "checkbox.png", "tab_button.png", "recipe_book.png", "recipe_button.png"]:
        salvar(recolorir(Image.open(os.path.join(van, T, "gui", nome))), T, "gui", nome)
    cont = os.path.join(van, T, "gui/container")
    for raiz, _, arquivos in os.walk(cont):
        for nome in arquivos:
            if not nome.endswith(".png"):
                continue
            rel = os.path.relpath(os.path.join(raiz, nome), os.path.join(van, T))
            salvar(recolorir(Image.open(os.path.join(raiz, nome))), T, rel)

    B = "textures/block"
    salvar(bloco_ferro(), B, "iron_block.png")
    salvar(vidro(), B, "glass.png")
    salvar(porta(True), B, "oak_door_top.png")
    salvar(porta(False), B, "oak_door_bottom.png")
    salvar(porta_item(), "textures/item", "oak_door.png")
    salvar(tocha(), B, "torch.png")
    salvar(lampada_led(), B, "sea_lantern.png")
    for face in ["top", "front", "side"]:
        salvar(bancada(face), B, f"crafting_table_{face}.png")
    salvar(fornalha("front"), B, "furnace_front.png")
    salvar(fornalha("front", acesa=True), B, "furnace_front_on.png")
    salvar(fornalha("side"), B, "furnace_side.png")
    salvar(fornalha("top"), B, "furnace_top.png")
    salvar(fornalha("front", alto=True), B, "blast_furnace_front.png")
    salvar(fornalha("front", acesa=True, alto=True), B, "blast_furnace_front_on.png")
    salvar(fornalha("side", alto=True), B, "blast_furnace_side.png")
    salvar(fornalha("top", alto=True), B, "blast_furnace_top.png")
    salvar(bau(Image.open(os.path.join(van, "textures/entity/chest/normal.png"))), "textures/entity/chest", "normal.png")
    print("visual gerado")


if __name__ == "__main__":
    main()
