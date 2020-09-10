layers
- ContinentLayer
  - PLAINS 10% prob
  - OCEAN 90% prob
- ScaleLayer
  - wiggles upper layers
- LandLayer
- ScaleLayer
- LandLayer
- LandLayer
- LandLayer
- IslandLayer
- ClimateLayer.Cold
- LandLayer
- ClimateLayer.Temperate
- ClimateLayer.Cool
- ClimateLayer.Special
- ScaleLayer
- ScaleLayer
- LandLayer
- MushroomLayer
- DeepOceanLayer
- BaseBiomesLayer
- BambooJungleLayer

layer notes
- ContinentLayer
  - PLAINS 10%
  - OCEAN 90%
- ScaleLayer
  - wiggles upper layers
- LandLayer <- XCrossLayer
  - if (center is shallow ocean) and (all diags are shallow)
      return center
  - if (center is not shallow ocean) and (all diags are shallow)
      80% return center else continue
  - if (center is not shallow ocean) and (all diags are not shallow)
      return center
  - if (center is not shallow ocean) and (some diags are shallow, some not)
      80% return center else continue
  - if center not shallow ocean || sw, se, ne, nw are shallow ocean
    - if center is shallow ocean || sw, se, ne, nw are not shallow ocean || 80% prob
        keep center
    - if Biome.isShallowOcean(nw)
          return center if forest else shallow nw
    - if Biome.isShallowOcean(sw)
          return center if forest else shallow sw
    - if Biome.isShallowOcean(ne)
          return center if forest else shallow ne
    - if Biome.isShallowOcean(se)
          return center if forest else shallow se
      return center
  - else
      1/3 chance return some of sw, se, ne, nw
      return center if forest else some of sw, se, ne, nw 

small truth table for the big if in beginning in land layer sample
center is shallow   all diag are shallow    all diag not shallow    res
1                   1                       0                       return center
1                   0                       1                       else
1                   0                       0                       else
0                   1                       0                       80%
0                   0                       1                       return center
0                   0                       0                       80%

- IslandLayer <- CrossLayer
  - if center, n, e, s, w are all shallow ocean
    - 50% return plains else return center (keep ocean)
    
    
- ClimateLayer.Cold
  - if is shallow ocean
      return center
  - 1/6 chance return forest
  - 1/6 chance return mountains
  - 2/3 chance return plains
     
- ClimateLayer.Temperate
  - if center not plains
      return center
  - all of (n, e, s, w) are not in (mountains, forest)
      ^ equivalent to all of (n, e, s, w) are either (plains or ocean)
      return center
  - else
      ^ equivalent of center is plains or (any of (n, e, s, w) are in (mountains, forest))
      return desert

- ClimateLayer.Cool
  - if center not forest
      return center
  - all of (n, e, s, w) are not in (plains, desert)
        ^ equivalent to all of (n, e, s, w) are either (forest or mountain or ocean)
      return center
  - else return mountains

- ClimateLayer.Special
  - if center is shallow ocean
      return center
  - if 1/13 chance
      return with nextInt(15) special bits
  - else return center

- MushroomLayer <- XCrossLayer
  - if all diags are shallow ocean and 1% return mushroom fields
  - else return center

- DeepOceanLayer
  - if center is not shallow ocean
      return shallow ocean
  - if all of (n, e, s, w) are shallow ocean
      return deep ocean

- BaseBiomesLayer
  - if center is ocean or mushroom fields
      return center
  - if center is plains and special bits
      1/3 chance return badlands plateau else wooded badlands plateau
  - if center is plains and not special bits
      1/2 chance return desert
      1/3 chance return savanna
      1/6 chance return plains
  - if center is desert and special bits
      return jungle
  - if center is desert and no special bits
      1/6 chance return forest
      1/6 chance return dark forest
      1/6 chance return mountains
      1/6 chance return plains
      1/6 chance return birch forest
      1/6 chance return swamp
  - if center is mountains and special bits
      return giant tree taiga
  - if center is desert and no special bits
      1/4 chance return forest
      1/4 chance return mountains
      1/4 chance return taiga
      1/4 chance return plains
  - if center is forest
      3/4 chance return snowy tundra
      1/4 chance return snowy taiga
  - else return mushroom fields (although this should not happen I think)
  
- BambooJungleLayer
  - if center is jungle
      10% return bamboo jungle
  - else return center