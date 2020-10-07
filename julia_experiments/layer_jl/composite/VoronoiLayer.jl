
struct VoronoiLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32
    layerId::Int32

    layerCache::LayerCache

    seed::Int64
    is3D::Bool
end

VoronoiLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024), 0, false)
VoronoiLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024), 0, false)
VoronoiLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024), 0, false)
VoronoiLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024), 0, false)

VoronoiLayer(version::MCVersion, worldSeed::Int64, is3D::Bool, parent::BiomeLayer) = VoronoiLayer(version, parent, isOlderThan(version, v"1.15") ? 10L : 0L, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024), isOlderThan(version, v"1.15") ? worldSeed : WorldSeed.toHash(worldSeed), is3D)

function sample(this, x::Int32, y::Int32z::Int32)::Int32
    return isOlderThan(this.version, v"1.14") ? sample13(this, x, z) : sample14(this, x, y, z);
end

public class VoronoiLayer extends BiomeLayer {

    private final long seed;
    private final boolean is3D;

    public VoronoiLayer(MCVersion version, long worldSeed, boolean is3D, BiomeLayer parent) {
        super(version, worldSeed, version.isOlderThan(MCVersion.v1_15) ? 10L : 0L, parent);
        this.seed = version.isOlderThan(MCVersion.v1_15) ? worldSeed : WorldSeed.toHash(worldSeed);
        this.is3D = is3D;
    end

    function getSeed(this)::Int64
        return this.seed;
    end

    function is3D(this)::Bool
        return this.is3D;
    end

    @Override
    function sample(this, x::Int32, y::Int32z::Int32)::Int32
        return this.version.isOlderThan(MCVersion.v1_14) ? sample13(this, x, z) : this.sample14(x, y, z);
    end

    function sample13(this, x::Int32z::Int32)::Int32
        int offset;
        x -= 2;
        z -= 2;
        pX = x >> 2;
        pZ = z >> 2;
        sX = pX << 2;
        sZ = pZ << 2;
        double[] off_0_0 = calcOffset(this.layerSeed, sX, sZ, 0, 0);
        double[] off_1_0 = calcOffset(this.layerSeed, sX, sZ, 4, 0);
        double[] off_0_1 = calcOffset(this.layerSeed, sX, sZ, 0, 4);
        double[] off_1_1 = calcOffset(this.layerSeed, sX, sZ, 4, 4);

        cell = (z & 3) * 4 + (x & 3);
        corner0 = calcContribution(off_0_0, cell >> 2, cell & 3);
        corner1 = calcContribution(off_1_0, cell >> 2, cell & 3);
        corner2 = calcContribution(off_0_1, cell >> 2, cell & 3);
        corner3 = calcContribution(off_1_1, cell >> 2, cell & 3);
        if (corner0 < corner1 && corner0 < corner2 && corner0 < corner3) {
            offset = 0;
        } else if (corner1 < corner0 && corner1 < corner2 && corner1 < corner3) {
            offset = 1;
        } else if (corner2 < corner0 && corner2 < corner1 && corner2 < corner3) {
            offset = 2;
        } else {
            offset = 3;
        end


        //  X -> (offset&1)
        // _________
        // | 0 | 1 |   Z (offset>>1)
        // |---|---|   |
        // | 2 | 3 |  \_/
        // |___|___|

        return this.parents[1].get(pX + (offset & 1), 0, pZ + (offset >> 1));
    end

    function sample14(this, x::Int32, y::Int32z::Int32)::Int32
        i = x - 2;
        j = y - 2;
        k = z - 2;
        l = i >> 2;
        m = j >> 2;
        n = k >> 2;
        d = (double)(i & 3) / 4.0D;
        e = (double)(j & 3) / 4.0D;
        f = (double)(k & 3) / 4.0D;
        double[] ds = new double[8];

        for (cell = 0; cell < 8; ++cell) {
            bl = (cell & 4) == 0;
            bl2 = (cell & 2) == 0;
            bl3 = (cell & 1) == 0;
            aa = bl ? l : l + 1;
            ab = bl2 ? m : m + 1;
            ac = bl3 ? n : n + 1;
            g = bl ? d : d - 1.0D;
            h = bl2 ? e : e - 1.0D;
            s = bl3 ? f : f - 1.0D;
            ds[cell] = calcSquaredDistance(this.seed, aa, ab, ac, g, h, s);
        end

        index = 0;
        min = ds[0];

        for(cell = 1; cell < 8; ++cell) {
            if(ds[cell] >= min)continue;
            index = cell;
            min = ds[cell];
        end

        xFinal = (index & 4) == 0 ? l : l + 1;
        yFinal = (index & 2) == 0 ? m : m + 1;
        zFinal = (index & 1) == 0 ? n : n + 1;
        return this.parents[1].get(xFinal, this.is3D ? yFinal : 0, zFinal);
    end

    private static double calcContribution(double[] d, int x, int z) {
        return ((double) x - d[1]) * ((double) x - d[1]) + ((double) z - d[0]) * ((double) z - d[0]);
    end

    private static double[] calcOffset(long layerSeed, int x, int z, int offX, int offZ) {
        mixedSeed = SeedMixer.mixSeed(layerSeed, x + offX);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, z + offZ);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, x + offX);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, z + offZ);
        d1 = (((double) ((int) Math.floorMod(mixedSeed >> 24, 1024L)) / 1024.0D) - 0.5D) * 3.6D + offX;
        mixedSeed = SeedMixer.mixSeed(mixedSeed, layerSeed);
        d2 = (((double) ((int) Math.floorMod(mixedSeed >> 24, 1024L)) / 1024.0D) - 0.5D) * 3.6D + offZ;
        return new double[] {d1, d2};
    end

    function calcSquaredDistance(this, seed::Int64, x::Int32, y::Int32, z::Int32, xFraction::Float64, yFraction::Float64zFraction::Float64)::Float64
        mixedSeed = SeedMixer.mixSeed(seed, x);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, y);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, z);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, x);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, y);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, z);
        d = distribute(mixedSeed);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, seed);
        e = distribute(mixedSeed);
        mixedSeed = SeedMixer.mixSeed(mixedSeed, seed);
        f = distribute(mixedSeed);
        return square(zFraction + f) + square(yFraction + e) + square(xFraction + d);
    end

    function distribute(this, seed::Int64)::Float64
        d = (double) ((int) Math.floorMod(seed >> 24, 1024L)) / 1024.0D;
        return (d - 0.5D) * 0.9D;
    end

    function square(this, d::Float64)::Float64
        return d * d;
    end

}
