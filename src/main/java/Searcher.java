import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.EndBiomeSource;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.CPos;
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

    public static void searchRandomly(int searchSize, long startSeedStructure, Collection<StructureInfo<?, ?>> sList, Collection<Biome> bList, Dimension dimension, int incrementer, int biomePrecision) {
        Vec3i origin = new Vec3i(0, 0,0);
        ChunkRand rand = new ChunkRand();
        int totalStructures = sList.size();

        Map<StructureInfo<?, ?>, List<CPos>> structures = new HashMap<>();
        sList = sList.stream().distinct().collect(Collectors.toList());
        for(long structureSeed = startSeedStructure; structureSeed < 1L << 48; structureSeed++, structures.clear()) {
            for(StructureInfo<?, ?> searchProvider: sList) {
                // here was code for stopping, but I just run it until it's killed

                RegionStructure<?,?> searchStructure = searchProvider.structure;
                RegionStructure.Data<?> lowerBound = searchStructure.at(-searchSize >> 4, -searchSize >> 4);
                RegionStructure.Data<?> upperBound = searchStructure.at(searchSize >> 4, searchSize >> 4);

                List<CPos> foundStructures = new ArrayList<>();

                for(int regionX = lowerBound.regionX; regionX <= upperBound.regionX; regionX++) {
                    for(int regionZ = lowerBound.regionZ; regionZ <= upperBound.regionZ; regionZ++) {
                        CPos struct = searchStructure.getInRegion(structureSeed, regionX, regionZ, rand);
                        if(struct == null)continue;
                        if(struct.distanceTo(origin, DistanceMetric.CHEBYSHEV) > searchSize >> 4)continue;
                        foundStructures.add(struct);
                    }
                }
                if (foundStructures.size() < searchProvider.getMinOccurrences()) break;
                if(foundStructures.isEmpty())break;
                structures.put(searchProvider, foundStructures);
            }

            //System.out.println(structures.size() + " " + sList.size());

            //System.out.println("Found structure seed " + structureSeed + ", checking biomes...");

            for(long upperBits = 0; upperBits < 1L << biomePrecision; upperBits++) {
                long worldSeed = (upperBits << 48) | structureSeed;
                // here was code for stopping, but I just run it until it's killed

                int structureCount = 0;

                for(Map.Entry<StructureInfo<?, ?>, List<CPos>> e : structures.entrySet()) {
                    StructureInfo<?, ?> structure = e.getKey();
                    List<CPos> starts = e.getValue();
                    BiomeSource source = Searcher.getBiomeSource(e.getKey().getDimension(), worldSeed);
                    RegionStructure<?,?> searchStructure = structure.structure;
                    for(CPos start : starts) {
                        if(!searchStructure.canSpawn(start.getX(), start.getZ(), source))continue;
                        structureCount++;
                        if(structureCount >= structure.getMinOccurrences()){
                            break;
                        }
                    }
                }
                if(structureCount != totalStructures)continue;

                if(bList.size() != 0){
                    ArrayList<Biome> bi = new ArrayList<>(bList);
                    ArrayList<Biome> allBiomesFound = BiomeSearcher.findBiome(searchSize, worldSeed, bi, incrementer);
                    if(allBiomesFound.size() != 0)continue;
                }

                if(Main.SEARCH_SHADOW){
                    //todo: add logging
//                    util.console(String.valueOf(worldSeed) + " (Shadow: " + WorldSeed.getShadowSeed(worldSeed) + " )");
                } else {
                    //todo: add logging
//                    util.console(String.valueOf(worldSeed));
                }
            }
            // here was code for stopping, but I just run it until it's killed
        }
    }

    public static BiomeSource getBiomeSource(Dimension dimension, long worldSeed) {
        BiomeSource source = null;

        switch(dimension){
            case OVERWORLD:
                if(Main.WORLD_TYPE == Main.WorldType.LARGE_BIOMES){
                    source = new OverworldBiomeSource(Main.VERSION, worldSeed, 6 ,4);
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
