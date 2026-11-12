# LunarMiners

Mod de Minecraft (Forge 1.20.1) feito do zero: uma base brasileira na Lua, mineração, pesquisa,
criação de vida, máquinas, veículos, armaduras, alienígenas e, no fim, a volta para a Terra.

## Versão atual: 0.6 — "Grandioso"
**Tudo maior**
- Grande Painel Solar: painel 3x3 inclinado num mastro alto, 60 FE/t (4x o pequeno)
- Rover no estilo do LRV das missões Apollo, em tamanho grande: 6 rodas altas, dois assentos abertos (o astronauta aparece inteiro), painel de controle, teto de painéis solares, bagageiro e antena parabólica

**Eventos (inspirados em filmes e jogos de sobrevivência espacial)**
- Chuva de meteoros (à noite): rochas em chamas caem perto de você, explodem e deixam Minério de Meteorito. A base fica protegida
- Tempestade solar (de dia): aviso 30 s antes; quem estiver a céu aberto toma radiação (o traje de titânio protege mais). Os painéis solares geram 3x
- Comandos para testar (com cheats): `/lunar meteoros`, `/lunar tempestade`, `/lunar oxigenio`

**Equipamentos**
- Scanner de Minérios: mostra quantos minérios há em 24 blocos, a distância e a direção do mais perto
- Módulo de Jetpack (no suporte de vida): segure PULAR no ar para voar, gastando O₂ do traje
- Torre de Comunicação: com 150k FE, chama a Terra e uma cápsula de suprimentos desce freando com retrofoguetes perto da torre (uma vez por dia)

## Versões anteriores
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
- `tools/gerar_dados.py`: modelos, receitas, saques, tags, nomes e geração do mundo
- `tools/gerar_visual.py <assets/minecraft original>`: logo, menus, inventários e blocos comuns
