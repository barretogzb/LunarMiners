# LunarMiners

Mod de Minecraft (Forge 1.20.1) feito do zero: uma base brasileira na Lua, mineração, pesquisa,
criação de vida, máquinas, veículos, armaduras, alienígenas e, no fim, a volta para a Terra.

## Versão atual: 0.3 — "Visual Lunar"
- Tela inicial nova: logo LUNARMINERS, "Missão Lunar Brasileira" e frases em português
- Panorama do menu renderizado: base em cúpula, tanques de oxigênio, bandeira do Brasil e a Terra no céu
- Menus, botões, barra de itens e todos os inventários no tema aço lunar
- Blocos comuns retexturizados: bancada de montagem, fornalhas, caixa de carga, vidro de habitat, porta de eclusa, lâmpadas LED
- Base Cruzeiro do Sul refeita como cúpula de vidro com nervuras, eclusa de entrada e tanques de oxigênio
- Locais abandonados de outras missões, com bandeiras de vários países e baús de suprimentos
- Tudo vem dentro do mod (não precisa ativar texture pack)

## Versões anteriores
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
- `tools/gerar_visual.py <assets/minecraft original>`: logo, menus, inventários e blocos comuns
