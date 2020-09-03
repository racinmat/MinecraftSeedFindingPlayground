import com.google.common.collect.ImmutableList
import kaptainwutax.seedutils.mc.ChunkRand
import kaptainwutax.seedutils.mc.seed.WorldSeed
import kaptainwutax.seedutils.util.math.Vec3i
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.util.concurrent.ConcurrentMap
import java.util.stream.Collectors

object VerifyResults {

    fun fromCsv(seeds: Map<Long, DoubleArray>, name: String) {
        val out = FileWriter(name)
        CSVPrinter(out, CSVFormat.DEFAULT.withHeader(*Main.HEADERS)).use { printer ->
            for ((seed, distances) in seeds) {
                printer.printRecord(seed, distances[0], distances[1], distances[2], distances[3], distances[4])
            }
        }
    }

    fun fromCsv(name: String): List<SeedResult> {
        val reader: Reader = FileReader(name)
        val records: Iterable<CSVRecord> = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)
        return records.map {
            SeedResult(it["seed"].toLong(),
                    Main.STRUCT_NAMES.map { s -> s!! to it[s].toDouble() }.toMap() as ConcurrentMap<String, Double>,
                    Main.BIOME_NAMES.map { s -> s!! to it[s].toDouble() }.toMap() as ConcurrentMap<String, Double>
            )
        }
    }

    fun evalSeed(worldSeed: Long): SeedResult {
        val structureSeed = WorldSeed.toStructureSeed(worldSeed)
        val origin = Vec3i(0, 0, 0)
        val rand = ChunkRand()
        val structures = Searcher.getStructuresPosList(structureSeed, ImmutableList.copyOf(Main.STRUCTURES), origin, rand)!!
        return Searcher.searchWorldSeed(Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS, worldSeed, structures, Main.ALL_OF_ANY_OF_BIOMES, Main.BIOME_SEARCH_SPACING, origin, rand)
    }

    fun checkSeedResult(seedResult: SeedResult): Boolean {
        val worldSeed = seedResult.seed
        val structureSeed = WorldSeed.toStructureSeed(worldSeed)
        val origin = Vec3i(0, 0, 0)
        val rand = ChunkRand()
        val structures = Searcher.getStructuresPosList(structureSeed, ImmutableList.copyOf(Main.STRUCTURES), origin, rand)!!
        val result = Searcher.searchWorldSeed(Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS, worldSeed, structures, Main.ALL_OF_ANY_OF_BIOMES, Main.BIOME_SEARCH_SPACING, origin, rand)
        println("checking seed: $worldSeed")
        println("expected: $seedResult")
        println("expected: $result")
        return seedResult.equals(result)
    }

    @Throws(IOException::class)
    fun fixResults() {
        File("good_seeds").listFiles { f, name ->
            name.matches(Regex("distances_\\d+_\\d+.csv"))
        }!!.forEach {
            Main.toCsv(fromCsv(
                    "good_seeds/${it.name}").map { evalSeed(it.seed) },
                    "good_seeds/${it.nameWithoutExtension}_fixed.csv")
        }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
//        for (var seed : new long[]{2590977160621592647L,
//                8494351847174180868L,
//                -7931401893752860728L}) {
//            System.out.println(WorldSeed.toStructureSeed(seed));
//        }

//        fixResults();
//        var results = fromCsv("old_multithread_seeds_2/distances_0_70.csv");
////        var results = fromCsv("broken_small/distances_0_10.csv");
//        var results = fromCsv("good_seeds/distances_0_10.csv");
        val results = fromCsv("distances_0_70.csv")
        //        var results = fromCsv("distances_4168_50.csv");
//        var results = fromCsv("distances_8449_50.csv");
//        var results = fromCsv("good_seeds/distances_4168_50_fixed.csv");
        for (result in results) {
            val areSame = checkSeedResult(result)
            println("are same: $areSame")
        }
        //
//        GlobalState.shutdown();
    }
}