import GlobalState.addSeed
import GlobalState.examineSeed
import GlobalState.getCurrentSeed
import GlobalState.nextSeed
import GlobalState.trySeed
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import kaptainwutax.biomeutils.Biome
import kaptainwutax.biomeutils.layer.land.BaseBiomesLayer
import kaptainwutax.biomeutils.source.BiomeSource
import kaptainwutax.biomeutils.source.EndBiomeSource
import kaptainwutax.biomeutils.source.NetherBiomeSource
import kaptainwutax.biomeutils.source.OverworldBiomeSource
import kaptainwutax.featureutils.structure.RegionStructure
import kaptainwutax.seedutils.lcg.rand.JRand
import kaptainwutax.seedutils.mc.ChunkRand
import kaptainwutax.seedutils.mc.Dimension
import kaptainwutax.seedutils.mc.pos.BPos
import kaptainwutax.seedutils.mc.pos.CPos
import kaptainwutax.seedutils.util.math.Vec3i
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.math.absoluteValue

data class SeedResult(
        var seed: Long,
        var structureDistances: Map<String, Double>,
        var biomeDistances: Map<String, Double>) {

    override fun toString(): String {
        return "SeedResult{" +
                "seed=$seed" +
                ", structureDistances=$structureDistances" +
                ", biomeDistances=$biomeDistances" +
                "}"
    }
}


data class StructureInfo<C : RegionStructure.Config, D : RegionStructure.Data<*>>
@JvmOverloads constructor(val structure: RegionStructure<C, D>, val dimension: Dimension, val isRequired: Boolean, val maxDistance: Int = Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS) {
    val structName: String = if (dimension == Dimension.OVERWORLD) structure.name else "${structure.name}_${dimension.getName()}"

}

data class BiomeInfo
@JvmOverloads constructor(val biomesList: ImmutableList<Biome>, val name: String, val isRequired: Boolean, val maxDistance: Int = Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS) {
}

class SearchingThread(private val structures: ImmutableList<StructureInfo<*, *>>, private val biomes: ImmutableList<BiomeInfo>) : Thread(), Runnable {
    override fun run() {
        searching()
    }

    private fun searching() {
        var structureSeed: Long = 0
        while (Main.STRUCTURE_SEED_MAX > getCurrentSeed()) {

            // I want to do search in a range of seeds so I can iteratively scan different ranges
            if (structureSeed >= Main.STRUCTURE_SEED_MAX) {
                break
            }
            structureSeed = nextSeed
            Searcher.searchStructureSeed(structureSeed, structures, biomes, Main.BIOME_SEARCH_SPACING)
        }
    }
}

object Searcher {

    fun <T, U> cartesianProduct(c1: Iterable<T>, c2: Iterable<U>): List<Pair<T, U>> {
        return c1.flatMap { lhsElem -> c2.map { rhsElem -> lhsElem to rhsElem } }
    }

    fun getStructuresPosList(structureSeed: Long, sList: ImmutableList<StructureInfo<*, *>>, origin: Vec3i?, rand: ChunkRand?): Map<StructureInfo<*, *>, ImmutableList<CPos>>? {
        return sList.map { structureInfo->
            val structure = structureInfo.structure
            val structSearchRange = structureInfo.maxDistance
            val lowerBound = structure.at(-structSearchRange shr 4, -structSearchRange shr 4)
            val upperBound = structure.at(structSearchRange shr 4, structSearchRange shr 4)
            val structPositions = cartesianProduct(
                    lowerBound.regionX..upperBound.regionX,
                    lowerBound.regionZ..upperBound.regionZ).map f@{ (regionX, regionZ) ->
                val structPos = structure.getInRegion(structureSeed, regionX, regionZ, rand) ?: return@f null
                if (structPos.distanceTo(origin, Main.DISTANCE) > (structSearchRange shr 4)) return@f null
                structPos
            }.filterNotNull()
            // not enough structures in the region, this seed is not interesting, quitting
            if (structPositions.isEmpty() && structureInfo.isRequired) {
                GlobalState.incr(structureInfo.structName)
                return null
            }
            structureInfo to ImmutableList.copyOf(structPositions)
        }.toMap()
    }

    fun searchStructureSeed(
            structureSeed: Long, sList: ImmutableList<StructureInfo<*, *>>, bList: ImmutableList<BiomeInfo>,
            biomeCheckSpacing: Int) {
        val origin = Vec3i(0, 0, 0)
        val rand = ChunkRand()
        val structures = getStructuresPosList(structureSeed, sList, origin, rand) ?: return

        // 16 upper bits for biomes
        for (upperBits in 0 until (1L shl 16)) {
            if (upperBits % 10000 == 0L) {
//            if(upperBits % 100 == 0) {
                GlobalState.OUTPUT_THREAD.execute({ Main.LOGGER.info("will check struct seed: $structureSeed, upperBits: $upperBits") })
            }
            val worldSeed: Long = (upperBits shl 48) or structureSeed
            val seedResult = searchWorldSeed(worldSeed, structures, bList, biomeCheckSpacing, origin, rand)
                    ?: continue
            addSeed(seedResult)
        }
        // here was code for stopping, but I just run it until it's killed
    }

    fun searchWorldSeed(worldSeed: Long, structures: Map<StructureInfo<*, *>, List<CPos>>,
                        bList: ImmutableList<BiomeInfo>, biomeCheckSpacing: Int, origin: Vec3i?, rand: ChunkRand): SeedResult? {
        //caching BiomeSources per seed so I utilize the caching https://discordapp.com/channels/505310901461581824/532998733135085578/749750365716480060
        val sources: ConcurrentMap<Dimension, BiomeSource> = ConcurrentHashMap()
        var structPoss = structures

        trySeed()

        //use dark forest based shortcutting only if I search for mansion and require it
        if (structPoss.keys.any { it.structName == "mansion" && it.isRequired }) {
            structPoss = ConcurrentHashMap(structPoss)  // because I modify here the position list during pruning
            val dim = Dimension.OVERWORLD
            if (!sources.containsKey(dim)) sources[dim] = getBiomeSource(dim, worldSeed)
            val source = sources[dim]!!
            // here will be shortcutting
            val baseBlayer = source.getLayer(18)
            val secScalelayer = source.getLayer(21)

            //filtering through mansion positions
            val mansion = structPoss.keys.first { it.structName == "mansion" && it.isRequired }
            val mansionNewPositions = structPoss[mansion]?.filter f@{ mansionPos ->
                val bpos = mansionPos.toBlockPos()
                val rpos18 = bpos.toRegionPos(baseBlayer.scale)
                val rpos21 = bpos.toRegionPos(secScalelayer.scale)
                val localSeed = Test.getLocalSeed(baseBlayer, worldSeed, rpos18.x, rpos18.z)
                val localSeedS = Test.getLocalSeed(baseBlayer, worldSeed, rpos18.x, rpos18.z + 1)
                val localSeedE = Test.getLocalSeed(baseBlayer, worldSeed, rpos18.x + 1, rpos18.z)
                val localSeedSE = Test.getLocalSeed(baseBlayer, worldSeed, rpos18.x + 1, rpos18.z + 1)
                val randCanHitDarkForest = Math.floorMod(localSeed shr 24, 6) == 1 || Math.floorMod(localSeedS shr 24, 6) == 1 ||
                        Math.floorMod(localSeedE shr 24, 6) == 1 || Math.floorMod(localSeedSE shr 24, 6) == 1
                if (!randCanHitDarkForest) return@f false

                // second heuristic based on looking at the lowest level where we know we get the dark forest
                val bid = Biome.DARK_FOREST.id
                val l18canBeDarkForest = baseBlayer.get(rpos18.x, 0, rpos18.z) == bid || baseBlayer.get(rpos18.x, 0, rpos18.z + 1) == bid ||
                        baseBlayer.get(rpos18.x + 1, 0, rpos18.z) == bid || baseBlayer.get(rpos18.x + 1, 0, rpos18.z + 1) == bid
                if (!l18canBeDarkForest) return@f false

                // third heuristic based on looking at the lowest level where we know we get the dark forest
                val l21canBeDarkForest = secScalelayer.get(rpos21.x, 0, rpos21.z) == bid || secScalelayer.get(rpos21.x, 0, rpos21.z + 1) == bid ||
                        secScalelayer.get(rpos21.x + 1, 0, rpos21.z) == bid || secScalelayer.get(rpos21.x + 1, 0, rpos21.z + 1) == bid
                if (!l21canBeDarkForest) return@f false

                true
            }

            if (mansionNewPositions.isNullOrEmpty()) return null
            structPoss[mansion] = mansionNewPositions   // to use the result of pruned positions
        }

        //after the shortcut
        examineSeed()

        val bigConst = 10e9
        // here was code for stopping, but I just run it until it's killed
        val structureDistances: ConcurrentMap<String, Double> = ConcurrentHashMap()
        for ((structure, positions) in structPoss.entries) {
            val dim = structure.dimension
            if (!sources.containsKey(dim)) sources[dim] = getBiomeSource(dim, worldSeed)
            val source = sources[dim]
            val searchStructure = structure.structure

            //todo: do some sorting of positions by distance from origin, so I could cut it once I find nearest one
//            var a_range = ContiguousSet.create(Range.closed(-5, 5), DiscreteDomain.integers());
//            var structDistance = 1e6;
//            for (var coords : Sets.cartesianProduct(a_range, a_range).stream().sorted((x1, x2) -> Math.abs(x1.get(0)) + Math.abs(x1.get(1)) - Math.abs(x2.get(0)) - Math.abs(x2.get(1))).collect(Collectors.toList())) {
            var minDistance = bigConst // some big number, I don't want Double.MAX_VALUE
            for (pos in positions) {
                if (!searchStructure.canSpawn(pos.x, pos.z, source)) continue
                val curDist = pos.toBlockPos().distanceTo(origin, Main.DISTANCE)
                if (curDist < minDistance) minDistance = curDist
            }
            // I require this structure and it's not there, end the search before testing biomes
            if (minDistance >= bigConst && structure.isRequired) {
                GlobalState.incr(structure.structName)
                return null
            }
            structureDistances[structure.structName] = minDistance
        }
        val biomeDistances = bList.map f@{ bInfo ->
            val biomesName = bInfo.name
//        val biomeDistances: ConcurrentMap<String, Double> = bList.entries.map f@{(biomesName, biomesList)->
            if (bInfo.biomesList.size == 0) return@f null
            //this is hardcoded for overworld, I should make sure biomelist is from same dimension and make it work in general
            if (!sources.containsKey(Dimension.OVERWORLD)) sources[Dimension.OVERWORLD] = getBiomeSource(Dimension.OVERWORLD, worldSeed)
            val source = sources[Dimension.OVERWORLD]!!
            //todo: add here computation of how many times I hit dark forest and number of seeds I prune using shortcuting
            val biomePos = distToAnyBiomeKaptainWutax(bInfo.maxDistance, bInfo.biomesList, biomeCheckSpacing, source, rand)
                    ?: run {
                        GlobalState.incr(bInfo.biomesList.map { it.name }.joinToString(", "))
                        if (bInfo.isRequired) return null else return@f biomesName to bigConst
                    }
            return@f biomesName to biomePos.distanceTo(origin, Main.DISTANCE)
        }.filterNotNull().toMap()
        return SeedResult(worldSeed, structureDistances, biomeDistances)
    }

    fun getBiomeSource(dimension: Dimension?, worldSeed: Long): BiomeSource {
        return when (dimension) {
            Dimension.OVERWORLD -> {
                if (Main.WORLD_TYPE === Main.WorldType.LARGE_BIOMES) {
                    OverworldBiomeSource(Main.VERSION, worldSeed, 6, 4)
                } else {
                    OverworldBiomeSource(Main.VERSION, worldSeed)
                }
            }
            Dimension.NETHER -> {
                NetherBiomeSource(Main.VERSION, worldSeed)
            }
            Dimension.END -> EndBiomeSource(Main.VERSION, worldSeed)
            else -> throw IllegalArgumentException("Unknown dimension")
        }
    }

    fun distToAnyBiomeKaptainWutax(searchSize: Int, biomeToFind: Collection<Biome?>?, biomeCheckSpacing: Int, source: BiomeSource, rand: JRand): BPos? {
        return source.locateBiome(0, 0, 0, searchSize, biomeCheckSpacing, biomeToFind, rand, true)
    }

    // kaptain adviced me to implement my own https://discordapp.com/channels/505310901461581824/532998733135085578/749728029520953374
    // but it's slower, so I'm reverting to his
    fun distToAnyBiomeMine(searchSize: Int, biomeToFind: Collection<Biome?>, biomeCheckSpacing: Int, source: BiomeSource, rand: JRand?): BPos? {
        var i = 0
        // I know I don't care about nether and end biomes.
        // basically I copied this from locateBiome so I'd start from beginning and went to outer blocks, I hardcoded variant of checkByLayer=true
        var depth = 0
        while (depth <= searchSize) {
            var z = -depth
            while (z <= depth) {
                val isZEdge = z.absoluteValue == depth
                var x = -depth
                while (x <= depth) {
                    val isXEdge = x.absoluteValue == depth
                    if (!isXEdge && !isZEdge) {
                        x += biomeCheckSpacing
                        continue
                    }
                    i++
                    if (biomeToFind.contains(source.getBiome(x, 0, z))) {
                        println(i)
                        return BPos(x, 0, z)
                    }
                    x += biomeCheckSpacing
                }
                z += biomeCheckSpacing
            }
            depth += biomeCheckSpacing
        }
        println(i)
        return null
    }
}