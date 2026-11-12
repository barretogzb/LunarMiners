# LunarMiners

Mod de Minecraft (Forge 1.20.1) feito do zero: uma base brasileira na Lua, mineração, pesquisa,
criação de vida, máquinas, veículos, armaduras, alienígenas e, no fim, a volta para a Terra.

## Versão atual: 0.5 — "Energia e Eclusas"
**Rede elétrica (Forge Energy)**
- Painel Solar: 15 FE/t de dia, com céu aberto
- Cabo de Energia: se conecta sozinho a cabos e máquinas
- Banco de Baterias: 400k FE, guarda a energia do dia para a noite (a faixa verde mostra a carga)
- Reator de Hélio-3: queima cristais de hélio-3 e gera 160 FE/t, de dia ou de noite

**Ciclo do oxigênio**
- Eletrolisador: transforma gelo lunar em O₂ usando energia e enche os tanques (raio de 32 blocos)
- Tanques: guardam O₂ de verdade; cada corpo empilhado aumenta a capacidade
- Distribuidor: gasta energia e O₂ dos tanques a cada segundo (mais pessoas = mais consumo). Sem energia ou sem O₂ ele desliga e o ar da base acaba

**Eclusa**
- Porta de Eclusa: pressuriza antes de abrir (chiado e vapor, 2,5 s), fecha sozinha, e tem trava de segurança — não abre se outra porta da eclusa estiver aberta

**Blocos próprios no lugar dos do jogo**
- Cápsula de Dormir (cama), Bastão de Luz (iluminação), Ração Espacial (comida)
- Nenhum bloco comum é retexturizado mais

**Rover Lunar**
- Dirigível (WASD), 2 lugares, sobe degraus, bateria com teto solar e compartimento de carga (agache + clique)
- A base já começa com um rover estacionado do lado de fora da eclusa

## Versões anteriores
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
- `tools/gerar_dados.py`: modelos, receitas, saques, tags, nomes e geração do mundo
- `tools/gerar_visual.py <assets/minecraft original>`: logo, menus, inventários e blocos comuns
