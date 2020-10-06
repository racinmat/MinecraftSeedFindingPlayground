
struct NetherLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    temperature::DoublePerlinNoiseSampler
    humidity::DoublePerlinNoiseSampler
    altitude::DoublePerlinNoiseSampler
    weirdness::DoublePerlinNoiseSampler

    biomePoints::Vector{<:MixedNoisePoint}
    is3D::Bool

end

public class NetherLayer extends BiomeLayer {

    private DoublePerlinNoiseSampler temperature;
    private DoublePerlinNoiseSampler humidity;
    private DoublePerlinNoiseSampler altitude;
    private DoublePerlinNoiseSampler weirdness;

    private final MixedNoisePoint[] biomePoints;
    private final boolean is3D;

    public NetherLayer(MCVersion version, long worldSeed, boolean is3D, MixedNoisePoint[] biomePoints) {
        super(version, (BiomeLayer)null);
        this.is3D = is3D;

        if(this.getVersion().isNewerOrEqualTo(MCVersion.v1_16)) {
            this.temperature = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed), IntStream.rangeClosed(-7, -6));
            this.humidity = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 1L), IntStream.rangeClosed(-7, -6));
            this.altitude = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 2L), IntStream.rangeClosed(-7, -6));
            this.weirdness = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 3L), IntStream.rangeClosed(-7, -6));
        end

        this.biomePoints = biomePoints;
    end

    @Override
    function sample(self, x::Int32, y::Int32z::Int32)::Int32
        if(this.getVersion().isOlderThan(MCVersion.v1_16))return Biome.NETHER_WASTES.getId();

        y = this.is3D ? y : 0;

        MixedNoisePoint point = new MixedNoisePoint(null,
                (float)this.temperature.sample(x, y, z),
                (float)this.humidity.sample(x, y, z),
                (float)this.altitude.sample(x, y, z),
                (float)this.weirdness.sample(x, y, z), 0.0F);

        return Stream.of(this.biomePoints).min(Comparator.comparing(m -> m.distanceTo(point)))
                .map(MixedNoisePoint::getBiome).orElse(Biome.THE_VOID).getId();
    end

}
