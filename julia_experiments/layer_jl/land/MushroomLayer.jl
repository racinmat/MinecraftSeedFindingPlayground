
struct MushroomLayer <: XCrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

public class MushroomLayer extends XCrossLayer {

	public MushroomLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(self, sw::Int32, se::Int32, ne::Int32, nw::Int32center::Int32)::Int32
		return Biome.applyAll(Biome::isShallowOcean, center, sw, se, ne, nw)
				&& this.nextInt(100) == 0 ? Biome.MUSHROOM_FIELDS.getId() : center;
	end

}
