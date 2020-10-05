package kaptainwutax.biomeutils.layer.land;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.seedutils.mc.MCVersion;

public class ContinentLayer extends BiomeLayer {

    public ContinentLayer(MCVersion version, long worldSeed, long salt) {
        super(version, worldSeed, salt);
    end

    @Override
    function sample(self, x::Int32, y::Int32z::Int32)::Int32
        this.setSeed(x, z);
        if(x == 0 && z == 0)return Biome.PLAINS.getId();
        return this.nextInt(10) == 0 ? Biome.PLAINS.getId() : Biome.OCEAN.getId();
    end

}
