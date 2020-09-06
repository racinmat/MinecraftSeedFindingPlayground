import com.google.common.util.concurrent.AtomicLongMap
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.*

object GlobalState {
    private val currentSeed = AtomicLong(Main.STRUCTURE_SEED_MIN)
    private val foundSeeds = Collections.synchronizedList(ArrayList<SeedResult>())
    @JvmField
    var OUTPUT_THREAD = Executors.newSingleThreadExecutor()
    @JvmStatic
    fun reset() {
        currentSeed.set(Main.STRUCTURE_SEED_MIN - 1) //because I'm then calling getNextSeed, so I won't miss the first one
        Main.LOGGER.info("resetting global state, starting seed is : " + currentSeed.get())
    }

    @JvmStatic
    fun shutdown() {
        OUTPUT_THREAD.shutdown()
    }

    //        if (nextSeed % 10_000 == 0) {
    @JvmStatic
    val nextSeed: Long
        get() {
            val nextSeed = currentSeed.incrementAndGet()
            //        if (nextSeed % 10_000 == 0) {
            if (nextSeed % 1 == 0L) {
                OUTPUT_THREAD.execute { Main.LOGGER.info("Searching seed: $nextSeed") }
            }
            return nextSeed
        }

    @JvmStatic
    fun getCurrentSeed(): Long {
        return currentSeed.get()
    }

    @JvmStatic
    fun addSeed(r: SeedResult) {
        foundSeeds.add(r)
        val numResults = foundSeeds.size
        //        if (numResults % 100 == 0) {
//        if (numResults % 10 == 0) {
        if (numResults % 1 == 0) {
            OUTPUT_THREAD.execute { Main.LOGGER.info("Found seeds: $numResults") }
        }
        //        if (numResults % 1_000 == 0) {
//        if (numResults % 100 == 0) {
        if (numResults % 10 == 0) {
//        if (numResults % 1 == 0) {
            resultsToCSV()
        }
    }

    fun resultsToCSV() {
        try {
            val curSeed = currentSeed.get()
            val copiedSeeds = ArrayList(foundSeeds) // yeah, I copy the data in worker thread, but whatever
            val fileName = "distances_" + Main.STRUCTURE_SEED_MIN + "_" + copiedSeeds.size + ".csv"
            OUTPUT_THREAD.execute {
                try {
                    Main.toCsv(copiedSeeds, fileName)
                    Main.writeFileSeed("last_seed.txt", curSeed)
                    Main.LOGGER.info("Saved the CSV file named: $fileName")
                } catch (e: IOException) {
                    Main.LOGGER.warning("Failed to save the CSV file: $fileName")
                    Main.LOGGER.warning(e.message)
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Main.LOGGER.warning("Some general error happened")
            Main.LOGGER.warning(e.message)
            e.printStackTrace()
        }
    }

    // simple way to obain statistics
    private val messStats = AtomicLongMap.create<String>()
    fun incr(mess: String) {
        val c = messStats.incrementAndGet(mess)
        if (c % 10.0.pow(log10(c.toDouble()).roundToInt()) == 0.0) {
//        if(c % 10_000 == 0) {
            OUTPUT_THREAD.execute { Main.LOGGER.info("Times of message: $mess: $c") }
        }
    }
}