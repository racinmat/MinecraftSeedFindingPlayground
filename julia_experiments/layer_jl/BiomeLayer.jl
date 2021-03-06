
abstract type BiomeLayer end

# public abstract class BiomeLayer {
#
#     private final MCVersion version;
#     private final BiomeLayer[] parents;
#
#     public long salt;
#     public long layerSeed;
#     public long localSeed;
#
#     scale = -1;
#     layerId = -1;
#
#     layerCache = new LayerCache(1024);
#
#     public BiomeLayer(MCVersion version, BiomeLayer... parents) {
#         this.version = version;
#         this.parents = parents;
#     end
#
#     public BiomeLayer(MCVersion version) {
#         this(version, (BiomeLayer)null);
#     end
#
#     public BiomeLayer(MCVersion version, long worldSeed, long salt, BiomeLayer... parents) {
#         this(version, parents);
#         this.salt = salt;
#         this.layerSeed = getLayerSeed(worldSeed, this.salt);
#     end
#
#     public BiomeLayer(MCVersion version, long worldSeed, long salt) {
#         this(version, worldSeed, salt, (BiomeLayer)null);
#     end
#
#     function getVersion(this)::MCVersion
#         return this.version;
#     end
#
#     function setScale(this, scale::Int32)::Nothing
#         this.scale = scale;
#     end
#
#     function setLayerId(this, layerId::Int32)::Nothing
#         this.layerId = layerId;
#     end
#
#     function getScale(this)::Int32
#         return this.scale;
#     end
#
#     function hasParent(this)::Bool
#         return this.parents.length > 0;
#     end
#
#     function getLayerId(this)::Int32
#         return layerId;
#     end
#
#     function getParent(this)::BiomeLayer
#         return this.parents[1];
#     end
#
#     function getParent(this, id::Int32)::BiomeLayer
#         return this.parents[id];
#     end
#
#     function isMergingLayer(this)::Bool
#         return this.parents.length > 1;
#     end
#
#     public BiomeLayer[] getParents() {
#         return this.parents;
#     end
#
#     function get(this, x::Int32, y::Int32z::Int32)::Int32
#         return this.layerCache.get(x, y, z, this::sample);
#     end
#
#     public abstract int sample(int x, int y, int z);
#
#     function getLayerSeed(this, worldSeed::Int64salt::Int64)::Int64
#
#         midSalt = SeedMixer.mixSeed(salt, salt);
#         midSalt = SeedMixer.mixSeed(midSalt, salt);
#         midSalt = SeedMixer.mixSeed(midSalt, salt);
#         layerSeed = SeedMixer.mixSeed(worldSeed, midSalt);
#         layerSeed = SeedMixer.mixSeed(layerSeed, midSalt);
#         layerSeed = SeedMixer.mixSeed(layerSeed, midSalt);
#         return layerSeed;
#     end
#
#     function getLocalSeed(this, layerSeed::Int64, x::Int32z::Int32)::Int64
#         layerSeed = SeedMixer.mixSeed(layerSeed, x);
#         layerSeed = SeedMixer.mixSeed(layerSeed, z);
#         layerSeed = SeedMixer.mixSeed(layerSeed, x);
#         layerSeed = SeedMixer.mixSeed(layerSeed, z);
#         return layerSeed;
#     end
#
#     function getLocalSeed(this, worldSeed::Int64, salt::Int64, x::Int32z::Int32)::Int64
#         return getLocalSeed(getLayerSeed(worldSeed, salt), x, z);
#     end
#
#     function setSeed(this, x::Int32z::Int32)::Nothing
#         this.localSeed = BiomeLayer.getLocalSeed(this.layerSeed, x, z);
#     end
#
#     function nextInt(this, bound::Int32)::Int32
#         // warning for JDK lower than 1.9 its important to left the (long) cast
#         // @formatter:off
#         i = (int)Math.floorMod(this.localSeed >> 24, (long)bound);
#         // @formatter:on
#         this.localSeed = SeedMixer.mixSeed(this.localSeed, this.layerSeed);
#         return i;
#     end
#
#     function choose(this, a::Int32b::Int32)::Int32
#         return nextInt(this, 2) == 0 ? a : b;
#     end
#
#     function choose(this, a::Int32, b::Int32, c::Int32d::Int32)::Int32
#         i = nextInt(this, 4);
#         return i == 0 ? a : i == 1 ? b : i == 2 ? c : d;
#     end
#
# }
