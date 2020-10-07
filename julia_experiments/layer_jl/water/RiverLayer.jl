
struct RiverLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

RiverLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
RiverLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
RiverLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
RiverLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class RiverLayer extends BiomeLayer {

	public RiverLayer(MCVersion version, long worldSeed, long salt, BiomeLayer... parents) {
		super(version, worldSeed, salt, parents);
	end

	@Override
	function sample(this, x::Int32, y::Int32z::Int32)::Int32
		landStackCenter = this.getParent(0).get(x, y, z);
		noiseStackCenter = this.getParent(1).get(x, y, z);

		if(Biome.isOcean(landStackCenter))return landStackCenter;

		if(noiseStackCenter == Biome.RIVER.getId()) {
			if(landStackCenter == Biome.SNOWY_TUNDRA.getId()) {
				return Biome.FROZEN_RIVER.getId();
			} else {
				return landStackCenter != Biome.MUSHROOM_FIELDS.getId() && landStackCenter != Biome.MUSHROOM_FIELD_SHORE.getId() ? noiseStackCenter & 255 : Biome.MUSHROOM_FIELD_SHORE.getId();
			end
		end

		return landStackCenter;
	end

}
