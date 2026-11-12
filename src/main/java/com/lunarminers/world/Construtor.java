package com.lunarminers.world;

import com.lunarminers.registry.ModBlocks;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Ferramentas para construir habitats redondos: cúpulas, túneis cilíndricos,
 * pisos circulares e fundações que encostam no terreno (nada flutua).
 * Só escreve onde {@code permitido} aceitar (usado para respeitar os limites de chunk na geração).
 */
public class Construtor {
    public final LevelAccessor level;
    private final Predicate<BlockPos> permitido;

    public Construtor(LevelAccessor level, Predicate<BlockPos> permitido) {
        this.level = level;
        this.permitido = permitido;
    }

    public boolean pode(BlockPos p) {
        return permitido.test(p);
    }

    public void set(BlockPos p, BlockState s) {
        if (permitido.test(p)) {
            level.setBlock(p, s, 2);
        }
    }

    public Bandeiras.Colocador colocador() {
        return this::set;
    }

    private static BlockState wall() {
        return ModBlocks.HABITAT_WALL.get().defaultBlockState();
    }

    private static BlockState glass() {
        return ModBlocks.HABITAT_GLASS.get().defaultBlockState();
    }

    private static BlockState floor() {
        return ModBlocks.HABITAT_FLOOR.get().defaultBlockState();
    }

    /** Desce uma coluna de rocha até encostar no chão firme. */
    public void fundacao(BlockPos topo, BlockState bloco, int maxProf) {
        for (int d = 0; d < maxProf; d++) {
            BlockPos p = topo.below(d);
            if (!permitido.test(p)) {
                return;
            }
            BlockState atual = level.getBlockState(p);
            if (d > 0 && !atual.canBeReplaced()) {
                return;
            }
            level.setBlock(p, bloco, 2);
        }
    }

    /**
     * Aplaina um disco: limpa acima de {@code y} e preenche abaixo com regolito/rocha,
     * misturando a borda com o terreno em volta.
     */
    public void aplainar(BlockPos centro, int raio, int alturaLimpa) {
        BlockState regolito = ModBlocks.MOON_REGOLITH.get().defaultBlockState();
        BlockState rocha = ModBlocks.MOON_ROCK.get().defaultBlockState();
        BlockState ar = Blocks.AIR.defaultBlockState();
        int y = centro.getY();
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dz = -raio; dz <= raio; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > raio) {
                    continue;
                }
                int x = centro.getX() + dx;
                int z = centro.getZ() + dz;
                // perto da borda, a altura limpa vai diminuindo (rampa suave)
                int limpa = dist > raio - 4 ? Math.max(1, (int) (alturaLimpa * (raio - dist) / 4.0)) : alturaLimpa;
                for (int h = 0; h <= limpa; h++) {
                    set(new BlockPos(x, y + h, z), ar);
                }
                set(new BlockPos(x, y - 1, z), regolito);
                fundacao(new BlockPos(x, y - 2, z), rocha, 40);
            }
        }
    }

    public void pisoCircular(BlockPos centro, double raio, BlockState bloco) {
        int r = (int) Math.ceil(raio);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= raio * raio) {
                    BlockPos p = centro.offset(dx, -1, dz);
                    set(p, bloco);
                    fundacao(p.below(), ModBlocks.MOON_ROCK.get().defaultBlockState(), 40);
                }
            }
        }
    }

    /**
     * Cúpula hemisférica de vidro com nervuras de parede (anel da base, meridianos e anéis).
     * {@code danificada} remove parte do vidro (postos abandonados).
     */
    public void cupula(BlockPos c, int raio, double quebrada, java.util.Random rnd) {
        BlockState ar = Blocks.AIR.defaultBlockState();
        double r = raio;
        for (int dx = -raio - 1; dx <= raio + 1; dx++) {
            for (int dz = -raio - 1; dz <= raio + 1; dz++) {
                for (int h = 0; h <= raio + 1; h++) {
                    double dist = Math.sqrt(dx * dx + dz * dz + h * h);
                    BlockPos p = c.offset(dx, h, dz);
                    if (dist < r - 0.5) {
                        set(p, ar);
                        continue;
                    }
                    if (dist > r + 0.5) {
                        continue;
                    }
                    double ang = Math.toDegrees(Math.atan2(dz, dx));
                    double resto = Math.abs(((ang % 45) + 45) % 45);
                    double plano = Math.sqrt(dx * dx + dz * dz);
                    boolean meridiano = plano < 1.0 || Math.min(resto, 45 - resto) * plano / 57.3 < 0.55;
                    boolean anel = h <= 1 || Math.abs(h - raio * 0.55) < 0.5;
                    boolean nervura = meridiano || anel;
                    if (!nervura && quebrada > 0 && rnd.nextDouble() < quebrada) {
                        set(p, ar);
                    } else {
                        set(p, nervura ? wall() : glass());
                    }
                }
            }
        }
        pisoCircular(c, raio - 0.5, floor());
    }

    /**
     * Túnel cilíndrico horizontal ligando dois pontos ao longo de um eixo.
     * Abre passagem nas cúpulas e não coloca casca dentro delas.
     */
    public void tunel(BlockPos inicio, Direction dir, int comprimento, Predicate<BlockPos> dentroDeCupula) {
        BlockState ar = Blocks.AIR.defaultBlockState();
        Direction lado = dir.getClockWise();
        for (int t = 0; t <= comprimento; t++) {
            BlockPos eixo = inicio.relative(dir, t);
            for (int a = -3; a <= 3; a++) {
                for (int h = -1; h <= 5; h++) {
                    BlockPos p = eixo.relative(lado, a).above(h);
                    double d = Math.sqrt(a * a + (h - 1.5) * (h - 1.5));
                    if (h == -1) {
                        if (Math.abs(a) <= 2) {
                            set(p, floor());
                            fundacao(p.below(), ModBlocks.MOON_ROCK.get().defaultBlockState(), 40);
                        }
                        continue;
                    }
                    if (d < 2.1) {
                        set(p, ar);
                    } else if (d <= 3.1 && !dentroDeCupula.test(p)) {
                        boolean janela = h >= 3 && Math.abs(a) <= 1 && t % 3 != 0;
                        boolean costela = t % 4 == 0;
                        set(p, janela && !costela ? glass() : wall());
                    }
                }
            }
        }
    }

    /** Tanque de oxigênio: dois corpos + válvula. */
    public void tanque(BlockPos base, int corpos) {
        for (int i = 0; i < corpos; i++) {
            set(base.above(i), ModBlocks.OXYGEN_TANK.get().defaultBlockState());
        }
        set(base.above(corpos), ModBlocks.OXYGEN_TANK_CAP.get().defaultBlockState());
    }

    public int alturaChao(int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }
}
