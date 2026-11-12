# LunarMiners

Mod de Minecraft (Forge 1.20.1) feito do zero: uma base brasileira na Lua, mineração, pesquisa,
criação de vida, máquinas, veículos, armaduras, alienígenas e, no fim, a volta para a Terra.

## Versão atual: 0.2 — "A Lua"
- O mundo padrão agora é a Lua: terreno de regolito e rocha lunar, sem água, crateras, mares lunares escuros
- Céu espacial: estrelas o tempo todo, o Sol e a Terra no céu, sem nuvens nem chuva
- Gravidade baixa (pulos altos, quedas mais leves)
- Alienígenas que atacam à noite e em cavernas (não queimam no sol)
- Base Lunar Cruzeiro do Sul no ponto de nascimento, com estufa, bandeiras do Brasil e kit de sobrevivência
- Minérios lunares: nióbio e ferro
- Mundos criados antes da 0.2 continuam com o terreno antigo: crie um mundo novo

## Como cada atualização chega no Technic
1. Cada envio para a `main` dispara o GitHub Actions, que compila o mod e monta o modpack.
2. O pacote sai sempre no mesmo link:
   `https://raw.githubusercontent.com/barretogzb/LunarMiners/modpack/LunarMiners-modpack.zip`
3. No technicpack.net: **Edit Modpack → Versions**, coloque o número de versão que aparece na release
   (ex.: `0.1.0.1`) e clique em **Update Version**.
4. Reabra o Technic Launcher para ele baixar a versão nova.

O texture pack vem em `resourcepacks/`. Ative uma vez em **Opções → Pacotes de recursos**.

## Texturas
As texturas são geradas por código em `tools/gerar_texturas.py` (`python3 tools/gerar_texturas.py`).
