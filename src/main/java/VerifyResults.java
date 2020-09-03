import com.google.common.collect.ImmutableList;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.seed.WorldSeed;
import kaptainwutax.seedutils.util.math.Vec3i;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
                    Arrays.stream(Main.STRUCT_NAMES).collect(Collectors.<String, String, Double>toConcurrentMap(s ->s, s -> Double.valueOf(record.get(s)))),
                    Arrays.stream(Main.BIOME_NAMES).collect(Collectors.<String, String, Double>toConcurrentMap(s->(String) s, s-> Double.valueOf(record.get(s))))
            ));
        }
        return results;
    }

    public static SeedResult evalSeed(long worldSeed) {
        var structureSeed = WorldSeed.toStructureSeed(worldSeed);
        Vec3i origin = new Vec3i(0, 0, 0);
        ChunkRand rand = new ChunkRand();
        var structures = Searcher.getStructuresPosList(structureSeed, ImmutableList.copyOf(Main.STRUCTURES), origin, rand);
        return Searcher.searchWorldSeed(Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS, worldSeed, structures, Main.ALL_OF_ANY_OF_BIOMES, Main.BIOME_SEARCH_SPACING, origin, rand);
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

    public static void fixResults() throws IOException {
        var resDir = new File("good_seeds");
        var resultFiles = resDir.listFiles((File f, String name)->name.matches("distances_\\d+_\\d+.csv"));
        assert resultFiles != null;
        for (var fromFile : resultFiles) {
            var resStruct = fromCsv("good_seeds/"+fromFile.getName());

            Main.toCsv(resStruct.stream().map(s->evalSeed(s.seed)).collect(Collectors.toList()), "good_seeds/"+fromFile.getName().split("\\.")[0]+"_fixed.csv");
        }
    }
    public static void main(String[] args) throws IOException {
//        for (var seed : new long[]{2590977160621592647L,
//                8494351847174180868L,
//                -7931401893752860728L}) {
//            System.out.println(WorldSeed.toStructureSeed(seed));
//        }

//        fixResults();
//        var results = fromCsv("old_multithread_seeds_2/distances_0_70.csv");
////        var results = fromCsv("broken_small/distances_0_10.csv");
//        var results = fromCsv("good_seeds/distances_0_10.csv");
        var results = fromCsv("distances_0_70.csv");
//        var results = fromCsv("distances_4168_50.csv");
//        var results = fromCsv("distances_8449_50.csv");
//        var results = fromCsv("good_seeds/distances_4168_50_fixed.csv");
        for (var result : results) {
            var areSame = checkSeedResult(result);
            System.out.println("are same: " + areSame);
        }
//
//        GlobalState.shutdown();
    }
}
