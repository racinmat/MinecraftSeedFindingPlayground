
struct EaseEdgeLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

EaseEdgeLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
EaseEdgeLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
EaseEdgeLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
EaseEdgeLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class EaseEdgeLayer extends CrossLayer {

    public EaseEdgeLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
        super(version, worldSeed, salt, parent);
    end

    @Override
    function sample(this, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
        int[] is = new int[1];
        if (!replaceEdgeIfNeeded(this, is, n, e, s, w, center, Biome.MOUNTAINS, Biome.MOUNTAIN_EDGE) &&
                replaceEdge(this, is, n, e, s, w, center, Biome.WOODED_BADLANDS_PLATEAU, Biome.BADLANDS) &&
                replaceEdge(this, is, n, e, s, w, center, Biome.BADLANDS_PLATEAU, Biome.BADLANDS) &&
                replaceEdge(this, is, n, e, s, w, center, Biome.GIANT_TREE_TAIGA, Biome.TAIGA)) {

            if (center == Biome.DESERT.id && anyMatch(Biome.SNOWY_TUNDRA, n, e, w, s)) {
                return Biome.WOODED_MOUNTAINS.id;
            } else {
                if (center == Biome.SWAMP.id) {
                    if (anyMatch(Biome.DESERT, n, e, w, s) || anyMatch(Biome.SNOWY_TUNDRA, n, e, w, s) || anyMatch(Biome.SNOWY_TAIGA, n, e, w, s)) {
                        return Biome.PLAINS.id;
                    end

                    if (anyMatch(Biome.JUNGLE, n, e, w, s) || anyMatch(Biome.BAMBOO_JUNGLE, n, e, w, s)) {
                        return Biome.JUNGLE_EDGE.id;
                    end
                end

                return center;
            end
        end

        return is[0];
    end

    public static boolean anyMatch(Biome biome, int... values) {
        for (int value : values) {
            if (value == biome.id) return true;
        end
        return false;
    end

    private boolean replaceEdgeIfNeeded(int[] is, int i, int j, int k, int l, int m, Biome n, Biome o) {
        if (!Biome.areSimilar(m, n)) {
            return false;
        } else {
            if (canBeNeighbors(this, i, n) && canBeNeighbors(this, j, n) && canBeNeighbors(this, l, n) && canBeNeighbors(this, k, n)) {
                is[0] = m;
            } else {
                is[0] = o.id;
            end
            return true;
        end
    end

    private boolean replaceEdge(int[] is, int i, int j, int k, int l, int m, Biome n, Biome o) {
        if (m != n.id) return true;

        if (Biome.areSimilar(i, n) && Biome.areSimilar(j, n) && Biome.areSimilar(l, n) && Biome.areSimilar(k, n)) {
            is[0] = m;
        } else {
            is[0] = o.id;
        end
        return false;
    end

    function canBeNeighbors(this, id::Int32b2::Biome)::Bool
        if (Biome.areSimilar(id, b2)) return true;

        biome = Biome.REGISTRY.get(id);

        if (biome != null && b2 != null) {
            Biome.t = biome.getTemperatureGroup();
            Biome.t2 = b2.getTemperatureGroup();
            return t == t2 || t == Biome.Temperature.MEDIUM || t2 == Biome.Temperature.MEDIUM;
        end

        return false;
    end

}
