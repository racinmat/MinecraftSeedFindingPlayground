import GlobalState.addSeed
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import kaptainwutax.biomeutils.Biome
import kaptainwutax.biomeutils.source.*
import kaptainwutax.seedutils.lcg.rand.JRand
import kaptainwutax.seedutils.mc.ChunkRand
import kaptainwutax.seedutils.mc.Dimension
import kaptainwutax.seedutils.mc.pos.BPos
import kaptainwutax.seedutils.mc.pos.CPos
import kaptainwutax.seedutils.util.math.Vec3i
import java.util.concurrent.*

object Searcher {

    fun <T, U> cartesianProduct(c1: Iterable<T>, c2: Iterable<U>): List<Pair<T, U>> {
        return c1.flatMap { lhsElem -> c2.map { rhsElem -> lhsElem to rhsElem } }
    }

    fun getStructuresPosList(structureSeed: Long, sList: ImmutableList<StructureInfo<*, *>>, origin: Vec3i?, rand: ChunkRand?): ConcurrentMap<StructureInfo<*, *>, List<CPos>>? {
        val structures: ConcurrentMap<StructureInfo<*, *>, List<CPos>> = ConcurrentHashMap()
        for (structureInfo: StructureInfo<*, *> in sList) {
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
//                GlobalState.incr(structureInfo.getStructName());
                return null
            }
            structures[structureInfo] = structPositions
        }
        return structures
    }

    fun searchStructureSeed(
            blockSearchRadius: Int, structureSeed: Long, sList: ImmutableList<StructureInfo<*, *>>,
            bList: ImmutableMap<String, ImmutableList<Biome>>, biomeCheckSpacing: Int) {
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
            val seedResult = searchWorldSeed(blockSearchRadius, worldSeed, structures, bList, biomeCheckSpacing, origin, rand)
                    ?: continue
            addSeed(seedResult)
        }
        // here was code for stopping, but I just run it until it's killed
    }

    fun searchWorldSeed(
            blockSearchRadius: Int, worldSeed: Long, structures: ConcurrentMap<StructureInfo<*, *>, List<CPos>>,
            bList: ImmutableMap<String, ImmutableList<Biome>>, biomeCheckSpacing: Int, origin: Vec3i?, rand: ChunkRand): SeedResult? {
        //caching BiomeSources per seed so I utilize the caching https://discordapp.com/channels/505310901461581824/532998733135085578/749750365716480060
        val sources: ConcurrentMap<Dimension, BiomeSource?> = ConcurrentHashMap()
        // here was code for stopping, but I just run it until it's killed
        val structureDistances: ConcurrentMap<String, Double> = ConcurrentHashMap()
        for ((structure, positions) in structures.entries) {
            val dim = structure.dimension
            if (!sources.containsKey(dim)) sources[dim] = getBiomeSource(dim, worldSeed)
            val source = sources[dim]
            val searchStructure = structure.structure

            //todo: do some sorting of positions by distance from origin, so I could cut it once I find nearest one
//            var a_range = ContiguousSet.create(Range.closed(-5, 5), DiscreteDomain.integers());
//            var structDistance = 1e6;
//            for (var coords : Sets.cartesianProduct(a_range, a_range).stream().sorted((x1, x2) -> Math.abs(x1.get(0)) + Math.abs(x1.get(1)) - Math.abs(x2.get(0)) - Math.abs(x2.get(1))).collect(Collectors.toList())) {
            val bigConst = 10e9
            var minDistance = bigConst // some big number, I don't want Double.MAX_VALUE
            for (pos: CPos in positions) {
                if (!searchStructure.canSpawn(pos.x, pos.z, source)) continue
                val curDist = pos.toBlockPos().distanceTo(origin, Main.DISTANCE)
                if (curDist < minDistance) minDistance = curDist
            }
            // I require this structure and it's not there, end the search before testing biomes
            if (minDistance >= bigConst && structure.isRequired) {
//                GlobalState.incr(structure.getStructName());
                return null
            }
            structureDistances[structure.structName] = minDistance
        }
        val biomeDistances: ConcurrentMap<String, Double> = bList.entries.map f@{(biomesName, biomesList)->
            if (biomesList.size == 0) return@f null
            if (!sources.containsKey(Dimension.OVERWORLD)) sources[Dimension.OVERWORLD] = getBiomeSource(Dimension.OVERWORLD, worldSeed)
            val source = sources[Dimension.OVERWORLD]!!
            val biomePos: BPos = distToAnyBiomeKaptainWutax(blockSearchRadius, biomesList, biomeCheckSpacing, source, rand)
                    ?: //                    GlobalState.incr(biomesList.stream().map(Biome::getName).collect(Collectors.joining(", ")));
                    return@f null // returns null when no biome is found, skipping this seed
            return@f biomesName to biomePos.distanceTo(origin, Main.DISTANCE)
        }.filterNotNull().toMap() as ConcurrentMap<String, Double>
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
                val isZEdge = Math.abs(z) == depth
                var x = -depth
                while (x <= depth) {
                    val isXEdge = Math.abs(x) == depth
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