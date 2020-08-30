import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.*;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import kaptainwutax.seedutils.util.math.Vec3i;

import java.util.*;
import java.util.stream.Collectors;

public class Searcher {
//KaptainWutax(Wat's "cool" meme?)vcera v 21:27
//7 minutes sounds like quite a lot
//for 9 million seeds
//    funky_face vcera v 21:28
//It's definitely not very optimized

    public static void searchStructureSeed(
            int blockSearchRadius, long structureSeed, Collection<StructureInfo<?, ?>> sList,
            Map<String, List<Biome>> bList, int biomeCheckSpacing) {
        Vec3i origin = new Vec3i(0, 0, 0);
        ChunkRand rand = new ChunkRand();

        Map<StructureInfo<?, ?>, List<CPos>> structures = new HashMap<>();
        for (var structureInfo : sList) {
            // here was code for stopping, but I just run it until it's killed
            RegionStructure<?, ?> structure = structureInfo.structure;
            RegionStructure.Data<?> lowerBound = structure.at(-blockSearchRadius >> 4, -blockSearchRadius >> 4);
            RegionStructure.Data<?> upperBound = structure.at(blockSearchRadius >> 4, blockSearchRadius >> 4);

            List<CPos> structPositions = new ArrayList<>();

            for (int regionX = lowerBound.regionX; regionX <= upperBound.regionX; regionX++) {
                for (int regionZ = lowerBound.regionZ; regionZ <= upperBound.regionZ; regionZ++) {
                    var struct = structure.getInRegion(structureSeed, regionX, regionZ, rand);
                    if (struct == null) continue;
                    if (struct.distanceTo(origin, DistanceMetric.EUCLIDEAN) > blockSearchRadius >> 4) continue;
                    structPositions.add(struct);
                }
            }
            // not enough structures in the region, this seed is not interesting, quitting
            if (structPositions.isEmpty() && structureInfo.required) return;
            structures.put(structureInfo, structPositions);
        }

        if (structures.size() != sList.size()) return;

        // 16 upper bits for biomes
        for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
            Main.LOGGER.info("will check upperBits: " + upperBits);
            long worldSeed = (upperBits << 48) | structureSeed;
            // here was code for stopping, but I just run it until it's killed

            Map<String, Double> structureDistances = new HashMap<>();
            for (var e : structures.entrySet()) {
                var structure = e.getKey();
                var positions = e.getValue();
                var source = Searcher.getBiomeSource(structure.getDimension(), worldSeed);
                var searchStructure = structure.structure;

                var minDistance = 10e9; // some big number, I don't want Double.MAX_VALUE
                for (var pos : positions) {
                    if (!searchStructure.canSpawn(pos.getX(), pos.getZ(), source)) continue;
                    var curDist = pos.toBlockPos().distanceTo(origin, DistanceMetric.EUCLIDEAN);
                    if (curDist < minDistance) minDistance = curDist;
                }
                structureDistances.put(structure.structName, minDistance);
            }

            Map<String, Double> biomeDistances = new HashMap<>();
            for (var e : bList.entrySet()) {
                var biomesName = e.getKey();
                var biomesList = e.getValue();

                if (biomesList.size() != 0) {
                    var biomePos = BiomeSearcher.distToAnyBiome(blockSearchRadius, worldSeed, biomesList, biomeCheckSpacing, rand);
                    if (biomePos == null) continue;     // returns null when no biome is found
                    var biomeDist = biomePos.distanceTo(origin, DistanceMetric.EUCLIDEAN);
                    biomeDistances.put(biomesName, biomeDist);
                }

                GlobalState.addSeed(new SeedResult(worldSeed, structureDistances, biomeDistances));
            }
        }
        // here was code for stopping, but I just run it until it's killed
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
