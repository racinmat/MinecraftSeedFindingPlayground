import kaptainwutax.featureutils.structure.RegionStructure;
import kaptainwutax.seedutils.mc.Dimension;
import kaptainwutax.seedutils.mc.MCVersion;

public class StructureInfo<C extends RegionStructure.Config, D extends RegionStructure.Data<?>> {

    RegionStructure<C, D> structure;
    Dimension dimension;
    int minOccurrences;
    int maxDistance;
    String structName;

    public StructureInfo(RegionStructure<C, D> structure, Dimension dimension, int minOccurrences, int maxDistance){
        this.structure = structure;
        this.dimension = dimension;
        this.minOccurrences = minOccurrences;
        this.maxDistance = maxDistance;
        this.structName = structure.getName();
    }

    public StructureInfo(RegionStructure<C, D> structure, Dimension dimension, int minOccurrences){
        this(structure, dimension, minOccurrences, Main.STRUCTURE_SEARCH_RADIUS);
    }

    public RegionStructure<C, D> getStructure() {
        return this.structure;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public int getMinOccurrences(){
        return this.minOccurrences;
    }

    public void setMinOccurrences(int minOccurrences) {
        this.minOccurrences = minOccurrences;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public String getStructName() {
        return structName;
    }
}
