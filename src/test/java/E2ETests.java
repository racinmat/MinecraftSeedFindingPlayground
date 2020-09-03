import com.google.common.collect.ImmutableList;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.seed.WorldSeed;
import kaptainwutax.seedutils.util.math.Vec3i;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class E2ETests {

    static Stream<Arguments> seedsProvider() {
        return Stream.of(
                Arguments.arguments(5920544660131938314L, 672.0, 864.0, 240.0, 688.0, 256.0, 928.0, 2000.0, 1088.0, 944.0, 208.0, 1344.0, 480.0, 768.0, 1152.0, 768.0, 384.0, 768.0),
                Arguments.arguments(-54887620458576219L, 784.0, 512.0, 288.0, 1344.0, 80.0, 1296.0, 912.0, 928.0, 1472.0, 320.0, 1440.0, 96.0, 768.0, 768.0, 768.0, 0.0, 1152.0),
                Arguments.arguments(-694117292568475871L, 880.0, 928.0, 208.0, 672.0, 272.0, 560.0, 1024.0, 1216.0, 1408.0, 96.0, 528.0, 80.0, 768.0, 768.0, 768.0, 384.0, 1152.0),
                Arguments.arguments(5189554145614366854L, 432.0, 1008.0, 256.0, 1296.0, 368.0, 1184.0, 1264.0, 1312.0, 1056.0, 96.0, 272.0, 640.0, 768.0, 768.0, 768.0, 384.0, 768.0),
                Arguments.arguments(-6360208573753980773L, 160.0, 1296.0, 544.0, 272.0, 416.0, 1376.0, 1728.0, 720.0, 1296.0, 80.0, 288.0, 208.0, 768.0, 1152.0, 768.0, 384.0, 1152.0),
                Arguments.arguments(113434415614396642L, 672.0, 1232.0, 288.0, 992.0, 160.0, 1184.0, 944.0, 1024.0, 1072.0, 160.0, 1312.0, 208.0, 768.0, 768.0, 768.0, 0.0, 1152.0),
                Arguments.arguments(-3092565569119973997L, 512.0, 1232.0, 528.0, 1008.0, 432.0, 1232.0, 528.0, 1328.0, 976.0, 272.0, 1200.0, 432.0, 1152.0, 768.0, 384.0, 384.0, 1152.0),
                Arguments.arguments(-8596808738696851733L, 768.0, 1056.0, 64.0, 656.0, 32.0, 800.0, 480.0, 1120.0, 512.0, 208.0, 1456.0, 144.0, 1152.0, 768.0, 768.0, 0.0, 384.0),
                Arguments.arguments(-8949778359492014136L, 400.0, 1264.0, 608.0, 1168.0, 352.0, 1360.0, 2000.0, 1392.0, 784.0, 288.0, 1488.0, 288.0, 1152.0, 1152.0, 1152.0, 384.0, 768.0),
                Arguments.arguments(2590977160621592647L, 224.0, 1456.0, 80.0, 1232.0, 304.0, 816.0, 1888.0, 640.0, 1184.0, 240.0, 944.0, 416.0, 768.0, 1152.0, 768.0, 0.0, 1152.0),
                Arguments.arguments(4253649848051438691L, 192.0, 784.0, 240.0, 640.0, 208.0, 704.0, 1744.0, 1136.0, 1232.0, 256.0, 320.0, 272.0, 768.0, 768.0, 768.0, 0.0, 1152.0),
                Arguments.arguments(-2617717283409095065L, 656.0, 1216.0, 192.0, 224.0, 176.0, 1216.0, 624.0, 832.0, 1168.0, 192.0, 832.0, 416.0, 768.0, 768.0, 768.0, 384.0, 768.0),
                Arguments.arguments(-4993929036800450820L, 560.0, 1008.0, 144.0, 1120.0, 112.0, 272.0, 688.0, 1264.0, 1296.0, 224.0, 1408.0, 208.0, 1152.0, 1152.0, 768.0, 0.0, 1152.0),
                Arguments.arguments(-6017935002073816534L, 400.0, 384.0, 224.0, 960.0, 160.0, 1344.0, 2000.0, 1376.0, 1056.0, 544.0, 992.0, 544.0, 1152.0, 768.0, 1152.0, 0.0, 768.0),
                Arguments.arguments(-3327597174673365489L, 672.0, 864.0, 80.0, 1312.0, 128.0, 544.0, 1632.0, 1312.0, 784.0, 384.0, 1344.0, 224.0, 768.0, 768.0, 384.0, 0.0, 1152.0),
                Arguments.arguments(-8293660188779468463L, 192.0, 240.0, 224.0, 528.0, 384.0, 704.0, 1984.0, 1168.0, 1104.0, 352.0, 1360.0, 464.0, 1152.0, 768.0, 768.0, 384.0, 768.0),
                Arguments.arguments(-3427239316428936559L, 768.0, 1376.0, 304.0, 1424.0, 416.0, 1296.0, 1664.0, 1168.0, 1040.0, 96.0, 704.0, 672.0, 1152.0, 1152.0, 1152.0, 384.0, 1152.0),
                Arguments.arguments(2696530276888095532L, 416.0, 1376.0, 208.0, 224.0, 96.0, 1408.0, 1024.0, 944.0, 864.0, 384.0, 752.0, 80.0, 768.0, 768.0, 384.0, 384.0, 768.0),
                Arguments.arguments(-3013471100664272075L, 240.0, 1264.0, 256.0, 624.0, 320.0, 720.0, 1696.0, 1328.0, 1472.0, 256.0, 192.0, 592.0, 1152.0, 1152.0, 1152.0, 384.0, 1152.0)
        );
    }
    @ParameterizedTest
    @MethodSource("seedsProvider")
    public void testGoodSeeds(long seed, double village, double swamp_hut, double shipwreck, double pillager_outpost,
                              double ocean_ruin, double monument, double mansion, double jungle_pyramid, double igloo,
                              double fortress_nether, double desert_pyramid, double buried_treasure, double jungles,
                              double mushrooms, double mesas, double oceans, double icy) {
        var structureSeed = WorldSeed.toStructureSeed(seed);
        Vec3i origin = new Vec3i(0, 0, 0);
        ChunkRand rand = new ChunkRand();
        var structures = Searcher.getStructuresPosList(structureSeed,
                ImmutableList.copyOf(Main.STRUCTURES), origin, rand);
        assertNotNull(structures);
        var seedResult = Searcher.searchWorldSeed(Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS, seed, structures,
                Main.ALL_OF_ANY_OF_BIOMES, Main.BIOME_SEARCH_SPACING, origin, rand);

        assertNotNull(seedResult);
        assertEquals(seedResult.seed, seed);
        assertEquals(seedResult.structureDistances.get("village"), village);
        assertEquals(seedResult.structureDistances.get("swamp_hut"), swamp_hut);
        assertEquals(seedResult.structureDistances.get("shipwreck"), shipwreck);
        assertEquals(seedResult.structureDistances.get("pillager_outpost"), pillager_outpost);
        assertEquals(seedResult.structureDistances.get("ocean_ruin"), ocean_ruin);
        assertEquals(seedResult.structureDistances.get("monument"), monument);
        assertEquals(seedResult.structureDistances.get("mansion"), mansion);
        assertEquals(seedResult.structureDistances.get("jungle_pyramid"), jungle_pyramid);
        assertEquals(seedResult.structureDistances.get("igloo"), igloo);
        assertEquals(seedResult.structureDistances.get("fortress_nether"), fortress_nether);
        assertEquals(seedResult.structureDistances.get("desert_pyramid"), desert_pyramid);
        assertEquals(seedResult.structureDistances.get("buried_treasure"), buried_treasure);
        assertEquals(seedResult.biomeDistances.get("jungles"), jungles);
        assertEquals(seedResult.biomeDistances.get("mushrooms"), mushrooms);
        assertEquals(seedResult.biomeDistances.get("mesas"), mesas);
        assertEquals(seedResult.biomeDistances.get("oceans"), oceans);
        assertEquals(seedResult.biomeDistances.get("icy"), icy);
    }

}
