import Main.VERSION
import Searcher.cartesianProduct
import VerifyResults.fromCsv
import kaptainwutax.biomeutils.Biome
import kaptainwutax.biomeutils.layer.BiomeLayer
import kaptainwutax.biomeutils.layer.land.BaseBiomesLayer
import kaptainwutax.biomeutils.layer.land.ContinentLayer
import kaptainwutax.biomeutils.layer.temperature.ClimateLayer
import kaptainwutax.biomeutils.source.OverworldBiomeSource
import kaptainwutax.featureutils.structure.Mansion
import kaptainwutax.featureutils.structure.RegionStructure
import kaptainwutax.seedutils.mc.ChunkRand
import kaptainwutax.seedutils.mc.MCVersion
import kaptainwutax.seedutils.mc.pos.CPos
import kaptainwutax.seedutils.util.math.Vec3i
import krangl.*
import java.io.File
import java.time.LocalDateTime
import java.util.*
import kotlin.streams.toList
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalTime
object SeedBiomeExperiments {


    private fun mansionPositions(structure: RegionStructure<*, *>, seed: Long, origin: Vec3i, rand: ChunkRand): List<CPos> {
        val structSearchRange = 2000
        val lowerBound = structure.at(-structSearchRange shr 4, -structSearchRange shr 4)
        val upperBound = structure.at(structSearchRange shr 4, structSearchRange shr 4)
        return cartesianProduct(
                lowerBound.regionX..upperBound.regionX,
                lowerBound.regionZ..upperBound.regionZ).map f@{ (regionX, regionZ) ->
            val structPos = structure.getInRegion(seed, regionX, regionZ, rand) ?: return@f null
            if (structPos.distanceTo(origin, Main.DISTANCE) > (structSearchRange shr 4)) return@f null
            structPos
        }.filterNotNull()
    }

    fun experiments() {
        val mansion = Mansion(MCVersion.v1_16_1)
        val mansionPos = CPos(-68, -117)
        val source = OverworldBiomeSource(VERSION, 370702544327933952L)
        val bpos = mansionPos.toBlockPos()
        println(source.getBiome(bpos.x, 0, bpos.z).name)
        for (layer in source.layers) {
            val layerPos = bpos.toRegionPos(layer.scale)
            val bid = layer.get(layerPos.x, 0, layerPos.z)
            bpos.toRegionPos(layer.scale)
            println("layer id: ${layer.layerId}, name: ${layer.javaClass.name}, scale: ${layer.scale}, biome id: ${bid}, name: ${Biome.REGISTRY[bid]?.name}")
        }
        print("end")
    }

    fun biomeNameAtRPos(layer: BiomeLayer, x: Int, z: Int) = Biome.REGISTRY[layer.get(x, 0, z)]?.name

    /*
    * Results of experiments performed on 12142828 mansions positions in distance up to 2000 blocks chebyshev distance
    * from origin (0, 0, 0):
    * layer18name wrong num  layer18name2x2 wrong num   layer20name wrong num   layer21name wrong num    layer21name2x2 wrong num  surfaceName wrong num   baseBiomeRoll1 wrong num   baseBiomeRoll12x2 wrong num   continentRoll0 wrong num   coldBiomeRoll1 wrong num
    *               7563612                         0                  425742                  425742                           0                      0                    7194366                             0                   10922837                    3428268
    * Based on this it seems for our usecase we need to check if 2x2 [(x,z);(x+1,z+1)] grid generates at least one nextInt(6) == 1
    * */

    fun Boolean.toInt() = if (this) 1 else 0

    fun benchHeuristics(numTries: Long) {
        val structure = Mansion(VERSION)
//        val seed = 0L
        var tries = 0L
        val results = mutableMapOf<String, Int>()
        val resultTimes = mutableMapOf<String, Double>()
        val seedsKept = mutableMapOf(
                "baseBiomeRoll12x2" to 0,
                "biomecheck182x2" to 0,
                "biomecheck212x2" to 0,
                "biomecheckSource" to 0
        )    //here I calculate from how many seeds I discarded all positions using this heuristic
        var seedsTried = 0
        while (true) {
            val seed = Random().nextLong()
            seedsTried++
//            todo: here find mansion and try as many biomes as you can, make statistics of each part, how many positives and false positives it had and how much time it took
            val origin = Vec3i(0, 0, 0)
            val rand = ChunkRand()
            val mansionPositions = mansionPositions(structure, seed, origin, rand)
            val source = OverworldBiomeSource(VERSION, seed)
            var baseBiomeRollAnyFound = false
            var biomecheck18AnyFound = false
            var biomecheck21AnyFound = false
            var biomecheckSourceAnyFound = false
            if (tries > numTries) break
            for (mansionPos in mansionPositions) {
                tries++
                val baseBlayer = source.getLayer(18)
                val firstScalelayer = source.getLayer(20)
                val secondScalelayer = source.getLayer(21)
                val bpos = mansionPos.toBlockPos()

                // cuts off 49% positions, not even 1% of seeds, is 78 faster than canSpawn
                // tempered rolls dark forest on basebiome
                val baseBiomeRoll12x2 = measureTimedValue {
                    val rpos18 = bpos.toRegionPos(baseBlayer.scale)
                    val localSeed = Test.getLocalSeed(baseBlayer, seed, rpos18.x, rpos18.z)
                    val localSeedS = Test.getLocalSeed(baseBlayer, seed, rpos18.x, rpos18.z + 1)
                    val localSeedE = Test.getLocalSeed(baseBlayer, seed, rpos18.x + 1, rpos18.z)
                    val localSeedSE = Test.getLocalSeed(baseBlayer, seed, rpos18.x + 1, rpos18.z + 1)
                    val isDarkForest = Math.floorMod(localSeed shr 24, 6) == 1 || Math.floorMod(localSeedS shr 24, 6) == 1 ||
                            Math.floorMod(localSeedE shr 24, 6) == 1 || Math.floorMod(localSeedSE shr 24, 6) == 1
                    isDarkForest
                }
                if (!results.containsKey("baseBiomeRoll12x2")) results["baseBiomeRoll12x2"] = 0
                if (!resultTimes.containsKey("baseBiomeRoll12x2")) resultTimes["baseBiomeRoll12x2"] = 0.0
                results["baseBiomeRoll12x2"] = results["baseBiomeRoll12x2"]!! + baseBiomeRoll12x2.value.toInt()
                resultTimes["baseBiomeRoll12x2"] = resultTimes["baseBiomeRoll12x2"]!! + baseBiomeRoll12x2.duration.inSeconds
                baseBiomeRollAnyFound = baseBiomeRollAnyFound || baseBiomeRoll12x2.value

                // cuts off 84% positions, 20% seeds, is 2.7x faster than canSpawn
                // just checks, but lower level
                val biomecheck182x2 = measureTimedValue {
                    val rpos18 = bpos.toRegionPos(baseBlayer.scale)
                    val bid = Biome.DARK_FOREST.id
                    val isDarkForest = baseBlayer.get(rpos18.x, 0, rpos18.z) == bid || baseBlayer.get(rpos18.x, 0, rpos18.z + 1) == bid ||
                            baseBlayer.get(rpos18.x + 1, 0, rpos18.z) == bid || baseBlayer.get(rpos18.x + 1, 0, rpos18.z + 1) == bid
                    isDarkForest
                }

                if (!results.containsKey("biomecheck182x2")) results["biomecheck182x2"] = 0
                if (!resultTimes.containsKey("biomecheck182x2")) resultTimes["biomecheck182x2"] = 0.0
                results["biomecheck182x2"] = results["biomecheck182x2"]!! + biomecheck182x2.value.toInt()
                resultTimes["biomecheck182x2"] = resultTimes["biomecheck182x2"]!! + biomecheck182x2.duration.inSeconds
                biomecheck18AnyFound = biomecheck18AnyFound || biomecheck182x2.value

                // cuts off 92% positions, 47% seeds, is 2.6x faster than canSpawn
                // just check, but seems it cuts lots of seeds
                val biomecheck212x2 = measureTimedValue {
                    val rpos21 = bpos.toRegionPos(secondScalelayer.scale)
                    val bid = Biome.DARK_FOREST.id
                    val isDarkForest = secondScalelayer.get(rpos21.x, 0, rpos21.z) == bid || secondScalelayer.get(rpos21.x, 0, rpos21.z + 1) == bid ||
                            secondScalelayer.get(rpos21.x + 1, 0, rpos21.z) == bid || secondScalelayer.get(rpos21.x + 1, 0, rpos21.z + 1) == bid
                    isDarkForest
                }

                if (!results.containsKey("biomecheck212x2")) results["biomecheck212x2"] = 0
                if (!resultTimes.containsKey("biomecheck212x2")) resultTimes["biomecheck212x2"] = 0.0
                results["biomecheck212x2"] = results["biomecheck212x2"]!! + biomecheck212x2.value.toInt()
                resultTimes["biomecheck212x2"] = resultTimes["biomecheck212x2"]!! + biomecheck212x2.duration.inSeconds
                biomecheck21AnyFound = biomecheck21AnyFound || biomecheck212x2.value

//                val biomecheck182x2 = measureTimedValue {
//                // MAKE SURE SPECIAL DOESNT INTERFERE
//                localSeed = Test.getLocalSeed(Test.SPECIAL, seed, pos.get(0), pos.get(1))
//                if (Math.floorMod(localSeed shr 24, 13) == 0) { // is a special one ;)
//                    bad = true
//                    break
//                }

                val biomecheckSource = measureTimedValue {
                    val isDarkForest = structure.canSpawn(mansionPos.x, mansionPos.z, source)
                    isDarkForest
                }
                if (!results.containsKey("biomecheckSource")) results["biomecheckSource"] = 0
                if (!resultTimes.containsKey("biomecheckSource")) resultTimes["biomecheckSource"] = 0.0
                results["biomecheckSource"] = results["biomecheckSource"]!! + biomecheckSource.value.toInt()
                resultTimes["biomecheckSource"] = resultTimes["biomecheckSource"]!! + biomecheckSource.duration.inSeconds
                biomecheckSourceAnyFound = biomecheckSourceAnyFound || biomecheckSource.value
            }

            if (baseBiomeRollAnyFound) seedsKept["baseBiomeRoll12x2"] = seedsKept["baseBiomeRoll12x2"]!! + 1
            if (biomecheck18AnyFound) seedsKept["biomecheck182x2"] = seedsKept["biomecheck182x2"]!! + 1
            if (biomecheck21AnyFound) seedsKept["biomecheck212x2"] = seedsKept["biomecheck212x2"]!! + 1
            if (biomecheckSourceAnyFound) seedsKept["biomecheckSource"] = seedsKept["biomecheckSource"]!! + 1
        }
        println("results:")
        println(results)
        println("result times:")
        println(resultTimes)
        println("seeds to search next:")
        println(seedsKept)
        println("from $seedsTried seeds total")
    }

    @JvmStatic
    fun main(args: Array<String>) {
//        the big dataset is upt to seed 5590 or sth. like that

//        benchHeuristics(100_000L)
//        return

//        experiments()
//
//        val layer = source.getLayer(18)
//        assert(layer is BaseBiomesLayer)
//        assert(mansion.canSpawn(mansionPos.x, mansionPos.z, source))
//        val biomeId = layer.get(bpos.x / layer.scale, 0, bpos.x / layer.scale)
//        Biome.REGISTRY[biomeId]


//        for (var seed : new long[]{2590977160621592647L,
//                8494351847174180868L,
//                -7931401893752860728L}) {
//            System.out.println(WorldSeed.toStructureSeed(seed));
//        }

//        fixResults();
//        var results = fromCsv("old_multithread_seeds_2/distances_0_70.csv");
////        var results = fromCsv("broken_small/distances_0_10.csv");
//        var results = fromCsv("good_seeds/distances_0_10.csv");
//        val results = fromCsv("mansions2examine/distances_0_120.csv")
//        val results = fromCsv("mansions2examine2/distances_0_43100.csv")
        val results = fromCsv("distances_0_11930000.csv")
        val seeds = results.map { it.seed }
        var i = 0
        val mansionRows = seeds.parallelStream().flatMap { seed ->
//            println("\nseed: $seed")
            val structure = Mansion(VERSION)
            val origin = Vec3i(0, 0, 0)
            val rand = ChunkRand()
            val mansionPositions = mansionPositions(structure, seed, origin, rand)
            val source = OverworldBiomeSource(VERSION, seed)
            val spawnedMansionPositions = mansionPositions.filter { structure.canSpawn(it.x, it.z, source) }
            val specialBlayer = source.getLayer(12)
            val baseBlayer = source.getLayer(18)
            val firstScalelayer = source.getLayer(20)
            val secondScalelayer = source.getLayer(21)
            assert(baseBlayer is BaseBiomesLayer)
//            println("num mansions: ${spawnedMansionPositions.size}")
            spawnedMansionPositions.map { mansionPos ->
                i += 1
                if (i % 10_000 == 0) println("${LocalDateTime.now()} done $i mansions from ${seeds.size} seeds, percent ${100 * i.toFloat() / seeds.size.toFloat()}%")
//                println("mansion pos: ${mansionPos}")
                val bpos = mansionPos.toBlockPos()
                val rpos12 = bpos.toRegionPos(specialBlayer.scale)
                val rpos18 = bpos.toRegionPos(baseBlayer.scale)
                val layer18name = biomeNameAtRPos(baseBlayer, rpos18.x, rpos18.z)
                val layer18nameE = biomeNameAtRPos(baseBlayer, rpos18.x + 1, rpos18.z)
                val layer18nameS = biomeNameAtRPos(baseBlayer, rpos18.x, rpos18.z + 1)
                val layer18nameSE = biomeNameAtRPos(baseBlayer, rpos18.x + 1, rpos18.z + 1)
//                println("layer 18 biome: $layer18name, should be ${Biome.DARK_FOREST.name}")
                val rpos20 = bpos.toRegionPos(secondScalelayer.scale)
                val layer20name = biomeNameAtRPos(secondScalelayer, rpos20.x, rpos20.z)
                val rpos21 = bpos.toRegionPos(secondScalelayer.scale)
                val layer21name = biomeNameAtRPos(secondScalelayer, rpos21.x, rpos21.z)
                val layer21nameE = biomeNameAtRPos(secondScalelayer, rpos21.x + 1, rpos21.z)
                val layer21nameS = biomeNameAtRPos(secondScalelayer, rpos21.x, rpos21.z + 1)
                val layer21nameSE = biomeNameAtRPos(secondScalelayer, rpos21.x + 1, rpos21.z + 1)
//                println("layer 21 biome: $layer21name, should be ${Biome.DARK_FOREST.name}")
//                assert(source.getBiome(bpos.x, 0, bpos.z) == Biome.DARK_FOREST)
//                println("layer 18 biome: ${Biome.REGISTRY[bid]?.name}")
                val surfaceName = source.getBiome(bpos.x, 0, bpos.z).name
//                println("source biome: $surfaceName")

                // base biome choose dark forest roll
                val localSeed = Test.getLocalSeed(Test.getLayer(BaseBiomesLayer::class.java), seed, rpos18.x, rpos18.z)
                val baseBiomeRoll1 = Math.floorMod(localSeed shr 24, 6) == 1
                val localSeedS = Test.getLocalSeed(Test.getLayer(BaseBiomesLayer::class.java), seed, rpos18.x, rpos18.z + 1)
                val baseBiomeRoll1S = Math.floorMod(localSeedS shr 24, 6) == 1
                val localSeedE = Test.getLocalSeed(Test.getLayer(BaseBiomesLayer::class.java), seed, rpos18.x + 1, rpos18.z)
                val baseBiomeRoll1E = Math.floorMod(localSeedE shr 24, 6) == 1
                val localSeedSE = Test.getLocalSeed(Test.getLayer(BaseBiomesLayer::class.java), seed, rpos18.x + 1, rpos18.z + 1)
                val baseBiomeRoll1SE = Math.floorMod(localSeedSE shr 24, 6) == 1
//                println("will roll dark forest in desert in BASEBIOMES $baseBiomeRoll1")
                // continent choose plains
                val localSeedCont = Test.getLocalSeed(Test.getLayer(ContinentLayer::class.java), seed, rpos18.x, rpos18.z)
                val continentRoll0 = Math.floorMod(localSeedCont shr 24, 10) == 0

                // cold gives us plains, so the temperate has chance to give desert
                val coldSeed = Test.getLocalSeed(Test.getLayer(ClimateLayer.Cold::class.java), seed, rpos12.x, rpos12.z)
                val coldRollOk = Math.floorMod(coldSeed shr 24, 6) > 1
                val coldSeedS = Test.getLocalSeed(Test.getLayer(ClimateLayer.Cold::class.java), seed, rpos12.x, rpos12.z + 1)
                val coldRollOkS = Math.floorMod(coldSeedS shr 24, 6) > 1
                val coldSeedE = Test.getLocalSeed(Test.getLayer(ClimateLayer.Cold::class.java), seed, rpos12.x + 1, rpos12.z)
                val coldRollOkE = Math.floorMod(coldSeedE shr 24, 6) > 1
                val coldSeedSE = Test.getLocalSeed(Test.getLayer(ClimateLayer.Cold::class.java), seed, rpos12.x + 1, rpos12.z + 1)
                val coldRollOkSE = Math.floorMod(coldSeedSE shr 24, 6) > 1

//                println("will roll plains in CONTINENT $continentRoll0")
                // not enough structures in the region, this seed is not interesting, quitting
//            if (structPositions.isEmpty() && structureInfo.isRequired) {
//                GlobalState.incr(structureInfo.structName);
//                return null
//            }
//            structures[structureInfo] = structPositions
//                listOf(seed, bpos, layer18name, layer21name, surfaceName, baseBiomeRoll1, continentRoll0)
                mapOf("seed" to seed, "bpos" to bpos,
                        "layer18name" to layer18name, "layer18nameE" to layer18nameE,
                        "layer18nameS" to layer18nameS, "layer18nameSE" to layer18nameSE,
                        "layer20name" to layer20name,
                        "layer21name" to layer21name, "layer21nameE" to layer21nameE,
                        "layer21nameS" to layer21nameS, "layer21nameSE" to layer21nameSE,
                        "surfaceName" to surfaceName,
                        "baseBiomeRoll1" to baseBiomeRoll1, "baseBiomeRoll1S" to baseBiomeRoll1S,
                        "baseBiomeRoll1E" to baseBiomeRoll1E, "baseBiomeRoll1SE" to baseBiomeRoll1SE,
                        "continentRoll0" to continentRoll0,
                        "coldRollGt1" to coldRollOk, "coldRollGt1E" to coldRollOkE,
                        "coldRollGt1S" to coldRollOkS, "coldRollGt1SE" to coldRollOkSE)

            }.stream()
        }.toList()

        val df = dataFrameOf(mansionRows)
        df.print(maxWidth = 350)
//        df.writeCSV(File("mansions_0_43100.csv"))
        df.writeCSV(File("mansions_0_11930000.csv"))

        println(df.schema())
        val summ = df.summarize(
                "layer18name wrong num" to { it["layer18name"].eq("dark_forest").not().count { it } },
                "layer18nameS wrong num" to { it["layer18nameS"].eq("dark_forest").not().count { it } },
                "layer18nameE wrong num" to { it["layer18nameE"].eq("dark_forest").not().count { it } },
                "layer18nameSE wrong num" to { it["layer18nameSE"].eq("dark_forest").not().count { it } },
                "layer18name2x2 wrong num" to { df.rows.map { "dark_forest" !in listOf(it["layer18name"], it["layer18nameS"], it["layer18nameE"], it["layer18nameSE"]) }.count { it } },
                "layer20name wrong num" to { it["layer20name"].eq("dark_forest").not().count { it } },
                "layer21name wrong num" to { it["layer21name"].eq("dark_forest").not().count { it } },
                "layer21name2x2 wrong num" to { df.rows.map { "dark_forest" !in listOf(it["layer21name"], it["layer21nameS"], it["layer21nameE"], it["layer21nameSE"]) }.count { it } },
                "surfaceName wrong num" to { it["surfaceName"].asStrings().count { it !in setOf("dark_forest", "dark_forest_hills") } },
                "baseBiomeRoll1 wrong num" to { it["baseBiomeRoll1"].eq(false).count { it } },
                "baseBiomeRoll12x2 wrong num" to { df.rows.map { !listOf(it["baseBiomeRoll1"], it["baseBiomeRoll1S"], it["baseBiomeRoll1E"], it["baseBiomeRoll1SE"]).any() }.count { it } },
                "continentRoll0 wrong num" to { it["continentRoll0"].eq(false).count { it } },
                "coldBiomeRoll1 wrong num" to { it["coldRollGt1"].eq(false).count { it } },
                "coldBiomeRoll12x2 wrong num" to { df.rows.map { !listOf(it["coldRollGt1"], it["coldRollGt1S"], it["coldRollGt1E"], it["coldRollGt1SE"]).any() }.count { it } }
        )
        summ.print(maxWidth = 350)

        GlobalState.shutdown()
    }
}
