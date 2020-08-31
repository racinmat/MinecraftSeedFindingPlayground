import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kaptainwutax.biomeutils.Biome;

import java.util.List;

public class SearchingThread extends Thread implements Runnable {

    private final ImmutableList<StructureInfo<?, ?>> structures;
    private final ImmutableMap<String, List<Biome>> biomes;
    private final int blockSearchRadius;

    public SearchingThread(int blockSearchRadius, ImmutableList<StructureInfo<?, ?>> structures, ImmutableMap<String, List<Biome>> biomes) {
        this.structures = structures;
        this.biomes = biomes;
        this.blockSearchRadius = blockSearchRadius;
    }

    @Override
    public void run() {
        searching();
    }

    private void searching() {
        long structureSeed = 0;
        while (Main.STRUCTURE_SEED_MAX > GlobalState.getCurrentSeed()) {

            // I want to do search in a range of seeds so I can iteratively scan different ranges
            if (structureSeed >= Main.STRUCTURE_SEED_MAX) {
                break;
            }

            structureSeed = GlobalState.getNextSeed();
            Searcher.searchStructureSeed(blockSearchRadius, structureSeed, structures, biomes, Main.BIOME_SEARCH_SPACING);
        }
    }
}
