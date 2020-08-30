import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.EndBiomeSource;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.mc.seed.WorldSeed;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import kaptainwutax.seedutils.util.math.Vec3i;

import java.util.*;
import java.util.stream.Collectors;

public class Searcher {
//KaptainWutax(Wat's "cool" meme?)včera v 21:27
//7 minutes sounds like quite a lot
//for 9 million seeds
//    funky_facevčera v 21:28
//It's definitely not very optimized

    public static void searchStructureSeed(int blockSearchRadius, long structureSeed, Collection<StructureInfo<?, ?>> sList, Collection<Biome> bList, int biomeCheckSpacing) {
        Vec3i origin = new Vec3i(0, 0, 0);
        ChunkRand rand = new ChunkRand();
        int totalStructures = sList.size();

        Map<StructureInfo<?, ?>, List<CPos>> structures = new HashMap<>();
        sList = sList.stream().distinct().collect(Collectors.toList());
        for (var searchProvider : sList) {
            // here was code for stopping, but I just run it until it's killed
            RegionStructure<?, ?> searchStructure = searchProvider.structure;
            RegionStructure.Data<?> lowerBound = searchStructure.at(-blockSearchRadius >> 4, -blockSearchRadius >> 4);
            RegionStructure.Data<?> upperBound = searchStructure.at(blockSearchRadius >> 4, blockSearchRadius >> 4);

            List<CPos> foundStructures = new ArrayList<>();

            for (int regionX = lowerBound.regionX; regionX <= upperBound.regionX; regionX++) {
                for (int regionZ = lowerBound.regionZ; regionZ <= upperBound.regionZ; regionZ++) {
                    var struct = searchStructure.getInRegion(structureSeed, regionX, regionZ, rand);
                    if (struct == null) continue;
                    if (struct.distanceTo(origin, DistanceMetric.CHEBYSHEV) > blockSearchRadius >> 4) continue;
                    foundStructures.add(struct);
                }
            }
            if (foundStructures.size() < searchProvider.getMinOccurrences()) break;
            if (foundStructures.isEmpty()) break;
            structures.put(searchProvider, foundStructures);
        }

        // 16 upper bits for biomes
        for (long upperBits = 0; upperBits < 1L << 16; upperBits++) {
            long worldSeed = (upperBits << 48) | structureSeed;
            // here was code for stopping, but I just run it until it's killed

            int structureCount = 0;

            for (var e : structures.entrySet()) {
                var structure = e.getKey();
                var starts = e.getValue();
                var source = Searcher.getBiomeSource(e.getKey().getDimension(), worldSeed);
                var searchStructure = structure.structure;
                for (var start : starts) {
                    if (!searchStructure.canSpawn(start.getX(), start.getZ(), source)) continue;
                    structureCount++;
                    if (structureCount >= structure.getMinOccurrences()) {
                        break;
                    }
                }
            }
            if (structureCount != totalStructures) continue;

            if (bList.size() != 0) {
                ArrayList<Biome> bi = new ArrayList<>(bList);
                ArrayList<Biome> allBiomesFound = BiomeSearcher.findBiome(blockSearchRadius, worldSeed, bi, biomeCheckSpacing);
                if (allBiomesFound.size() != 0) continue;
            }

            GlobalState.addSeed(new SeedResult(worldSeed, null));
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
            default:
                System.out.println("USE OVERWORLD, NETHER, OR END");
                break;
        }

        return source;
    }
}
