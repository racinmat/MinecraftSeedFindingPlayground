import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.BPos;

import java.util.*;

public class BiomeSearcher {

    public static ArrayList<Biome> findBiome(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int biomeCheckSpacing) {
        // Since I'm deleting out of the array to make sure we are checking everytime properly I am shallow copying the array
        ArrayList<Biome> biomesToFindCopy = new ArrayList<>(biomeToFind);
        //BiomeSource source = Searcher.getBiomeSource(dimension, worldSeed);
        //BiomeSource source = Searcher.getBiomeSource("OVERWORLD", worldSeed);
        BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
        BiomeSource source1 = Searcher.getBiomeSource(Dimension.NETHER, worldSeed);
        BiomeSource source2 = Searcher.getBiomeSource(Dimension.END, worldSeed);

        for (int i = -searchSize; i < searchSize; i += biomeCheckSpacing) {
            for (int j = -searchSize; j < searchSize; j += biomeCheckSpacing) {
                biomesToFindCopy.remove(source.getBiome(i, 0, j));
                biomesToFindCopy.remove(source1.getBiome(i, 0, j));
                biomesToFindCopy.remove(source2.getBiome(i, 0, j));

                if (biomesToFindCopy.isEmpty()) {
                    //System.out.format("Found world seed %d (Shadow %d)\n", worldSeed, WorldSeed.getShadowSeed(worldSeed));
                    return biomesToFindCopy;
                }
            }
        }

        return biomesToFindCopy;
    }

    public static boolean containsAllBiomes(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int biomeCheckSpacing) {
        // Since I'm deleting out of the array to make sure we are checking everytime properly I am shallow copying the array
        Set<Biome> biomesToFindCopy = new HashSet<>(biomeToFind);
        // I know I don't care about nether and end biomes.
        var source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);

        for (int i = -searchSize; i < searchSize; i += biomeCheckSpacing) {
            for (int j = -searchSize; j < searchSize; j += biomeCheckSpacing) {
                biomesToFindCopy.remove(source.getBiome(i, 0, j));

                if (biomesToFindCopy.isEmpty()) {
                    //System.out.format("Found world seed %d (Shadow %d)\n", worldSeed, WorldSeed.getShadowSeed(worldSeed));
                    return true;
                }
            }
        }

        return false;
    }

    //26.7 s, 53.5% of time
    public static BPos distToAnyBiomeKaptainWutax(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int biomeCheckSpacing, JRand rand) {
        var source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
        return source.locateBiome(0, 0, 0, searchSize, biomeCheckSpacing, biomeToFind, rand, true);
    }

    // kaptain adviced me to implement my own https://discordapp.com/channels/505310901461581824/532998733135085578/749728029520953374
    public static BPos distToAnyBiomeMine(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int biomeCheckSpacing, JRand rand) {
        var i = 0;
        var source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
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
