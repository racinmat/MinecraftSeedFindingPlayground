import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

//todo: prolly use sth. like this to have centralized iteration over seeds
public class GlobalState {
    private static AtomicLong currentSeed = new AtomicLong(Main.STRUCTURE_SEED_MIN);
    private static Collection<SeedResult> foundSeeds = Collections.synchronizedCollection(new ArrayList<>());


    public static void reset(){
        currentSeed.set(Main.STRUCTURE_SEED_MIN);
    }

    public static long getNextSeed() {
        var nextSeed = currentSeed.incrementAndGet();
        if (nextSeed % 10_000 == 0) {
            Main.LOGGER.info("Searching seed: " + nextSeed);
        }
        return nextSeed;
    }

    public static long getCurrentSeed() {
        return currentSeed.get();
    }

    public static void addSeed(SeedResult r) {
        foundSeeds.add(r);
        var numResults = foundSeeds.size();
        if (numResults % 10_000 == 0) {
            Main.LOGGER.info("Found seeds: " + numResults);
        }
    }
}
