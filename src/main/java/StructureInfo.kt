import kaptainwutax.featureutils.structure.RegionStructure
import kaptainwutax.seedutils.mc.Dimension

data class StructureInfo<C : RegionStructure.Config, D : RegionStructure.Data<*>>
    @JvmOverloads constructor(val structure: RegionStructure<C, D>, val dimension: Dimension, val isRequired: Boolean, val maxDistance: Int = Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS) {
    val structName: String = if (dimension == Dimension.OVERWORLD) structure.name else  "${structure.name}_${dimension.getName()}"

}