import java.util.concurrent.atomic.AtomicLong;

//todo: prolly use sth. like this to have centralized iteration over seeds
public class GlobalState {
    private static AtomicLong currentSeed = new AtomicLong(Main.STRUCTURE_SEED_MIN);

    public static void reset(){
        currentSeed.set(Main.STRUCTURE_SEED_MIN);
    }

    public static long getNextSeed() {
        return currentSeed.incrementAndGet();
    }

    public static long getCurrentSeed() {
        return currentSeed.get();
    }
}
