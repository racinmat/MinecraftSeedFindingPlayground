import Main.VERSION
import Searcher.cartesianProduct
import VerifyResults.fromCsv
import kaptainwutax.biomeutils.Biome
import kaptainwutax.biomeutils.layer.land.BaseBiomesLayer
import kaptainwutax.biomeutils.layer.land.ContinentLayer
import kaptainwutax.biomeutils.source.OverworldBiomeSource
import kaptainwutax.featureutils.structure.Mansion
import kaptainwutax.featureutils.structure.RegionStructure
import kaptainwutax.seedutils.mc.ChunkRand
import kaptainwutax.seedutils.mc.MCVersion
import kaptainwutax.seedutils.mc.pos.CPos
import kaptainwutax.seedutils.util.math.Vec3i
import krangl.dataFrameOf
import krangl.eq
import krangl.not

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

    //dark forest should be on layer 18, but sometimes it's not
    // and it gets there on some of scaling layers, it's there every time on layer 21
    @JvmStatic
    fun main(args: Array<String>) {
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
        val results = fromCsv("mansions2examine/distances_0_120.csv")
        val seeds = results.map { it.seed }
        val mansionRows = seeds.flatMap { seed ->
            println("\nseed: $seed")
            val structure = Mansion(VERSION)
            val origin = Vec3i(0, 0, 0)
            val rand = ChunkRand()
            val mansionPositions = mansionPositions(structure, seed, origin, rand)
            val source = OverworldBiomeSource(VERSION, seed)
            val spawnedMansionPositions = mansionPositions.filter { structure.canSpawn(it.x, it.z, source) }
            val baseBlayer = source.getLayer(18)
            val secondScalelayer = source.getLayer(21)
            assert(baseBlayer is BaseBiomesLayer)
            println("num mansions: ${spawnedMansionPositions.size}")
            spawnedMansionPositions.map { mansionPos ->
                println("mansion pos: ${mansionPos}")
                val bpos = mansionPos.toBlockPos()
                val rpos = bpos.toRegionPos(baseBlayer.scale)
                val bid = baseBlayer.get(rpos.x, 0, rpos.z)
                val layer18name = Biome.REGISTRY[bid]?.name
                println("layer 18 biome: $layer18name, should be ${Biome.DARK_FOREST.name}")
                val rpos2 = bpos.toRegionPos(secondScalelayer.scale)
                val bid2 = secondScalelayer.get(rpos2.x, 0, rpos2.z)
                val layer21name = Biome.REGISTRY[bid2]?.name
                println("layer 21 biome: $layer21name, should be ${Biome.DARK_FOREST.name}")
//                assert(source.getBiome(bpos.x, 0, bpos.z) == Biome.DARK_FOREST)
//                println("layer 18 biome: ${Biome.REGISTRY[bid]?.name}")
                val surfaceName = source.getBiome(bpos.x, 0, bpos.z).name
                println("source biome: $surfaceName")

                // base biome choose dark forest roll
                var localSeed = Test.getLocalSeed(Test.getLayer(BaseBiomesLayer::class.java), seed, rpos.x, rpos.z)
                val baseBiomeRoll1 = Math.floorMod(localSeed shr 24, 6) == 1
                println("will roll dark forest in desert in BASEBIOMES $baseBiomeRoll1")
                // continent choose plains
                localSeed = Test.getLocalSeed(Test.getLayer(ContinentLayer::class.java), seed, rpos.x, rpos.z)
                val continentRoll0 = Math.floorMod(localSeed shr 24, 10) == 0
                println("will roll plains in CONTINENT $continentRoll0")
                // not enough structures in the region, this seed is not interesting, quitting
//            if (structPositions.isEmpty() && structureInfo.isRequired) {
//                GlobalState.incr(structureInfo.structName);
//                return null
//            }
//            structures[structureInfo] = structPositions
//                listOf(seed, bpos, layer18name, layer21name, surfaceName, baseBiomeRoll1, continentRoll0)
                mapOf("seed" to seed, "bpos" to bpos, "layer18name" to layer18name, "layer21name" to layer21name,
                        "surfaceName" to surfaceName, "baseBiomeRoll1" to baseBiomeRoll1,
                        "continentRoll0" to continentRoll0)

            }
        }
        val df = dataFrameOf(mansionRows)
        println(df)
        println("layer18name wrong num: ${df["layer18name"].eq("dark_forest").not().count { it }}\n" +
                "layer21name wrong num: ${df["layer21name"].eq("dark_forest").not().count { it }}\n" +
                "surfaceName wrong num: ${df["surfaceName"].eq("dark_forest").not().count { it }}\n" +
                "baseBiomeRoll1 wrong num: ${df["baseBiomeRoll1"].eq(true).not().count { it }}\n" +
                "continentRoll0 wrong num: ${df["continentRoll0"].eq(true).not().count { it }}\n")
//        df.summarize(
//                "min_age" to { it["age"].min() },
//                "max_age" to { it["age"].max() }
//        )
//        df.summarize(
//                "min_age" to { it["age"].min() },
//                "max_age" to { it["age"].max() }
//        )

// for sake of r and python adoptability you can also use `=` here
//        df.summarize(
//                "min_age" `=` { it["age"].min() },
//                "max_age" `=` { it["age"].max() }
//        )
        //
//        GlobalState.shutdown();
    }
}