# AndEngineTMXTiledMapExtension

### What it can do
 * Load isometric maps created in tiled
 * Tile offsets (Isometric layers only)
 * Get the tile at scene X and Y location from touch event
 * Get the tile at a given row and column
 * Set isometric draw methods (Yes 3 inefficent methods to choose from!)

### How to use it
 * Exactly like you would before with the origin repo.
 * Use TSX tilesets (no idea if any other method works, I'm only interested in TSX tilesets)
 * When using an Isometric tileset with offsets in Tiled, the X offset has to be negative
 * To set the draw method call the TMXTiledMap method setIsometricDrawMethod
 
 
 ```java
TMXTiledMap txMap;
	try{
		txMap = tmxLoader.loadFromAsset(location);
		this.mMap = txMap;
		this.mMap.setIsometricDrawMethod(TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_PADDING);
		//Attach map
	}catch(final TMXLoadException tmxle){
		//catch error
	}
```

 * To get the TMXTile at a given location, as an example
 
 ```java
//Standard method of getting tile
	final float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(pX, pY);
	TMXLayer currentLayer = this.mMap.getTMXLayers().get(0);
	final TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0], pToTiles[1]);
	if(tmxSelected != null){
		//Got a tile
	}
	//Alternative way
	TMXTile TMXTileIsoAlt = this.mMap.getTMXLayers().get(0).getTMXTileAtIsometricAlternative(pToTiles);
		if(TMXTileIsoAlt != null){
			//got a tile
		}			
```

 * To get the centre of the tile
 
 ```java
	TMXTile pTile;
	float pX = pTile.getTileXIsoCentre();
	float pY = pTile.getTileYIsoCentre();
```
 