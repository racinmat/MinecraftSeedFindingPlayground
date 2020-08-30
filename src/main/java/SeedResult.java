import java.util.Map;

public class SeedResult {
    long seed;
    Map<String, Double> structureDistances;

    public SeedResult(long seed, Map<String, Double> structureDistances) {
        this.seed = seed;
        this.structureDistances = structureDistances;
    }
}
