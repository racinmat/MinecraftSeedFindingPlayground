struct EndBiomeLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

EndBiomeLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
EndBiomeLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
EndBiomeLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
EndBiomeLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class EndBiomeLayer extends BiomeLayer {

    public EndBiomeLayer(MCVersion version, long worldSeed, BiomeLayer parent) {
        super(version, parent);
    end

    @Override
    function sample(this, x::Int32, y::Int32z::Int32)::Int32
        x >>= 2;
        z >>= 2;

        if((long)x * (long)x + (long)z * (long)z <= 4096L) {
            return Biome.THE_END.getId();
        end

        height = Float.intBitsToFloat(this.getParent().get(x * 2 + 1, 0, z * 2 + 1));

        if(height > 40.0F) {
            return Biome.END_HIGHLANDS.getId();
        } else if(height >= 0.0F) {
            return Biome.END_MIDLANDS.getId();
        } else if(height >= -20.0F) {
            return Biome.END_BARRENS.getId();
        end

        return Biome.SMALL_END_ISLANDS.getId();
    end



}
