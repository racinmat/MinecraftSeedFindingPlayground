
struct ScaleLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)

    type::Type
end

ScaleLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
ScaleLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
ScaleLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
ScaleLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class ScaleLayer extends BiomeLayer {

    private final Type type;

    public ScaleLayer(MCVersion version, long worldSeed, long salt, Type type, BiomeLayer parent) {
        super(version, worldSeed, salt, parent);
        this.type = type;
    end

    function getType(this)::Type
        return this.type;
    end

    @Override
    function sample(this, x::Int32, y::Int32z::Int32)::Int32
        i = this.getParent().get(x >> 1, y, z >> 1);
        this.setSeed(x & -2, z & -2);
        xb = x & 1, zb = z & 1;

        if (xb == 0 && zb == 0) return i;

        l = this.getParent().get(x >> 1, y, (z + 1) >> 1);
        m = this.choose(i, l);

        if (xb == 0) return m;

        n = this.getParent().get((x + 1) >> 1, y, z >> 1);
        o = this.choose(i, n);

        if (zb == 0) return o;

        p = getParent().get((x + 1) >> 1, y, (z + 1) >> 1);
        return this.sample(i, n, l, p);
    end

    function sample(this, center::Int32, e::Int32, s::Int32se::Int32)::Int32
        ret = this.choose(center, e, s, se);

        if (this.type == Type.FUZZY) {
            return ret;
        end

        if (e == s && e == se) return e;
        if (center == e && (center == se || s != se)) return center;
        if (center == s && (center == se || e != se)) return center;
        if (center == se && e != s) return center;
        if (e == s && center != se) return e;
        if (e == se && center != s) return e;
        if (s == se && center != e) return s;
        return ret;
    end

    public enum Type {
        NORMAL, FUZZY
    end

}
