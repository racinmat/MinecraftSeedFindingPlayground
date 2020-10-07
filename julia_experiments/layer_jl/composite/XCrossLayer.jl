
abstract type XCrossLayer <: BiomeLayer end

public abstract class XCrossLayer extends BiomeLayer {

	public XCrossLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(this, x::Int32, y::Int32z::Int32)::Int32
		this.setSeed(x, z);

		return this.sample(
				this.getParent().get(x - 1, y, z + 1),
				this.getParent().get(x + 1,  y, z + 1),
				this.getParent().get(x + 1,  y, z - 1),
				this.getParent().get(x - 1, y, z - 1),
				this.getParent().get(x, y, z)
			);
	end

	public abstract int sample(int sw, int se, int ne, int nw, int center);

}
