# LunarMiners

Mod de Minecraft (Forge 1.20.1) feito do zero: uma base brasileira na Lua, mineração, pesquisa,
criação de vida, máquinas, veículos, armaduras, alienígenas e, no fim, a volta para a Terra.

## Versão atual: 0.4 — "Sobreviver na Lua"
**Sobrevivência**
- Oxigênio: fora da área pressurizada ele acaba (barra de bolhas). Sem traje: 15 s
- Distribuidor de Oxigênio: cria ar respirável num raio de 18 blocos, desde que tenha uma válvula de tanque por perto
- Trajes: Traje Espacial (alumínio) e Traje de Titânio (conjunto completo dá Resistência)
- Módulos na bigorna: tanque extra de O₂, visor noturno, servomotores (velocidade) e propulsores (pulo alto, sem dano de queda)

**Minérios e equipamentos** (cada um com uma função)
- Alumínio: ferramentas iniciais, trajes, máquinas e hastes (no lugar dos gravetos)
- Nióbio: ferramentas que mineram muito rápido e peças supercondutoras
- Titânio: as ferramentas e o traje mais resistentes
- Silício: painéis solares, luminárias e forno solar
- Hélio-3: raro e brilhante, no fundo da Lua — módulo de propulsores
- Gelo Lunar: em bolsões profundos e no fundo de crateras grandes — vira água

**Máquinas e móveis próprios, arredondados**
- Bancada de Montagem, Forno Solar (funciona sem combustível sob o Sol), Contêiner de Suprimentos
- Mesa redonda, banco, luminárias de teto e de chão, painel solar
- Painel, piso e vidro de habitat, plataforma de pouso

**Bases**
- Complexo Cruzeiro do Sul: cúpula central e três cúpulas ligadas por túneis (estufa, oficina, dormitório), eclusa, plataforma de pouso, fazenda de tanques, campo de painéis solares
- Postos abandonados de outras nações a mais de 1000 blocos de distância, encaixados no terreno
- Bandeiras reais 3x2 em mastros, de 10 países (kit de bandeira para fincar a sua)

## Versões anteriores
- 0.3 "Visual Lunar": tela inicial, menus e blocos comuns retexturizados
- 0.2 "A Lua": mundo lunar, céu espacial, gravidade baixa, alienígenas e base inicial
- 0.1: nióbio e build automático para o Technic

## Como cada atualização chega no Technic
1. Cada envio para a `main` dispara o GitHub Actions, que compila o mod e monta o modpack.
2. O pacote sai sempre no mesmo link:
   `https://raw.githubusercontent.com/barretogzb/LunarMiners/modpack/LunarMiners-modpack.zip`
3. No technicpack.net: **Edit Modpack → Versions**, coloque o número de versão que aparece na release
   (ex.: `0.1.0.1`) e clique em **Update Version**.
4. Reabra o Technic Launcher para ele baixar a versão nova.


## Texturas
As texturas são geradas por código:
- `tools/gerar_texturas.py`: blocos e itens do mod
- `tools/gerar_panorama.py`: panorama da tela inicial
- `tools/gerar_texturas_04.py` e `tools/gerar_bandeiras.py`: texturas da 0.4 e bandeiras
- `tools/gerar_dados.py`: modelos, receitas, saques, tags, nomes e geração do mundo
- `tools/gerar_visual.py <assets/minecraft original>`: logo, menus, inventários e blocos comuns
