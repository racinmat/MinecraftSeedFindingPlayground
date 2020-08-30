import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import com.google.common.primitives.Doubles;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.seedutils.mc.*;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Main {
    public enum WorldType {DEFAULT, LARGE_BIOMES};
    public static final MCVersion VERSION = MCVersion.v1_16_1;
    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();  // get max. number of cores
    public static final int STRUCTURE_SEARCH_RADIUS = 2_000;
    public static final WorldType WORLD_TYPE = WorldType.DEFAULT;
    public static final long STRUCTURE_SEED_MAX = 1L << 48;
    public static final boolean SEARCH_SHADOW = true;
    public static long STRUCTURE_SEED_MIN = 0;
    public static int BIOME_SEARCH_SPACING = 16;

    public static final StructureInfo<?, ?>[] STRUCTURES = new StructureInfo<?, ?>[]{
            new StructureInfo<>(new Village(VERSION), Dimension.OVERWORLD, 1, 1_000),
            new StructureInfo<>(new SwampHut(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new Shipwreck(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new RuinedPortal(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new RuinedPortal(VERSION), Dimension.NETHER, 0),
            new StructureInfo<>(new PillagerOutpost(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new OceanRuin(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new NetherFossil(VERSION), Dimension.NETHER, 0),
            new StructureInfo<>(new Monument(VERSION), Dimension.OVERWORLD, 1),
            new StructureInfo<>(new Mansion(VERSION), Dimension.OVERWORLD, 1),
            new StructureInfo<>(new JunglePyramid(VERSION), Dimension.OVERWORLD, 1),
            new StructureInfo<>(new Igloo(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new Fortress(VERSION), Dimension.NETHER, 1),
            new StructureInfo<>(new EndCity(VERSION), Dimension.END, 0),
            new StructureInfo<>(new DesertPyramid(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new BuriedTreasure(VERSION), Dimension.OVERWORLD, 0),
            new StructureInfo<>(new BastionRemnant(VERSION), Dimension.NETHER, 1),
    };
    public static final List<Biome> BIOMES = Biome.REGISTRY.values().stream()
            .filter(b -> b.getCategory() == Biome.Category.JUNGLE).collect(Collectors.toList());

    public static final double BIG_M = 1e6;
    //    public static final double SEED_THR = 1e-2;
    public static final double SEED_THR = -80000;

    public static long readFileSeed(String filename) throws FileNotFoundException {
        var file = new File(filename);
        var sc = new Scanner(file);
        if (sc.hasNextLine()) return Long.parseLong(sc.nextLine());
        return -1;
    }

    public static void writeFileSeed(String filename, int seed) throws IOException {
        var w = new PrintWriter(new FileWriter(filename));
        w.printf("%d\n", seed);
        w.close();
    }

    public static double getJungleDistance(long seed, ChunkRand rand) {
        return getJungleDistance(seed, rand, 1_000);
    }

    public static double getJungleDistance(long seed, ChunkRand rand, int radius) {
        Map<String, StructureInfo<?, ?>> a_map = Arrays.stream(STRUCTURES).collect(Collectors.toMap(s->s.getStructure().getClass().getName(), s->s, (prev, next) -> next, HashMap::new));
        OverworldBiomeSource layers = new OverworldBiomeSource(VERSION, seed);
        //  checkByLayer = true means finding nearest biome
        var nearestBiome = layers.locateBiome(0, 0, 0, radius, BIOME_SEARCH_SPACING, BIOMES, rand, true);
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
        for (var coords : Sets.cartesianProduct(a_range, a_range).stream().sorted((x1, x2) -> Math.abs(x1.get(0)) + Math.abs(x1.get(1)) - Math.abs(x2.get(0)) - Math.abs(x2.get(1))).collect(Collectors.toList())) {
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
        Map<String, Double> distances = Arrays.stream(STRUCTURES).collect(Collectors.toMap(
                StructureInfo::getStructName, s->BIG_M, (prev, next) -> next, HashMap::new));
        var endedOk = true;
        for (var structure : STRUCTURES) {
            var curDistance = getStructureDistance(seed, rand, structure.structure);
            distances.put(structure.structName, curDistance);
            if(curDistance >= BIG_M) {
                endedOk = false;
                break;
            }
        }

        var jungleDistance = !endedOk ? BIG_M : getJungleDistance(seed, rand);
//        var score = 10 * 100 / (villageDistance+1) + 100 / (jungleDistance+1);
        var score = 1 / 2. * (200. - distances.get(Structure.getName(Village.class)))
                + 1 / 15. * (1_500. - distances.get(Structure.getName(Mansion.class)))
                + 1 / 6. * (600. - distances.get(Structure.getName(SwampHut.class)))
                + 1 / 5. * (500. - distances.get(Structure.getName(Monument.class)))
                + 1 / 5. * (500. - jungleDistance);
        if (score >= SEED_THR) {
            System.out.printf("seed: %d, \t"
                    + "village distance: %.4f, \t"
                    + "mansion distance: %.4f, \t"
                    + "swamp hut distance: %.4f, \t"
                    + "monument distance: %.4f, \t"
                    + "jungle distance: %.4f, \t"
                    + "score: %.4f\n", seed, distances.get(Structure.getName(Village.class)),
                    distances.get(Structure.getName(Mansion.class)),
                    distances.get(Structure.getName(SwampHut.class)),
                    distances.get(Structure.getName(Monument.class)), jungleDistance, score
            );
        }
        return score;
    }

    public static double[] seedInfo(long seed, ChunkRand rand) {
        Map<String, Double> distances = Arrays.stream(STRUCTURES).collect(Collectors.toMap(
                StructureInfo::getStructName, s->BIG_M, (prev, next) -> next, HashMap::new));
        var endedOk = true;
        for (var structure : STRUCTURES) {
            var curDistance = getStructureDistance(seed, rand, structure.structure);
            distances.put(structure.structName, curDistance);
            if(curDistance >= BIG_M) {
                endedOk = false;
                break;
            }
        }

        var jungleDistance = !endedOk ? BIG_M : getJungleDistance(seed, rand);
//        var score = 10 * 100 / (villageDistance+1) + 100 / (jungleDistance+1);
        var score = 1 / 2. * (200. - distances.get(Structure.getName(Village.class)))
                + 1 / 15. * (1_500. - distances.get(Structure.getName(Mansion.class)))
                + 1 / 6. * (600. - distances.get(Structure.getName(SwampHut.class)))
                + 1 / 5. * (500. - distances.get(Structure.getName(Monument.class)))
                + 1 / 5. * (500. - jungleDistance);
        return Doubles.concat(Doubles.toArray(distances.values()), new double[]{jungleDistance, score});
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

    public static List<Long> searchSeeds() throws IOException {
        var rand = new ChunkRand();
        var seeds = new HashMap<Long, double[]>();

        STRUCTURE_SEED_MIN = readFileSeed("last_seed.txt") + 1;
        GlobalState.reset();
        for (long structureSeed = STRUCTURE_SEED_MIN; structureSeed < STRUCTURE_SEED_MAX; structureSeed++) {
            var info = seedInfo(structureSeed, rand);
            seeds.put(structureSeed, info);
            if (seeds.size() % 10_000 == 0) {
                System.out.println("Found " + seeds.size() + " seeds so far.");
                toCsv(seeds, "distances_" + STRUCTURE_SEED_MIN + "_" + structureSeed + ".csv");
            }
        }

        return new ArrayList<>(seeds.keySet());
    }

    public static List<Long> searchSeedsParallel() throws IOException {
        var rand = new ChunkRand();
        var seeds = new HashMap<Long, double[]>();

        STRUCTURE_SEED_MIN = readFileSeed("last_seed.txt") + 1;
        GlobalState.reset();
//        List<Type> input = Arrays.asList(...);
        var chunkSize = 10_000;

        for (long chunk = STRUCTURE_SEED_MIN / chunkSize; chunk < STRUCTURE_SEED_MAX / chunkSize; chunk++) {
            seeds.putAll(LongStream.rangeClosed(chunk * chunkSize + 1, (chunk + 1) * chunkSize)
                    .parallel()
                    .mapToObj(s->Map.entry(s, seedInfo(s, rand)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        var currentThreads = new ArrayList<Thread>();

        for (int i = 0; i < NUM_CORES; i++) {
            long startingStructureSeed = (long) Math.floor(Math.pow(2, 48) / NUM_CORES * i);
            Thread t = new SearchingThread(startingStructureSeed, STRUCTURE_SEARCH_RADIUS, STRUCTURES, BIOMES);
            t.start();
            currentThreads.add(t);
        }
        System.out.println(currentThreads.size());

        //todo: nějak to dodělat
        return new ArrayList<>(seeds.keySet());
    }


    public static String[] HEADERS = {"seed", "villageDistance", "mansionDistance", "swampHutDistance", "monumentDistance", "jungleDistance"};

    public static void toCsv(Map<Long, double[]> seeds, String name) throws IOException {
        var out = new FileWriter(name);
        try (var printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS))) {
            for (var entry : seeds.entrySet()) {
                var author = entry.getKey();
                var distances = entry.getValue();
                printer.printRecord(author, distances[0], distances[1], distances[2], distances[3], distances[4]);
            }
        }
    }

    public static void main(String[] args) throws IOException {
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        var seeds = findSeeds();
//        stopwatch.stop(); // optional
//        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//        System.out.println("time: " + stopwatch);
        searchSeeds();
    }
}
