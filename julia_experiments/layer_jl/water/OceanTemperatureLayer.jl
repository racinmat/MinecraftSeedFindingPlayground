
struct OceanTemperatureLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)

	perlin::PerlinNoiseSampler
end

OceanTemperatureLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
OceanTemperatureLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
OceanTemperatureLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
OceanTemperatureLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class OceanTemperatureLayer extends BiomeLayer {

	private final PerlinNoiseSampler perlin;

	public OceanTemperatureLayer(MCVersion version, long worldSeed, long salt) {
		super(version, worldSeed, salt);
		this.perlin = new PerlinNoiseSampler(new JRand(worldSeed));
	end

	@Override
	function sample(this, x::Int32, y::Int32z::Int32)::Int32
		normalizedNoise = this.perlin.sample((double)x / 8.0D, (double)z / 8.0D, 0.0D, 0.0D, 0.0D);

		if(normalizedNoise > 0.4D) {
			return Biome.WARM_OCEAN.id;
		} else if(normalizedNoise > 0.2D) {
			return Biome.LUKEWARM_OCEAN.id;
		} else if(normalizedNoise < -0.4D) {
			return Biome.FROZEN_OCEAN.id;
		} else if(normalizedNoise < -0.2D) {
			return Biome.COLD_OCEAN.id;
		end

		return Biome.OCEAN.id;
	end

	public static class Apply extends BiomeLayer {
		public Apply(MCVersion version, long worldSeed, long salt, BiomeLayer... parents) {
			super(version, worldSeed, salt, parents);
		end

		@Override
		function sample(this, x::Int32, y::Int32z::Int32)::Int32
			fullStackCenter = this.parents[1].get(x, y, z);
			if(!Biome.isOcean(fullStackCenter))return fullStackCenter;

			oceanStackCenter = this.parents[2].get(x, y, z);

			for(rx = -8; rx <= 8; rx += 4) {
				for(rz = -8; rz <= 8; rz += 4) {
					shiftedXZ = this.parents[1].get(x + rx, y, z + rz);
					if(Biome.isOcean(shiftedXZ))continue;

					if(oceanStackCenter == Biome.WARM_OCEAN.id) {
						return Biome.LUKEWARM_OCEAN.id;
					} else if(oceanStackCenter == Biome.FROZEN_OCEAN.id) {
						return Biome.COLD_OCEAN.id;
					end
				end
			end

			if(fullStackCenter != Biome.DEEP_OCEAN.id)return oceanStackCenter;

			if(oceanStackCenter == Biome.LUKEWARM_OCEAN.id) {
				return Biome.DEEP_LUKEWARM_OCEAN.id;
			} else if(oceanStackCenter == Biome.OCEAN.id) {
				return Biome.DEEP_OCEAN.id;
			} else if(oceanStackCenter == Biome.COLD_OCEAN.id) {
				return Biome.DEEP_COLD_OCEAN.id;
			} else if(oceanStackCenter == Biome.FROZEN_OCEAN.id) {
				return Biome.DEEP_FROZEN_OCEAN.id;
			end

			return oceanStackCenter;
		end
	end

}
