import com.google.common.collect.*;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.*;
import kaptainwutax.featureutils.structure.Mansion;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.Vec3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Searcher {
    //KaptainWutax(Wat's "cool" meme?)vcera v 21:27
//7 minutes sounds like quite a lot
//for 9 million seeds
//    funky_face vcera v 21:28
//It's definitely not very optimized

    public static ConcurrentMap<StructureInfo<?, ?>, List<CPos>> getStructuresPosList(long structureSeed, ImmutableList<StructureInfo<?, ?>> sList, Vec3i origin, ChunkRand rand) {
        ConcurrentMap<StructureInfo<?, ?>, List<CPos>> structures = new ConcurrentHashMap<>();
        for (var structureInfo : sList) {
            RegionStructure<?, ?> structure = structureInfo.getStructure();
            var structSearchRange = structureInfo.getMaxDistance();
            RegionStructure.Data<?> lowerBound = structure.at(-structSearchRange >> 4, -structSearchRange >> 4);
            RegionStructure.Data<?> upperBound = structure.at(structSearchRange >> 4, structSearchRange >> 4);

            List<CPos> structPositions = new ArrayList<>();

            for (int regionX = lowerBound.regionX; regionX <= upperBound.regionX; regionX++) {
                for (int regionZ = lowerBound.regionZ; regionZ <= upperBound.regionZ; regionZ++) {
                    var structPos = structure.getInRegion(structureSeed, regionX, regionZ, rand);
                    if (structPos == null) continue;
                    if (structPos.distanceTo(origin, Main.DISTANCE) > structSearchRange >> 4) continue;
                    structPositions.add(structPos);
                }
            }
            // not enough structures in the region, this seed is not interesting, quitting
            if (structPositions.isEmpty() && structureInfo.isRequired()) {
//                GlobalState.incr(structureInfo.getStructName());
                return null;
            }
            structures.put(structureInfo, structPositions);
        }
        return structures;
    }

    public static void searchStructureSeed(
            int blockSearchRadius, long structureSeed, ImmutableList<StructureInfo<?, ?>> sList,
            ImmutableMap<String, ImmutableList<Biome>> bList, int biomeCheckSpacing) {
        Vec3i origin = new Vec3i(0, 0, 0);
        ChunkRand rand = new ChunkRand();

        ConcurrentMap<StructureInfo<?, ?>, List<CPos>> structures = getStructuresPosList(structureSeed, sList, origin, rand);
        if (structures == null) return;

        // 16 upper bits for biomes
        for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
//            if(upperBits > 1000) break;
            if (upperBits % 10_000 == 0) {
//            if(upperBits % 100 == 0) {
                var message = "will check struct seed: " + structureSeed + ", upperBits: " + upperBits;
                GlobalState.OUTPUT_THREAD.execute(() -> Main.LOGGER.info(message));
            }
            long worldSeed = (upperBits << 48) | structureSeed;
            var seedResult = searchWorldSeed(blockSearchRadius, worldSeed, structures, bList, biomeCheckSpacing, origin, rand);
            if(seedResult == null) continue;
            GlobalState.addSeed(seedResult);
        }
        // here was code for stopping, but I just run it until it's killed
    }

    public static SeedResult searchWorldSeed(
            int blockSearchRadius, long worldSeed, ConcurrentMap<StructureInfo<?, ?>, List<CPos>> structures,
            ImmutableMap<String, ImmutableList<Biome>> bList, int biomeCheckSpacing, Vec3i origin, ChunkRand rand) {
        //caching BiomeSources per seed so I utilize the caching https://discordapp.com/channels/505310901461581824/532998733135085578/749750365716480060
        ConcurrentMap<Dimension, BiomeSource> sources = new ConcurrentHashMap<>();
        // here was code for stopping, but I just run it until it's killed

        ConcurrentMap<String, Double> structureDistances = new ConcurrentHashMap<>();
        for (var e : structures.entrySet()) {
            var structure = e.getKey();
            var positions = e.getValue();
            var dim = structure.getDimension();
            if (!sources.containsKey(dim)) sources.put(dim, Searcher.getBiomeSource(dim, worldSeed));
            var source = sources.get(dim);
            var searchStructure = structure.getStructure();

            //todo: do some sorting of positions by distance from origin, so I could cut it once I find nearest one
//            var a_range = ContiguousSet.create(Range.closed(-5, 5), DiscreteDomain.integers());
//            var structDistance = 1e6;
//            for (var coords : Sets.cartesianProduct(a_range, a_range).stream().sorted((x1, x2) -> Math.abs(x1.get(0)) + Math.abs(x1.get(1)) - Math.abs(x2.get(0)) - Math.abs(x2.get(1))).collect(Collectors.toList())) {
            final var bigConst = 10e9;
            var minDistance = bigConst; // some big number, I don't want Double.MAX_VALUE
            for (var pos : positions) {
                if (!searchStructure.canSpawn(pos.getX(), pos.getZ(), source)) continue;
                var curDist = pos.toBlockPos().distanceTo(origin, Main.DISTANCE);
                if (curDist < minDistance) minDistance = curDist;
            }
            // I require this structure and it's not there, end the search before testing biomes
            if (minDistance >= bigConst && structure.isRequired()) {
//                GlobalState.incr(structure.getStructName());
                return null;
            }
            structureDistances.put(structure.getStructName(), minDistance);
        }

        ConcurrentMap<String, Double> biomeDistances = new ConcurrentHashMap<>();
        for (var e : bList.entrySet()) {
            var biomesName = e.getKey();
            var biomesList = e.getValue();

            if (biomesList.size() != 0) {
                if (!sources.containsKey(Dimension.OVERWORLD))
                    sources.put(Dimension.OVERWORLD, Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed));
                var source = sources.get(Dimension.OVERWORLD);
                var biomePos = distToAnyBiomeKaptainWutax(blockSearchRadius, biomesList, biomeCheckSpacing, source, rand);
//                var biomePos = BiomeSearcher.distToAnyBiomeMine(blockSearchRadius, worldSeed, biomesList, biomeCheckSpacing, source, rand);
                if (biomePos == null) {
//                    GlobalState.incr(biomesList.stream().map(Biome::getName).collect(Collectors.joining(", ")));
                    return null;     // returns null when no biome is found, skipping this seed
                }
                var biomeDist = biomePos.distanceTo(origin, Main.DISTANCE);
                biomeDistances.put(biomesName, biomeDist);
            }

        }

        return new SeedResult(worldSeed, structureDistances, biomeDistances);
    }

    public static BiomeSource getBiomeSource(Dimension dimension, long worldSeed) {
        BiomeSource source = null;

        switch (dimension) {
            case OVERWORLD:
                if (Main.WORLD_TYPE == Main.WorldType.LARGE_BIOMES) {
                    source = new OverworldBiomeSource(Main.VERSION, worldSeed, 6, 4);
                } else {
                    source = new OverworldBiomeSource(Main.VERSION, worldSeed);
                }
                break;
            case NETHER:
                source = new NetherBiomeSource(Main.VERSION, worldSeed);
                break;
            case END:
                source = new EndBiomeSource(Main.VERSION, worldSeed);
                break;
        }

        return source;
    }

    public static BPos distToAnyBiomeKaptainWutax(int searchSize, Collection<Biome> biomeToFind, int biomeCheckSpacing, BiomeSource source, JRand rand) {
        return source.locateBiome(0, 0, 0, searchSize, biomeCheckSpacing, biomeToFind, rand, true);
    }

    // kaptain adviced me to implement my own https://discordapp.com/channels/505310901461581824/532998733135085578/749728029520953374
    // but it's slower, so I'm reverting to his
    public static BPos distToAnyBiomeMine(int searchSize, Collection<Biome> biomeToFind, int biomeCheckSpacing, BiomeSource source, JRand rand) {
        var i = 0;
        // I know I don't care about nether and end biomes.
        // basically I copied this from locateBiome so I'd start from beginning and went to outer blocks, I hardcoded variant of checkByLayer=true
        for (int depth = 0; depth <= searchSize; depth += biomeCheckSpacing) {
            for (int z = -depth; z <= depth; z += biomeCheckSpacing) {
                boolean isZEdge = Math.abs(z) == depth;
                for (int x = -depth; x <= depth; x += biomeCheckSpacing) {
                    boolean isXEdge = Math.abs(x) == depth;
                    if (!isXEdge && !isZEdge) continue;
                    i++;
                    if (biomeToFind.contains(source.getBiome(x, 0, z))) {
                        System.out.println(i);
                        return new BPos(x, 0, z);
                    }
                }
            }
        }
        System.out.println(i);
        return null;
    }

}
