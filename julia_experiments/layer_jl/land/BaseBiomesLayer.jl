package kaptainwutax.biomeutils.layer.land;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.Stats;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.seedutils.mc.MCVersion;

public class BaseBiomesLayer extends BiomeLayer {

    public static final Biome[] DRY_BIOMES = new Biome[] {
            Biome.DESERT, Biome.DESERT, Biome.DESERT, Biome.SAVANNA, Biome.SAVANNA, Biome.PLAINS
    };

    public static final Biome[] TEMPERATE_BIOMES = new Biome[] {
            Biome.FOREST, Biome.DARK_FOREST, Biome.MOUNTAINS, Biome.PLAINS, Biome.BIRCH_FOREST, Biome.SWAMP
    };

    public static final Biome[] COOL_BIOMES = new Biome[] {
            Biome.FOREST, Biome.MOUNTAINS, Biome.TAIGA, Biome.PLAINS
    };

    public static final Biome[] SNOWY_BIOMES = new Biome[] {
            Biome.SNOWY_TUNDRA, Biome.SNOWY_TUNDRA, Biome.SNOWY_TUNDRA, Biome.SNOWY_TAIGA
    };

    public BaseBiomesLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
        super(version, worldSeed, salt, parent);
    }

    @Override
    public int sample(int x, int y, int z) {
        this.setSeed(x, z);
        int center = this.getParent().get(x, y, z);
        int specialBits = (center >> 8) & 15; //the nextInt(15) + 1 in ClimateLayer.Special
        center &= ~0xF00; //removes the 4 special bits and keeps everything else
        Stats.incr("total");
        if(Biome.isOcean(center) || center == Biome.MUSHROOM_FIELDS.getId()) {
            Stats.incr("shroomOrOcean");
            return center;
        }

        if(center == Biome.PLAINS.getId()) {
            Stats.incr("branch1");
            if(specialBits > 0) {
                Stats.incr("mesa");
                return this.nextInt(3) == 0 ? Biome.BADLANDS_PLATEAU.getId() : Biome.WOODED_BADLANDS_PLATEAU.getId();
            }

            return DRY_BIOMES[this.nextInt(DRY_BIOMES.length)].getId();
        } else if(center == Biome.DESERT.getId()) {
            Stats.incr("branch2");
            if(specialBits > 0) {
                Stats.incr("jungle");
                return Biome.JUNGLE.getId();
            }

            return TEMPERATE_BIOMES[this.nextInt(TEMPERATE_BIOMES.length)].getId(); //nextInt(6)=1
        } else if(center == Biome.MOUNTAINS.getId()) {
            Stats.incr("branch3");
            if(specialBits > 0) {
                Stats.incr("taiga");
                return Biome.GIANT_TREE_TAIGA.getId();
            }

            return COOL_BIOMES[this.nextInt(COOL_BIOMES.length)].getId();
        } else if(center == Biome.FOREST.getId()) {
            Stats.incr("branch4");
            return SNOWY_BIOMES[this.nextInt(SNOWY_BIOMES.length)].getId();
        }
        Stats.incr("shroom");
        return Biome.MUSHROOM_FIELDS.getId();
    }

}
