import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

//todo: prolly use sth. like this to have centralized iteration over seeds
public class GlobalState {
    private static final AtomicLong currentSeed = new AtomicLong(Main.STRUCTURE_SEED_MIN);
    private static final List<SeedResult> foundSeeds = Collections.synchronizedList(new ArrayList<>());
    public static ExecutorService OUTPUT_THREAD = Executors.newSingleThreadExecutor();

    public static void reset(){
        currentSeed.set(Main.STRUCTURE_SEED_MIN-1); //because I'm then calling getNextSeed, so I won't miss the first one
        Main.LOGGER.info("resetting global state, starting seed is : " + currentSeed.get());
    }

    public static void shutdown() {
        OUTPUT_THREAD.shutdown();
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
//        if (numResults % 100 == 0) {
        if (numResults % 10 == 0) {
//        if (numResults % 1 == 0) {
            OUTPUT_THREAD.execute(()->Main.LOGGER.info("Found seeds: " + numResults));
        }
//        if (numResults % 1_000 == 0) {
        if (numResults % 100 == 0) {
//        if (numResults % 1 == 0) {
            resultsToCSV();
        }
    }

    public static void resultsToCSV() {
        try {
            var curSeed = currentSeed.get();
            var copiedSeeds = new ArrayList<>(foundSeeds);  // yeah, I copy the data in worker thread, but whatever
            var fileName = "distances_" + Main.STRUCTURE_SEED_MIN + "_" + copiedSeeds.size() + ".csv";
            OUTPUT_THREAD.execute(()->{
                try {
                    Main.toCsv(copiedSeeds, fileName);
                    Main.writeFileSeed("last_seed.txt", curSeed);
                } catch (IOException e) {
                    Main.LOGGER.warning("Failed to save the CSV file.");
                    Main.LOGGER.warning(e.getMessage());
                    e.printStackTrace();
                }
                Main.LOGGER.info("Failed to save the CSV file named: " + fileName);
            });
        } catch (Exception e) {
            Main.LOGGER.warning("Some general error happened");
            Main.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
