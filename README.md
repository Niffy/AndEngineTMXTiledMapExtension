# AndEngineTMXTiledMapExtension

##Thanks
[Tiled](http://www.mapeditor.org/ "Tiled")
Thanks to Thorbjørn Lindeijer , as parts of the isometric branch use code from the JAVA and Tiled 0.8.0(converted) When this has occured there should be a mention in the source code.  (Any problems regarding this please do contact me and we can fix it!)

### What it can do
 * Load isometric maps created in tiled
 * Tile offsets (Isometric layers only)
 * Get the tile at scene X and Y location from touch event
 * Get the tile at a given row and column
 * Set isometric draw methods (Yes 3 inefficent methods to choose from!)
 * Convert pixel coordinates to scene coordinates for Isometric maps
 * Set a map draw origin (NEW)

### How to use it
 * Exactly like you would before with the origin repo.
 * Use TSX tilesets (no idea if any other method works, I'm only interested in TSX tilesets)
 * When using an Isometric tileset with offsets in Tiled, the X offset has to be negative
 * To set the draw method call the TMXTiledMap method setIsometricDrawMethod
 * Set the map draw origin, in the load method. If you don't want one then use the values 0;
 
 
 ```java
TMXTiledMap txMap;
	try{
		txMap = tmxLoader.loadFromAsset(location,0,0);
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

###Features

- Tile Objects can be drawn on Isometric maps
- Polygons and Polylines points can now be converted to scene coordinates for isometric maps using a ConvertIsometricPixelToScene object
- Set a map draw origin, making it possible to have multiple maps in a scene. 


### Notes
Be careful when designing layers, as having layers impacts on the performance negatively. So avoid large maps and many layers. 

Always try and test on multiple devices. I get respectable results on my HTC Desire S and poor results on my HTC Desire.  
As expected I get great results on my Nexus 7 Tablet, using a large map like Large_isometricblocks in my example, zoomed out scrolling 
I can get about 27-33fps then sitting at around 33fps. 