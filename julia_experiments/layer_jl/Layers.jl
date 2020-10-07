struct MCVersion <: VersionNumber
end

isOlderThan(a::VersionNumber, b::VersionNumber) = a < b


include(joinpath(@__DIR__, "composite", "CrossLayer.jl"))
include(joinpath(@__DIR__, "composite", "VoronoiLayer.jl"))
include(joinpath(@__DIR__, "composite", "XCrossLayer.jl"))

include(joinpath(@__DIR__, "land", "BambooJungleLayer.jl"))
include(joinpath(@__DIR__, "land", "BaseBiomesLayer.jl"))
include(joinpath(@__DIR__, "land", "ContinentLayer.jl"))
include(joinpath(@__DIR__, "land", "HillsLayer.jl"))
include(joinpath(@__DIR__, "land", "IslandLayer.jl"))
include(joinpath(@__DIR__, "land", "LandLayer.jl"))
include(joinpath(@__DIR__, "land", "MushroomLayer.jl"))
include(joinpath(@__DIR__, "land", "NoiseLayer.jl"))
include(joinpath(@__DIR__, "land", "SunflowerPlainsLayer.jl"))

include(joinpath(@__DIR__, "nether", "NetherLayer.jl"))

include(joinpath(@__DIR__, "scale", "ScaleLayer.jl"))
include(joinpath(@__DIR__, "scale", "SmoothScaleLayer.jl"))

include(joinpath(@__DIR__, "shore", "EaseEdgeLayer.jl"))
include(joinpath(@__DIR__, "shore", "EaseBiomesLayer.jl"))

include(joinpath(@__DIR__, "temperature", "ClimateLayer.jl"))

include(joinpath(@__DIR__, "water", "DeepOceanLayer.jl"))
include(joinpath(@__DIR__, "water", "NoiseToRiverLayer.jl"))
include(joinpath(@__DIR__, "water", "OceanTemperatureLayer.jl"))
include(joinpath(@__DIR__, "water", "RiverLayer.jl"))

include(joinpath(@__DIR__, "BiomeLayer.jl"))
include(joinpath(@__DIR__, "LayerCache.jl"))
include(joinpath(@__DIR__, "LayerStack.jl"))

include(joinpath(@__DIR__, "source", "BiomeSource.jl"))
include(joinpath(@__DIR__, "source", "EndBiomeSource.jl"))
include(joinpath(@__DIR__, "source", "NetherBiomeSource.jl"))
include(joinpath(@__DIR__, "source", "OverworldBiomeSource.jl"))

bs = OverworldBiomeSource(v"1.14", 1)
bs.getBiome(0, 0, 0)
