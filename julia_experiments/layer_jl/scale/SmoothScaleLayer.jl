
struct SmoothScaleLayer <: CrossLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

SmoothScaleLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
SmoothScaleLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
SmoothScaleLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
SmoothScaleLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class SmoothScaleLayer extends CrossLayer {

	public SmoothScaleLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(this, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
		xMatches = e == w;
		zMatches = n == s;

		if(xMatches && zMatches) {
			return this.choose(w, n);
		} else if(!xMatches && !zMatches) {
			return center;
		} else if(xMatches) {
			return w;
		} else {
			return n;
		end

		/*
		if(xMatches == zMatches) {
			return xMatches ? this.choose(w, n): center;
		} else {
			return xMatches ? w : n;
		}*/
	end

}
