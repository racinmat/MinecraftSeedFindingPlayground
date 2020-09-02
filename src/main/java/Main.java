import com.google.common.collect.*;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
    public enum WorldType {DEFAULT, LARGE_BIOMES}
    public static final MCVersion VERSION = MCVersion.v1_16_1;
//    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();  // get max. number of cores
    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors() - 1;  // keep single thread free for output etc.
//    public static final int NUM_CORES = 1;      // for debugging
    public static final int STRUCTURE_AND_BIOME_SEARCH_RADIUS = 1_500;
    public static final WorldType WORLD_TYPE = WorldType.DEFAULT;
    public static final long STRUCTURE_SEED_MAX = 1L << 48;
//    public static final long STRUCTURE_SEED_MAX = 2;
    public static long STRUCTURE_SEED_MIN = 0;
    // discussion https://discordapp.com/channels/505310901461581824/532998733135085578/749723113033564283 that 16 is too small
    public static final int BIOME_SEARCH_SPACING = 96; // 6 chunks
    public static final double BIG_M = 1e6;
    //    public static final double SEED_THR = 1e-2;
    public static final DistanceMetric DISTANCE = DistanceMetric.CHEBYSHEV;
    public static final double SEED_THR = -80000;
    public static Logger LOGGER = null;

    public static final StructureInfo<?, ?>[] STRUCTURES = new StructureInfo<?, ?>[]{
            new StructureInfo<>(new Village(VERSION), Dimension.OVERWORLD, true, 1_000),
            new StructureInfo<>(new SwampHut(VERSION), Dimension.OVERWORLD, false),
            new StructureInfo<>(new Shipwreck(VERSION), Dimension.OVERWORLD, false),
//            new StructureInfo<>(new RuinedPortal(VERSION), Dimension.OVERWORLD, false),
//            new StructureInfo<>(new RuinedPortal(VERSION), Dimension.NETHER, false),
            new StructureInfo<>(new PillagerOutpost(VERSION), Dimension.OVERWORLD, false),
            new StructureInfo<>(new OceanRuin(VERSION), Dimension.OVERWORLD, false),
//            new StructureInfo<>(new NetherFossil(VERSION), Dimension.NETHER, false),
            new StructureInfo<>(new Monument(VERSION), Dimension.OVERWORLD, true),
            new StructureInfo<>(new Mansion(VERSION), Dimension.OVERWORLD, true, 2_000),
            new StructureInfo<>(new JunglePyramid(VERSION), Dimension.OVERWORLD, true),
            new StructureInfo<>(new Igloo(VERSION), Dimension.OVERWORLD, false),
            new StructureInfo<>(new Fortress(VERSION), Dimension.NETHER, true),
//            new StructureInfo<>(new EndCity(VERSION), Dimension.END, false),
            new StructureInfo<>(new DesertPyramid(VERSION), Dimension.OVERWORLD, false),
            new StructureInfo<>(new BuriedTreasure(VERSION), Dimension.OVERWORLD, false),
//            new StructureInfo<>(new BastionRemnant(VERSION), Dimension.NETHER, true),
    };

    // will search all of (any of biomes), so will search if any biome from each category will be found
    public static final List<Biome> jungles = Biome.REGISTRY.values().stream()
            .filter(b -> b.getCategory() == Biome.Category.JUNGLE).collect(Collectors.toList());
    public static final List<Biome> mushrooms = Biome.REGISTRY.values().stream()
            .filter(b -> b.getCategory() == Biome.Category.MUSHROOM).collect(Collectors.toList());
    public static final List<Biome> mesa = Biome.REGISTRY.values().stream()
            .filter(b -> b.getCategory() == Biome.Category.MESA).collect(Collectors.toList());
    public static final List<Biome> ocean = Biome.REGISTRY.values().stream()
            .filter(b -> b.getCategory() == Biome.Category.OCEAN).collect(Collectors.toList());
    public static final List<Biome> icy = Biome.REGISTRY.values().stream()
            .filter(b -> b.getCategory() == Biome.Category.ICY).collect(Collectors.toList());

    public static final ImmutableMap<String, ImmutableList<Biome>> ALL_OF_ANY_OF_BIOMES = ImmutableMap.of(
            "jungles", ImmutableList.copyOf(jungles),
            "mushrooms", ImmutableList.copyOf(mushrooms),
            "mesas", ImmutableList.copyOf(mesa),
            "oceans", ImmutableList.copyOf(ocean),
            "icy", ImmutableList.copyOf(icy)
    );

    public static final String[] STRUCT_NAMES = Arrays.stream(STRUCTURES).map(StructureInfo::getStructName).toArray(String[]::new);
    public static final String[] BIOME_NAMES = ALL_OF_ANY_OF_BIOMES.keySet().toArray(String[]::new);
    public static String[] HEADERS = ObjectArrays.concat(ObjectArrays.concat("seed", STRUCT_NAMES), BIOME_NAMES, String.class);

    public static void initBiomeGroups() {

    }

    public static long readFileSeed(String filename) throws FileNotFoundException {
        var file = new File(filename);
        var sc = new Scanner(file);
        if (sc.hasNextLine()) return Long.parseLong(sc.nextLine());
        return -1;
    }

    public static void writeFileSeed(String filename, long seed) throws IOException {
        var w = new PrintWriter(new FileWriter(filename));
        w.printf("%d\n", seed);
        w.close();
    }

    public static void searchSeeds() throws IOException {
        STRUCTURE_SEED_MIN = readFileSeed("last_seed.txt") + 1;
        GlobalState.reset();

        long structureSeed = 0;
        while (Main.STRUCTURE_SEED_MAX > GlobalState.getCurrentSeed()) {

            // I want to do search in a range of seeds so I can iteratively scan different ranges
            if (structureSeed >= Main.STRUCTURE_SEED_MAX) {
                break;
            }

            structureSeed = GlobalState.getNextSeed();
            Searcher.searchStructureSeed(Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS, structureSeed, ImmutableList.copyOf(STRUCTURES), ALL_OF_ANY_OF_BIOMES, Main.BIOME_SEARCH_SPACING);
        }
    }

    public static void searchSeedsParallel() throws IOException {
        STRUCTURE_SEED_MIN = readFileSeed("last_seed.txt") + 1;
        GlobalState.reset();
        var currentThreads = new ArrayList<Thread>();

        for (int i = 0; i < NUM_CORES; i++) {
            Thread t = new SearchingThread(ImmutableList.copyOf(STRUCTURES), ALL_OF_ANY_OF_BIOMES);
            t.start();
            currentThreads.add(t);
        }
        LOGGER.info("num threads: " + currentThreads.size());
        for (Thread currentThread : currentThreads) {
            try {
                currentThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void toCsv(Map<Long, double[]> seeds, String name) throws IOException {
        var out = new FileWriter(name);
        try (var printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS))) {
            for (var entry : seeds.entrySet()) {
                var seed = entry.getKey();
                var distances = entry.getValue();
                printer.printRecord(seed, distances[0], distances[1], distances[2], distances[3], distances[4]);
            }
        }
    }
//    seed,                 village, swamp_hut, shipwreck, pillager_outpost, ocean_ruin, monument, mansion, jungle_pyramid, igloo, fortress_nether, desert_pyramid, buried_treasure, jungles, mushrooms, mesas, oceans, icy
//    5920544660131938314,   1088.0,     928.0,    1344.0,            256.0,      240.0,    480.0,   688.0, 864.0, 944.0, 208.0, 672.0, 2000.0, 1152.0, 768.0, 384.0, 768.0, 768.0
    public static void toCsv(List<SeedResult> seeds, String name) throws IOException {
        var out = new FileWriter(name);
        //dopíci, špatně se řadí ty věci kurva, opravit toto!!!
        try (var printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS))) {
            for (var entry : seeds) {
                var row = new ArrayList<>();
                row.add(entry.seed);
                row.addAll(Arrays.stream(STRUCT_NAMES).map(s->entry.structureDistances.get(s)).collect(Collectors.toList()));
                row.addAll(Arrays.stream(BIOME_NAMES).map(s->entry.biomeDistances.get(s)).collect(Collectors.toList()));
                printer.printRecord(row);
            }
        }
    }

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        LOGGER = Logger.getLogger(Main.class.getName());
        initBiomeGroups();
    }

    public static void main(String[] args) throws IOException {
        // due to lack of reasonable constructors, I'm creating it here
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        stopwatch.stop(); // optional
//        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//        System.out.println("time: " + stopwatch);
//        searchSeeds();
        searchSeedsParallel();
        GlobalState.shutdown();
    }
}
