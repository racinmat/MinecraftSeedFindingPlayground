
public class Biome {

    public static Map<Integer, Biome> REGISTRY = new HashMap<>();

    private final int id;
    private final String name;
    private final Category category;
    private final Precipitation precipitation;
	private final float temperature;

	private final float scale;
	private final float depth;

    private final Biome parent;
    private Biome child;

    private final MCVersion version;
    private final Dimension dimension;

    public Biome(MCVersion version, Dimension dimension, int id, String name, Category category, Precipitation precipitation,
                 float temperature, float scale, float depth, Biome parent) {
        this.version = version;
        this.dimension = dimension;
        this.id = id;
        this.name = name;
        this.category = category;
        this.precipitation = precipitation;
        this.temperature = temperature;
		this.scale = scale;
		this.depth = depth;
		this.parent = parent;
        if (this.parent != null) this.parent.child = this;
        REGISTRY.put(this.id, this);
    end

    public MCVersion getVersion(){
        return this.version;
    end

    function getDimension(self)::Dimension
        return this.dimension;
    end

    function getId(self)::Int32
        return this.id;
    end

    function getName(self)::AbstractString
        return this.name;
    end

    function getCategory(self)::Category
        return this.category;
    end

    function getPrecipitation(self)::Precipitation
        return this.precipitation;
    end

    function getTemperature(self)::Float32
        return this.temperature;
    end

    function getTemperatureGroup(self)::Temperature
        if(this.category == Biome.Category.OCEAN) {
            return Temperature.OCEAN;
        } else if(getTemperature(this, ) < 0.2F) {
            return Temperature.COLD;
        } else if(getTemperature(this, ) < 1.0F) {
            return Temperature.MEDIUM;
        end

        return Temperature.WARM;
	end

	function getScale(self)::Float32
		return scale;
	end

	function getDepth(self)::Float32
		return depth;
	end

    function hasParent(self)::Bool
        return this.parent != null;
    end

    function getParent(self)::Biome
        return this.parent;
    end

    function hasChild(self)::Bool
        return this.child != null;
    end

    function getChild(self)::Biome
        return this.child;
    end

    function isShallowOcean(self, id::Int32)::Bool
        return id == Biome.WARM_OCEAN.getId() || id == Biome.LUKEWARM_OCEAN.getId() || id == Biome.OCEAN.getId()
                || id == Biome.COLD_OCEAN.getId() || id == Biome.FROZEN_OCEAN.getId();
    end

    function isOcean(self, id::Int32)::Bool
        return id == Biome.WARM_OCEAN.getId() || id == Biome.LUKEWARM_OCEAN.getId() || id == Biome.OCEAN.getId()
                || id == Biome.COLD_OCEAN.getId() || id == Biome.FROZEN_OCEAN.getId()
                || id == Biome.DEEP_WARM_OCEAN.getId() || id == Biome.DEEP_LUKEWARM_OCEAN.getId()
                || id == Biome.DEEP_OCEAN.getId() || id == Biome.DEEP_COLD_OCEAN.getId()
                || id == Biome.DEEP_FROZEN_OCEAN.getId();
    end


    function isRiver(self, id::Int32)::Bool
        return id == Biome.RIVER.getId() || id == Biome.FROZEN_RIVER.getId();
    end

    function areSimilar(self, id::Int32b2::Biome)::Bool
        if (b2 == null) return false;
        if (id == b2.getId()) return true;

        b = Biome.REGISTRY.get(id);
        if (b == null) return false;

        if (id != Biome.WOODED_BADLANDS_PLATEAU.getId() && id != Biome.BADLANDS_PLATEAU.getId()) {
            if (b.getCategory() != Biome.Category.NONE && b2.getCategory()
                    != Biome.Category.NONE && b.getCategory() == b2.getCategory()) {
                return true;
            end

            return b == b2;
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
        return comparator == comparable ? comparable : fallback;
    end

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

    public enum Category {
        NONE("none"), TAIGA("taiga"), EXTREME_HILLS("extreme_hills"),
        JUNGLE("jungle"), MESA("mesa"), PLAINS("plains"), SAVANNA("savanna"),
        ICY("icy"), THE_END("the_end"), BEACH("beach"), FOREST("forest"),
        OCEAN("ocean"), DESERT("desert"), RIVER("river"), SWAMP("swamp"),
        MUSHROOM("mushroom"), NETHER("nether");

        private final String name;

        Category(String name) {
            this.name = name;
        end

        function getName(self)::AbstractString
            return this.name;
        end
    end

    public enum Temperature {
        OCEAN("ocean"), COLD("cold"), MEDIUM("medium"), WARM("warm");

        private final String name;

        Temperature(String name) {
            this.name = name;
        end

        function getName(self)::AbstractString
            return this.name;
        end
    end

    public enum Precipitation {
        NONE("none"), RAIN("rain"), SNOW("snow");

        private final String name;

        Precipitation(String name) {
            this.name = name;
        end

        function getName(self)::AbstractString
            return this.name;
        end
    end

    public static final OCEAN = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 0, "ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.000F, null);
    public static final PLAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 1, "plains", Category.PLAINS, Precipitation.RAIN, 0.8F, 0.050F, 0.125F, null);
    public static final DESERT = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 2, "desert", Category.DESERT, Precipitation.NONE, 2.0F, 0.050F, 0.125F, null);
    public static final MOUNTAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 3, "mountains", Category.EXTREME_HILLS, Precipitation.RAIN, 0.2F, 0.500F, 1.000F, null);
    public static final FOREST = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 4, "forest", Category.FOREST, Precipitation.RAIN, 0.7F, 0.200F, 0.100F, null);
    public static final TAIGA = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 5, "taiga", Category.TAIGA, Precipitation.RAIN, 0.25F, 0.200F, 0.200F, null);
    public static final SWAMP = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 6, "swamp", Category.SWAMP, Precipitation.RAIN, 0.8F, 0.100F, -0.200F, null);
    public static final RIVER = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 7, "river", Category.RIVER, Precipitation.RAIN, 0.5F, 0.000F, -0.500F, null);
    public static final NETHER_WASTES = new Biome(MCVersion.v1_8, Dimension.NETHER, 8, "nether_wastes", Category.NETHER, Precipitation.NONE, 2.0F, 0.200F, 0.100F, null);
    public static final THE_END = new Biome(MCVersion.v1_8, Dimension.END, 9, "the_end", Category.THE_END, Precipitation.NONE, 0.5F, 0.200F, 0.100F, null);
    public static final FROZEN_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 10, "frozen_ocean", Category.OCEAN, Precipitation.SNOW, 0.0F, 0.100F, -1.000F, null);
    public static final FROZEN_RIVER = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 11, "frozen_river", Category.RIVER, Precipitation.SNOW, 0.0F, 0.000F, -0.500F, null);
    public static final SNOWY_TUNDRA = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 12, "snowy_tundra", Category.ICY, Precipitation.SNOW, 0.0F, 0.050F, 0.125F, null);
    public static final SNOWY_MOUNTAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 13, "snowy_mountains", Category.ICY, Precipitation.SNOW, 0.0F, 0.300F, 0.450F, null);
    public static final MUSHROOM_FIELDS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 14, "mushroom_fields", Category.MUSHROOM, Precipitation.RAIN, 0.9F, 0.300F, 0.200F, null);
    public static final MUSHROOM_FIELD_SHORE = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 15, "mushroom_field_shore", Category.MUSHROOM, Precipitation.RAIN, 0.9F, 0.025F, 0.000F, null);
    public static final BEACH = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 16, "beach", Category.BEACH, Precipitation.RAIN, 0.8F, 0.025F, 0.000F, null);
    public static final DESERT_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 17, "desert_hills", Category.DESERT, Precipitation.NONE, 2.0F, 0.300F, 0.450F, null);
    public static final WOODED_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 18, "wooded_hills", Category.FOREST, Precipitation.RAIN, 0.7F, 0.300F, 0.450F, null);
    public static final TAIGA_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 19, "taiga_hills", Category.TAIGA, Precipitation.RAIN, 0.25F, 0.300F, 0.450F, null);
    public static final MOUNTAIN_EDGE = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 20, "mountain_edge", Category.EXTREME_HILLS, Precipitation.RAIN, 0.2F, 0.300F, 0.800F, null);
    public static final JUNGLE = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 21, "jungle", Category.JUNGLE, Precipitation.RAIN, 0.95F, 0.200F, 0.100F, null);
    public static final JUNGLE_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 22, "jungle_hills", Category.JUNGLE, Precipitation.RAIN, 0.95F, 0.300F, 0.450F, null);
    public static final JUNGLE_EDGE = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 23, "jungle_edge", Category.JUNGLE, Precipitation.RAIN, 0.95F, 0.200F, 0.100F, null);
    public static final DEEP_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 24, "deep_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.800F, null);
    public static final STONE_SHORE = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 25, "stone_shore", Category.NONE, Precipitation.RAIN, 0.2F, 0.800F, 0.100F, null);
    public static final SNOWY_BEACH = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 26, "snowy_beach", Category.BEACH, Precipitation.SNOW, 0.05F, 0.025F, 0.000F, null);
    public static final BIRCH_FOREST = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 27, "birch_forest", Category.FOREST, Precipitation.RAIN, 0.6F, 0.200F, 0.100F, null);
    public static final BIRCH_FOREST_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 28, "birch_forest_hills", Category.FOREST, Precipitation.RAIN, 0.6F, 0.300F, 0.450F, null);
    public static final DARK_FOREST = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 29, "dark_forest", Category.FOREST, Precipitation.RAIN, 0.7F, 0.200F, 0.100F, null);
    public static final SNOWY_TAIGA = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 30, "snowy_taiga", Category.TAIGA, Precipitation.SNOW, -0.5F, 0.200F, 0.200F, null);
    public static final SNOWY_TAIGA_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 31, "snowy_taiga_hills", Category.TAIGA, Precipitation.SNOW, -0.5F, 0.300F, 0.450F, null);
    public static final GIANT_TREE_TAIGA = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 32, "giant_tree_taiga", Category.TAIGA, Precipitation.RAIN, 0.3F, 0.200F, 0.200F, null);
    public static final GIANT_TREE_TAIGA_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 33, "giant_tree_taiga_hills", Category.TAIGA, Precipitation.RAIN, 0.3F, 0.300F, 0.450F, null);
    public static final WOODED_MOUNTAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 34, "wooded_mountains", Category.EXTREME_HILLS, Precipitation.RAIN, 0.2F, 0.500F, 1.000F, null);
    public static final SAVANNA = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 35, "savanna", Category.SAVANNA, Precipitation.NONE, 1.2F, 0.050F, 0.125F, null);
    public static final SAVANNA_PLATEAU = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 36, "savanna_plateau", Category.SAVANNA, Precipitation.NONE, 1.0F, 0.025F, 1.500F, null);
    public static final BADLANDS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 37, "badlands", Category.MESA, Precipitation.NONE, 2.0F, 0.200F, 0.100F, null);
    public static final WOODED_BADLANDS_PLATEAU = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 38, "wooded_badlands_plateau", Category.MESA, Precipitation.NONE, 2.0F, 0.025F, 1.500F, null);
    public static final BADLANDS_PLATEAU = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 39, "badlands_plateau", Category.MESA, Precipitation.NONE, 2.0F, 0.025F, 1.500F, null);
    public static final SMALL_END_ISLANDS = new Biome(MCVersion.v1_13, Dimension.END, 40, "small_end_islands", Category.THE_END, Precipitation.NONE, 0.5F, 0.200F, 0.100F, null);
    public static final END_MIDLANDS = new Biome(MCVersion.v1_13, Dimension.END, 41, "end_midlands", Category.THE_END, Precipitation.NONE, 0.5F, 0.200F, 0.100F, null);
    public static final END_HIGHLANDS = new Biome(MCVersion.v1_13, Dimension.END, 42, "end_highlands", Category.THE_END, Precipitation.NONE, 0.5F, 0.200F, 0.100F, null);
    public static final END_BARRENS = new Biome(MCVersion.v1_13, Dimension.END, 43, "end_barrens", Category.THE_END, Precipitation.NONE, 0.5F, 0.200F, 0.100F, null);
    public static final WARM_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 44, "warm_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.000F, null);
    public static final LUKEWARM_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 45, "lukewarm_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.000F, null);
    public static final COLD_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 46, "cold_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.000F, null);
    public static final DEEP_WARM_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 47, "deep_warm_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.800F, null);
    public static final DEEP_LUKEWARM_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 48, "deep_lukewarm_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.800F, null);
    public static final DEEP_COLD_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 49, "deep_cold_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.800F, null);
    public static final DEEP_FROZEN_OCEAN = new Biome(MCVersion.v1_13, Dimension.OVERWORLD, 50, "deep_frozen_ocean", Category.OCEAN, Precipitation.RAIN, 0.5F, 0.100F, -1.800F, null);
    public static final THE_VOID = new Biome(MCVersion.v1_8, null, 127, "the_void", Category.NONE, Precipitation.NONE, 0.5F, 0.200F, 0.100F, null);
    public static final SUNFLOWER_PLAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 129, "sunflower_plains", Category.PLAINS, Precipitation.RAIN, 0.8F, 0.050F, 0.125F, Biome.PLAINS);
    public static final DESERT_LAKES = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 130, "desert_lakes", Category.DESERT, Precipitation.NONE, 2.0F, 0.250F, 0.225F, Biome.DESERT);
    public static final GRAVELLY_MOUNTAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 131, "gravelly_mountains", Category.EXTREME_HILLS, Precipitation.RAIN, 0.2F, 0.500F, 1.000F, Biome.MOUNTAINS);
    public static final FLOWER_FOREST = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 132, "flower_forest", Category.FOREST, Precipitation.RAIN, 0.7F, 0.400F, 0.100F, Biome.FOREST);
    public static final TAIGA_MOUNTAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 133, "taiga_mountains", Category.TAIGA, Precipitation.RAIN, 0.25F, 0.400F, 0.300F, Biome.TAIGA);
    public static final SWAMP_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 134, "swamp_hills", Category.SWAMP, Precipitation.RAIN, 0.8F, 0.300F, -0.100F, Biome.SWAMP);
    public static final ICE_SPIKES = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 140, "ice_spikes", Category.ICY, Precipitation.SNOW, 0.0F, 0.450F, 0.425F, Biome.SNOWY_TUNDRA);
    public static final MODIFIED_JUNGLE = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 149, "modified_jungle", Category.JUNGLE, Precipitation.RAIN, 0.95F, 0.400F, 0.200F, Biome.JUNGLE);
    public static final MODIFIED_JUNGLE_EDGE = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 151, "modified_jungle_edge", Category.JUNGLE, Precipitation.RAIN, 0.95F, 0.400F, 0.200F, Biome.JUNGLE_EDGE);
    public static final TALL_BIRCH_FOREST = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 155, "tall_birch_forest", Category.FOREST, Precipitation.RAIN, 0.6F, 0.400F, 0.200F, Biome.BIRCH_FOREST);
    public static final TALL_BIRCH_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 156, "tall_birch_hills", Category.FOREST, Precipitation.RAIN, 0.6F, 0.500F, 0.550F, Biome.BIRCH_FOREST_HILLS);
    public static final DARK_FOREST_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 157, "dark_forest_hills", Category.FOREST, Precipitation.RAIN, 0.7F, 0.400F, 0.200F, Biome.DARK_FOREST);
    public static final SNOWY_TAIGA_MOUNTAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 158, "snowy_taiga_mountains", Category.TAIGA, Precipitation.SNOW, -0.5F, 0.400F, 0.300F, Biome.SNOWY_TAIGA);
    public static final GIANT_SPRUCE_TAIGA = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 160, "giant_spruce_taiga", Category.TAIGA, Precipitation.RAIN, 0.25F, 0.200F, 0.200F, Biome.GIANT_TREE_TAIGA);
    public static final GIANT_SPRUCE_TAIGA_HILLS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 161, "giant_spruce_taiga_hills", Category.TAIGA, Precipitation.RAIN, 0.25F, 0.200F, 0.200F, Biome.GIANT_TREE_TAIGA_HILLS);
    public static final MODIFIED_GRAVELLY_MOUNTAINS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 162, "modified_gravelly_mountains", Category.EXTREME_HILLS, Precipitation.RAIN, 0.2F, 0.500F, 1.000F, Biome.WOODED_MOUNTAINS);
    public static final SHATTERED_SAVANNA = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 163, "shattered_savanna", Category.SAVANNA, Precipitation.NONE, 1.1F, 1.225F, 0.362F, Biome.SAVANNA);
    public static final SHATTERED_SAVANNA_PLATEAU = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 164, "shattered_savanna_plateau", Category.SAVANNA, Precipitation.NONE, 1.0F, 1.212F, 1.050F, Biome.SAVANNA_PLATEAU);
    public static final ERODED_BADLANDS = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 165, "eroded_badlands", Category.MESA, Precipitation.NONE, 2.0F, 0.200F, 0.100F, Biome.BADLANDS);
    public static final MODIFIED_WOODED_BADLANDS_PLATEAU = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 166, "modified_wooded_badlands_plateau", Category.MESA, Precipitation.NONE, 2.0F, 0.300F, 0.450F, Biome.WOODED_BADLANDS_PLATEAU);
    public static final MODIFIED_BADLANDS_PLATEAU = new Biome(MCVersion.v1_8, Dimension.OVERWORLD, 167, "modified_badlands_plateau", Category.MESA, Precipitation.NONE, 2.0F, 0.300F, 0.450F, Biome.BADLANDS_PLATEAU);
    public static final BAMBOO_JUNGLE = new Biome(MCVersion.v1_14, Dimension.OVERWORLD, 168, "bamboo_jungle", Category.JUNGLE, Precipitation.RAIN, 0.95F, 0.200F, 0.100F, null);
    public static final BAMBOO_JUNGLE_HILLS = new Biome(MCVersion.v1_14, Dimension.OVERWORLD, 169, "bamboo_jungle_hills", Category.JUNGLE, Precipitation.RAIN, 0.95F, 0.300F, 0.450F, null);
    public static final SOUL_SAND_VALLEY = new Biome(MCVersion.v1_16, Dimension.NETHER, 170, "soul_sand_valley", Category.NETHER, Precipitation.NONE, 2.0F, 0F, 0F, null);
    public static final CRIMSON_FOREST = new Biome(MCVersion.v1_16, Dimension.NETHER, 171, "crimson_forest", Category.NETHER, Precipitation.NONE, 2.0F, 0F, 0F, null);
    public static final WARPED_FOREST = new Biome(MCVersion.v1_16, Dimension.NETHER, 172, "warped_forest", Category.NETHER, Precipitation.NONE, 2.0F, 0F, 0F, null);
    public static final BASALT_DELTAS = new Biome(MCVersion.v1_16, Dimension.NETHER, 173, "basalt_deltas", Category.NETHER, Precipitation.NONE, 2.0F, 0F, 0F, null);

}
