
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
                return Biome.FOREST.getId();
            end
            if (i == 1) {
                Stats.incr("coldMountains");
                return Biome.MOUNTAINS.getId();
            }//<=1
            // >1
            Stats.incr("coldPlains");
            return Biome.PLAINS.getId();
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
            if (center == Biome.PLAINS.getId() && (n == Biome.MOUNTAINS.getId() || e == Biome.MOUNTAINS.getId()
                    || w == Biome.MOUNTAINS.getId() || s == Biome.MOUNTAINS.getId() || n == Biome.FOREST.getId()
                    || e == Biome.FOREST.getId() || w == Biome.FOREST.getId()
                    || s == Biome.FOREST.getId())) {
                Stats.incr("tempDesert");
                return Biome.DESERT.getId();
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
            if (center != Biome.FOREST.getId() || n != Biome.PLAINS.getId() && e != Biome.PLAINS.getId()
                    && w != Biome.PLAINS.getId() && s != Biome.PLAINS.getId() && n != Biome.DESERT.getId()
                    && e != Biome.DESERT.getId() && w != Biome.DESERT.getId()
                    && s != Biome.DESERT.getId()) {
                Stats.incr("coolNormal");
                return center;
            end
            else {
                Stats.incr("coolMountains");
                return Biome.MOUNTAINS.getId();
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
