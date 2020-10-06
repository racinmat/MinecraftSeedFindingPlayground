
struct IslandLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

public class IslandLayer extends CrossLayer {

	public IslandLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(self, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
		return Biome.applyAll(Biome::isShallowOcean, center, n, e, s, w)  && this.nextInt(2) == 0 ? Biome.PLAINS.getId() : center;
	end

}
