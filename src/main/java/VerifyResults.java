import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.featureutils.structure.*;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.seed.WorldSeed;
import kaptainwutax.seedutils.util.math.DistanceMetric;
import kaptainwutax.seedutils.util.math.Vec3i;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VerifyResults {


    public static void fromCsv(Map<Long, double[]> seeds, String name) throws IOException {
        var out = new FileWriter(name);
        try (var printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(Main.HEADERS))) {
            for (var entry : seeds.entrySet()) {
                var seed = entry.getKey();
                var distances = entry.getValue();
                printer.printRecord(seed, distances[0], distances[1], distances[2], distances[3], distances[4]);
            }
        }
    }

    public static List<SeedResult> fromCsv(String name) throws IOException {
        Reader in = new FileReader(name);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
        List<SeedResult> results = new ArrayList<>();
        for (CSVRecord record : records) {
            results.add(new SeedResult(Long.parseLong(record.get("seed")),
                    Arrays.stream(Main.structNames).collect(Collectors.<String, String, Double>toMap(s ->s, s -> Double.valueOf(record.get(s)))),
                    Arrays.stream(Main.biomeNames).collect(Collectors.<String, String, Double>toMap(s->(String) s,s-> Double.valueOf(record.get(s))))
            ));
        }
        return results;
    }

    public static boolean checkSeedResult(SeedResult seedResult) {
        var worldSeed = seedResult.seed;
        var structureSeed = WorldSeed.toStructureSeed(worldSeed);
        Vec3i origin = new Vec3i(0, 0, 0);
        ChunkRand rand = new ChunkRand();
        var structures = Searcher.getStructuresPosList(structureSeed, ImmutableList.copyOf(Main.STRUCTURES), origin, rand);
        var result = Searcher.searchWorldSeed(Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS, worldSeed, structures, Main.ALL_OF_ANY_OF_BIOMES, Main.BIOME_SEARCH_SPACING, origin, rand);
        System.out.println("checking seed: " + worldSeed);
        System.out.println("expected: " + seedResult);
        System.out.println("expected: " + result);
        return seedResult.equals(result);
    }

    public static void main(String[] args) throws IOException {
        var results = fromCsv("old_multithread_seeds_2/distances_0_70.csv");
        for (var result : results) {
            var areSame = checkSeedResult(result);
            System.out.println("are same: " + areSame);
        }

        GlobalState.shutdown();
    }
}
