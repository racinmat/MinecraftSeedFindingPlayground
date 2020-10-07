
struct EdgeBiomesLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

EdgeBiomesLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
EdgeBiomesLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
EdgeBiomesLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
EdgeBiomesLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class EdgeBiomesLayer extends CrossLayer {

    public EdgeBiomesLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
        super(version, worldSeed, salt, parent);
    end

    @Override
    function sample(this, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
        biome = Biome.REGISTRY.get(center);

        if (center == Biome.MUSHROOM_FIELDS.id) {
            if (Biome.applyAll((v) -> !Biome.isShallowOcean(v), n, e, s, w)) {
                return center;
            end
            return Biome.MUSHROOM_FIELD_SHORE.id;
        } else if (biome != null && biome.getCategory() == Biome.JUNGLE_CAT) {
            if (!(Biome.applyAll(EdgeBiomesLayer::isWooded, n, e, s, w))) {
                return Biome.JUNGLE_EDGE.id;
            end
            if (Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w)) {
                return center;
            end
            return Biome.BEACH.id;
        } else if (center != Biome.MOUNTAINS.id && center != Biome.WOODED_MOUNTAINS.id && center != Biome.MOUNTAIN_EDGE.id) {
            if (biome != null && biome.getPrecipitation() == Biome.SNOW) {
                if (!Biome.isOcean(center) && !(Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w))) {
                    return Biome.SNOWY_BEACH.id;
                end
            } else if (center != Biome.BADLANDS.id && center != Biome.WOODED_BADLANDS_PLATEAU.id) {
                if (!Biome.isOcean(center) && center != Biome.RIVER.id && center != Biome.SWAMP.id && !(Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w))) {
                    return Biome.BEACH.id;
                end
            } else if (Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w) && !(Biome.applyAll(EdgeBiomesLayer::isBadlands, n, e, s, w))) {
                return Biome.DESERT.id;
            end
        } else if (!Biome.isOcean(center) && !(Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w))) {
            return Biome.STONE_SHORE.id;
        end

        return center;
    end

    function isWooded(this, id::Int32)::Bool
        b = Biome.REGISTRY.get(id);
        if (b != null && b.getCategory() == Biome.JUNGLE_CAT) return true;
        return id == Biome.JUNGLE_EDGE.id || id == Biome.JUNGLE.id || id == Biome.JUNGLE_HILLS.id || id == Biome.FOREST.id || id == Biome.TAIGA.id || Biome.isOcean(id);
    end

    function isBadlands(this, id::Int32)::Bool
        return id == Biome.BADLANDS.id || id == Biome.WOODED_BADLANDS_PLATEAU.id || id == Biome.BADLANDS_PLATEAU.id || id == Biome.ERODED_BADLANDS.id || id == Biome.MODIFIED_WOODED_BADLANDS_PLATEAU.id || id == Biome.MODIFIED_BADLANDS_PLATEAU.id;
    end

}
