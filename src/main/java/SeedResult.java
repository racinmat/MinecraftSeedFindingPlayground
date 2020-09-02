import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

public class SeedResult {
    long seed;
    ConcurrentMap<String, Double> structureDistances;
    ConcurrentMap<String, Double> biomeDistances;

    public SeedResult(long seed, ConcurrentMap<String, Double> structureDistances, ConcurrentMap<String, Double> biomeDistances) {
        this.seed = seed;
        this.structureDistances = structureDistances;
        this.biomeDistances = biomeDistances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeedResult that = (SeedResult) o;
        return seed == that.seed &&
                Objects.equals(structureDistances, that.structureDistances) &&
                Objects.equals(biomeDistances, that.biomeDistances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seed, structureDistances, biomeDistances);
    }

    @Override
    public String toString() {
        return "SeedResult{" +
                "seed=" + seed +
                ", structureDistances=" + structureDistances +
                ", biomeDistances=" + biomeDistances +
                '}';
    }
}
