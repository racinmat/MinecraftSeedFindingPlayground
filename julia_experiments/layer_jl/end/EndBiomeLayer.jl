package kaptainwutax.biomeutils.layer.end;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.seedutils.mc.MCVersion;

public class EndBiomeLayer extends BiomeLayer {

    public EndBiomeLayer(MCVersion version, long worldSeed, BiomeLayer parent) {
        super(version, parent);
    end

    @Override
    function sample(self, x::Int32, y::Int32z::Int32)::Int32
        x >>= 2;
        z >>= 2;

        if((long)x * (long)x + (long)z * (long)z <= 4096L) {
            return Biome.THE_END.getId();
        end

        height = Float.intBitsToFloat(this.getParent().get(x * 2 + 1, 0, z * 2 + 1));

        if(height > 40.0F) {
            return Biome.END_HIGHLANDS.getId();
        } else if(height >= 0.0F) {
            return Biome.END_MIDLANDS.getId();
        } else if(height >= -20.0F) {
            return Biome.END_BARRENS.getId();
        end

        return Biome.SMALL_END_ISLANDS.getId();
    end



}
