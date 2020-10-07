
struct EndSimplexLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)

    SIMPLEX_SKIP::LCG = LCG.JAVA.combine(17292);
    simplex::SimplexNoiseSampler
end

EndSimplexLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
EndSimplexLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
EndSimplexLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
EndSimplexLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class EndSimplexLayer extends BiomeLayer {

    SIMPLEX_SKIP = LCG.JAVA.combine(17292);
    protected final SimplexNoiseSampler simplex;

    public EndSimplexLayer(MCVersion version, long worldSeed) {
        super(version);
        rand = new JRand(worldSeed);
        rand.advance(SIMPLEX_SKIP);
        this.simplex = new SimplexNoiseSampler(rand);
    end

    @Override
    function sample(this, x::Int32, y::Int32z::Int32)::Int32
        return this.simplex.sample2D(x, z) < -0.8999999761581421D ? 1 : 0;
    end

}
