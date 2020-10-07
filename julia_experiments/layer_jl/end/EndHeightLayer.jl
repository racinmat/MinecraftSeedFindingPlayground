
struct EndHeightLayer <: BiomeLayer
    version::MCVersion
    parents::Vector{<:BiomeLayer}

    salt::Int64
    layerSeed::Int64
    localSeed::Int64

    scale::Int32 = -1
    layerId::Int32 = -1

    layerCache::LayerCache = new LayerCache(1024)
end

EndHeightLayer(version::MCVersion, parents...) = VoronoiLayer(version, parents, 0, 0, 0, -1, -1, LayerCache(1024))
EndHeightLayer(version::MCVersion) = VoronoiLayer(version, nothing, 0, 0, 0, -1, -1, LayerCache(1024))
EndHeightLayer(version::MCVersion, worldSeed::Int64, salt::Int64, parents...) = VoronoiLayer(version, parents, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))
EndHeightLayer(version::MCVersion, worldSeed::Int64, salt::Int64) = VoronoiLayer(version, nothing, salt, getLayerSeed(worldSeed, salt), 0, -1, -1, LayerCache(1024))

public class EndHeightLayer extends BiomeLayer {

    public EndHeightLayer(MCVersion version, long worldSeed, BiomeLayer parent) {
        super(version, parent);
    end

    @Override
    function sample(this, x::Int32, y::Int32z::Int32)::Int32
        scaledX = x / 2;
        scaledZ = z / 2;
        oddX = x % 2;
        oddZ = z % 2;
        height = 100.0F - (float)Math.sqrt((float)(x * x + z * z)) * 8.0F;
        height = clamp(height);

        for(rx = -12; rx <= 12; ++rx) {
            for(rz = -12; rz <= 12; ++rz) {
                shiftedX = scaledX + rx;
                shiftedZ = scaledZ + rz;
                if (shiftedX * shiftedX + shiftedZ * shiftedZ > 4096L && this.parents[1].get(shiftedX, 0, shiftedZ) == 1) {
                    elevation = (Math.abs((float)shiftedX) * 3439.0F + Math.abs((float)shiftedZ) * 147.0F) % 13.0F + 9.0F;
                    smoothX = (float)(oddX - rx * 2);
                    smoothZ = (float)(oddZ - rz * 2);
                    noise = 100.0F - (float)Math.sqrt(smoothX * smoothX + smoothZ * smoothZ) * elevation;
                    noise = clamp(noise);
                    height = Math.max(height, noise);
                end
            end
        end

        return Float.floatToIntBits(height);
    end

    protected static float clamp(float value) {
        if(value < -100.0F)return -100.0F;
        return Math.min(value, 80.0F);
    end

}
