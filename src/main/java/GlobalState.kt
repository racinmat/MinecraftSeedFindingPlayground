import com.google.common.util.concurrent.AtomicLongMap
import kaptainwutax.biomeutils.Biome
import kaptainwutax.biomeutils.Stats
import kaptainwutax.biomeutils.StatsCallback
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

object GlobalState {
    private val currentSeed = AtomicLong(Main.STRUCTURE_SEED_MIN)
    private val foundSeeds = Collections.synchronizedList(ArrayList<SeedResult>())
    private val triedSeeds = AtomicLong(0)
    private val examinedSeeds = AtomicLong(0)

    var LOGGING = false

    @JvmField
    var OUTPUT_THREAD = Executors.newSingleThreadExecutor()

    @JvmStatic
    fun reset() {
        currentSeed.set(Main.STRUCTURE_SEED_MIN - 1) //because I'm then calling getNextSeed, so I won't miss the first one
        Main.LOGGER.info("resetting global state, starting seed is : ${currentSeed.get()}")
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

    fun getCurrentSeed(): Long {
        return currentSeed.get()
    }

    @JvmStatic
    fun addSeed(r: SeedResult) {
        foundSeeds.add(r)
        val numResults = foundSeeds.size
//        if (numResults % 10_000 == 0) {
        if (numResults % 1_000 == 0) {
//        if (numResults % 100 == 0) {
//        if (numResults % 10 == 0) {
//        if (numResults % 1 == 0) {
            OUTPUT_THREAD.execute { Main.LOGGER.info("Found seeds: $numResults") }
        }
//        if (numResults % 100_000 == 0) {
        if (numResults % 10_000 == 0) {
//        if (numResults % 1_000 == 0) {
//        if (numResults % 100 == 0) {
//        if (numResults % 10 == 0) {
//        if (numResults % 1 == 0) {
            resultsToCSV()
        }
    }

    @JvmStatic
    fun trySeed() {
        triedSeeds.incrementAndGet()
    }

    @JvmStatic
    fun examineSeed() {
        examinedSeeds.incrementAndGet()
    }

    @JvmStatic
    fun numTriedSeeds(): Long {
        return triedSeeds.get()
    }

    @JvmStatic
    fun numExaminedSeeds(): Long {
        return examinedSeeds.get()
    }

    fun printBiomeLayersStats() {
        Main.LOGGER.info("total branch 1: desert, savannah, plains ${Stats.getCount("branch1").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total branch 2: forest, daark forest, plains, mountains, birch forest, swamp ${Stats.getCount("branch2").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total branch 3: forest, mountains, taiga, plains ${Stats.getCount("branch3").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total branch 4: snowy tundra, snowy taiga ${Stats.getCount("branch4").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total mushroom branch ${Stats.getCount("shroom").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total giant taiga ${Stats.getCount("taiga").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total mesaa ${Stats.getCount("mesa").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total jungle ${Stats.getCount("jungle").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("total ocean or mushroom ${Stats.getCount("shroomOrOcean").toFloat() / Stats.getCount("total").toFloat() * 100}%")
        Main.LOGGER.info("")
        Main.LOGGER.info("###################CLIMATE#######################")
        Main.LOGGER.info("")
        Main.LOGGER.info("total Cold Forest ${Stats.getCount("coldForest").toFloat() / Stats.getCount("totalCold").toFloat() * 100}%")
        Main.LOGGER.info("total Cold Mountains ${Stats.getCount("coldMountains").toFloat() / Stats.getCount("totalCold").toFloat() * 100}%")
        Main.LOGGER.info("total Cold Plains ${Stats.getCount("coldPlains").toFloat() / Stats.getCount("totalCold").toFloat() * 100}%")
        Main.LOGGER.info("")
        Main.LOGGER.info("total Temp Normal ${Stats.getCount("tempNormal").toFloat() / Stats.getCount("totalTemp").toFloat() * 100}%")
        Main.LOGGER.info("total Temp Desert ${Stats.getCount("tempDesert").toFloat() / Stats.getCount("totalTemp").toFloat() * 100}%")
        Main.LOGGER.info("")
        Main.LOGGER.info("total Cool Normal ${Stats.getCount("coolNormal").toFloat() / Stats.getCount("totalCool").toFloat() * 100}%")
        Main.LOGGER.info("total Cool Mountains ${Stats.getCount("coolMountains").toFloat() / Stats.getCount("totalCool").toFloat() * 100}%")
        Main.LOGGER.info("")
        Main.LOGGER.info("total Special Normal ${Stats.getCount("specialNormal").toFloat() / Stats.getCount("totalSpe").toFloat() * 100}%")
        Main.LOGGER.info("total Special Spec ${Stats.getCount("specialSpe").toFloat() / Stats.getCount("totalSpe").toFloat() * 100}%")
        Main.LOGGER.info("")
        Main.LOGGER.info("###################MY WANTED SEEDS#######################")
        Main.LOGGER.info("Targeted ${Biome.DARK_FOREST.name} over total: ${foundSeeds.size / numExaminedSeeds().toFloat() * 100}% compared to standard deviated probability:${foundSeeds.size / numTriedSeeds().toFloat() * 100}%")
        Main.LOGGER.info("Found in total ${foundSeeds.size} viable seeds from ${numExaminedSeeds()} examined and ${numTriedSeeds()} tried.")
    }

    fun resultsToCSV() {
        try {
            val curSeed = currentSeed.get()
            val copiedSeeds = ArrayList(foundSeeds) // yeah, I copy the data in worker thread, but whatever
            val fileName = "distances_${Main.STRUCTURE_SEED_MIN}_${copiedSeeds.size}.csv"
            OUTPUT_THREAD.execute {
                try {
                    Main.toCsv(copiedSeeds, fileName)
                    Main.writeFileSeed("last_seed.txt", curSeed)
                    Main.LOGGER.info("Saved the CSV file named: $fileName")
                    Main.LOGGER.info("Found ${copiedSeeds.size} seeds.")
                    if (LOGGING) printBiomeLayersStats()
                } catch (e: IOException) {
                    Main.LOGGER.warning("Failed to save the CSV file: $fileName")
                    Main.LOGGER.warning(e.message)
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Main.LOGGER.warning("Some  general error happened")
            Main.LOGGER.warning(e.message)
            e.printStackTrace()
        }
    }

    // simple way to obain statistics
    private val messStats = AtomicLongMap.create<String>()
    fun incr(mess: String) {
        if (!LOGGING) return
        val c = messStats.incrementAndGet(mess)
        if (shouldLog(c)) {
            OUTPUT_THREAD.execute { Main.LOGGER.info("Times of message: $mess: $c") }
        }
    }

    init {
        Stats.statsCallback = StatsCallback { messStats, mess, c ->
            if (shouldLog(c)) {
                OUTPUT_THREAD.execute { Main.LOGGER.info("Times of message: $mess: $c") }
            }

        }
    }

    private fun shouldLog(c: Long) = c % 10.0.pow(max(log10(c.toDouble()).roundToInt(), 2)) == 0.0
}
