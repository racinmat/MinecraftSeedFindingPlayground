import kaptainwutax.biomeutils.Biome;
import kaptainwutax.seedutils.mc.Dimension;

import java.io.IOException;
import java.util.*;

public class SearchingThread extends Thread implements Runnable{

    private long startSeedStructure;
    private StructureInfo<?, ?>[] structures;
    private List<Biome> biomes;
    private int blockSearchRadius;

    public SearchingThread(long startSeedStructure, int blockSearchRadius, StructureInfo<?, ?>[] structures, List<Biome> biomes) {
        this.startSeedStructure = startSeedStructure;
        this.structures = structures;
        this.biomes = biomes;
        this.blockSearchRadius = blockSearchRadius;
    }

    @Override
    public void run() {
        /*
         * - Create the appropriate searching lists
         * - Determine which searching functions to use based on lists
         * - check that all values are being passed up correctly to variables
         * - Profit (Run this as many times to speed up searching within computer limits ofc)
         */
        try {
            searching();
        } catch (IOException e) {
            System.out.println("IO Exception");
        } catch (InterruptedException e) {
            System.out.println("Interupeted");
        }
    }

    private void searching() throws IOException, InterruptedException {
        long structureSeed = 0;
        //todo: need to use searcher, so I'll use the code for randomized search, in distributed manner iterate over structure seeds, but bruteforce biome part locally
        while ( Main.STRUCTURE_SEED_MAX > GlobalState.getCurrentSeed()) {

            // I want to do search in a range of seeds so I can iteratively scan different ranges
            if(structureSeed >= Main.STRUCTURE_SEED_MAX){
                break;
            }

            structureSeed = GlobalState.getNextSeed();

            //Make sure to create new copies everytime so it doesnt give false positives
            var si = Arrays.asList(this.structures);
            var bi = new ArrayList<>(this.biomes);
            Searcher.searchStructureSeed(blockSearchRadius, structureSeed, si, bi, Main.BIOME_SEARCH_SPACING);
            if (si.size() != 0) {
                si = StructureSearcher.findStructure(blockSearchRadius, structureSeed, si);
                if (si.size() != 0) continue;
            }
            if (bi.size() != 0) {
                bi = BiomeSearcher.findBiome(blockSearchRadius, structureSeed, bi, Main.BIOME_SEARCH_SPACING);
                if (bi.size() != 0) continue;
            }

            if (si.size() == 0 && bi.size() == 0) {
                //gather found seed
                //print out the world seed (Plus possibly more information)
            } else {
                System.out.println("Failed");
            }
        }
    }
}
