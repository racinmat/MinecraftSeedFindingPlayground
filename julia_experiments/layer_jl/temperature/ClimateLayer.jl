
struct ColdClimateLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

ColdClimateLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
ColdClimateLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
ColdClimateLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
ColdClimateLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

struct TemperateClimateLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

struct CoolClimateLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

struct SpecialClimateLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

public class ClimateLayer {
    public static class Cold extends BiomeLayer {
        public Cold(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
            super(version, worldSeed, salt, parent);
        end

        @Override
        function sample(this, x::Int32, y::Int32z::Int32)::Int32
            value = this.parents[1].get(x, y, z);
            Stats.incr("totalCold");
            if (Biome.isShallowOcean(value)) {
                return value;
            end

            setSeed(this, x, z);
            i = nextInt(this, 6);
            if (i == 0) {
                Stats.incr("coldForest");
                return Biome.FOREST.id;
            end
            if (i == 1) {
                Stats.incr("coldMountains");
                return Biome.MOUNTAINS.id;
            }//<=1
            // >1
            Stats.incr("coldPlains");
            return Biome.PLAINS.id;
        end
    end

    public static class Temperate extends CrossLayer {
        public Temperate(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
            super(version, worldSeed, salt, parent);
        end

        @Override
        function sample(this, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
            Stats.incr("totalTemp");
            // escape this one needs plains on center
            // and either mountains or forest on one of the side
            if (center == Biome.PLAINS.id && (n == Biome.MOUNTAINS.id || e == Biome.MOUNTAINS.id
                    || w == Biome.MOUNTAINS.id || s == Biome.MOUNTAINS.id || n == Biome.FOREST.id
                    || e == Biome.FOREST.id || w == Biome.FOREST.id
                    || s == Biome.FOREST.id)) {
                Stats.incr("tempDesert");
                return Biome.DESERT.id;
            end
            Stats.incr("tempNormal");
            return center;
        end
    end

    public static class Cool extends CrossLayer {
        public Cool(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
            super(version, worldSeed, salt, parent);
        end

        @Override
        function sample(this, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
            Stats.incr("totalCool");
            if (center != Biome.FOREST.id || n != Biome.PLAINS.id && e != Biome.PLAINS.id
                    && w != Biome.PLAINS.id && s != Biome.PLAINS.id && n != Biome.DESERT.id
                    && e != Biome.DESERT.id && w != Biome.DESERT.id
                    && s != Biome.DESERT.id) {
                Stats.incr("coolNormal");
                return center;
            end
            else {
                Stats.incr("coolMountains");
                return Biome.MOUNTAINS.id;
            end
        end
    end

    public static class Special extends BiomeLayer {
        public Special(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
            super(version, worldSeed, salt, parent);
        end

        @Override
        function sample(this, x::Int32, y::Int32z::Int32)::Int32
            i = this.parents[1].get(x, y, z);
            Stats.incr("totalSpe");
            if (Biome.isShallowOcean(i)) {
                return i;
            end
            setSeed(this, x, z);

            if (nextInt(this, 13) == 0) {
                i |= (1 + nextInt(this, 15)) << 8;
                Stats.incr("specialSpe");
            end
            else{
                Stats.incr("specialNormal");
            end
            return i;
        end
    end

}
