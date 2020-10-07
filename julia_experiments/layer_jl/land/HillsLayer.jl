
struct HillsLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

HillsLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
HillsLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
HillsLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
HillsLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class HillsLayer extends BiomeLayer {

	public HillsLayer(MCVersion version, long worldSeed, long salt, BiomeLayer... parents) {
		super(version, worldSeed, salt, parents);
	end

	@Override
	function sample(this, x::Int32, y::Int32z::Int32)::Int32
		setSeed(this, x, z);
		i = this.parents[1].get(x, y, z); // biomes
		j = this.parents[2].get(x, y, z); // noise (river)

		k = (j - 2) % 29;
		Biome biome3;

		if(!Biome.isShallowOcean(i) && j >= 2 && k == 1) {
			biome = Biome.REGISTRY.get(i);

			if(biome == null || !biome.hasParent()) {
				biome3 = biome == null ? null : biome.getChild();
				return biome3 == null ? i : biome3.id;
			end
		end

		if(nextInt(this, 3) == 0 || k == 0) {
			l = i;
			if (i == Biome.DESERT.id) {
				l = Biome.DESERT_HILLS.id;
			} else if(i == Biome.FOREST.id) {
				l = Biome.WOODED_HILLS.id;
			} else if(i == Biome.BIRCH_FOREST.id) {
				l = Biome.BIRCH_FOREST_HILLS.id;
			} else if(i == Biome.DARK_FOREST.id) {
				l = Biome.PLAINS.id;
			} else if(i == Biome.TAIGA.id) {
				l = Biome.TAIGA_HILLS.id;
			} else if(i == Biome.GIANT_TREE_TAIGA.id) {
				l = Biome.GIANT_TREE_TAIGA_HILLS.id;
			} else if(i == Biome.SNOWY_TAIGA.id) {
				l = Biome.SNOWY_TAIGA_HILLS.id;
			} else if(i == Biome.PLAINS.id) {
				l = nextInt(this, 3) == 0 ? Biome.WOODED_HILLS.id : Biome.FOREST.id;
			} else if(i == Biome.SNOWY_TUNDRA.id) {
				l = Biome.SNOWY_MOUNTAINS.id;
			} else if(i == Biome.JUNGLE.id) {
				l = Biome.JUNGLE_HILLS.id;
			} else if(i == Biome.BAMBOO_JUNGLE.id) {
				l = Biome.BAMBOO_JUNGLE_HILLS.id;
			} else if(i == Biome.OCEAN.id) {
				l = Biome.DEEP_OCEAN.id;
			} else if(i == Biome.LUKEWARM_OCEAN.id) {
				l = Biome.DEEP_LUKEWARM_OCEAN.id;
			} else if(i == Biome.COLD_OCEAN.id) {
				l = Biome.DEEP_COLD_OCEAN.id;
			} else if(i == Biome.FROZEN_OCEAN.id) {
				l = Biome.DEEP_FROZEN_OCEAN.id;
			} else if(i == Biome.MOUNTAINS.id) {
				l = Biome.WOODED_MOUNTAINS.id;
			} else if(i == Biome.SAVANNA.id) {
				l = Biome.SAVANNA_PLATEAU.id;
			} else if(Biome.areSimilar(i, Biome.WOODED_BADLANDS_PLATEAU)) {
				l = Biome.BADLANDS.id;
			end
			// in 1.12 this check is only for DEEP_OCEAN but since the other can't spawn, its ok
			else if((i == Biome.DEEP_OCEAN.id || i == Biome.DEEP_LUKEWARM_OCEAN.id
					|| i == Biome.DEEP_COLD_OCEAN.id || i == Biome.DEEP_FROZEN_OCEAN.id)
					&& nextInt(this, 3) == 0) {
				l = nextInt(this, 2) == 0 ? Biome.PLAINS.id : Biome.FOREST.id;
			end

			if(k == 0 && l != i) {
				biome3 = Biome.REGISTRY.get(l).getChild();
				l = biome3 == null ? i : biome3.id;
			end

			if(l != i) {
				m = 0;
				b = Biome.REGISTRY.get(i);
				if(Biome.areSimilar(this.parents[1].get(x, y,z - 1), b))m++;
				if(Biome.areSimilar(this.parents[1].get(x + 1, y, z), b))m++;
				if(Biome.areSimilar(this.parents[1].get(x - 1, y, z), b))m++;
				if(Biome.areSimilar(this.parents[1].get(x, y,z + 1), b))m++;
				if(m >= 3)return l;
			end
		end

		return i;
	end
}
