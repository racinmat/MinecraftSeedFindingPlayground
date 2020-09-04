import kaptainwutax.featureutils.structure.RegionStructure
import kaptainwutax.seedutils.mc.Dimension
import java.util.*

class StructureInfo<C : RegionStructure.Config, D : RegionStructure.Data<*>>
    @JvmOverloads constructor(val structure: RegionStructure<C, D>, val dimension: Dimension, val isRequired: Boolean, val maxDistance: Int = Main.STRUCTURE_AND_BIOME_SEARCH_RADIUS) {
    val structName: String = if (dimension == Dimension.OVERWORLD) structure.name else  "${structure.name}_${dimension.getName()}"
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as StructureInfo<*, *>
        return maxDistance == that.maxDistance && isRequired == that.isRequired &&
                structure == that.structure && dimension == that.dimension &&
                structName == that.structName
    }

    override fun hashCode(): Int {
        return Objects.hash(structure, dimension, maxDistance, structName, isRequired)
    }

}