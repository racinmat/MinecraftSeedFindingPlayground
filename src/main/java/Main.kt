import GlobalState.LOGGING
import GlobalState.getCurrentSeed
import GlobalState.nextSeed
import GlobalState.reset
import GlobalState.shutdown
import com.google.common.collect.ImmutableList
import kaptainwutax.biomeutils.Biome
import kaptainwutax.biomeutils.Stats
import kaptainwutax.featureutils.structure.*
import kaptainwutax.seedutils.mc.Dimension
import kaptainwutax.seedutils.mc.MCVersion
import kaptainwutax.seedutils.util.math.DistanceMetric
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.*
import java.util.*
import java.util.logging.LogManager
import java.util.logging.Logger

fun filterBiomes(predicate: (Biome) -> Boolean) = ImmutableList.copyOf(Biome.REGISTRY.values.filter { predicate(it) })

fun Boolean.toInt() = if (this) 1 else 0

object Main {
    @JvmField
//    val VERSION = MCVersion.v1_16_2
    val VERSION = MCVersion.v1_12_2

    //        val NUM_CORES = Runtime.getRuntime().availableProcessors();  // get max. number of cores
//    val NUM_CORES = Runtime.getRuntime().availableProcessors() - 1 // keep single thread free for output etc.
    val NUM_CORES = Runtime.getRuntime().availableProcessors() - 2
//    val NUM_CORES = 1 // for debugging

    const val STRUCTURE_AND_BIOME_SEARCH_RADIUS = 1500

    @JvmField
    val WORLD_TYPE = WorldType.DEFAULT
    const val STRUCTURE_SEED_MAX = 1L shl 48

    //    public static final long STRUCTURE_SEED_MAX = 2;
    var STRUCTURE_SEED_MIN: Long = 0

    // discussion https://discordapp.com/channels/505310901461581824/532998733135085578/749723113033564283 that 16 is too small
    const val BIOME_SEARCH_SPACING = 96 // 6 chunks

    //    public static final double SEED_THR = 1e-2;
    @JvmField
    val DISTANCE = DistanceMetric.CHEBYSHEV

    @JvmField
    var LOGGER: Logger = Logger.getLogger(Main::class.java.name)

    @JvmField
    val STRUCTURES = listOf(
//            StructureInfo(Village(VERSION), Dimension.OVERWORLD, true, 500)//,
            StructureInfo(Village(VERSION), Dimension.OVERWORLD, true, 1_000)//,
//            StructureInfo(SwampHut(VERSION), Dimension.OVERWORLD, false),
//            StructureInfo(Shipwreck(VERSION), Dimension.OVERWORLD, false),
//            StructureInfo(RuinedPortal(VERSION), Dimension.OVERWORLD, false),
//            StructureInfo(RuinedPortal(VERSION), Dimension.NETHER, false),
//            StructureInfo(PillagerOutpost(VERSION), Dimension.OVERWORLD, false),
//            StructureInfo(OceanRuin(VERSION), Dimension.OVERWORLD, false),
//            StructureInfo(NetherFossil(VERSION), Dimension.NETHER, false),
//            StructureInfo(Monument(VERSION), Dimension.OVERWORLD, false),
//            StructureInfo(Mansion(VERSION), Dimension.OVERWORLD, true, 2_000),
//            StructureInfo(JunglePyramid(VERSION), Dimension.OVERWORLD, true),
//            StructureInfo(Igloo(VERSION), Dimension.OVERWORLD, false),
//            StructureInfo(Fortress(VERSION), Dimension.NETHER, true),
//            StructureInfo(EndCity(VERSION), Dimension.END, false),
//            StructureInfo(DesertPyramid(VERSION), Dimension.OVERWORLD, true, 1_000),
//            StructureInfo(BuriedTreasure(VERSION), Dimension.OVERWORLD, false)
    ).sortedBy { - (it.isRequired.toInt() * 100 + it.structure.spacing) } // so I have at first required structures which have highest spacing -> there is least of them on given area

    // will search all of (any of biomes), so will search if any biome from each category will be found
    //only overworld biomes can be here because of hardcoded things
    @JvmField
    val ALL_OF_ANY_OF_BIOMES = listOf(
//            BiomeInfo(filterBiomes { it.category == Biome.Category.JUNGLE }, "jungles", true),
//            BiomeInfo(filterBiomes { it.category == Biome.Category.MUSHROOM }, "mushrooms", false),
//            BiomeInfo(filterBiomes { it.category == Biome.Category.MESA }, "mesas", true),
//            BiomeInfo(filterBiomes { it.category == Biome.Category.OCEAN }, "oceans", true),
//            BiomeInfo(filterBiomes { it.category == Biome.Category.ICY }, "icy", true),

            BiomeInfo(filterBiomes { it.category == Biome.Category.JUNGLE }, "jungles", true, 2_000),
            BiomeInfo(filterBiomes { it.category == Biome.Category.MUSHROOM }, "mushrooms", true, 2_000),
            BiomeInfo(filterBiomes { it.category == Biome.Category.MESA }, "mesas", true, 2_000),
            BiomeInfo(filterBiomes { it.category == Biome.Category.EXTREME_HILLS }, "extreme_hills", true, 2_000),
            BiomeInfo(filterBiomes { it == Biome.SHATTERED_SAVANNA || it == Biome.SHATTERED_SAVANNA_PLATEAU }, "shattered_savannah", true, 2_000),
            BiomeInfo(filterBiomes { it.category == Biome.Category.ICY }, "icy", true, 3_000)
    ).sortedBy { - (it.isRequired.toInt()) }.let { ImmutableList.copyOf(it) }
//    val ALL_OF_ANY_OF_BIOMES = emptyMap<String, ImmutableList<Biome>>()

    var STRUCT_NAMES = STRUCTURES.map { it.structName }
    var BIOME_NAMES = ALL_OF_ANY_OF_BIOMES.map { it.name }.toTypedArray()
    var HEADERS = (listOf("seed") + STRUCT_NAMES + BIOME_NAMES).toTypedArray()

    //things to used for shortcutting, inferred from requirements
    val MANSION_REQUIRED = STRUCTURES.any { it.structName == "mansion" && it.isRequired }
    val JUNGLE_REQUIRED = ALL_OF_ANY_OF_BIOMES.any { it.biomesList.equals(filterBiomes { it.category == Biome.Category.JUNGLE }) && it.isRequired }


    @Throws(FileNotFoundException::class)
    fun readFileSeed(filename: String): Long {
        val file = File(filename)
        val sc = Scanner(file)
        return if (sc.hasNextLine()) sc.nextLine().toLong() else -1
    }

    @Throws(IOException::class)
    fun writeFileSeed(filename: String, seed: Long) {
        val w = PrintWriter(FileWriter(filename))
        w.printf("%d\n", seed)
        w.close()
    }

    @Throws(IOException::class)
    fun searchSeeds() {
        STRUCTURE_SEED_MIN = readFileSeed("last_seed.txt") + 1
        reset()
        var structureSeed: Long = 0
        while (STRUCTURE_SEED_MAX > getCurrentSeed()) {
            // I want to do search in a range of seeds so I can iteratively scan different ranges
            if (structureSeed >= STRUCTURE_SEED_MAX) {
                break
            }
            structureSeed = nextSeed
            Searcher.searchStructureSeed(structureSeed, ImmutableList.copyOf(STRUCTURES), ALL_OF_ANY_OF_BIOMES, BIOME_SEARCH_SPACING)
        }
    }

    @Throws(IOException::class)
    fun searchSeedsParallel() {
        STRUCTURE_SEED_MIN = readFileSeed("last_seed.txt") + 1
        println("Loaded ${STRUCTURE_SEED_MIN - 1} from file, going to perform seed search starting with structure seed $STRUCTURE_SEED_MIN.")
        reset()
        val currentThreads = ArrayList<Thread>()
        for (i in 0 until NUM_CORES) {
            val t: Thread = SearchingThread(ImmutableList.copyOf(STRUCTURES), ALL_OF_ANY_OF_BIOMES)
            t.start()
            currentThreads.add(t)
        }
        LOGGER.info("num threads: " + currentThreads.size)
        for (currentThread in currentThreads) {
            try {
                currentThread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    fun toCsv(seeds: Map<Long, DoubleArray>, name: String) {
        val out = FileWriter(name)
        CSVPrinter(out, CSVFormat.DEFAULT.withHeader(*HEADERS)).use { printer ->
            for ((seed, distances) in seeds) {
                printer.printRecord(seed, distances[0], distances[1], distances[2], distances[3], distances[4])
            }
        }
    }

    @Throws(IOException::class)
    fun toCsv(seeds: List<SeedResult>, name: String) {
        val out = FileWriter(name)
        CSVPrinter(out, CSVFormat.DEFAULT.withHeader(*HEADERS)).use { printer ->
            seeds.forEach { s ->
                printer.printRecord(listOf(s.seed) +
                        STRUCT_NAMES.map { s.structureDistances[it] } +
                        BIOME_NAMES.map { s.biomeDistances[it] })
            }
        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // due to lack of reasonable constructors, I'm creating it here
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        stopwatch.stop(); // optional
//        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//        System.out.println("time: " + stopwatch);
//        searchSeeds();
        searchSeedsParallel()
        shutdown()
    }

    enum class WorldType {
        DEFAULT, LARGE_BIOMES
    }

    init {
        //todo: figure out how to do flushing of logger so it corresponds to prints
        LogManager.getLogManager().readConfiguration(Main.javaClass.classLoader.getResourceAsStream("logging.properties"))
        LOGGER = Logger.getLogger(Main::class.java.name)
        LOGGING = false
        GlobalState.RESULT_DIR = "kouzelnici_good_seeds"
        Stats.LOGGING = LOGGING
    }
//todo: try dry run without outputting things, benchmark how many seeds per second
// check statistics from Neils fork
// slowly start adding shortcuts and benchmark it
}