# LunarMiners

Mod de Minecraft (Forge 1.20.1) feito do zero: uma base brasileira na Lua, mineração, pesquisa,
criação de vida, máquinas, veículos, armaduras, alienígenas e, no fim, a volta para a Terra.

## Versão atual: 0.7 — "Ameaça Alienígena"
- Colmeias alienígenas: grandes cúpulas orgânicas meio enterradas, a mais de 700 blocos da base, com túneis de entrada, pilares de resina brilhante, ovos pelo chão e um contêiner com saque
- Ovos alienígenas: eclodem quando você chega perto
- Alienígena Cuspidor: mantém distância e cospe ácido (dano + veneno)
- Rainha Alienígena: chefe com barra de vida, cospe ácido, invoca filhotes e fica furiosa com menos de metade da vida. Deixa o Núcleo da Rainha — a peça-chave do foguete para voltar à Terra (próxima versão)

## Versões anteriores
- 0.6 "Grandioso": painel solar grande, rover estilo Apollo, meteoros, tempestade solar, scanner, jetpack e torre de comunicação
- 0.5 "Energia e Eclusas": rede elétrica, ciclo do oxigênio, eclusa, cápsula de dormir e rover
- 0.4 "Sobreviver na Lua": oxigênio, trajes, minérios, ferramentas, máquinas e móveis, bandeiras reais, base grande
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
- `tools/gerar_texturas_05.py`: texturas da 0.5 (rede elétrica, eclusa, rover)
- `tools/gerar_texturas_06.py`: texturas da 0.6 (painel grande, torre, meteorito, rover grande)
- `tools/gerar_texturas_07.py`: texturas da 0.7 (colmeia e alienígenas)
- `tools/gerar_dados.py`: modelos, receitas, saques, tags, nomes e geração do mundo
- `tools/gerar_visual.py <assets/minecraft original>`: logo, menus, inventários e blocos comuns
