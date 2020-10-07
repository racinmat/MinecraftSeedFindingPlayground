
struct SunflowerPlainsLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

SunflowerPlainsLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
SunflowerPlainsLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
SunflowerPlainsLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
SunflowerPlainsLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class SunflowerPlainsLayer extends BiomeLayer {

	public SunflowerPlainsLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(this, x::Int32, y::Int32z::Int32)::Int32
		this.setSeed(x, z);
		value = this.getParent().get(x, y, z);
		return value == Biome.PLAINS.getId() && this.nextInt(57) == 0 ? Biome.SUNFLOWER_PLAINS.getId() : value;
	end

}
