
struct DeepOceanLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64
end

public class DeepOceanLayer extends CrossLayer {

	public DeepOceanLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(self, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
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
