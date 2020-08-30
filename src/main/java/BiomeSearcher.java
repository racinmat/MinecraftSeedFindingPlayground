import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.pos.BPos;

import java.util.*;

public class BiomeSearcher {

    //26.7 s, 53.5% of time
    public static BPos distToAnyBiomeKaptainWutax(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int biomeCheckSpacing, BiomeSource source, JRand rand) {
        return source.locateBiome(0, 0, 0, searchSize, biomeCheckSpacing, biomeToFind, rand, true);
    }

    // kaptain adviced me to implement my own https://discordapp.com/channels/505310901461581824/532998733135085578/749728029520953374
    public static BPos distToAnyBiomeMine(int searchSize, long worldSeed, Collection<Biome> biomeToFind, int biomeCheckSpacing, BiomeSource source, JRand rand) {
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
