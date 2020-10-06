
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

public class SunflowerPlainsLayer extends BiomeLayer {

	public SunflowerPlainsLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(self, x::Int32, y::Int32z::Int32)::Int32
		this.setSeed(x, z);
		value = this.getParent().get(x, y, z);
		return value == Biome.PLAINS.getId() && this.nextInt(57) == 0 ? Biome.SUNFLOWER_PLAINS.getId() : value;
	end

}
