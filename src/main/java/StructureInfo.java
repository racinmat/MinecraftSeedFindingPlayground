import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.mc.Dimension;

public class StructureInfo<C extends RegionStructure.Config, D extends RegionStructure.Data<?>> {

    RegionStructure<C, D> structure;
    Dimension dimension;
    int maxDistance;
    String structName;
    boolean required;

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
}
