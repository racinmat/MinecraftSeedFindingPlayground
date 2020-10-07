
struct DeepOceanLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

DeepOceanLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
DeepOceanLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
DeepOceanLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
DeepOceanLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class DeepOceanLayer extends CrossLayer {

	public DeepOceanLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(this, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
		if(!Biome.isShallowOcean(center)) {
			return center;
		end

		i = 0;
		if(Biome.isShallowOcean(n))i++;
		if(Biome.isShallowOcean(e))i++;
		if(Biome.isShallowOcean(w))i++;
		if(Biome.isShallowOcean(s))i++;

		if(i > 3) {
			if(center == Biome.WARM_OCEAN.getId())return Biome.DEEP_WARM_OCEAN.getId();
			if(center == Biome.LUKEWARM_OCEAN.getId())return Biome.DEEP_LUKEWARM_OCEAN.getId();
			if(center == Biome.OCEAN.getId())return Biome.DEEP_OCEAN.getId();
			if(center == Biome.COLD_OCEAN.getId())return Biome.DEEP_COLD_OCEAN.getId();
			if(center == Biome.FROZEN_OCEAN.getId())return Biome.DEEP_FROZEN_OCEAN.getId();
			return Biome.DEEP_OCEAN.getId();
		end

		return center;
	end

}
