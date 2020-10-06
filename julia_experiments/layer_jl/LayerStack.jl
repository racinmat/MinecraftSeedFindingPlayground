
struct LayerStack{T} <:Vector{T} where {T<:BiomeLayer}
	layerIdCounter::Int32
end

LayerStack{T}() <:Vector{T} where {T<:BiomeLayer} = LayerStack{T}(0)

public  class LayerStack<T extends BiomeLayer> extends ArrayList<T> {

	layerIdCounter = 0;

	@Override
	function add(self, layer::T)::Bool
		layer.setLayerId(this.layerIdCounter++);
		return super.add(layer);
	end

	function setScales(self)::Nothing
		this.setRecursiveScale(this.get(this.size() - 1), 1);
	end

	function setRecursiveScale(self, last::BiomeLayerscale::Int32)::Nothing
		if(last == null)return;
		max = 0;

		for(BiomeLayer biomeLayer: last.getParents()) {
			shift = 0;
			if(last instanceof ScaleLayer)shift = 1;
			else if(last instanceof VoronoiLayer)shift = 2;

			this.setRecursiveScale(biomeLayer, scale << shift);
			max = Math.max(max, scale);
		end

		last.setScale(max);
	end

}
