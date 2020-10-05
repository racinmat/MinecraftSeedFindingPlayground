package kaptainwutax.biomeutils.layer.land;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.seedutils.mc.MCVersion;

public class SunflowerPlainsLayer extends BiomeLayer {

	public SunflowerPlainsLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(self, x::Int32, y::Int32z::Int32)::Int32
		this.setSeed(x, z);
		value = this.getParent().get(x, y, z);
		return value == Biome.PLAINS.getId() && this.nextInt(57) == 0 ? Biome.SUNFLOWER_PLAINS.getId() : value;
	end

}
