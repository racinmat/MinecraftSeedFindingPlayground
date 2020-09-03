import GlobalState.getCurrentSeed
import GlobalState.nextSeed
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import kaptainwutax.biomeutils.Biome

class SearchingThread(private val structures: ImmutableList<StructureInfo<*, *>>, private val biomes: ImmutableMap<String, ImmutableList<Biome>>) : Thread(), Runnable {
    override fun run() {
        searching()
    }

    private fun searching() {
        var structureSeed: Long = 0
        while (Main.STRUCTURE_SEED_MAX > getCurrentSeed()) {

            // I want to do search in a range of seeds so I can iteratively scan different ranges
            if (structureSeed >= Main.STRUCTURE_SEED_MAX) {
                break
            }
            structureSeed = nextSeed
            Searcher.searchStructureSeed(Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS, structureSeed, structures, biomes, Main.BIOME_SEARCH_SPACING)
        }
    }
}