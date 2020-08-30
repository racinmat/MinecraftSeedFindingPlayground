import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.seedutils.mc.Dimension;

import java.util.ArrayList;
import java.util.Collection;

public class BiomeSearcher {

    public static ArrayList<Biome> findBiome(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int biomeCheckSpacing) {
        // Since I'm deleting out of the array to make sure we are checking everytime properly I am shallow copying the array
        ArrayList<Biome> biomesToFindCopy = new ArrayList<>(biomeToFind);
        //BiomeSource source = Searcher.getBiomeSource(dimension, worldSeed);
        //BiomeSource source = Searcher.getBiomeSource("OVERWORLD", worldSeed);
        BiomeSource source = Searcher.getBiomeSource(Dimension.OVERWORLD, worldSeed);
        BiomeSource source1 = Searcher.getBiomeSource(Dimension.NETHER, worldSeed);
        BiomeSource source2 = Searcher.getBiomeSource(Dimension.END, worldSeed);

        for(int i = -searchSize; i < searchSize; i += biomeCheckSpacing) {
            for(int j = -searchSize; j < searchSize; j += biomeCheckSpacing) {
                biomesToFindCopy.remove(source.getBiome(i, 0, j));
                biomesToFindCopy.remove(source1.getBiome(i, 0, j));
                biomesToFindCopy.remove(source2.getBiome(i, 0, j));

                if(biomesToFindCopy.isEmpty()) {
                    //System.out.format("Found world seed %d (Shadow %d)\n", worldSeed, WorldSeed.getShadowSeed(worldSeed));
                    return biomesToFindCopy;
                }
            }
        }

        return biomesToFindCopy;
    }

}
