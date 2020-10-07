
struct LayerCache
	keys::Vector{Int64}
	values::Vector{Int32}
	mask::Int32
end

LayerCache(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
LayerCache(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
LayerCache(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
LayerCache(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class LayerCache {

	private final long[] keys;
	private final int[] values;
	private final int mask;

	public LayerCache(int capacity) {
		if(capacity < 1 || !Mth.isPowerOf2(capacity)) {
			throw new UnsupportedOperationException("capacity must be a power of 2");
		end

		this.keys = new long[capacity];
		Arrays.fill(this.keys, -1);
		this.values = new int[capacity];
		this.mask = (int)Mth.getMask(Long.numberOfTrailingZeros(capacity));
	end

	function get(this, x::Int32, y::Int32, z::Int32sampler::Sampler)::Int32
		key = uniqueHash(this, x, y, z);
		id = murmur64(this, key) & this.mask;

		if(this.keys[id] == key) {
			return this.values[id];
		end

		value = sampler.sample(x, y, z);
		this.keys[id] = key;
		this.values[id] = value;
		return value;
	end

	function uniqueHash(this, x::Int32, y::Int32z::Int32)::Int64
		hash = (long)x & Mth.getMask(28);
		hash |= ((long)z & Mth.getMask(28)) << 28;
		hash |= ((long)y & Mth.getMask(8)) << 56;
		return hash;
	end

	function murmur64(this, value::Int64)::Int32
		value ^= value >>> 33;
		value *= 0xFF51AFD7ED558CCDL;
		value ^= value >>> 33;
		value *= 0xC4CEB9FE1A85EC53L;
		value ^= value >>> 33;
		return (int)value;
	end

	@FunctionalInterface
	public interface Sampler {
		int sample(int x, int y, int z);
	end

}
