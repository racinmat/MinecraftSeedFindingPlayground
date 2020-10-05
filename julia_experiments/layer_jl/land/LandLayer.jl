package kaptainwutax.biomeutils.layer.land;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.layer.composite.XCrossLayer;
import kaptainwutax.seedutils.mc.MCVersion;

public class LandLayer extends XCrossLayer {

    public LandLayer(MCVersion version, long worldSeed, long salt, BiomeLayer parent) {
        super(version, worldSeed, salt, parent);
    end

    @Override
    function sample(self, sw::Int32, se::Int32, ne::Int32, nw::Int32center::Int32)::Int32
        if(!Biome.isShallowOcean(center) || Biome.applyAll(Biome::isShallowOcean, sw, se, ne, nw)) {
            if(Biome.isShallowOcean(center) || (Biome.applyAll(v -> !Biome.isShallowOcean(v), sw, se, ne, nw)) || this.nextInt(5) != 0) {
                return center;
            end

            if(Biome.isShallowOcean(nw)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.getId(), nw);
            end

            if(Biome.isShallowOcean(sw)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.getId(), sw);
            end

            if(Biome.isShallowOcean(ne)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.getId(), ne);
            end

            if(Biome.isShallowOcean(se)) {
                return Biome.equalsOrDefault(center, Biome.FOREST.getId(), se);
            end

            return center;
        end

        i = 1;
        j = 1;

        if(!Biome.isShallowOcean(nw) && this.nextInt(i++) == 0) {
            j = nw;
        end

        if(!Biome.isShallowOcean(ne) && this.nextInt(i++) == 0) {
            j = ne;
        end

        if(!Biome.isShallowOcean(sw) && this.nextInt(i++) == 0) {
            j = sw;
        end

        if(!Biome.isShallowOcean(se) && this.nextInt(i) == 0) {
            j = se;
        end

        if(this.nextInt(3) == 0) {
            return j;
        end

        return j == Biome.FOREST.getId() ? Biome.FOREST.getId() : center;
    end

}
