@enum Category begin
	NONE
	TAIGA_CAT
	EXTREME_HILLS
    JUNGLE_CAT
	MESA
	PLAINS_CAT
	SAVANNA_CAT
    ICY
	THE_END_CAT
	BEACH_CAT
	FOREST_CAT
    OCEAN_CAT
	DESERT_CAT
	RIVER_CAT
	SWAMP_CAT
    MUSHROOM
	NETHER_CAT
end

@enum Temperature begin
    OCEAN_TEMP
	COLD
	MEDIUM
	WARM
end

@enum Precipitation begin
    NONE_PRECIP
	RAIN
	SNOW
end

@enum Dimension begin
    OVERWORLD=0
    NETHER=1
    END=-1
end

mutable struct Biome
	id::Int
	name::String
	category::Category
	precipitation::Precipitation
	temperature::Float32

	scale::Float32
	depth::Float32

	parent::Union{Biome, Nothing}
	child::Union{Biome, Nothing}

	version::MCVersion
	dimension::Union{Dimension, Nothing}

	function Biome(version::MCVersion, dimension::Union{Dimension, Nothing}, id::Int, name::String, category::Category, precipitation::Precipitation,
	      temperature::Float32, scale::Float32, depth::Float32, parent::Union{Biome, Nothing})
		b = new(id, name, category, precipitation, temperature, scale, depth, parent, nothing, version, dimension)
	    if !isnothing(b.parent)
			b.parent.child = b
		end
		REGISTRY[id] = b
		b
	end

	Biome() = new()

end

REGISTRY = Dict{Int, Biome}()

function getTemperatureGroup(this)::Temperature
    if this.category == OCEAN
        OCEAN_TEMP
    elseif this.temperature < 0.2
        COLD
    elseif this.temperature < 1.0
        MEDIUM
    end
    WARM
end

function isShallowOcean(this, id::Int)::Bool
    return id == WARM_OCEAN.id || id == LUKEWARM_OCEAN.id || id == OCEAN.id || id == COLD_OCEAN.id || id == FROZEN_OCEAN.id;
end

function isOcean(this, id::Int)::Bool
    return id == WARM_OCEAN.id || id == LUKEWARM_OCEAN.id || id == OCEAN.id || id == COLD_OCEAN.id || id == FROZEN_OCEAN.id || id == DEEP_WARM_OCEAN.id || id == DEEP_LUKEWARM_OCEAN.id || id == DEEP_OCEAN.id || id == DEEP_COLD_OCEAN.id || id == DEEP_FROZEN_OCEAN.id
end

function isRiver(this, id::Int)::Bool
    return id == Biome.RIVER.id || id == Biome.FROZEN_RIVER.id
end

function areSimilar(this, id::Int, b2::Biome)::Bool
    if (b2 == null) return false
    if (id == b2.id) return true

    b = Biome.REGISTRY.get(id);
    if (b == null) return false;

    if (id != Biome.WOODED_BADLANDS_PLATEAU.id && id != Biome.BADLANDS_PLATEAU.id) {
        if (b.getCategory() != Biome.Category.NONE && b2.getCategory()
                != Biome.Category.NONE && b.getCategory() == b2.getCategory()) {
            return true
        end

        return b == b2
    end

    return b2 == Biome.WOODED_BADLANDS_PLATEAU || b2 == Biome.BADLANDS_PLATEAU;
end

public static boolean applyAll(Function<Integer, Boolean> function, int... ints) {
    for(int i : ints) {
        if(!function.apply(i)) {
            return false;
        end
    end

    return true;
end

function equalsOrDefault(self, comparator::Int32, comparable::Int32fallback::Int32)::Int32
    comparator == comparable ? comparable : fallback
end

OCEAN = Biome(v"1.8", OVERWORLD, 0, "ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.000f0, nothing)
PLAINS = Biome(v"1.8", OVERWORLD, 1, "plains", PLAINS_CAT, RAIN, 0.8f0, 0.050f0, 0.125f0, nothing)
DESERT = Biome(v"1.8", OVERWORLD, 2, "desert", DESERT_CAT, NONE_PRECIP, 2.0f0, 0.050f0, 0.125f0, nothing)
MOUNTAINS = Biome(v"1.8", OVERWORLD, 3, "mountains", EXTREME_HILLS, RAIN, 0.2f0, 0.500f0, 1.000f0, nothing)
FOREST = Biome(v"1.8", OVERWORLD, 4, "forest", FOREST_CAT, RAIN, 0.7f0, 0.200f0, 0.100f0, nothing)
TAIGA = Biome(v"1.8", OVERWORLD, 5, "taiga", TAIGA_CAT, RAIN, 0.25f0, 0.200f0, 0.200f0, nothing)
SWAMP = Biome(v"1.8", OVERWORLD, 6, "swamp", SWAMP_CAT, RAIN, 0.8f0, 0.100f0, -0.200f0, nothing)
RIVER = Biome(v"1.8", OVERWORLD, 7, "river", RIVER_CAT, RAIN, 0.5f0, 0.000f0, -0.500f0, nothing)
NETHER_WASTES = Biome(v"1.8", NETHER, 8, "nether_wastes", NETHER_CAT, NONE_PRECIP, 2.0f0, 0.200f0, 0.100f0, nothing)
THE_END = Biome(v"1.8", END, 9, "the_end", THE_END_CAT, NONE_PRECIP, 0.5f0, 0.200f0, 0.100f0, nothing)
FROZEN_OCEAN = Biome(v"1.13", OVERWORLD, 10, "frozen_ocean", OCEAN_CAT, SNOW, 0.0f0, 0.100f0, -1.000f0, nothing)
FROZEN_RIVER = Biome(v"1.8", OVERWORLD, 11, "frozen_river", RIVER_CAT, SNOW, 0.0f0, 0.000f0, -0.500f0, nothing)
SNOWY_TUNDRA = Biome(v"1.8", OVERWORLD, 12, "snowy_tundra", ICY, SNOW, 0.0f0, 0.050f0, 0.125f0, nothing)
SNOWY_MOUNTAINS = Biome(v"1.8", OVERWORLD, 13, "snowy_mountains", ICY, SNOW, 0.0f0, 0.300f0, 0.450f0, nothing)
MUSHROOM_FIELDS = Biome(v"1.8", OVERWORLD, 14, "mushroom_fields", MUSHROOM, RAIN, 0.9f0, 0.300f0, 0.200f0, nothing)
MUSHROOM_FIELD_SHORE = Biome(v"1.8", OVERWORLD, 15, "mushroom_field_shore", MUSHROOM, RAIN, 0.9f0, 0.025f0, 0.000f0, nothing)
BEACH = Biome(v"1.8", OVERWORLD, 16, "beach", BEACH_CAT, RAIN, 0.8f0, 0.025f0, 0.000f0, nothing)
DESERT_HILLS = Biome(v"1.8", OVERWORLD, 17, "desert_hills", DESERT_CAT, NONE_PRECIP, 2.0f0, 0.300f0, 0.450f0, nothing)
WOODED_HILLS = Biome(v"1.8", OVERWORLD, 18, "wooded_hills", FOREST_CAT, RAIN, 0.7f0, 0.300f0, 0.450f0, nothing)
TAIGA_HILLS = Biome(v"1.8", OVERWORLD, 19, "taiga_hills", TAIGA_CAT, RAIN, 0.25f0, 0.300f0, 0.450f0, nothing)
MOUNTAIN_EDGE = Biome(v"1.8", OVERWORLD, 20, "mountain_edge", EXTREME_HILLS, RAIN, 0.2f0, 0.300f0, 0.800f0, nothing)
JUNGLE = Biome(v"1.8", OVERWORLD, 21, "jungle", JUNGLE_CAT, RAIN, 0.95f0, 0.200f0, 0.100f0, nothing)
JUNGLE_HILLS = Biome(v"1.8", OVERWORLD, 22, "jungle_hills", JUNGLE_CAT, RAIN, 0.95f0, 0.300f0, 0.450f0, nothing)
JUNGLE_EDGE = Biome(v"1.8", OVERWORLD, 23, "jungle_edge", JUNGLE_CAT, RAIN, 0.95f0, 0.200f0, 0.100f0, nothing)
DEEP_OCEAN = Biome(v"1.13", OVERWORLD, 24, "deep_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.800f0, nothing)
STONE_SHORE = Biome(v"1.8", OVERWORLD, 25, "stone_shore", NONE, RAIN, 0.2f0, 0.800f0, 0.100f0, nothing)
SNOWY_BEACH = Biome(v"1.8", OVERWORLD, 26, "snowy_beach", BEACH_CAT, SNOW, 0.05f0, 0.025f0, 0.000f0, nothing)
BIRCH_FOREST = Biome(v"1.8", OVERWORLD, 27, "birch_forest", FOREST_CAT, RAIN, 0.6f0, 0.200f0, 0.100f0, nothing)
BIRCH_FOREST_HILLS = Biome(v"1.8", OVERWORLD, 28, "birch_forest_hills", FOREST_CAT, RAIN, 0.6f0, 0.300f0, 0.450f0, nothing)
DARK_FOREST = Biome(v"1.8", OVERWORLD, 29, "dark_forest", FOREST_CAT, RAIN, 0.7f0, 0.200f0, 0.100f0, nothing)
SNOWY_TAIGA = Biome(v"1.8", OVERWORLD, 30, "snowy_taiga", TAIGA_CAT, SNOW, -0.5f0, 0.200f0, 0.200f0, nothing)
SNOWY_TAIGA_HILLS = Biome(v"1.8", OVERWORLD, 31, "snowy_taiga_hills", TAIGA_CAT, SNOW, -0.5f0, 0.300f0, 0.450f0, nothing)
GIANT_TREE_TAIGA = Biome(v"1.8", OVERWORLD, 32, "giant_tree_taiga", TAIGA_CAT, RAIN, 0.3f0, 0.200f0, 0.200f0, nothing)
GIANT_TREE_TAIGA_HILLS = Biome(v"1.8", OVERWORLD, 33, "giant_tree_taiga_hills", TAIGA_CAT, RAIN, 0.3f0, 0.300f0, 0.450f0, nothing)
WOODED_MOUNTAINS = Biome(v"1.8", OVERWORLD, 34, "wooded_mountains", EXTREME_HILLS, RAIN, 0.2f0, 0.500f0, 1.000f0, nothing)
SAVANNA = Biome(v"1.8", OVERWORLD, 35, "savanna", SAVANNA_CAT, NONE_PRECIP, 1.2f0, 0.050f0, 0.125f0, nothing)
SAVANNA_PLATEAU = Biome(v"1.8", OVERWORLD, 36, "savanna_plateau", SAVANNA_CAT, NONE_PRECIP, 1.0f0, 0.025f0, 1.500f0, nothing)
BADLANDS = Biome(v"1.8", OVERWORLD, 37, "badlands", MESA, NONE_PRECIP, 2.0f0, 0.200f0, 0.100f0, nothing)
WOODED_BADLANDS_PLATEAU = Biome(v"1.8", OVERWORLD, 38, "wooded_badlands_plateau", MESA, NONE_PRECIP, 2.0f0, 0.025f0, 1.500f0, nothing)
BADLANDS_PLATEAU = Biome(v"1.8", OVERWORLD, 39, "badlands_plateau", MESA, NONE_PRECIP, 2.0f0, 0.025f0, 1.500f0, nothing)
SMALL_END_ISLANDS = Biome(v"1.13", END, 40, "small_end_islands", THE_END_CAT, NONE_PRECIP, 0.5f0, 0.200f0, 0.100f0, nothing)
END_MIDLANDS = Biome(v"1.13", END, 41, "end_midlands", THE_END_CAT, NONE_PRECIP, 0.5f0, 0.200f0, 0.100f0, nothing)
END_HIGHLANDS = Biome(v"1.13", END, 42, "end_highlands", THE_END_CAT, NONE_PRECIP, 0.5f0, 0.200f0, 0.100f0, nothing)
END_BARRENS = Biome(v"1.13", END, 43, "end_barrens", THE_END_CAT, NONE_PRECIP, 0.5f0, 0.200f0, 0.100f0, nothing)
WARM_OCEAN = Biome(v"1.13", OVERWORLD, 44, "warm_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.000f0, nothing)
LUKEWARM_OCEAN = Biome(v"1.13", OVERWORLD, 45, "lukewarm_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.000f0, nothing)
COLD_OCEAN = Biome(v"1.13", OVERWORLD, 46, "cold_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.000f0, nothing)
DEEP_WARM_OCEAN = Biome(v"1.13", OVERWORLD, 47, "deep_warm_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.800f0, nothing)
DEEP_LUKEWARM_OCEAN = Biome(v"1.13", OVERWORLD, 48, "deep_lukewarm_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.800f0, nothing)
DEEP_COLD_OCEAN = Biome(v"1.13", OVERWORLD, 49, "deep_cold_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.800f0, nothing)
DEEP_FROZEN_OCEAN = Biome(v"1.13", OVERWORLD, 50, "deep_frozen_ocean", OCEAN_CAT, RAIN, 0.5f0, 0.100f0, -1.800f0, nothing)
THE_VOID = Biome(v"1.8", nothing, 127, "the_void", NONE, NONE_PRECIP, 0.5f0, 0.200f0, 0.100f0, nothing)
SUNFLOWER_PLAINS = Biome(v"1.8", OVERWORLD, 129, "sunflower_plains", PLAINS_CAT, RAIN, 0.8f0, 0.050f0, 0.125f0, PLAINS)
DESERT_LAKES = Biome(v"1.8", OVERWORLD, 130, "desert_lakes", DESERT_CAT, NONE_PRECIP, 2.0f0, 0.250f0, 0.225f0, DESERT)
GRAVELLY_MOUNTAINS = Biome(v"1.8", OVERWORLD, 131, "gravelly_mountains", EXTREME_HILLS, RAIN, 0.2f0, 0.500f0, 1.000f0, MOUNTAINS)
FLOWER_FOREST = Biome(v"1.8", OVERWORLD, 132, "flower_forest", FOREST_CAT, RAIN, 0.7f0, 0.400f0, 0.100f0, FOREST)
TAIGA_MOUNTAINS = Biome(v"1.8", OVERWORLD, 133, "taiga_mountains", TAIGA_CAT, RAIN, 0.25f0, 0.400f0, 0.300f0, TAIGA)
SWAMP_HILLS = Biome(v"1.8", OVERWORLD, 134, "swamp_hills", SWAMP_CAT, RAIN, 0.8f0, 0.300f0, -0.100f0, SWAMP)
ICE_SPIKES = Biome(v"1.8", OVERWORLD, 140, "ice_spikes", ICY, SNOW, 0.0f0, 0.450f0, 0.425f0, SNOWY_TUNDRA)
MODIFIED_JUNGLE = Biome(v"1.8", OVERWORLD, 149, "modified_jungle", JUNGLE_CAT, RAIN, 0.95f0, 0.400f0, 0.200f0, JUNGLE)
MODIFIED_JUNGLE_EDGE = Biome(v"1.8", OVERWORLD, 151, "modified_jungle_edge", JUNGLE_CAT, RAIN, 0.95f0, 0.400f0, 0.200f0, JUNGLE_EDGE)
TALL_BIRCH_FOREST = Biome(v"1.8", OVERWORLD, 155, "tall_birch_forest", FOREST_CAT, RAIN, 0.6f0, 0.400f0, 0.200f0, BIRCH_FOREST)
TALL_BIRCH_HILLS = Biome(v"1.8", OVERWORLD, 156, "tall_birch_hills", FOREST_CAT, RAIN, 0.6f0, 0.500f0, 0.550f0, BIRCH_FOREST_HILLS)
DARK_FOREST_HILLS = Biome(v"1.8", OVERWORLD, 157, "dark_forest_hills", FOREST_CAT, RAIN, 0.7f0, 0.400f0, 0.200f0, DARK_FOREST)
SNOWY_TAIGA_MOUNTAINS = Biome(v"1.8", OVERWORLD, 158, "snowy_taiga_mountains", TAIGA_CAT, SNOW, -0.5f0, 0.400f0, 0.300f0, SNOWY_TAIGA)
GIANT_SPRUCE_TAIGA = Biome(v"1.8", OVERWORLD, 160, "giant_spruce_taiga", TAIGA_CAT, RAIN, 0.25f0, 0.200f0, 0.200f0, GIANT_TREE_TAIGA)
GIANT_SPRUCE_TAIGA_HILLS = Biome(v"1.8", OVERWORLD, 161, "giant_spruce_taiga_hills", TAIGA_CAT, RAIN, 0.25f0, 0.200f0, 0.200f0, GIANT_TREE_TAIGA_HILLS)
MODIFIED_GRAVELLY_MOUNTAINS = Biome(v"1.8", OVERWORLD, 162, "modified_gravelly_mountains", EXTREME_HILLS, RAIN, 0.2f0, 0.500f0, 1.000f0, WOODED_MOUNTAINS)
SHATTERED_SAVANNA = Biome(v"1.8", OVERWORLD, 163, "shattered_savanna", SAVANNA_CAT, NONE_PRECIP, 1.1f0, 1.225f0, 0.362f0, SAVANNA)
SHATTERED_SAVANNA_PLATEAU = Biome(v"1.8", OVERWORLD, 164, "shattered_savanna_plateau", SAVANNA_CAT, NONE_PRECIP, 1.0f0, 1.212f0, 1.050f0, SAVANNA_PLATEAU)
ERODED_BADLANDS = Biome(v"1.8", OVERWORLD, 165, "eroded_badlands", MESA, NONE_PRECIP, 2.0f0, 0.200f0, 0.100f0, BADLANDS)
MODIFIED_WOODED_BADLANDS_PLATEAU = Biome(v"1.8", OVERWORLD, 166, "modified_wooded_badlands_plateau", MESA, NONE_PRECIP, 2.0f0, 0.300f0, 0.450f0, WOODED_BADLANDS_PLATEAU)
MODIFIED_BADLANDS_PLATEAU = Biome(v"1.8", OVERWORLD, 167, "modified_badlands_plateau", MESA, NONE_PRECIP, 2.0f0, 0.300f0, 0.450f0, BADLANDS_PLATEAU)
BAMBOO_JUNGLE = Biome(v"1.14", OVERWORLD, 168, "bamboo_jungle", JUNGLE_CAT, RAIN, 0.95f0, 0.200f0, 0.100f0, nothing)
BAMBOO_JUNGLE_HILLS = Biome(v"1.14", OVERWORLD, 169, "bamboo_jungle_hills", JUNGLE_CAT, RAIN, 0.95f0, 0.300f0, 0.450f0, nothing)
SOUL_SAND_VALLEY = Biome(v"1.16", NETHER, 170, "soul_sand_valley", NETHER_CAT, NONE_PRECIP, 2.0f0, 0f0, 0f0, nothing)
CRIMSON_FOREST = Biome(v"1.16", NETHER, 171, "crimson_forest", NETHER_CAT, NONE_PRECIP, 2.0f0, 0f0, 0f0, nothing)
WARPED_FOREST = Biome(v"1.16", NETHER, 172, "warped_forest", NETHER_CAT, NONE_PRECIP, 2.0f0, 0f0, 0f0, nothing)
BASALT_DELTAS = Biome(v"1.16", NETHER, 173, "basalt_deltas", NETHER_CAT, NONE_PRECIP, 2.0f0, 0f0, 0f0, nothing)

public class Biome {

    public Biome.Data at(int x, int z) {
        return new Biome.Data(this, x, z);
    end

    public static class Data {
        public final Predicate<Biome> predicate;
        public final Biome biome;
        public final int x;
        public final int z;

        public Data(Biome biome, int x, int z) {
            this(b -> b == biome, biome, x, z);
        end

        public Data(Predicate<Biome> predicate, int x, int z) {
            this(predicate, null, x, z);
        end

        protected Data(Predicate<Biome> predicate, Biome biome, int x, int z) {
            this.predicate = predicate;
            this.biome = biome;
            this.x = x;
            this.z = z;
        end

        function test(self, source::OverworldBiomeSource)::Bool
            return this.predicate.test(source.getBiome(this.x, 0, this.z));
        end
    end


}
