"""Gera os JSONs do mod: modelos 3D arredondados, estados de bloco, receitas, saques,
tags, nomes (pt-BR/en), minérios na geração do mundo e as estruturas.

Uso: python3 tools/gerar_dados.py
"""
import json
import os

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(RAIZ, "src/main/resources")
A = os.path.join(RES, "assets/lunarminers")
D = os.path.join(RES, "data/lunarminers")
M = "lunarminers:"
PAISES = ["brasil", "eua", "china", "russia", "india", "japao", "franca", "italia", "alemanha", "argentina"]
NOMES_PAISES = {"brasil": ("Brasil", "Brazil"), "eua": ("EUA", "USA"), "china": ("China", "China"),
                "russia": ("Rússia", "Russia"), "india": ("Índia", "India"), "japao": ("Japão", "Japan"),
                "franca": ("França", "France"), "italia": ("Itália", "Italy"), "alemanha": ("Alemanha", "Germany"),
                "argentina": ("Argentina", "Argentina")}


def w(caminho, dado):
    os.makedirs(os.path.dirname(caminho), exist_ok=True)
    with open(caminho, "w", encoding="utf-8") as f:
        json.dump(dado, f, indent=2, ensure_ascii=False)
        f.write("\n")


def ler(caminho):
    with open(caminho, encoding="utf-8") as f:
        return json.load(f)


# ---------------- modelos ----------------

def r5(v):
    return round(v * 2) / 2


def caixa(f, t, lado, topo=None, base=None, rot=None):
    topo = topo or lado
    base = base or topo
    el = {"from": [r5(x) for x in f], "to": [r5(x) for x in t], "faces": {
        "north": {"texture": lado}, "south": {"texture": lado}, "east": {"texture": lado}, "west": {"texture": lado},
        "up": {"texture": topo}, "down": {"texture": base}}}
    if rot:
        el["rotation"] = rot
    return el


def cilindro(r, y0, y1, lado, topo=None, base=None, cx=8, cz=8):
    """Octógono aproximado com 3 caixas (fica redondo no jogo)."""
    a, b, c = r, r * 0.62, r * 0.87
    return [caixa([cx - a, y0, cz - b], [cx + a, y1, cz + b], lado, topo, base),
            caixa([cx - b, y0, cz - a], [cx + b, y1, cz + a], lado, topo, base),
            caixa([cx - c, y0, cz - c], [cx + c, y1, cz + c], lado, topo, base)]


def modelo(nome, texturas, elementos, render=None, particula=None):
    t = {k: (v if ":" in v else M + "block/" + v) for k, v in texturas.items()}
    t["particle"] = t.get(particula) if particula else list(t.values())[0]
    m = {"parent": "minecraft:block/block", "textures": t, "elements": elementos}
    if render:
        m["render_type"] = "minecraft:" + render
    w(f"{A}/models/block/{nome}.json", m)


def cubo(nome, textura=None, render=None):
    m = {"parent": "minecraft:block/cube_all", "textures": {"all": M + "block/" + (textura or nome)}}
    if render:
        m["render_type"] = "minecraft:" + render
    w(f"{A}/models/block/{nome}.json", m)


def estado_simples(nome, modelo_nome=None):
    w(f"{A}/blockstates/{nome}.json", {"variants": {"": {"model": M + "block/" + (modelo_nome or nome)}}})


def item_bloco(nome, modelo_nome=None):
    w(f"{A}/models/item/{nome}.json", {"parent": M + "block/" + (modelo_nome or nome)})


def item_plano(nome, textura=None, mao=False):
    w(f"{A}/models/item/{nome}.json", {"parent": "minecraft:item/" + ("handheld" if mao else "generated"),
                                       "textures": {"layer0": M + "item/" + (textura or nome)}})


ROT_Y = {"north": 0, "east": 90, "south": 180, "west": 270}


def modelos():
    # blocos cúbicos
    for b in ["lunar_ice", "moon_titanium_ore", "moon_aluminum_ore", "moon_silicon_ore", "moon_helium3_ore",
              "titanium_block", "aluminum_block", "habitat_wall", "habitat_floor", "landing_pad"]:
        cubo(b)
        estado_simples(b)
        item_bloco(b)
    cubo("habitat_glass", render="translucent")
    estado_simples("habitat_glass")
    item_bloco("habitat_glass")

    # Bancada de Montagem
    el = cilindro(5, 0, 12, "#side", "#edge")
    el += cilindro(8, 12, 14.5, "#edge", "#top")
    el.append(caixa([4, 14.5, 10], [12, 20, 10.5], "#holo"))
    modelo("assembly_bench", {"side": "bench_side", "edge": "bench_edge", "top": "bench_top", "holo": "holo_screen"},
           el, render="translucent", particula="side")

    # Forno Solar (aceso e apagado)
    for aceso in (False, True):
        el = cilindro(6, 0, 11, "#side", "#side")
        el.append({"from": [4.5, 1.5, 1.5], "to": [11.5, 9, 3], "faces": {
            "north": {"texture": "#front"}, "east": {"texture": "#side"}, "west": {"texture": "#side"},
            "up": {"texture": "#side"}, "down": {"texture": "#side"}}})
        el.append(caixa([7.5, 11, 7.5], [8.5, 12, 8.5], "#side"))
        el += cilindro(7.5, 12, 13.5, "#dish_edge", "#dish")
        nome = "solar_furnace_on" if aceso else "solar_furnace"
        modelo(nome, {"side": "solar_furnace_side", "front": "solar_furnace_front_on" if aceso else "solar_furnace_front",
                      "dish": "solar_dish", "dish_edge": "solar_furnace_side"}, el)
    variantes = {}
    for f, r in ROT_Y.items():
        for aceso in ("false", "true"):
            v = {"model": M + "block/" + ("solar_furnace_on" if aceso == "true" else "solar_furnace")}
            if r:
                v["y"] = r
            variantes[f"facing={f},lit={aceso}"] = v
    w(f"{A}/blockstates/solar_furnace.json", {"variants": variantes})
    item_bloco("solar_furnace")

    # Contêiner de Suprimentos (cápsula arredondada)
    el = [caixa([1, 0, 3], [15, 12, 13], "#side", "#top", "#bottom"),
          caixa([3, 0, 1], [13, 12, 15], "#side", "#top", "#bottom"),
          caixa([2, 0, 2], [14, 12, 14], "#side", "#top", "#bottom"),
          caixa([1.5, 5.5, 1.5], [14.5, 6.5, 14.5], "#band")]
    modelo("supply_crate", {"side": "crate_side", "top": "crate_top", "bottom": "habitat_floor", "band": "crate_band"}, el)
    estado_simples("supply_crate")
    item_bloco("supply_crate")

    # Distribuidor de Oxigênio
    el = cilindro(5, 0, 2, "#metal")
    el += cilindro(4, 2, 10, "#side", "#metal")
    el += cilindro(4.5, 5, 6.5, "#glow")
    el += cilindro(5.5, 10, 13, "#metal", "#top")
    modelo("oxygen_distributor", {"metal": "furniture_metal", "side": "distributor_side", "glow": "lamp_glow",
                                  "top": "distributor_top"}, el, particula="side")
    estado_simples("oxygen_distributor")
    item_bloco("oxygen_distributor")

    # Painel Solar
    el = [caixa([7, 0, 7], [9, 8, 9], "#pole")]
    el += cilindro(3, 0, 1, "#pole")
    el.append(caixa([0, 8, 2], [16, 9, 14], "#edge", "#cell", "#edge",
                    rot={"origin": [8, 8.5, 8], "axis": "x", "angle": -22.5}))
    modelo("solar_panel", {"pole": "furniture_metal", "edge": "panel_edge", "cell": "solar_cell"}, el, particula="cell")
    estado_simples("solar_panel")
    item_bloco("solar_panel")

    # Móveis
    el = cilindro(3.5, 0, 1, "#metal") + cilindro(1.5, 1, 12, "#metal") + cilindro(7.5, 12, 14, "#metal", "#top")
    modelo("round_table", {"metal": "furniture_metal", "top": "table_top"}, el)
    el = cilindro(3, 0, 1, "#metal") + cilindro(1, 1, 7, "#metal") + cilindro(5, 7, 9, "#metal", "#seat")
    modelo("stool", {"metal": "furniture_metal", "seat": "seat"}, el)
    el = cilindro(3, 15, 16, "#metal") + cilindro(5, 12, 15, "#glow", "#metal") + cilindro(3.5, 10.5, 12, "#glow")
    modelo("ceiling_lamp", {"metal": "furniture_metal", "glow": "lamp_glow"}, el)
    el = cilindro(3, 0, 1, "#metal") + [caixa([7.25, 1, 7.25], [8.75, 11, 8.75], "#metal")] + cilindro(4, 11, 16, "#glow")
    modelo("floor_lamp", {"metal": "furniture_metal", "glow": "lamp_glow"}, el)
    for b in ["round_table", "stool", "ceiling_lamp", "floor_lamp"]:
        estado_simples(b)
        item_bloco(b)

    # Tanque (reaproveita os modelos existentes)
    for b in ["oxygen_tank", "oxygen_tank_cap"]:
        estado_simples(b)
        item_bloco(b)

    # Mastro
    modelo("flag_pole", {"pole": "flag_pole"}, [caixa([7, 0, 7], [9, 16, 9], "#pole")])
    modelo("flag_pole_top", {"pole": "flag_pole"}, cilindro(2, 0, 3, "#pole") + [caixa([7.5, 3, 7.5], [8.5, 5, 8.5], "#pole")])
    estado_simples("flag_pole")
    estado_simples("flag_pole_top")
    item_bloco("flag_pole")

    # Tecido das bandeiras: 6 partes por país
    for pais in PAISES:
        variantes = {}
        for parte in range(6):
            nome = f"flag/{pais}_{parte}"
            tex = M + f"block/flag/{pais}_{parte}"
            w(f"{A}/models/block/{nome}.json", {"textures": {"cloth": tex, "particle": tex}, "elements": [{
                "from": [0, 0, 7.75], "to": [16, 16, 8.25], "faces": {
                    "north": {"uv": [0, 0, 16, 16], "texture": "#cloth"},
                    "south": {"uv": [16, 0, 0, 16], "texture": "#cloth"},
                    "east": {"uv": [0, 0, 1, 16], "texture": "#cloth"},
                    "west": {"uv": [15, 0, 16, 16], "texture": "#cloth"},
                    "up": {"uv": [0, 0, 16, 1], "texture": "#cloth"},
                    "down": {"uv": [0, 15, 16, 16], "texture": "#cloth"}}}]})
            for f, r in ROT_Y.items():
                v = {"model": M + "block/" + nome}
                if r:
                    v["y"] = r
                variantes[f"facing={f},part={parte}"] = v
        w(f"{A}/blockstates/flag_{pais}.json", {"variants": variantes})
        item_plano(f"flag_kit_{pais}", f"flag_kit/{pais}")

    # Itens
    for i in ["raw_titanium", "titanium_ingot", "raw_aluminum", "aluminum_ingot", "aluminum_rod", "silicon",
              "helium3_crystal", "ice_shard", "o2_tank_module", "night_vision_module", "servo_module", "thruster_module"]:
        item_plano(i)
    for metal in ["aluminum", "niobium", "titanium"]:
        for f in ["pickaxe", "axe", "shovel", "sword", "hoe"]:
            item_plano(f"{metal}_{f}", mao=True)
    for traje in ["space_suit", "titanium_suit"]:
        for p in ["helmet", "chestplate", "leggings", "boots"]:
            item_plano(f"{traje}_{p}")


# ---------------- saques ----------------

def saque_proprio(b):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
            "entries": [{"type": "minecraft:item", "name": M + b}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def saque_minerio(b, item, minimo=1, maximo=1):
    funcs = []
    if maximo > 1 or minimo != 1:
        funcs.append({"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": minimo, "max": maximo}})
    funcs += [{"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
              {"function": "minecraft:explosion_decay"}]
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": M + b, "conditions": [{"condition": "minecraft:match_tool", "predicate": {
            "enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}]},
        {"type": "minecraft:item", "name": item if ":" in item else M + item, "functions": funcs}]}]}]}


def saques():
    for b in ["titanium_block", "aluminum_block", "habitat_wall", "habitat_floor", "habitat_glass", "landing_pad",
              "assembly_bench", "solar_furnace", "supply_crate", "oxygen_distributor", "solar_panel", "round_table",
              "stool", "ceiling_lamp", "floor_lamp", "flag_pole", "oxygen_tank", "oxygen_tank_cap"]:
        w(f"{D}/loot_tables/blocks/{b}.json", saque_proprio(b))
    w(f"{D}/loot_tables/blocks/moon_titanium_ore.json", saque_minerio("moon_titanium_ore", "raw_titanium"))
    w(f"{D}/loot_tables/blocks/moon_aluminum_ore.json", saque_minerio("moon_aluminum_ore", "raw_aluminum", 1, 2))
    w(f"{D}/loot_tables/blocks/moon_silicon_ore.json", saque_minerio("moon_silicon_ore", "silicon", 1, 3))
    w(f"{D}/loot_tables/blocks/moon_helium3_ore.json", saque_minerio("moon_helium3_ore", "helium3_crystal"))
    w(f"{D}/loot_tables/blocks/lunar_ice.json", saque_minerio("lunar_ice", "ice_shard", 2, 4))
    w(f"{D}/loot_tables/blocks/flag_pole_top.json", {"type": "minecraft:block", "pools": []})
    for pais in PAISES:
        w(f"{D}/loot_tables/blocks/flag_{pais}.json", {"type": "minecraft:block", "pools": []})


# ---------------- tags ----------------

def tags():
    picareta = ["niobium_ore", "deepslate_niobium_ore", "niobium_block", "moon_rock", "moon_cobblestone", "moon_deep_rock",
                "moon_niobium_ore", "moon_iron_ore", "moon_titanium_ore", "moon_aluminum_ore", "moon_silicon_ore",
                "moon_helium3_ore", "lunar_ice", "titanium_block", "aluminum_block", "habitat_wall", "habitat_floor",
                "habitat_glass", "landing_pad", "assembly_bench", "solar_furnace", "supply_crate", "oxygen_distributor",
                "oxygen_tank", "oxygen_tank_cap", "solar_panel", "round_table", "stool", "ceiling_lamp", "floor_lamp",
                "flag_pole", "flag_pole_top"]
    w(f"{RES}/data/minecraft/tags/blocks/mineable/pickaxe.json", {"replace": False, "values": [M + b for b in picareta]})
    w(f"{RES}/data/minecraft/tags/blocks/mineable/shovel.json", {"replace": False, "values": [M + "moon_regolith", M + "moon_mare_regolith"]})
    w(f"{RES}/data/minecraft/tags/blocks/needs_iron_tool.json", {"replace": False, "values": [M + b for b in [
        "niobium_ore", "deepslate_niobium_ore", "moon_niobium_ore", "niobium_block", "moon_titanium_ore", "moon_helium3_ore",
        "titanium_block"]]})
    w(f"{RES}/data/minecraft/tags/blocks/needs_stone_tool.json", {"replace": False, "values": [M + b for b in [
        "moon_iron_ore", "moon_aluminum_ore", "moon_silicon_ore", "aluminum_block"]]})
    novos = ["moon_regolith", "moon_mare_regolith", "moon_rock", "moon_cobblestone", "moon_deep_rock", "moon_niobium_ore",
             "moon_iron_ore", "moon_titanium_ore", "moon_aluminum_ore", "moon_silicon_ore", "moon_helium3_ore", "lunar_ice"]
    w(f"{RES}/data/minecraft/tags/blocks/overworld_carver_replaceables.json", {"replace": False, "values": [M + b for b in novos]})
    w(f"{RES}/data/minecraft/tags/blocks/impermeable.json", {"replace": False, "values": [M + "habitat_glass"]})
    for nome, item in [("ingots/titanium", "titanium_ingot"), ("ingots/aluminum", "aluminum_ingot"),
                       ("raw_materials/titanium", "raw_titanium"), ("raw_materials/aluminum", "raw_aluminum"),
                       ("rods/aluminum", "aluminum_rod"), ("gems/silicon", "silicon"), ("silicon", "silicon")]:
        w(f"{RES}/data/forge/tags/items/{nome}.json", {"replace": False, "values": [M + item]})
    w(f"{RES}/data/lunarminers/tags/worldgen/biome/has_outpost.json", {"replace": False, "values": [
        M + "lunar_highlands", M + "lunar_maria", M + "crater_fields"]})


# ---------------- receitas ----------------

def shaped(nome, padrao, chaves, resultado, qtd=1, cat="misc"):
    usados = set("".join(padrao))
    k = {c: ({"item": v} if ":" in v else {"item": M + v}) for c, v in chaves.items() if c in usados}
    w(f"{D}/recipes/{nome}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": padrao, "key": k,
                                   "result": {"item": resultado if ":" in resultado else M + resultado, "count": qtd}})


def shapeless(nome, ingredientes, resultado, qtd=1):
    w(f"{D}/recipes/{nome}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
                                   "ingredients": [{"item": i if ":" in i else M + i} for i in ingredientes],
                                   "result": {"item": resultado if ":" in resultado else M + resultado, "count": qtd}})


def forno(nome, entrada, saida, xp=0.7, explosao=True):
    tipos = [("smelting", 200), ("blasting", 100)] if explosao else [("smelting", 200)]
    for tipo, tempo in tipos:
        w(f"{D}/recipes/{nome}_{tipo}.json", {"type": f"minecraft:{tipo}", "category": "misc",
            "ingredient": {"item": entrada if ":" in entrada else M + entrada},
            "result": saida if ":" in saida else M + saida, "experience": xp, "cookingtime": tempo})


def receitas():
    forno("titanium_ingot_from_raw", "raw_titanium", "titanium_ingot", 1.0)
    forno("titanium_ingot_from_ore", "moon_titanium_ore", "titanium_ingot", 1.0)
    forno("aluminum_ingot_from_raw", "raw_aluminum", "aluminum_ingot")
    forno("aluminum_ingot_from_ore", "moon_aluminum_ore", "aluminum_ingot")
    forno("glass_from_regolith", "moon_regolith", "minecraft:glass", 0.1, explosao=False)
    for metal in ["titanium", "aluminum"]:
        shaped(f"{metal}_block", ["###", "###", "###"], {"#": f"{metal}_ingot"}, f"{metal}_block", cat="building")
        shapeless(f"{metal}_ingot_from_block", [f"{metal}_block"], f"{metal}_ingot", 9)
    shaped("aluminum_rod", ["A", "A"], {"A": "aluminum_ingot"}, "aluminum_rod", 4)

    for metal, ing in [("aluminum", "aluminum_ingot"), ("niobium", "niobium_ingot"), ("titanium", "titanium_ingot")]:
        k = {"M": ing, "R": "aluminum_rod"}
        shaped(f"{metal}_pickaxe", ["MMM", " R ", " R "], k, f"{metal}_pickaxe", cat="equipment")
        shaped(f"{metal}_axe", ["MM", "MR", " R"], k, f"{metal}_axe", cat="equipment")
        shaped(f"{metal}_shovel", ["M", "R", "R"], k, f"{metal}_shovel", cat="equipment")
        shaped(f"{metal}_sword", ["M", "M", "R"], k, f"{metal}_sword", cat="equipment")
        shaped(f"{metal}_hoe", ["MM", " R", " R"], k, f"{metal}_hoe", cat="equipment")

    a = {"A": "aluminum_ingot", "G": "habitat_glass", "T": "oxygen_tank"}
    shaped("space_suit_helmet", ["AAA", "AGA"], a, "space_suit_helmet", cat="equipment")
    shaped("space_suit_chestplate", ["A A", "ATA", "AAA"], a, "space_suit_chestplate", cat="equipment")
    shaped("space_suit_leggings", ["AAA", "A A", "A A"], a, "space_suit_leggings", cat="equipment")
    shaped("space_suit_boots", ["A A", "A A"], a, "space_suit_boots", cat="equipment")
    for peca, padrao in [("helmet", ["TTT", "TST"]), ("chestplate", ["T T", "TST", "TTT"]),
                         ("leggings", ["TTT", "TST", "T T"]), ("boots", ["TST", "T T"])]:
        shaped(f"titanium_suit_{peca}", padrao, {"T": "titanium_ingot", "S": f"space_suit_{peca}"},
               f"titanium_suit_{peca}", cat="equipment")

    shaped("o2_tank_module", ["A A", "ATA", "AIA"], {"A": "aluminum_ingot", "T": "oxygen_tank", "I": "ice_shard"}, "o2_tank_module")
    shaped("night_vision_module", ["SGS", "AHA"], {"S": "silicon", "G": "habitat_glass", "A": "aluminum_ingot", "H": "alien_goo"}, "night_vision_module")
    shaped("servo_module", ["NSN", "R R"], {"N": "niobium_ingot", "S": "silicon", "R": "aluminum_rod"}, "servo_module")
    shaped("thruster_module", ["AHA", "N N"], {"A": "aluminum_ingot", "H": "helium3_crystal", "N": "niobium_ingot"}, "thruster_module")

    shaped("assembly_bench", ["AAA", "ACA", "R R"], {"A": "aluminum_ingot", "C": "minecraft:crafting_table", "R": "aluminum_rod"}, "assembly_bench")
    shaped("assembly_bench_from_scratch", ["SAS", "ARA", "R R"], {"A": "aluminum_ingot", "S": "silicon", "R": "aluminum_rod"}, "assembly_bench")
    shaped("solar_furnace", ["SSS", "AMA", "AAA"], {"S": "silicon", "A": "aluminum_ingot", "M": "moon_cobblestone"}, "solar_furnace")
    shaped("supply_crate", ["AAA", "A A", "AAA"], {"A": "aluminum_ingot"}, "supply_crate")
    shaped("oxygen_distributor", ["ASA", "NTN", "AAA"], {"A": "aluminum_ingot", "S": "silicon", "N": "niobium_ingot", "T": "oxygen_tank"}, "oxygen_distributor")
    shaped("oxygen_tank", ["A A", "AGA", "A A"], {"A": "aluminum_ingot", "G": "habitat_glass"}, "oxygen_tank", 2, "building")
    shaped("oxygen_tank_cap", [" N ", "ATA"], {"N": "niobium_ingot", "A": "aluminum_ingot", "T": "oxygen_tank"}, "oxygen_tank_cap", 1, "building")
    shaped("habitat_wall", ["RRR", "RAR", "RRR"], {"R": "moon_rock", "A": "aluminum_ingot"}, "habitat_wall", 8, "building")
    shaped("habitat_floor", ["CCC", "CAC", "CCC"], {"C": "moon_cobblestone", "A": "aluminum_ingot"}, "habitat_floor", 8, "building")
    shaped("habitat_glass", ["GGG", "GAG", "GGG"], {"G": "minecraft:glass", "A": "aluminum_ingot"}, "habitat_glass", 8, "building")
    shaped("landing_pad", ["FFF", "FSF", "FFF"], {"F": "habitat_floor", "S": "silicon"}, "landing_pad", 8, "building")
    shaped("round_table", ["AAA", " R ", " A "], {"A": "aluminum_ingot", "R": "aluminum_rod"}, "round_table", 1, "building")
    shaped("stool", ["AAA", " R "], {"A": "aluminum_ingot", "R": "aluminum_rod"}, "stool", 2, "building")
    shaped("ceiling_lamp", [" A ", "SGS"], {"A": "aluminum_ingot", "S": "silicon", "G": "habitat_glass"}, "ceiling_lamp", 2, "building")
    shaped("floor_lamp", ["SGS", " R ", "AAA"], {"A": "aluminum_ingot", "S": "silicon", "G": "habitat_glass", "R": "aluminum_rod"}, "floor_lamp", 2, "building")
    shaped("solar_panel", ["SSS", " R ", "AAA"], {"S": "silicon", "R": "aluminum_rod", "A": "aluminum_ingot"}, "solar_panel", 1, "building")
    shaped("flag_pole", ["R", "R", "R"], {"R": "aluminum_rod"}, "flag_pole", 3, "building")
    shapeless("flag_kit_brasil", ["aluminum_rod", "aluminum_rod", "aluminum_rod", "minecraft:green_dye",
                                  "minecraft:yellow_dye", "minecraft:blue_dye"], "flag_kit_brasil")
    shapeless("water_from_ice", ["minecraft:bucket", "ice_shard", "ice_shard", "ice_shard", "ice_shard"], "minecraft:water_bucket")
    shaped("lunar_ice", ["II", "II"], {"I": "ice_shard"}, "lunar_ice", 1, "building")
    shaped("torch_from_silicon", ["S", "R"], {"S": "silicon", "R": "aluminum_rod"}, "minecraft:torch", 4)


# ---------------- geração ----------------

def minerio_cf(nome, bloco, tamanho, so_profundo=False):
    alvos = [] if so_profundo else [{"target": {"predicate_type": "minecraft:block_match", "block": M + "moon_rock"}, "state": {"Name": M + bloco}}]
    alvos.append({"target": {"predicate_type": "minecraft:block_match", "block": M + "moon_deep_rock"}, "state": {"Name": M + bloco}})
    w(f"{D}/worldgen/configured_feature/{nome}.json", {"type": "minecraft:ore", "config": {
        "size": tamanho, "discard_chance_on_air_exposure": 0.0, "targets": alvos}})


def minerio_pf(nome, quantidade, minimo, maximo, forma="uniform"):
    altura = {"type": f"minecraft:{forma}", "min_inclusive": minimo, "max_inclusive": maximo}
    w(f"{D}/worldgen/placed_feature/{nome}.json", {"feature": M + nome, "placement": [
        {"type": "minecraft:count", "count": quantidade}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": altura}, {"type": "minecraft:biome"}]})


def geracao():
    minerio_cf("moon_titanium_ore", "moon_titanium_ore", 6)
    minerio_pf("moon_titanium_ore", 8, {"above_bottom": 0}, {"absolute": 80}, "trapezoid")
    minerio_cf("moon_aluminum_ore", "moon_aluminum_ore", 10)
    minerio_pf("moon_aluminum_ore", 16, {"absolute": -32}, {"absolute": 180})
    minerio_cf("moon_silicon_ore", "moon_silicon_ore", 8)
    minerio_pf("moon_silicon_ore", 12, {"absolute": 0}, {"absolute": 160})
    minerio_cf("moon_helium3_ore", "moon_helium3_ore", 4, so_profundo=True)
    minerio_pf("moon_helium3_ore", 4, {"above_bottom": 0}, {"absolute": 4}, "trapezoid")
    minerio_cf("lunar_ice_pocket", "lunar_ice", 14, so_profundo=True)
    minerio_pf("lunar_ice_pocket", 3, {"above_bottom": 4}, {"absolute": -10})
    ordem = [M + x for x in ["moon_iron_ore", "moon_niobium_ore", "moon_titanium_ore", "moon_aluminum_ore",
                             "moon_silicon_ore", "moon_helium3_ore", "lunar_ice_pocket"]]
    for b in ["lunar_highlands", "lunar_maria", "crater_fields"]:
        p = f"{D}/worldgen/biome/{b}.json"
        d = ler(p)
        while len(d["features"]) < 7:
            d["features"].append([])
        d["features"][4] = []
        d["features"][6] = ordem
        w(p, d)
    w(f"{D}/worldgen/structure/abandoned_outpost.json", {"type": M + "abandoned_outpost", "biomes": "#" + M + "has_outpost",
        "step": "surface_structures", "spawn_overrides": {}, "terrain_adaptation": "beard_thin"})
    w(f"{D}/worldgen/structure_set/abandoned_outposts.json", {"structures": [{"structure": M + "abandoned_outpost", "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "salt": 77410233, "spacing": 64, "separation": 40}})


# ---------------- nomes ----------------

def nomes():
    pt = ler(f"{A}/lang/pt_br.json")
    en = ler(f"{A}/lang/en_us.json")
    blocos = {
        "lunar_ice": ("Gelo Lunar", "Lunar Ice"), "moon_titanium_ore": ("Minério de Titânio", "Titanium Ore"),
        "moon_aluminum_ore": ("Minério de Alumínio", "Aluminum Ore"), "moon_silicon_ore": ("Minério de Silício", "Silicon Ore"),
        "moon_helium3_ore": ("Minério de Hélio-3", "Helium-3 Ore"), "titanium_block": ("Bloco de Titânio", "Block of Titanium"),
        "aluminum_block": ("Bloco de Alumínio", "Block of Aluminum"), "habitat_wall": ("Painel de Habitat", "Habitat Panel"),
        "habitat_floor": ("Piso de Habitat", "Habitat Floor"), "habitat_glass": ("Vidro de Habitat", "Habitat Glass"),
        "landing_pad": ("Plataforma de Pouso", "Landing Pad"), "assembly_bench": ("Bancada de Montagem", "Assembly Bench"),
        "solar_furnace": ("Forno Solar", "Solar Furnace"), "supply_crate": ("Contêiner de Suprimentos", "Supply Crate"),
        "oxygen_distributor": ("Distribuidor de Oxigênio", "Oxygen Distributor"), "solar_panel": ("Painel Solar", "Solar Panel"),
        "round_table": ("Mesa Redonda", "Round Table"), "stool": ("Banco", "Stool"), "ceiling_lamp": ("Luminária de Teto", "Ceiling Lamp"),
        "floor_lamp": ("Luminária de Chão", "Floor Lamp"), "flag_pole": ("Mastro", "Flag Pole"), "flag_pole_top": ("Topo do Mastro", "Flag Pole Top"),
    }
    for k, (p, e) in blocos.items():
        pt[f"block.lunarminers.{k}"] = p
        en[f"block.lunarminers.{k}"] = e
    for pais in PAISES:
        p, e = NOMES_PAISES[pais]
        pt[f"block.lunarminers.flag_{pais}"] = f"Bandeira: {p}"
        en[f"block.lunarminers.flag_{pais}"] = f"Flag: {e}"
        pt[f"item.lunarminers.flag_kit_{pais}"] = f"Kit de Bandeira: {p}"
        en[f"item.lunarminers.flag_kit_{pais}"] = f"Flag Kit: {e}"
    itens = {
        "raw_titanium": ("Titânio Bruto", "Raw Titanium"), "titanium_ingot": ("Lingote de Titânio", "Titanium Ingot"),
        "raw_aluminum": ("Alumínio Bruto", "Raw Aluminum"), "aluminum_ingot": ("Lingote de Alumínio", "Aluminum Ingot"),
        "aluminum_rod": ("Haste de Alumínio", "Aluminum Rod"), "silicon": ("Silício", "Silicon"),
        "helium3_crystal": ("Cristal de Hélio-3", "Helium-3 Crystal"), "ice_shard": ("Fragmento de Gelo", "Ice Shard"),
        "o2_tank_module": ("Módulo: Tanque Extra de O₂", "Module: Extra O₂ Tank"),
        "night_vision_module": ("Módulo: Visor Noturno", "Module: Night Visor"),
        "servo_module": ("Módulo: Servomotores", "Module: Servo Motors"),
        "thruster_module": ("Módulo: Propulsores", "Module: Thrusters"),
    }
    for metal, (mp, me) in {"aluminum": ("Alumínio", "Aluminum"), "niobium": ("Nióbio", "Niobium"), "titanium": ("Titânio", "Titanium")}.items():
        for f, (fp, fe) in {"pickaxe": ("Picareta", "Pickaxe"), "axe": ("Machado", "Axe"), "shovel": ("Pá", "Shovel"),
                            "sword": ("Espada", "Sword"), "hoe": ("Enxada", "Hoe")}.items():
            itens[f"{metal}_{f}"] = (f"{fp} de {mp}", f"{me} {fe}")
    for traje, (tp, te) in {"space_suit": ("do Traje Espacial", "Space Suit"), "titanium_suit": ("do Traje de Titânio", "Titanium Suit")}.items():
        for p, (pp, pe) in {"helmet": ("Capacete", "Helmet"), "chestplate": ("Suporte de Vida", "Life Support"),
                            "leggings": ("Calças", "Leggings"), "boots": ("Botas", "Boots")}.items():
            itens[f"{traje}_{p}"] = (f"{pp} {tp}", f"{te} {pe}")
    for k, (p, e) in itens.items():
        pt[f"item.lunarminers.{k}"] = p
        en[f"item.lunarminers.{k}"] = e
    extras = {
        "container.lunarminers.assembly_bench": ("Bancada de Montagem", "Assembly Bench"),
        "container.lunarminers.solar_furnace": ("Forno Solar", "Solar Furnace"),
        "container.lunarminers.supply_crate": ("Contêiner de Suprimentos", "Supply Crate"),
        "message.lunarminers.welcome": ("§bBem-vindo ao Complexo Lunar Cruzeiro do Sul! §7Seu traje e o kit estão no contêiner da cúpula central. Fora da base você depende do oxigênio do traje — fique de olho nas bolhas.",
                                        "§bWelcome to the Cruzeiro do Sul Lunar Complex! §7Your suit and kit are in the central dome crate. Outside the base you rely on your suit's oxygen — watch the bubbles."),
        "message.lunarminers.no_oxygen": ("Sem oxigênio! Volte para a base!", "Out of oxygen! Get back to the base!"),
        "message.lunarminers.low_oxygen": ("Oxigênio baixo: %s s", "Low oxygen: %s s"),
        "message.lunarminers.distributor_on": ("Distribuidor ativo: %s válvula(s) de tanque, ar num raio de %s blocos", "Distributor active: %s tank valve(s), air within %s blocks"),
        "message.lunarminers.distributor_off": ("Sem tanque! Coloque uma válvula de tanque de oxigênio a até %s blocos", "No tank! Place an oxygen tank valve within %s blocks"),
        "message.lunarminers.flag_no_space": ("Sem espaço para fincar a bandeira", "No room to plant the flag"),
        "tooltip.lunarminers.flag_kit": ("Clique no chão para fincar o mastro com a bandeira", "Click the ground to plant the pole and flag"),
        "tooltip.lunarminers.suit_oxygen": ("Oxigênio: %s min %s s", "Oxygen: %s min %s s"),
        "tooltip.lunarminers.module_installed": ("Módulo: %s (x%s)", "Module: %s (x%s)"),
        "tooltip.lunarminers.titanium_set": ("Conjunto completo: Resistência", "Full set: Resistance"),
        "tooltip.lunarminers.module.o2_tank": ("Suporte de vida: +5 min de oxigênio (até 3)", "Life support: +5 min of oxygen (up to 3)"),
        "tooltip.lunarminers.module.night_vision": ("Capacete: visão noturna", "Helmet: night vision"),
        "tooltip.lunarminers.module.servo": ("Calças: mais velocidade (até 2)", "Leggings: more speed (up to 2)"),
        "tooltip.lunarminers.module.thruster": ("Botas: pulo alto e sem dano de queda", "Boots: high jump and no fall damage"),
        "tooltip.lunarminers.module_how": ("Instale numa bigorna junto com a peça do traje", "Install it on an anvil with the suit piece"),
    }
    for k, (p, e) in extras.items():
        pt[k] = p
        en[k] = e
    w(f"{A}/lang/pt_br.json", pt)
    w(f"{A}/lang/en_us.json", en)


# ---------------- 0.5: energia, oxigênio, eclusa, cápsula, rover ----------------

def dados_05():
    # cabo: miolo + braços (multipart)
    modelo("cable_core", {"c": "cable"}, [caixa([5.5, 5.5, 5.5], [10.5, 10.5, 10.5], "#c")])
    modelo("cable_arm", {"c": "cable"}, [caixa([6, 6, 0], [10, 10, 5.5], "#c")])
    rot = {"north": {}, "south": {"y": 180}, "east": {"y": 90}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
    partes = [{"apply": {"model": M + "block/cable_core"}}]
    for d, r in rot.items():
        ap = {"model": M + "block/cable_arm", "uvlock": False}
        ap.update(r)
        partes.append({"when": {d: "true"}, "apply": ap})
    w(f"{A}/blockstates/cable.json", {"multipart": partes})
    w(f"{A}/models/item/cable.json", {"parent": "minecraft:block/block", "textures": {"c": M + "block/cable", "particle": M + "block/cable"},
        "elements": [caixa([0, 5.5, 5.5], [16, 10.5, 10.5], "#c")], "display": {"gui": {"rotation": [30, 45, 0], "scale": [0.8, 0.8, 0.8]}}})

    # bateria: 5 níveis de carga (faixa luminosa sobe)
    variantes = {}
    for n in range(5):
        el = [caixa([1, 0, 1], [15, 2, 15], "#metal"), caixa([2, 2, 2], [14, 14, 14], "#side", "#top"),
              caixa([1, 14, 1], [15, 15, 15], "#metal")]
        if n > 0:
            el.append(caixa([1.5, 3, 1.5], [14.5, 3 + 2.5 * n, 14.5], "#glow"))
        modelo(f"battery_{n}", {"metal": "furniture_metal", "side": "battery_side", "top": "battery_top", "glow": "battery_glow"},
               el, render="translucent", particula="side")
        variantes[f"charge={n}"] = {"model": M + f"block/battery_{n}"}
    w(f"{A}/blockstates/battery.json", {"variants": variantes})
    item_bloco("battery", "battery_4")

    # gerador de hélio-3 (reator com núcleo brilhante)
    for aceso in (False, True):
        el = cilindro(7, 0, 2, "#metal") + cilindro(5, 2, 13, "#side", "#metal") + cilindro(3, 4, 11, "#core") \
             + cilindro(6.5, 13, 15, "#metal", "#top")
        el.append({"from": [5, 3, 1.5], "to": [11, 10, 3], "faces": {"north": {"texture": "#front"}, "east": {"texture": "#metal"},
                   "west": {"texture": "#metal"}, "up": {"texture": "#metal"}, "down": {"texture": "#metal"}}})
        nome = "he3_generator_on" if aceso else "he3_generator"
        modelo(nome, {"metal": "furniture_metal", "side": "he3_side", "core": "he3_core_on" if aceso else "he3_core",
                      "top": "distributor_top", "front": "he3_front_on" if aceso else "he3_front"}, el, render="translucent",
               particula="side")
    variantes = {}
    for f, r in ROT_Y.items():
        for aceso in ("false", "true"):
            v = {"model": M + "block/" + ("he3_generator_on" if aceso == "true" else "he3_generator")}
            if r:
                v["y"] = r
            variantes[f"facing={f},lit={aceso}"] = v
    w(f"{A}/blockstates/he3_generator.json", {"variants": variantes})
    item_bloco("he3_generator")

    # eletrolisador (cilindro com bolhas) e distribuidor aceso/apagado
    for aceso in (False, True):
        el = cilindro(6, 0, 2, "#metal") + cilindro(5, 2, 13, "#glass") + cilindro(3, 2, 12, "#water") \
             + cilindro(6, 13, 15, "#metal", "#top") + [caixa([7, 15, 7], [9, 16, 9], "#metal")]
        modelo("electrolyzer_on" if aceso else "electrolyzer",
               {"metal": "furniture_metal", "glass": "habitat_glass", "water": "electrolyzer_water_on" if aceso else "electrolyzer_water",
                "top": "distributor_top"}, el, render="translucent", particula="metal")
        w(f"{A}/blockstates/electrolyzer.json", {"variants": {"lit=false": {"model": M + "block/electrolyzer"},
                                                               "lit=true": {"model": M + "block/electrolyzer_on"}}})
        el = cilindro(5, 0, 2, "#metal") + cilindro(4, 2, 10, "#side", "#metal") + cilindro(4.5, 5, 6.5, "#glow") \
             + cilindro(5.5, 10, 13, "#metal", "#top")
        modelo("oxygen_distributor_on" if aceso else "oxygen_distributor",
               {"metal": "furniture_metal", "side": "distributor_side", "glow": "lamp_glow" if aceso else "lamp_off",
                "top": "distributor_top"}, el, particula="side")
    w(f"{A}/blockstates/oxygen_distributor.json", {"variants": {"lit=false": {"model": M + "block/oxygen_distributor"},
                                                                 "lit=true": {"model": M + "block/oxygen_distributor_on"}}})
    item_bloco("electrolyzer")
    item_bloco("oxygen_distributor", "oxygen_distributor_on")

    # porta de eclusa: painel grosso de 2 blocos; aberta = só o batente
    porta_tex = {"door": "airlock_door_bottom", "top": "airlock_door_top", "frame": "furniture_metal", "warn": "airlock_warning"}
    modelo("airlock_door_bottom", porta_tex, [
        {"from": [0, 0, 6], "to": [16, 16, 10], "faces": {"north": {"texture": "#door"}, "south": {"texture": "#door"},
         "east": {"texture": "#frame"}, "west": {"texture": "#frame"}, "up": {"texture": "#frame"}, "down": {"texture": "#frame"}}}])
    modelo("airlock_door_top", porta_tex, [
        {"from": [0, 0, 6], "to": [16, 16, 10], "faces": {"north": {"texture": "#top"}, "south": {"texture": "#top"},
         "east": {"texture": "#frame"}, "west": {"texture": "#frame"}, "up": {"texture": "#frame"}, "down": {"texture": "#frame"}}}])
    batente = [caixa([0, 0, 6], [2, 16, 10], "#warn"), caixa([14, 0, 6], [16, 16, 10], "#warn")]
    modelo("airlock_door_open_bottom", porta_tex, batente)
    modelo("airlock_door_open_top", porta_tex, batente + [caixa([2, 14, 6], [14, 16, 10], "#frame")])
    variantes = {}
    for f, r in ROT_Y.items():
        for metade in ("lower", "upper"):
            for aberta in ("false", "true"):
                for ciclo in ("false", "true"):
                    base = "airlock_door_open_" if aberta == "true" else "airlock_door_"
                    v = {"model": M + "block/" + base + ("bottom" if metade == "lower" else "top")}
                    if r:
                        v["y"] = r
                    variantes[f"cycling={ciclo},facing={f},half={metade},open={aberta}"] = v
    w(f"{A}/blockstates/airlock_door.json", {"variants": variantes})
    item_plano("airlock_door")

    # cápsula de dormir (2 partes)
    cab = [caixa([1, 0, 1], [15, 3, 16], "#base", "#base"), caixa([2, 3, 2], [14, 7, 16], "#mattress", "#mattress"),
           caixa([1.5, 3, 1], [14.5, 11, 4], "#shell", "#shell"), caixa([2, 7, 4], [14, 10.5, 16], "#glass", "#glass")]
    pe = [caixa([1, 0, 0], [15, 3, 15], "#base", "#base"), caixa([2, 3, 0], [14, 7, 14], "#mattress", "#mattress"),
          caixa([1.5, 3, 12], [14.5, 9, 15], "#shell", "#shell"), caixa([2, 7, 0], [14, 9.5, 12], "#glass", "#glass")]
    tex_cap = {"base": "furniture_metal", "mattress": "seat", "shell": "habitat_wall", "glass": "habitat_glass"}
    modelo("sleeping_pod_head", tex_cap, cab, render="translucent", particula="shell")
    modelo("sleeping_pod_foot", tex_cap, pe, render="translucent", particula="shell")
    variantes = {}
    # a cabeça fica do lado para onde a cama "aponta": no modelo, a cabeceira é o lado norte (z=0)
    rot_cama = {"north": 0, "east": 90, "south": 180, "west": 270}
    for f, r in rot_cama.items():
        for parte in ("head", "foot"):
            for ocupada in ("false", "true"):
                v = {"model": M + f"block/sleeping_pod_{parte}"}
                if r:
                    v["y"] = r
                variantes[f"facing={f},occupied={ocupada},part={parte}"] = v
    w(f"{A}/blockstates/sleeping_pod.json", {"variants": variantes})
    item_plano("sleeping_pod")

    # bastão de luz
    modelo("light_stick", {"stick": "furniture_metal", "glow": "lamp_glow"},
           [caixa([7, 0, 7], [9, 7, 9], "#stick"), caixa([6.5, 7, 6.5], [9.5, 11, 9.5], "#glow")])
    estado_simples("light_stick")
    item_plano("light_stick")

    # tanque: a válvula agora tem estado próprio simples
    estado_simples("oxygen_tank_cap")

    # itens
    item_plano("rover_kit")
    item_plano("space_ration")

    # saques
    for b in ["cable", "battery", "he3_generator", "electrolyzer", "light_stick"]:
        w(f"{D}/loot_tables/blocks/{b}.json", saque_proprio(b))
    w(f"{D}/loot_tables/blocks/airlock_door.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": M + "airlock_door", "conditions": [{"condition": "minecraft:block_state_property",
            "block": M + "airlock_door", "properties": {"half": "lower"}}]}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    w(f"{D}/loot_tables/blocks/sleeping_pod.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
        "entries": [{"type": "minecraft:item", "name": M + "sleeping_pod", "conditions": [{"condition": "minecraft:block_state_property",
            "block": M + "sleeping_pod", "properties": {"part": "head"}}]}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

    # tags
    t = f"{RES}/data/minecraft/tags/blocks/mineable/pickaxe.json"
    d = ler(t)
    for b in ["cable", "battery", "he3_generator", "electrolyzer", "airlock_door"]:
        if M + b not in d["values"]:
            d["values"].append(M + b)
    w(t, d)
    w(f"{RES}/data/minecraft/tags/blocks/beds.json", {"replace": False, "values": [M + "sleeping_pod"]})

    # receitas
    shaped("cable", ["AAA", "NNN", "AAA"], {"A": "aluminum_ingot", "N": "niobium_ingot"}, "cable", 12)
    shaped("battery", ["ASA", "NIN", "ASA"], {"A": "aluminum_ingot", "S": "silicon", "N": "niobium_ingot", "I": "ice_shard"}, "battery")
    shaped("he3_generator", ["TNT", "HGH", "TCT"], {"T": "titanium_ingot", "N": "niobium_ingot", "H": "helium3_crystal",
                                                     "G": "habitat_glass", "C": "cable"}, "he3_generator")
    shaped("electrolyzer", ["ACA", "GIG", "ASA"], {"A": "aluminum_ingot", "C": "cable", "G": "habitat_glass",
                                                   "I": "ice_shard", "S": "silicon"}, "electrolyzer")
    shaped("solar_panel", ["SSS", "NRN", "ACA"], {"S": "silicon", "N": "niobium_ingot", "R": "aluminum_rod",
                                                  "A": "aluminum_ingot", "C": "cable"}, "solar_panel", 1, "building")
    shaped("airlock_door", ["AA", "GA", "WA"], {"A": "aluminum_ingot", "G": "habitat_glass", "W": "habitat_wall"}, "airlock_door", 1, "building")
    shaped("sleeping_pod", ["GGG", "WSW", "AAA"], {"G": "habitat_glass", "W": "habitat_wall", "S": "minecraft:white_wool",
                                                   "A": "aluminum_ingot"}, "sleeping_pod", 1, "building")
    shaped("sleeping_pod_lunar", ["GGG", "WRW", "AAA"], {"G": "habitat_glass", "W": "habitat_wall", "R": "space_ration",
                                                         "A": "aluminum_ingot"}, "sleeping_pod", 1, "building")
    shaped("light_stick", ["S", "R"], {"S": "silicon", "R": "aluminum_rod"}, "light_stick", 6, "building")
    import os as _os
    velho = f"{D}/recipes/torch_from_silicon.json"
    if _os.path.exists(velho):
        _os.remove(velho)
    shaped("rover_kit", ["TGT", "CBC", "WSW"], {"T": "titanium_ingot", "G": "habitat_glass", "C": "cable", "B": "battery",
                                                 "W": "aluminum_block", "S": "solar_panel"}, "rover_kit")
    shapeless("space_ration", ["minecraft:wheat", "minecraft:wheat", "minecraft:carrot", "aluminum_ingot"], "space_ration", 3)
    shapeless("space_ration_potato", ["minecraft:baked_potato", "minecraft:baked_potato", "minecraft:carrot", "aluminum_ingot"], "space_ration", 3)

    # nomes
    pt = ler(f"{A}/lang/pt_br.json")
    en = ler(f"{A}/lang/en_us.json")
    novos = {
        "block.lunarminers.cable": ("Cabo de Energia", "Power Cable"),
        "block.lunarminers.battery": ("Banco de Baterias", "Battery Bank"),
        "block.lunarminers.he3_generator": ("Reator de Hélio-3", "Helium-3 Reactor"),
        "block.lunarminers.electrolyzer": ("Eletrolisador", "Electrolyzer"),
        "block.lunarminers.airlock_door": ("Porta de Eclusa", "Airlock Door"),
        "block.lunarminers.sleeping_pod": ("Cápsula de Dormir", "Sleeping Pod"),
        "item.lunarminers.sleeping_pod": ("Cápsula de Dormir", "Sleeping Pod"),
        "block.lunarminers.light_stick": ("Bastão de Luz", "Light Stick"),
        "item.lunarminers.rover_kit": ("Kit do Rover Lunar", "Lunar Rover Kit"),
        "item.lunarminers.space_ration": ("Ração Espacial", "Space Ration"),
        "entity.lunarminers.rover": ("Rover Lunar", "Lunar Rover"),
        "tooltip.lunarminers.rover_kit": ("Clique no chão para montar. Agache + clique no rover abre a carga.", "Click the ground to deploy. Sneak + click the rover opens its cargo."),
        "message.lunarminers.rover_battery": ("Bateria do rover: %s%%", "Rover battery: %s%%"),
        "message.lunarminers.solar_on": ("Painel solar gerando %s FE/t — %s", "Solar panel generating %s FE/t — %s"),
        "message.lunarminers.solar_off": ("Painel solar parado (sem sol) — %2$s", "Solar panel idle (no sun) — %2$s"),
        "message.lunarminers.battery": ("Bateria %s%% — %s", "Battery %s%% — %s"),
        "message.lunarminers.he3": ("Reator: %s s de combustível — %s. Coloque cristais de hélio-3.", "Reactor: %s s of fuel — %s. Insert helium-3 crystals."),
        "message.lunarminers.electrolyzer": ("Eletrolisador: %s de gelo — %s — O₂ nos tanques: %s", "Electrolyzer: %s ice — %s — O₂ in tanks: %s"),
        "message.lunarminers.distributor_on": ("Distribuidor LIGADO — O₂ nos tanques: %s — %s — ar num raio de %s blocos", "Distributor ON — O₂ in tanks: %s — %s — air within %s blocks"),
        "message.lunarminers.distributor_no_energy": ("Distribuidor DESLIGADO: sem energia! — O₂: %s — %s", "Distributor OFF: no power! — O₂: %s — %s"),
        "message.lunarminers.distributor_no_oxygen": ("Distribuidor DESLIGADO: tanques vazios! — O₂: %s — %s", "Distributor OFF: tanks empty! — O₂: %s — %s"),
        "message.lunarminers.tank": ("Tanque de O₂: %s / %s", "O₂ tank: %s / %s"),
        "message.lunarminers.cable": ("Cabo: %s FE", "Cable: %s FE"),
        "message.lunarminers.airlock_start": ("Ciclando a eclusa... aguarde a pressurização", "Cycling airlock... wait for pressurization"),
        "message.lunarminers.airlock_cycling": ("A eclusa já está ciclando", "The airlock is already cycling"),
        "message.lunarminers.airlock_locked": ("Trava de segurança: feche a outra porta da eclusa primeiro", "Safety lock: close the other airlock door first"),
        "message.lunarminers.welcome": ("§bBem-vindo ao Complexo Lunar Cruzeiro do Sul! §7Seu traje e o kit estão no contêiner da cúpula central. A base vive de energia solar e dos tanques de O₂: de noite as baterias seguram. Para sair, use a eclusa ao sul — tem um rover estacionado lá fora.",
                                        "§bWelcome to the Cruzeiro do Sul Lunar Complex! §7Your suit and kit are in the central dome crate. The base runs on solar power and O₂ tanks: batteries carry the night. To go out, use the south airlock — there is a rover parked outside."),
    }
    for k, (a_, b_) in novos.items():
        pt[k] = a_
        en[k] = b_
    w(f"{A}/lang/pt_br.json", pt)
    w(f"{A}/lang/en_us.json", en)


if __name__ == "__main__":
    modelos()
    saques()
    tags()
    receitas()
    geracao()
    nomes()
    dados_05()
    print("dados gerados")
