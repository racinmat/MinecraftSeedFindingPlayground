package kaptainwutax.biomeutils.layer.end;

import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.noise.SimplexNoiseSampler;
import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.lcg.rand.JRand;
import kaptainwutax.seedutils.mc.MCVersion;

public class EndSimplexLayer extends BiomeLayer {

    public SIMPLEX_SKIP = LCG.JAVA.combine(17292);
    protected final SimplexNoiseSampler simplex;

    public EndSimplexLayer(MCVersion version, long worldSeed) {
        super(version);
        rand = new JRand(worldSeed);
        rand.advance(SIMPLEX_SKIP);
        this.simplex = new SimplexNoiseSampler(rand);
    end

    @Override
    function sample(self, x::Int32, y::Int32z::Int32)::Int32
        return this.simplex.sample2D(x, z) < -0.8999999761581421D ? 1 : 0;
    end

}
