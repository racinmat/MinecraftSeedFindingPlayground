import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.mc.Dimension;

import java.util.Objects;

public class StructureInfo<C extends RegionStructure.Config, D extends RegionStructure.Data<?>> {

    private final RegionStructure<C, D> structure;
    private final Dimension dimension;
    private final int maxDistance;
    private final String structName;
    private final boolean required;

    public StructureInfo(RegionStructure<C, D> structure, Dimension dimension, boolean required, int maxDistance){
        this.structure = structure;
        this.dimension = dimension;
        this.required = required;
        this.maxDistance = maxDistance;
        this.structName = dimension == Dimension.OVERWORLD ? structure.getName() : structure.getName()+"_"+dimension.name;
    }

    public StructureInfo(RegionStructure<C, D> structure, Dimension dimension, boolean required){
        this(structure, dimension, required, Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS);
    }

    public RegionStructure<C, D> getStructure() {
        return this.structure;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public String getStructName() {
        return structName;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StructureInfo<?, ?> that = (StructureInfo<?, ?>) o;
        return maxDistance == that.maxDistance &&
                required == that.required &&
                Objects.equals(structure, that.structure) &&
                dimension == that.dimension &&
                Objects.equals(structName, that.structName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure, dimension, maxDistance, structName, required);
    }
}
