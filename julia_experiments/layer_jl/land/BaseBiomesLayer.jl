
struct BaseBiomesLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

BaseBiomesLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
BaseBiomesLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
BaseBiomesLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
BaseBiomesLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

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
    end

    @Override
    function sample(this, x::Int32, y::Int32z::Int32)::Int32
        setSeed(this, x, z);
        center = this.parents[1].get(x, y, z);
        specialBits = (center >> 8) & 15; //the nextInt(15) + 1 in ClimateLayer.Special
        center &= ~0xF00; //removes the 4 special bits and keeps everything else
        Stats.incr("total");
        if(Biome.isOcean(center) || center == Biome.MUSHROOM_FIELDS.id) {
            Stats.incr("shroomOrOcean");
            return center;
        end

        if(center == Biome.PLAINS.id) {
            Stats.incr("branch1");
            if(specialBits > 0) {
                Stats.incr("mesa");
                return nextInt(this, 3) == 0 ? Biome.BADLANDS_PLATEAU.id : Biome.WOODED_BADLANDS_PLATEAU.id;
            end

            return DRY_BIOMES[nextInt(this, DRY_BIOMES.length)].id;
        } else if(center == Biome.DESERT.id) {
            Stats.incr("branch2");
            if(specialBits > 0) {
                Stats.incr("jungle");
                return Biome.JUNGLE.id;
            end

            return TEMPERATE_BIOMES[nextInt(this, TEMPERATE_BIOMES.length)].id; //nextInt(6)=1
        } else if(center == Biome.MOUNTAINS.id) {
            Stats.incr("branch3");
            if(specialBits > 0) {
                Stats.incr("taiga");
                return Biome.GIANT_TREE_TAIGA.id;
            end

            return COOL_BIOMES[nextInt(this, COOL_BIOMES.length)].id;
        } else if(center == Biome.FOREST.id) {
            Stats.incr("branch4");
            return SNOWY_BIOMES[nextInt(this, SNOWY_BIOMES.length)].id;
        end
        Stats.incr("shroom");
        return Biome.MUSHROOM_FIELDS.id;
    end

}
