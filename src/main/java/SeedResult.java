import java.util.Map;

public class SeedResult {
    long seed;
    Map<String, Double> structureDistances;
    Map<String, Double> biomeDistances;

    public SeedResult(long seed, Map<String, Double> structureDistances, Map<String, Double> biomeDistances) {
        this.seed = seed;
        this.structureDistances = structureDistances;
        this.biomeDistances = biomeDistances;
    }

}
