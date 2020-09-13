import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.Stats;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.layer.land.BaseBiomesLayer;
import kaptainwutax.biomeutils.layer.land.ContinentLayer;
import kaptainwutax.biomeutils.layer.temperature.ClimateLayer;
import kaptainwutax.biomeutils.source.BiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.seedutils.mc.MCVersion;


public class Test {
    public final static MCVersion VERSION = MCVersion.v1_16;
    public final static OverworldBiomeSource SOURCE = new OverworldBiomeSource(VERSION, 0L);
    public final static BiomeLayer CONTINENT = getLayer(ContinentLayer.class);
    public final static BiomeLayer COLD = getLayer(ClimateLayer.Cold.class);
    public final static BiomeLayer BASEBIOMES = getLayer(BaseBiomesLayer.class);
    public final static BiomeLayer SPECIAL = getLayer(ClimateLayer.Special.class);

    public static BiomeLayer getLayer(Class<? extends BiomeLayer> layerClass) {
        for (int i = 0; i < SOURCE.getLayers().size(); i++) {
            if (SOURCE.getLayer(i).getClass().equals(layerClass)) return SOURCE.getLayer(i);
        }
        return SOURCE.voronoi;
    }

    public static BiomeLayer getLayer(Class<? extends BiomeLayer> layerClass, OverworldBiomeSource source) {
        for (int i = 0; i < source.getLayers().size(); i++) {
            if (source.getLayer(i).getClass().equals(layerClass)) return source.getLayer(i);
        }
        return source.voronoi;
    }

    public static long getLocalSeed(BiomeLayer biomeLayer, long seed, int posX, int posZ) {
        long layerSeed = BiomeLayer.getLayerSeed(seed, biomeLayer.salt);
        return BiomeLayer.getLocalSeed(layerSeed, posX, posZ);
    }

    public static void main(String[] args) {
        Biome biome = Biome.DARK_FOREST;
        int count = 0;
        int count2 = 0;
        int[][] center = {{0, 0}};
        int[][] outer = {{0, 1}, {0, -1},
                {-1, 0}, {1, 0}};
        for (long seed = 0; seed < Long.MAX_VALUE; seed++) {

            long localSeed;

            // force plains at 0 0; 0 1; 1 0; 1 1
            boolean bad = false;
            for (int[] pos : center) {
                // INCREASE PLAINS AT CENTER
                localSeed = getLocalSeed(CONTINENT, seed, pos[0], pos[1]);
                if (Math.floorMod(localSeed >> 24, 10) != 0) { // if ocean
                    bad = true;
                    break;
                }
                // MAKE SURE COLD GIVE US PLAINS
                localSeed = getLocalSeed(COLD, seed, pos[0], pos[1]);
                if (Math.floorMod(localSeed >> 24, 6) <= 1) { // forest or mountains
                    bad = true;
                    break;
                }

                // BASE BIOME CHOOSE DARK ROLL
                localSeed = getLocalSeed(BASEBIOMES, seed, pos[0], pos[1]);
                if (Math.floorMod(localSeed >> 24, 6) != 1) { // Not dark forest
                    bad = true;
                    break;
                }
                // MAKE SURE SPECIAL DOESNT INTERFERE
                localSeed = getLocalSeed(SPECIAL, seed, pos[0], pos[1]);
                if (Math.floorMod(localSeed >> 24, 13) == 0) { // is a special one ;)
                    bad = true;
                    break;
                }
            }
            if (bad) continue;

            // force mountains or forest on outer
            for (int[] pos : outer) {
                // MAKE SURE AROUND PLAINS THERE IS SURROUNDING MOUNTAINS OR FOREST
                localSeed = getLocalSeed(COLD, seed, pos[0], pos[1]);
                if (Math.floorMod(localSeed >> 24, 6) > 1) { // plains
                    bad = true;
                    break;
                }
            }
            if (bad) continue;


            // force forest or mountains at 0

            count++;
            OverworldBiomeSource source = new OverworldBiomeSource(MCVersion.v1_16, seed, 4, 4);

            if (source.getBiome(0, 0, 0) == biome) {
                System.out.println(seed);
                count2++;
            }
            if (seed > 1500000) {
                System.out.println("total branch 1: desert, savannah, plains " + Stats.getCount("branch1") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total branch 2: forest, daark forest, plains, mountains, birch forest, swamp " + Stats.getCount("branch2") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total branch 3: forest, mountains, taiga, plains " + Stats.getCount("branch3") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total branch 4: snowy tundra, snowy taiga " + Stats.getCount("branch4") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total mushroom branch " + Stats.getCount("shroom") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total giant taiga " + Stats.getCount("taiga") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total mesaa " + Stats.getCount("mesa") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total jungle " + Stats.getCount("jungle") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println("total ocean or mushroom " + Stats.getCount("shroomOrOcean") / (float) Stats.getCount("total") * 100 + "%");
                System.out.println();
                System.out.println("###################CLIMATE#######################3");
                System.out.println();
                System.out.println("total Cold Forest " + Stats.getCount("coldForest") / (float) Stats.getCount("totalCold") * 100 + "%");
                System.out.println("total Cold Mountains " + Stats.getCount("coldMountains") / (float) Stats.getCount("totalCold") * 100 + "%");
                System.out.println("total Cold Plains " + Stats.getCount("coldPlains") / (float) Stats.getCount("totalCold") * 100 + "%");
                System.out.println();
                System.out.println("total Temp Normal " + Stats.getCount("tempNormal") / (float) Stats.getCount("totalTemp") * 100 + "%");
                System.out.println("total Temp Desert " + Stats.getCount("tempDesert") / (float) Stats.getCount("totalTemp") * 100 + "%");
                System.out.println();
                System.out.println("total Cool Normal " + Stats.getCount("coolNormal") / (float) Stats.getCount("totalCool") * 100 + "%");
                System.out.println("total Cool Mountains " + Stats.getCount("coolMountains") / (float) Stats.getCount("totalCool") * 100 + "%");
                System.out.println();
                System.out.println("total Special Normal " + Stats.getCount("specialNormal") / (float) Stats.getCount("totalSpe") * 100 + "%");
                System.out.println("total Special Spec " + Stats.getCount("specialSpe") / (float) Stats.getCount("totalSpe") * 100 + "%");
                System.out.println();
                System.out.println("###################BIOME#######################3");
                System.out.println("Targeted " + biome.getName() + " over total: " + count2 / (float) count * 100 + "% compared to standard deviated probability:" + count2 / (float) seed * 100 + "%");

                break;
            }
        }
    }
}