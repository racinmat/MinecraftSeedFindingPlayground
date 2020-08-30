import kaptainwutax.biomeutils.Biome;
import kaptainwutax.seedutils.mc.Dimension;

import java.io.IOException;
import java.util.*;

public class SearchingThread extends Thread implements Runnable{

    private long startSeedStructure;
    private StructureInfo<?, ?>[] structures;
    private Map<String, List<Biome>> biomes;
    private int blockSearchRadius;

    public SearchingThread(long startSeedStructure, int blockSearchRadius, StructureInfo<?, ?>[] structures, Map<String, List<Biome>> biomes) {
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
            Main.LOGGER.warning("IO Exception");
        } catch (InterruptedException e) {
            Main.LOGGER.warning("Interupeted");
        }
        Main.LOGGER.info("Ended");
    }

    private void searching() throws IOException, InterruptedException {
        long structureSeed = 0;
        while ( Main.STRUCTURE_SEED_MAX > GlobalState.getCurrentSeed()) {

            // I want to do search in a range of seeds so I can iteratively scan different ranges
            if(structureSeed >= Main.STRUCTURE_SEED_MAX){
                break;
            }

            structureSeed = GlobalState.getNextSeed();
            //Make sure to create new copies everytime so it doesnt give false positives
            var si = Arrays.asList(this.structures);
//            Searcher.searchStructureSeed(blockSearchRadius, structureSeed, si, biomes, Main.BIOME_SEARCH_SPACING);
        }
    }
}
