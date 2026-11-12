"""Renderiza o panorama da tela inicial: a Base Cruzeiro do Sul na Lua, com a Terra no céu.

Cada pixel das 6 faces do cubo vira um raio de visão; o script calcula o que esse raio
encontra (céu, Terra, montanhas, cúpula da base, tanques, bandeira ou chão com crateras).
Assim as faces se encaixam sem emenda.

Uso: python3 tools/gerar_panorama.py
"""
import math
import os

import numpy as np
from PIL import Image

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SAIDA = os.path.join(RAIZ, "src/main/resources/assets/minecraft/textures/gui/title/background")
N = 1024
ALTURA_CAMERA = 1.7


def normalizar(v):
    return v / np.linalg.norm(v, axis=-1, keepdims=True)


def raios_da_face(k):
    s = (np.arange(N) + 0.5) / N * 2 - 1
    u, v = np.meshgrid(s, s)
    um = np.ones_like(u)
    if k < 4:
        ang = math.radians(90 * k)
        local = np.stack([u, -v, um], axis=-1)
        x = local[..., 0] * math.cos(ang) + local[..., 2] * math.sin(ang)
        z = -local[..., 0] * math.sin(ang) + local[..., 2] * math.cos(ang)
        d = np.stack([x, local[..., 1], z], axis=-1)
    elif k == 4:
        d = np.stack([u, um, v], axis=-1)
    else:
        d = np.stack([u, -um, -v], axis=-1)
    return normalizar(d)


def hash2(a, b):
    h = np.sin(a * 127.1 + b * 311.7) * 43758.5453
    return h - np.floor(h)


def ruido(x, y):
    """Ruído suave (value noise) com interpolação."""
    xi, yi = np.floor(x), np.floor(y)
    fx, fy = x - xi, y - yi
    fx = fx * fx * (3 - 2 * fx)
    fy = fy * fy * (3 - 2 * fy)
    a = hash2(xi, yi)
    b = hash2(xi + 1, yi)
    c = hash2(xi, yi + 1)
    d = hash2(xi + 1, yi + 1)
    return (a * (1 - fx) + b * fx) * (1 - fy) + (c * (1 - fx) + d * fx) * fy


def fbm(x, y, oitavas=4):
    total, amp, norm = 0.0, 1.0, 0.0
    for _ in range(oitavas):
        total = total + ruido(x, y) * amp
        norm += amp
        x, y, amp = x * 2.03, y * 2.03, amp * 0.5
    return total / norm


SOL = normalizar(np.array([-0.55, 0.22, -0.8]))
TERRA = normalizar(np.array([0.42, 0.40, 0.81]))
TERRA_RAIO = math.radians(7.5)


def ceu(d):
    yaw = np.arctan2(d[..., 0], d[..., 2])
    elev = np.arcsin(np.clip(d[..., 1], -1, 1))
    cor = np.zeros(d.shape, dtype=np.float64)
    cor[...] = [3, 5, 14]
    # brilho suave perto do horizonte
    hz = np.exp(-np.abs(elev) * 9)
    cor += hz[..., None] * np.array([10, 14, 30])
    # Via Láctea: faixa num grande círculo inclinado
    eixo = normalizar(np.array([0.3, 0.85, -0.42]))
    dist = np.abs(d @ eixo)
    faixa = np.exp(-(dist / 0.16) ** 2)
    nuvem = fbm(yaw * 6, elev * 6) * 1.2
    cor += (faixa * nuvem)[..., None] * np.array([34, 30, 52])
    # estrelas (hash pela direção quantizada: contínuo entre faces)
    q = 900.0
    a = np.floor(d[..., 0] * q)
    b = np.floor(d[..., 1] * q)
    c = np.floor(d[..., 2] * q)
    h = hash2(a + c * 0.37, b - c * 0.61)
    limiar = 0.9965 - faixa * 0.004
    est = h > limiar
    brilho = (h - limiar) / (1 - limiar)
    tom = hash2(b, a)
    estrela = np.stack([200 + 55 * tom, 210 + 45 * tom, 255 * np.ones_like(tom)], axis=-1)
    cor = np.where(est[..., None], cor + estrela * (0.45 + 0.55 * brilho[..., None]), cor)
    # Sol com halo
    cs = np.clip(d @ SOL, -1, 1)
    ang = np.arccos(cs)
    cor += (np.exp(-(ang / 0.05) ** 2) * 255)[..., None] * np.array([1.0, 0.97, 0.9])
    cor += (np.exp(-(ang / 0.25) ** 2) * 60)[..., None] * np.array([1.0, 0.9, 0.75])
    # Terra
    ct = np.clip(d @ TERRA, -1, 1)
    at = np.arccos(ct)
    dentro = at < TERRA_RAIO
    # ponto na esfera da Terra
    up = np.array([0.0, 1.0, 0.0])
    dx = normalizar(np.cross(up, TERRA))
    dy = np.cross(TERRA, dx)
    px = (d @ dx) / math.sin(TERRA_RAIO)
    py = (d @ dy) / math.sin(TERRA_RAIO)
    r2 = np.clip(px ** 2 + py ** 2, 0, 1)
    pz = np.sqrt(1 - r2)
    normal = np.stack([px, py, pz], axis=-1)
    luz = normalizar(np.array([-0.7, 0.25, 0.5]))
    dif = np.clip(normal @ luz, 0, 1)
    lon = np.arctan2(px, pz) * 3
    lat = py * 3
    cont = fbm(lon * 0.9 + 5, lat * 0.9 + 3, 5)
    terra = np.where((cont > 0.53)[..., None], np.array([62, 128, 64]), np.array([24, 70, 160]))
    terra = np.where((cont > 0.62)[..., None], np.array([150, 135, 90]), terra)
    nuv = fbm(lon * 1.6 + 11, lat * 3.0 + 7, 5)
    alfa = np.clip((nuv - 0.55) / 0.12, 0, 1)[..., None]
    terra = terra * (1 - alfa) + np.array([235, 240, 250]) * alfa
    terra = terra * (0.08 + 0.92 * dif[..., None])
    borda = np.clip((r2 - 0.8) / 0.2, 0, 1)
    terra = terra * (1 - borda[..., None] * 0.3) + borda[..., None] * np.array([90, 150, 255]) * dif[..., None]
    cor = np.where(dentro[..., None], terra, cor)
    halo = np.clip(1 - (at - TERRA_RAIO) / 0.03, 0, 1) * (at >= TERRA_RAIO)
    cor += halo[..., None] * np.array([30, 70, 160])
    return cor


def perfil_montanhas(yaw):
    h = (np.sin(yaw * 3 + 1.0) * 0.012 + np.sin(yaw * 7 + 2.0) * 0.008
         + np.sin(yaw * 13 + 0.5) * 0.004 + np.sin(yaw * 29) * 0.0015 + 0.022)
    return np.clip(h, 0.004, None)


def chao(d, mascara):
    """Raios que descem: calcula o ponto no chão e desenha regolito com crateras."""
    t = ALTURA_CAMERA / np.clip(-d[..., 1], 1e-4, None)
    gx = d[..., 0] * t
    gz = d[..., 2] * t
    dist = np.sqrt(gx ** 2 + gz ** 2)
    base = 120 + 34 * (fbm(gx * 0.35, gz * 0.35, 5) - 0.5) + 10 * (ruido(gx * 4, gz * 4) - 0.5)
    cor = np.stack([base, base, base + 4], axis=-1)
    for escala, intensidade in [(6.0, 1.0), (19.0, 1.0), (55.0, 0.8)]:
        cx = np.floor(gx / escala)
        cz = np.floor(gz / escala)
        existe = hash2(cx, cz) > 0.45
        ox = (cx + 0.35 + 0.3 * hash2(cz, cx)) * escala
        oz = (cz + 0.35 + 0.3 * hash2(cx + 3, cz + 7)) * escala
        raio = escala * (0.09 + 0.15 * hash2(cx + 11, cz + 5))
        rx = gx - ox
        rz = gz - oz
        rd = np.sqrt(rx ** 2 + rz ** 2) / raio
        lado = (rx * SOL[0] + rz * SOL[2]) / (np.sqrt(rx ** 2 + rz ** 2) + 1e-6)
        dentro = existe & (rd < 1.0)
        aro = existe & (rd >= 1.0) & (rd < 1.25)
        fundo = 0.6 + 0.3 * rd ** 2 + 0.22 * lado * rd * intensidade
        cor = np.where(dentro[..., None], cor * fundo[..., None], cor)
        cor = np.where(aro[..., None], cor * (1.0 - 0.18 * lado[..., None] * intensidade), cor)
    # escurece ao longe e funde com as montanhas
    nevoa = np.clip(dist / 400, 0, 1)
    cor = cor * (1 - nevoa[..., None] * 0.45) + nevoa[..., None] * np.array([70, 70, 76])
    return cor


def interseccao_esfera(d, centro, raio):
    o = np.array([0.0, ALTURA_CAMERA, 0.0])
    oc = o - centro
    b = d @ oc
    c = oc @ oc - raio * raio
    disc = b * b - c
    t = -b - np.sqrt(np.clip(disc, 0, None))
    ok = (disc > 0) & (t > 0)
    p = o + d * t[..., None]
    ok &= p[..., 1] >= 0
    return ok, t, p


def interseccao_cilindro(d, cx, cz, raio, altura):
    ox, oy, oz = 0.0, ALTURA_CAMERA, 0.0
    a = d[..., 0] ** 2 + d[..., 2] ** 2
    bx = ox - cx
    bz = oz - cz
    b = 2 * (d[..., 0] * bx + d[..., 2] * bz)
    c = bx * bx + bz * bz - raio * raio
    disc = b * b - 4 * a * c
    t = (-b - np.sqrt(np.clip(disc, 0, None))) / (2 * a + 1e-9)
    y = oy + d[..., 1] * t
    ok = (disc > 0) & (t > 0) & (y >= 0) & (y <= altura)
    # tampa arredondada
    return ok, t, y


def estruturas(d, cor, t_atual):
    """Cúpula da base, tanques de oxigênio e bandeira do Brasil."""
    centro = np.array([-9.0, 0.0, 26.0])
    raio = 9.0
    ok, t, p = interseccao_esfera(d, centro, raio)
    ok &= t < t_atual
    n = (p - centro) / raio
    lon = np.arctan2(n[..., 0], n[..., 2])
    lat = np.arcsin(np.clip(n[..., 1], -1, 1))
    nerv = (np.abs(np.sin(lon * 6)) < 0.08) | (np.abs(np.sin(lat * 5)) < 0.07) | (lat < 0.12)
    luz = np.clip(n @ SOL, 0, 1)
    refl = np.clip(n @ normalizar(np.array([0.3, 0.8, -0.5])), 0, 1) ** 8
    vidro = np.array([40, 70, 110]) * (0.4 + 0.6 * luz[..., None]) + refl[..., None] * 160
    interior = np.array([255, 210, 120]) * (0.25 * (n[..., 1] < 0.5))[..., None]
    vidro = vidro + interior
    metal = np.array([150, 160, 180]) * (0.35 + 0.65 * luz[..., None])
    dom = np.where(nerv[..., None], metal, vidro)
    cor = np.where(ok[..., None], dom, cor)
    t_atual = np.where(ok, t, t_atual)

    for i, (tx, tz) in enumerate([(4.0, 22.0), (6.2, 24.5), (8.4, 27.0)]):
        okc, tc, yc = interseccao_cilindro(d, tx, tz, 1.1, 4.2)
        okc &= tc < t_atual
        p = np.stack([d[..., 0] * tc, ALTURA_CAMERA + d[..., 1] * tc, d[..., 2] * tc], axis=-1)
        nx = (p[..., 0] - tx) / 1.1
        nz = (p[..., 2] - tz) / 1.1
        luz = np.clip(nx * SOL[0] + nz * SOL[2], 0, 1)
        branco = np.array([220, 225, 235]) * (0.3 + 0.7 * luz[..., None])
        faixa = (yc > 2.6) & (yc < 3.1)
        tanque = np.where(faixa[..., None], np.array([40, 120, 220]) * (0.4 + 0.6 * luz[..., None]), branco)
        cor = np.where(okc[..., None], tanque, cor)
        t_atual = np.where(okc, tc, t_atual)

    # mastro e bandeira do Brasil (plano voltado para a câmera)
    fx, fz = -1.5, 15.0
    okm, tm, ym = interseccao_cilindro(d, fx, fz, 0.06, 4.0)
    okm &= tm < t_atual
    cor = np.where(okm[..., None], np.array([180, 185, 195]), cor)
    t_atual = np.where(okm, tm, t_atual)
    normal = normalizar(np.array([-fx, 0.0, -fz]))
    o = np.array([0.0, ALTURA_CAMERA, 0.0])
    denom = d @ normal
    tp = ((np.array([fx, 0.0, fz]) - o) @ normal) / np.where(np.abs(denom) < 1e-6, 1e-6, denom)
    p = o + d * tp[..., None]
    lateral = normalizar(np.cross(np.array([0.0, 1.0, 0.0]), normal))
    s = (p - np.array([fx, 0.0, fz])) @ lateral
    y = p[..., 1]
    w, h = 2.4, 1.6
    okf = (tp > 0) & (tp < t_atual) & (s > 0) & (s < w) & (y > 4.0 - h) & (y < 4.0)
    sx = s / w - 0.5
    sy = (y - (4.0 - h / 2)) / h
    losango = np.abs(sx) / 0.42 + np.abs(sy) / 0.40 < 1
    circulo = sx ** 2 * (w / h) ** 2 + sy ** 2 < 0.07
    bandeira = np.where(circulo[..., None], np.array([30, 50, 140]),
                        np.where(losango[..., None], np.array([250, 210, 40]), np.array([20, 140, 60])))
    cor = np.where(okf[..., None], bandeira * 0.9, cor)
    return cor


def renderizar(k):
    d = raios_da_face(k)
    yaw = np.arctan2(d[..., 0], d[..., 2])
    elev = np.arcsin(np.clip(d[..., 1], -1, 1))
    cor = ceu(d)
    t_atual = np.full(elev.shape, 1e9)
    # montanhas distantes
    h = perfil_montanhas(yaw)
    mont = (elev < h) & (elev > -0.004)
    tom = 70 + 50 * np.clip((h - elev) / 0.03, 0, 1) * (0.6 + 0.4 * np.sin(yaw * 23) ** 2)
    luzm = 0.7 + 0.3 * np.cos(yaw - math.atan2(SOL[0], SOL[2]))
    cor = np.where(mont[..., None], np.stack([tom, tom, tom + 6], axis=-1) * luzm[..., None], cor)
    baixo = elev <= -0.004
    cor = np.where(baixo[..., None], chao(d, baixo), cor)
    t_chao = ALTURA_CAMERA / np.clip(-d[..., 1], 1e-4, None)
    t_atual = np.where(baixo, t_chao, t_atual)
    cor = estruturas(d, cor, t_atual)
    return Image.fromarray(np.clip(cor, 0, 255).astype(np.uint8), "RGB")


def main():
    os.makedirs(SAIDA, exist_ok=True)
    for k in range(6):
        renderizar(k).save(os.path.join(SAIDA, f"panorama_{k}.png"))
    print("panorama gerado")


if __name__ == "__main__":
    main()
