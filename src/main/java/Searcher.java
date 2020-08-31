import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.*;
import kaptainwutax.featureutils.structure.Mansion;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import kaptainwutax.seedutils.util.math.Vec3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Searcher {
//KaptainWutax(Wat's "cool" meme?)vcera v 21:27
//7 minutes sounds like quite a lot
//for 9 million seeds
//    funky_face vcera v 21:28
//It's definitely not very optimized

    public static void searchStructureSeed(
            int blockSearchRadius, long structureSeed, ImmutableList<StructureInfo<?, ?>> sList,
            ImmutableMap<String, ImmutableList<Biome>> bList, int biomeCheckSpacing) {
        Vec3i origin = new Vec3i(0, 0, 0);
        ChunkRand rand = new ChunkRand();

        ConcurrentMap<StructureInfo<?, ?>, List<CPos>> structures = new ConcurrentHashMap<>();
        for (var structureInfo : sList) {
            // here was code for stopping, but I just run it until it's killed
            RegionStructure<?, ?> structure = structureInfo.getStructure();
            RegionStructure.Data<?> lowerBound = structure.at(-blockSearchRadius >> 4, -blockSearchRadius >> 4);
            RegionStructure.Data<?> upperBound = structure.at(blockSearchRadius >> 4, blockSearchRadius >> 4);

            List<CPos> structPositions = new ArrayList<>();

            for (int regionX = lowerBound.regionX; regionX <= upperBound.regionX; regionX++) {
                for (int regionZ = lowerBound.regionZ; regionZ <= upperBound.regionZ; regionZ++) {
                    var structPos = structure.getInRegion(structureSeed, regionX, regionZ, rand);
                    if (structPos == null) continue;
                    if (structPos.distanceTo(origin, DistanceMetric.EUCLIDEAN) > structureInfo.getMaxDistance() >> 4) continue;
                    structPositions.add(structPos);
                }
            }
            // not enough structures in the region, this seed is not interesting, quitting
            if (structPositions.isEmpty() && structureInfo.isRequired()) return;
            structures.put(structureInfo, structPositions);
        }

        if (structures.size() != sList.size()) return;

        // 16 upper bits for biomes
        for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
//            if(upperBits > 1000) break;
            if (upperBits % 10_000 == 0) {
//            if(upperBits % 100 == 0) {
                var message = "will check struct seed: " + structureSeed + ", upperBits: " + upperBits;
                GlobalState.OUTPUT_THREAD.execute(() -> Main.LOGGER.info(message));
            }
            long worldSeed = (upperBits << 48) | structureSeed;
            searchWorldSeed(blockSearchRadius, worldSeed, structures, bList, biomeCheckSpacing, origin, rand);
        }
        // here was code for stopping, but I just run it until it's killed
    }

    public static void searchWorldSeed(
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

            final var bigConst = 10e9;
            var minDistance = bigConst; // some big number, I don't want Double.MAX_VALUE
            for (var pos : positions) {
                if (!searchStructure.canSpawn(pos.getX(), pos.getZ(), source)) continue;
                var curDist = pos.toBlockPos().distanceTo(origin, DistanceMetric.EUCLIDEAN);
                if (curDist < minDistance) minDistance = curDist;
            }
            // I require this structure and it's not there, end the search before testing biomes
            if (minDistance == bigConst && structure.isRequired()) return;
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
                var biomePos = BiomeSearcher.distToAnyBiomeKaptainWutax(blockSearchRadius, biomesList, biomeCheckSpacing, source, rand);
//                var biomePos = BiomeSearcher.distToAnyBiomeMine(blockSearchRadius, worldSeed, biomesList, biomeCheckSpacing, source, rand);
                if (biomePos == null) return;     // returns null when no biome is found, skipping this seed
                var biomeDist = biomePos.distanceTo(origin, DistanceMetric.EUCLIDEAN);
                biomeDistances.put(biomesName, biomeDist);
            }

        }

        var a_mansion = structures.keySet().stream().filter(s -> s.getStructure() instanceof Mansion).findFirst();
        if(a_mansion.isPresent() && !structures.get(a_mansion.get()).isEmpty()) {
            var mansion = a_mansion.get();
            var structs_str = structures.get(mansion).stream().map(Vec3i::toString).collect(Collectors.joining());
            OverworldBiomeSource biomeSource = (OverworldBiomeSource) sources.get(Dimension.OVERWORLD);
            Main.LOGGER.info("Found mansion! " + structs_str + " in world seed: " + worldSeed + " with spawn point: " + biomeSource.getSpawnPoint());
        }
        GlobalState.addSeed(new SeedResult(worldSeed, structureDistances, biomeDistances));
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
}
