import java.util.*
import java.util.concurrent.ConcurrentMap

data class SeedResult(
        var seed: Long,
        var structureDistances: ConcurrentMap<String, Double>,
        var biomeDistances: Map<String, Double>) {

    override fun toString(): String {
        return "SeedResult{" +
                "seed=$seed" +
                ", structureDistances=$structureDistances" +
                ", biomeDistances=$biomeDistances" +
                "}"
    }
}