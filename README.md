# LunarMiners

Mod de Minecraft (Forge 1.20.1) feito do zero: uma base brasileira na Lua, mineração, pesquisa,
criação de vida, máquinas, veículos, armaduras, alienígenas e, no fim, a volta para a Terra.

## Versão atual: 0.1
- Nióbio: minério (pedra e ardósia), nióbio bruto, lingote e bloco
- Geração do minério no subsolo (abaixo de Y 48)
- Aba própria no modo criativo
- Texture pack **LunarMiners Visual**: menu com panorama lunar e a Terra no céu, pedras com cara de solo lunar

## Como cada atualização chega no Technic
1. Cada envio para a `main` dispara o GitHub Actions, que compila o mod e monta o modpack.
2. O pacote sai sempre no mesmo link:
   `https://github.com/barretogzb/lunarminers/releases/latest/download/LunarMiners-modpack.zip`
3. No technicpack.net: **Edit Modpack → Versions**, coloque o número de versão que aparece na release
   (ex.: `0.1.0.1`) e clique em **Update Version**.
4. Reabra o Technic Launcher para ele baixar a versão nova.

O texture pack vem em `resourcepacks/`. Ative uma vez em **Opções → Pacotes de recursos**.

## Texturas
As texturas são geradas por código em `tools/gerar_texturas.py` (`python3 tools/gerar_texturas.py`).
