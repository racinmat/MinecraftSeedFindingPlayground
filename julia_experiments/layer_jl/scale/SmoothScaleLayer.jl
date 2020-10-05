package kaptainwutax.biomeutils.layer.scale;

import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.layer.composite.CrossLayer;
import kaptainwutax.seedutils.mc.MCVersion;

public class SmoothScaleLayer extends CrossLayer {

	public SmoothScaleLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	end

	@Override
	function sample(self, n::Int32, e::Int32, s::Int32, w::Int32center::Int32)::Int32
		xMatches = e == w;
		zMatches = n == s;

		if(xMatches && zMatches) {
			return this.choose(w, n);
		} else if(!xMatches && !zMatches) {
			return center;
		} else if(xMatches) {
			return w;
		} else {
			return n;
		end

		/*
		if(xMatches == zMatches) {
			return xMatches ? this.choose(w, n): center;
		} else {
			return xMatches ? w : n;
		}*/
	end

}
