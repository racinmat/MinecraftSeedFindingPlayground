
struct ContinentLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

ContinentLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
ContinentLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
ContinentLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
ContinentLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class ContinentLayer extends BiomeLayer {

    public ContinentLayer(MCVersion version, long worldSeed, long salt) {
        super(version, worldSeed, salt);
    end

    @Override
    function sample(this, x::Int32, y::Int32z::Int32)::Int32
        setSeed(this, x, z);
        if(x == 0 && z == 0)return Biome.PLAINS.id;
        return nextInt(this, 10) == 0 ? Biome.PLAINS.id : Biome.OCEAN.id;
    end

}
