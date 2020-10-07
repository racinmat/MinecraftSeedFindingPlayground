
abstract type CrossLayer <: BiomeLayer end

public abstract class CrossLayer extends BiomeLayer {

	public CrossLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(this, x::Int32, y::Int32z::Int32)::Int32
		setSeed(this, x, z);

		return sample(this, 
				this.parents[1].get(x, y, z - 1),
				this.parents[1].get(x + 1, y, z),
				this.parents[1].get(x, y,z + 1),
				this.parents[1].get(x - 1, y, z),
				this.parents[1].get(x, y, z)
			);
	end

	public abstract int sample(int n, int e, int s, int w, int center);

}
