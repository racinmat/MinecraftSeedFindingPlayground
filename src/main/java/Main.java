import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.seedutils.mc.*;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    public static final MCVersion VERSION = MCVersion.v1_16_1;

    public static final Village VILLAGE = new Village(VERSION);
    public static final Mansion MANSION = new Mansion(VERSION);
    public static final SwampHut SWAMP_HUT = new SwampHut(VERSION);
    public static final Monument MONUMENT = new Monument(VERSION);
    public static final DesertPyramid DESERT_PYRAMID = new DesertPyramid(VERSION);
    public static final JunglePyramid JUNGLE_PYRAMID = new JunglePyramid(VERSION);

    public static final Set<Biome> JUNGLES = Biome.REGISTRY.values().stream()
            .filter(b -> b.getCategory() == Biome.Category.JUNGLE).collect(Collectors.toSet());
    public static final double BIG_M = 1e6;
    //    public static final double SEED_THR = 1e-2;
    public static final double SEED_THR = -80000;

    public static double getJungleDistance(long seed, ChunkRand rand) {
        return getJungleDistance(seed, rand, 1_000);
    }

    public static double getJungleDistance(long seed, ChunkRand rand, int radius) {
        OverworldBiomeSource layers = new OverworldBiomeSource(MCVersion.v1_15, seed);
        var nearestBiome = layers.locateNearestBiome(0, 0, 0, radius, JUNGLES, rand);
        if (nearestBiome == null) {
            return BIG_M;
        }
        return nearestBiome.distanceTo(BPos.ZERO, DistanceMetric.EUCLIDEAN);
    }

    public static <C extends RegionStructure.Config, D extends RegionStructure.Data<?>> double getStructureDistance(long seed, ChunkRand rand, RegionStructure<C, D> struct) {
        var source = new OverworldBiomeSource(MCVersion.v1_16_2, seed);
        //Structures are placed per regions. Village regions are 32x32 chunks for example.

        var a_range = ContiguousSet.create(Range.closed(-5, 5), DiscreteDomain.integers());
        var structDistance = 1e6;
        for(var coords: Sets.cartesianProduct(a_range, a_range).stream().sorted((x1, x2) -> Math.abs(x1.get(0)) + Math.abs(x1.get(1)) - Math.abs(x2.get(0)) - Math.abs(x2.get(1))).collect(Collectors.toList())) {
            var structInst = struct.getInRegion(seed, coords.get(0), coords.get(1), rand);
            //Checks for village less than 50 blocks from (0, 0).
            structDistance = structInst.toBlockPos().distanceTo(BPos.ZERO, DistanceMetric.EUCLIDEAN);
            // big number for non-spawnable villages
            structDistance = !struct.canSpawn(structInst.getX(), structInst.getZ(), source) ? BIG_M : structDistance;
            if (structDistance < BIG_M) {
                break;
            }
        }
        return structDistance;
    }

    public static double scoreSeed(long seed, ChunkRand rand) {
        var villageDistance = getStructureDistance(seed, rand, VILLAGE);
        var mansionDistance = villageDistance >= BIG_M ? BIG_M : getStructureDistance(seed, rand, MANSION);
        var swampHutDistance = mansionDistance >= BIG_M ? BIG_M : getStructureDistance(seed, rand, SWAMP_HUT);
        var monumentDistance = swampHutDistance >= BIG_M ? BIG_M : getStructureDistance(seed, rand, MONUMENT);
        var desertPyramidDistance = monumentDistance >= BIG_M ? BIG_M : getStructureDistance(seed, rand, DESERT_PYRAMID);
        var junglePyramidDistance = desertPyramidDistance >= BIG_M ? BIG_M : getStructureDistance(seed, rand, JUNGLE_PYRAMID);

        var jungleDistance = junglePyramidDistance >= BIG_M ? BIG_M : getJungleDistance(seed, rand);
//        var score = 10 * 100 / (villageDistance+1) + 100 / (jungleDistance+1);
        var score = 1 / 2. * (200. - villageDistance)
                + 1 / 7. * (700. - mansionDistance)
                + 1 / 5. * (500. - swampHutDistance)
                + 1 / 5. * (500. - monumentDistance)
                + 1 / 5. * (500. - jungleDistance);
        if (score >= SEED_THR) {
            System.out.printf("seed: %d, \t"
                    + "village distance: %.4f, \t"
                    + "mansion distance: %.4f, \t"
                    + "swamp hut distance: %.4f, \t"
                    + "jungle distance: %.4f, \t"
                    + "score: %.4f\n", seed, villageDistance, mansionDistance, swampHutDistance, jungleDistance, score
            );
        }
        return score;
    }

    public static List<Long> findSeeds() {
        var rand = new ChunkRand();
        var seeds = new HashMap<Long, Double>();

        for (long structureSeed = 0; structureSeed < 1L << 48; structureSeed++) {
            //Now that we have a good village starting position, let's do the biome checks.
            //We have 2^16 attempts to get the biomes right.
            Stopwatch stopwatch = Stopwatch.createStarted();
            var score = scoreSeed(structureSeed, rand);
            seeds.put(structureSeed, score);
            stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
            if (score >= SEED_THR) System.out.println("time: " + stopwatch);
        }

        return new ArrayList<>(seeds.keySet());
    }

    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        var seeds = findSeeds();
        stopwatch.stop(); // optional
        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("time: " + stopwatch);
    }
}
