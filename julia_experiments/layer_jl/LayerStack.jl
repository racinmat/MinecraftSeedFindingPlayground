
struct LayerStack{T} <:Vector{T} where {T<:BiomeLayer}
	layerIdCounter::Int32
end

LayerStack{T}(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
LayerStack{T}(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
LayerStack{T}(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
LayerStack{T}(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

LayerStack{T}() <:Vector{T} where {T<:BiomeLayer} = LayerStack{T}(0)

public  class LayerStack<T extends BiomeLayer> extends ArrayList<T> {

	layerIdCounter = 0;

	@Override
	function add(this, layer::T)::Bool
		layer.setLayerId(this.layerIdCounter++);
		return super.add(layer);
	end

	function setScales(this)::Nothing
		setRecursiveScale(this, get(this, size(this, ) - 1), 1);
	end

	function setRecursiveScale(this, last::BiomeLayerscale::Int32)::Nothing
		if(last == null)return;
		max = 0;

		for(BiomeLayer biomeLayer: last.getParents()) {
			shift = 0;
			if(last instanceof ScaleLayer)shift = 1;
			else if(last instanceof VoronoiLayer)shift = 2;

			setRecursiveScale(this, biomeLayer, scale << shift);
			max = Math.max(max, scale);
		end

		last.setScale(max);
	end

}
