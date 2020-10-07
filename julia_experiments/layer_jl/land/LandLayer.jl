
struct LandLayer <: XCrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

LandLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
LandLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
LandLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
LandLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class LandLayer extends XCrossLayer {

    public LandLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
        super(version, worldSeed, salt, parent);
    end

    @Override
    function sample(this, sw::Int32, se::Int32, ne::Int32, nw::Int32center::Int32)::Int32
        if(!Biome.isShallowOcean(center) || Biome.applyAll(Biome::isShallowOcean, sw, se, ne, nw)) {
            if(Biome.isShallowOcean(center) || (Biome.applyAll(v -> !Biome.isShallowOcean(v), sw, se, ne, nw)) || nextInt(this, 5) != 0) {
                return center;
            end

            if(Biome.isShallowOcean(nw)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.id, nw);
            end

            if(Biome.isShallowOcean(sw)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.id, sw);
            end

            if(Biome.isShallowOcean(ne)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.id, ne);
            end

            if(Biome.isShallowOcean(se)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.id, se);
            end

            return center;
        end

        i = 1;
        j = 1;

        if(!Biome.isShallowOcean(nw) && nextInt(this, i++) == 0) {
            j = nw;
        end

        if(!Biome.isShallowOcean(ne) && nextInt(this, i++) == 0) {
            j = ne;
        end

        if(!Biome.isShallowOcean(sw) && nextInt(this, i++) == 0) {
            j = sw;
        end

        if(!Biome.isShallowOcean(se) && nextInt(this, i) == 0) {
            j = se;
        end

        if(nextInt(this, 3) == 0) {
            return j;
        end

        return j == Biome.FOREST.id ? Biome.FOREST.id : center;
    end

}
