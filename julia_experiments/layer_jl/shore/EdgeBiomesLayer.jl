
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

public class EdgeBiomesLayer extends CrossLayer {

    public EdgeBiomesLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
        super(version, worldSeed, salt, parent);
    end

    @Override
    function sample(self, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
        biome = Biome.REGISTRY.get(center);

        if (center == Biome.MUSHROOM_FIELDS.getId()) {
            if (Biome.applyAll((v) -> !Biome.isShallowOcean(v), n, e, s, w)) {
                return center;
            end
            return Biome.MUSHROOM_FIELD_SHORE.getId();
        } else if (biome != null && biome.getCategory() == Biome.Category.JUNGLE) {
            if (!(Biome.applyAll(EdgeBiomesLayer::isWooded, n, e, s, w))) {
                return Biome.JUNGLE_EDGE.getId();
            end
            if (Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w)) {
                return center;
            end
            return Biome.BEACH.getId();
        } else if (center != Biome.MOUNTAINS.getId() && center != Biome.WOODED_MOUNTAINS.getId() && center != Biome.MOUNTAIN_EDGE.getId()) {
            if (biome != null && biome.getPrecipitation() == Biome.Precipitation.SNOW) {
                if (!Biome.isOcean(center) && !(Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w))) {
                    return Biome.SNOWY_BEACH.getId();
                end
            } else if (center != Biome.BADLANDS.getId() && center != Biome.WOODED_BADLANDS_PLATEAU.getId()) {
                if (!Biome.isOcean(center) && center != Biome.RIVER.getId() && center != Biome.SWAMP.getId() && !(Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w))) {
                    return Biome.BEACH.getId();
                end
            } else if (Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w) && !(Biome.applyAll(EdgeBiomesLayer::isBadlands, n, e, s, w))) {
                return Biome.DESERT.getId();
            end
        } else if (!Biome.isOcean(center) && !(Biome.applyAll((v) -> !Biome.isOcean(v), n, e, s, w))) {
            return Biome.STONE_SHORE.getId();
        end

        return center;
    end

    function isWooded(self, id::Int32)::Bool
        b = Biome.REGISTRY.get(id);
        if (b != null && b.getCategory() == Biome.Category.JUNGLE) return true;
        return id == Biome.JUNGLE_EDGE.getId() || id == Biome.JUNGLE.getId() || id == Biome.JUNGLE_HILLS.getId() || id == Biome.FOREST.getId() || id == Biome.TAIGA.getId() || Biome.isOcean(id);
    end

    function isBadlands(self, id::Int32)::Bool
        return id == Biome.BADLANDS.getId() || id == Biome.WOODED_BADLANDS_PLATEAU.getId() || id == Biome.BADLANDS_PLATEAU.getId() || id == Biome.ERODED_BADLANDS.getId() || id == Biome.MODIFIED_WOODED_BADLANDS_PLATEAU.getId() || id == Biome.MODIFIED_BADLANDS_PLATEAU.getId();
    end

}
