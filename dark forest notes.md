# dark forest, layer by layer

- given mansion position on x,z
- ContinentLayer
  - PLAINS 10%
  - OCEAN 90%
  - thus: plains is if
    Math.floorMod(Test.getLocalSeed(Test.getLayer(ContinentLayer::class.java), seed, x, z) shr 24, 10) == 0
- ScaleLayer - fuzzy
  - x is even and z is even
    - return center
  - if x is even and z is odd
    - 50% return center
    - 50% return south/down
  - if z is even and x is odd
    - 50% return center
    - 50% return east/right
  - if is fuzzy
    - 1/4 chance return center
    - 1/4 chance return east/right
    - 1/4 chance return south/down
    - 1/4 chance return south-east/down-right
    
    
thus: ScaleLayer - fuzzy
//this is merged these 2 layers together
  - x is even and z is even
    - return Math.floorMod(Test.getLocalSeed(continent, seed, x, z) shr 24, 10) == 0
  - if x is even and z is odd
    - if Math.floorMod(Test.getLocalSeed(layer 1 (scale fuzzy), seed, x, z) shr 24, 2) == 0
        //return center
        return Math.floorMod(Test.getLocalSeed(continent, seed, x, z) shr 24, 10) == 0
      else
        //return south
        return Math.floorMod(Test.getLocalSeed(continent, seed, x, z+1) shr 24, 10) == 0
  - if z is even and x is odd
    - if Math.floorMod(Test.getLocalSeed(layer 1 (scale fuzzy), seed, x, z) shr 24, 2) == 0
        //return center
        return Math.floorMod(Test.getLocalSeed(continent, seed, x, z) shr 24, 10) == 0
      else
        //return east
        return Math.floorMod(Test.getLocalSeed(continent, seed, x+1, z) shr 24, 10) == 0
  - if is fuzzy
    - if Math.floorMod(Test.getLocalSeed(layer 1 (scale fuzzy), seed, x, z) shr 24, 4) == 0
        //return center
        return Math.floorMod(Test.getLocalSeed(continent, seed, x, z) shr 24, 10) == 0
    - if Math.floorMod(Test.getLocalSeed(layer 1 (scale fuzzy), seed, x, z) shr 24, 4) == 1
        //return east
        return Math.floorMod(Test.getLocalSeed(continent, seed, x+1, z) shr 24, 10) == 0
    - if Math.floorMod(Test.getLocalSeed(layer 1 (scale fuzzy), seed, x, z) shr 24, 4) == 2
        //return south
        return Math.floorMod(Test.getLocalSeed(continent, seed, x, z+1) shr 24, 10) == 0
    - if Math.floorMod(Test.getLocalSeed(layer 1 (scale fuzzy), seed, x, z) shr 24, 4) == 3
        //return south-east
        return Math.floorMod(Test.getLocalSeed(continent, seed, x+1, z+1) shr 24, 10) == 0
