import Main.BIOME_NAMES
import Main.STRUCT_NAMES
import Main.VERSION
import com.google.common.collect.ImmutableList
import kaptainwutax.biomeutils.Biome
import kaptainwutax.featureutils.structure.Village
import kaptainwutax.seedutils.mc.ChunkRand
import kaptainwutax.seedutils.mc.Dimension
import kaptainwutax.seedutils.mc.pos.BPos
import kaptainwutax.seedutils.mc.seed.WorldSeed
import kaptainwutax.seedutils.util.math.Vec3i
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.*

object FurtherFilterResults {

    fun fromCsv(name: String): List<SeedResult> {
        val reader: Reader = FileReader(name)
        val records: Iterable<CSVRecord> = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)
        return records.map {
            SeedResult(it["seed"].toLong(),
                    Main.STRUCT_NAMES.map { s -> s to it[s].toDouble() }.toMap(),
                    Main.BIOME_NAMES.map { s -> s to it[s].toDouble() }.toMap()
            )
        }
    }

    fun evalSeed(worldSeed: Long): SeedResult? {
        val structureSeed = WorldSeed.toStructureSeed(worldSeed)
        val origin = Vec3i(0, 0, 0)
        val rand = ChunkRand()
        val structures = Searcher.getStructuresPosList(structureSeed, ImmutableList.copyOf(Main.STRUCTURES), origin, rand) ?: return null
        return Searcher.searchWorldSeed(worldSeed, structures, Main.ALL_OF_ANY_OF_BIOMES, Main.BIOME_SEARCH_SPACING, origin, rand)
    }

    fun enrichSeed(result: SeedResult): SeedResult {
        val source = Searcher.getBiomeSource(Dimension.OVERWORLD, result.seed)
        val origin = Vec3i(0, 0, 0)
        val rand = ChunkRand()
        val savannasLayer = source.getLayer(26)
        val savannas = Main.ALL_OF_ANY_OF_BIOMES.first { it.name == "shattered_savannah" }
        val numSavannas = Searcher.cartesianProduct(
                -savannas.maxDistance until savannas.maxDistance+savannasLayer.scale*2 step savannasLayer.scale,
                -savannas.maxDistance until savannas.maxDistance+savannasLayer.scale*2 step savannasLayer.scale).filter { (bposX, bposZ) ->
            val bpos = BPos(bposX, 0, bposZ)
            val rpos26 = bpos.toRegionPos(savannasLayer.scale)

            // special gives us special, so it results in jungle in base biome layer
            val biomeId = savannasLayer.get(rpos26.x, 0, rpos26.z)
            biomeId == Biome.SHATTERED_SAVANNA.id || biomeId == Biome.SHATTERED_SAVANNA_PLATEAU.id
        }.size

        val structure = Village(VERSION)
        val structSearchRange = 2048
        val lowerBound = structure.at(-structSearchRange shr 4, -structSearchRange shr 4)
        val upperBound = structure.at(structSearchRange shr 4, structSearchRange shr 4)
        val numVillages = Searcher.cartesianProduct(
                lowerBound.regionX..upperBound.regionX,
                lowerBound.regionZ..upperBound.regionZ).map f@{ (regionX, regionZ) ->
            val structPos = structure.getInRegion(result.seed, regionX, regionZ, rand) ?: return@f null
            if (structPos.distanceTo(origin, Main.DISTANCE) > (structSearchRange shr 4)) return@f null
            if (!structure.canSpawn(structPos.x, structPos.z, source)) return@f null
            structPos
        }.filterNotNull().size
        val newMap = result.biomeDistances.toMutableMap()
        val newMap2 = result.structureDistances.toMutableMap()
        newMap.put("numPointsSavannas", numSavannas.toDouble())
        newMap2.put("numVillages", numVillages.toDouble())
        result.biomeDistances = newMap
        result.structureDistances = newMap2
        return result
    }

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
//        val results = fromCsv("distances_0_70.csv")
//        val results = fromCsv("distances_0_80000.csv")
//        val results = fromCsv("nurgle_good_seeds/distances_0_9600.csv")
        val results = fromCsv("kouzelnici_good_seeds/distances_0_9600.csv")
        //        var results = fromCsv("distances_4168_50.csv");
//        var results = fromCsv("distances_8449_50.csv");
//        var results = fromCsv("good_seeds/distances_4168_50_fixed.csv");
//        results.forEach {
//            println("are same: ${checkSeedResult(it)}")
//        }
        //
        BIOME_NAMES += listOf("numPointsSavannas")
        STRUCT_NAMES += listOf("numVillages")
        Main.HEADERS = (listOf("seed") + STRUCT_NAMES + BIOME_NAMES).toTypedArray()
//        Main.toCsv(results.map { enrichSeed(it) }, "nurgle_distances_good.csv")
        Main.toCsv(results.map { enrichSeed(it) }, "kouzelnici_distances_good.csv")
//        GlobalState.shutdown();
    }
}
