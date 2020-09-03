import java.util.*
import java.util.concurrent.ConcurrentMap

class SeedResult(var seed: Long, var structureDistances: ConcurrentMap<String, Double>, var biomeDistances: Map<String, Double>) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as SeedResult
        return seed == that.seed &&
                structureDistances == that.structureDistances &&
                biomeDistances == that.biomeDistances
    }

    override fun hashCode(): Int {
        return Objects.hash(seed, structureDistances, biomeDistances)
    }

    override fun toString(): String {
        return "SeedResult{" +
                "seed=$seed" +
                ", structureDistances=$structureDistances" +
                ", biomeDistances=$biomeDistances" +
                "}"
    }
}