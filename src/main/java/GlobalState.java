import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

//todo: prolly use sth. like this to have centralized iteration over seeds
public class GlobalState {
    private static AtomicLong currentSeed = new AtomicLong(Main.STRUCTURE_SEED_MIN);
    private static List<SeedResult> foundSeeds = Collections.synchronizedList(new ArrayList<>());
    public static Executor OUTPUT_THREAD = Executors.newSingleThreadExecutor();

    public static void reset(){
        currentSeed.set(Main.STRUCTURE_SEED_MIN);
        Main.LOGGER.info("resetting global state, starting seed is : " + currentSeed.get());
    }

    public static long getNextSeed() {
        var nextSeed = currentSeed.incrementAndGet();
//        if (nextSeed % 10_000 == 0) {
        if (nextSeed % 1 == 0) {
            OUTPUT_THREAD.execute(()->Main.LOGGER.info("Searching seed: " + nextSeed));
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
            OUTPUT_THREAD.execute(()->Main.LOGGER.info("Found seeds: " + numResults));
        }
    }

    public static void resultsToCSV() {
        OUTPUT_THREAD.execute(()->{
            var copiedSeeds = new ArrayList<>(foundSeeds);
            try {
                Main.toCsv(copiedSeeds, "distances_" + Main.STRUCTURE_SEED_MIN + "_" + copiedSeeds.size() + ".csv");
            } catch (IOException e) {
                Main.LOGGER.warning("Failed to save the CSV file.");
                e.printStackTrace();
            }
        });
    }
}
