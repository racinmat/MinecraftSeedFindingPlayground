
abstract type CrossLayer <: BiomeLayer end

public abstract class CrossLayer extends BiomeLayer {

	public CrossLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(this, x::Int32, y::Int32z::Int32)::Int32
		this.setSeed(x, z);

		return this.sample(
				this.getParent().get(x, y, z - 1),
				this.getParent().get(x + 1, y, z),
				this.getParent().get(x, y,z + 1),
				this.getParent().get(x - 1, y, z),
				this.getParent().get(x, y, z)
			);
	end

	public abstract int sample(int n, int e, int s, int w, int center);

}
