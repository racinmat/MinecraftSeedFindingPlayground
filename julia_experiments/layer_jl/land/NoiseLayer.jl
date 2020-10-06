
struct NoiseLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

public class NoiseLayer extends BiomeLayer {

	public NoiseLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(self, x::Int32, y::Int32z::Int32)::Int32
		this.setSeed(x, z);
		i = this.getParent().get(x, y, z);
		return Biome.isShallowOcean(i) ? i : this.nextInt(299999) + 2;
	end

}
