package org.andengine.extension.tmx;

import java.io.DataInputStream;
import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.util.constants.TMXIsometricConstants;
import org.andengine.opengl.util.GLState;
import org.andengine.util.exception.MethodNotSupportedException;
import org.xml.sax.Attributes;

import android.R.integer;
/**
 * Now an interface so we can choose between a {@link TMXLayerHighPerformanceSpriteBatch} 
 * and {@link TMXLayerObjectTilesLowMemorySpriteBatch}
 * @author Paul Robinson
 * @since 3 Sep 2012 17:25:38
 */
public interface TMXLayer {

	public String getName();

	public int getWidth();

	public int getHeight();

	public int getTileColumns();

	public int getTileRows();

	public TMXTile[][] getTMXTiles();

	/**
	 * Get the {@link TMXTile} at a given location based on a row and column number.
	 * 
	 * @param pTileColumn {@link integer} of column location
	 * @param pTileRow {@link integer} of row location
	 * 
	 * @return {@link TMXTile} tile at given location <b>OR</b> <code>NULL</code>
	 * if no such tile exists (such as when the layer is {@link TMXLayerObjectTiles})<br>
	 * <code>null</code> may be returned if {@link #getAllocateTiles()} returns false.
	 * 
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public TMXTile getTMXTile(final int pTileColumn, final int pTileRow) throws ArrayIndexOutOfBoundsException;

	/**
	 * Same as the standard getTMXTile method but returns null if out of bounds.
	 * <br>The original method just threw and array index out of bounds exception,
	 * this first checks if the desired row and column are within range, if not
	 * then null is returned instead.
	 * 
	 * @param pTileColumn {@link integer} of column location
	 * @param pTileRow {@link integer} of row location
	 * @return {@link TMXTile} if tile is within bounds 
	 * <b>OR</b> <code>NULL</code> if out of bounds. <br>
	 * <code>null</code> may be returned if {@link #getAllocateTiles()} returns false.
	 * @see #getTMXTile(int, int) for a bit more info.
	 */
	public TMXTile getTMXTileCanReturnNull(final int pTileColumn, final int pTileRow);

	/**
	 * For this layer set the desired render method as defined in {@link TMXIsometricConstants}
	 * <br><b>Available draw methods:</b>
	 * <br> {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_ALL}
	 * <br> {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_SLIM}
	 * <br> {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_PADDING}
	 * <br> {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_TILED_SOURCE}
	 * @param pMethod {@link integer} of the method to use.
	 */
	public void setIsometricDrawMethod(final int pMethod);

	/**
	 * Get a TMXTile at a given location.
	 * <br> This takes into account the map orientation. 
	 * This currently supports <b>ORTHOGONAL</b> and <b>ISOMETRIC</b>,
	 * Check the Javadoc for each related method if there are any instructions.
	 * <br> <b>Note</b> If the map orientation is not supported, an error is logged
	 * and the normal orthogonal calculations used.
	 * <br> <b>Call: </b>
	 * <br> {@link #getTMXTileAtOrthogonal(float, float)}
	 * <br> {@link #getTMXTIleAtIsometric(float, float)}
	 * <br> <b>Isometric Note:</b> You can also call {@link #getTMXTIleAtIsometricAlternative(float[])}
	 * if you feel the standard implementation isn't working correctly. 
	 * 
	 * @param pX {@link Float} x touch location.
	 * @param pY {@link Float} y touch location.
	 * @return {@link TMXTile} of found location <b>OR</b> returns <code>null</code> 
	 * if not found, or the location is out side the bounds of the tmx file. <br>
	 * <code>null</code> may be returned if {@link #getAllocateTiles()} returns false.
	 */
	public TMXTile getTMXTileAt(final float pX, final float pY);

	/**
	 * Standard method to calculating the selected tile at a given location, on a Orthogonal map<br>
	 * <b>Note</b> The contents of this method was originally in {@link #getTMXTileAt(float, float)}
	 * 
	 * @param pX {@link Float} x touch location.
	 * @param pY {@link Float} y touch location.
	 * @return {@link TMXTile} of found location <b>OR</b> returns <code>null</code> 
	 * if not found, or the location is out side the bounds of the tmx file. <br>
	 * <code>null</code> may be returned if {@link #getAllocateTiles()} returns false.
	 */
	public TMXTile getTMXTileAtOrthogonal(final float pX, final float pY);

	/**
	 * Standard method to calculating the selected tile at a given location on an isometric map <br>
	 * <b>Usage</b> <br>
	 * From the touch event, execute <code>convertLocalToSceneCoordinates</code> 
	 * on the current scene and pass to this method the returned {@link Float} array. 
	 * <br> <b>Accessed via </b> The only way to access this method is through 
	 * {@link #getTMXTileAt(float, float)}.
	 * <br><b>Why</b> <br>
	 * This is method is a mix of  {@link #getTMXTileAtOrthogonal(float, float)}
	 * and {@link #getIsometricTileAtAlternative(float[])}.  This method calls
	 * <code>convertSceneToLocalCoordinates</code>
	 * 
	 * @param pX {@link Float} x touch location.
	 * @param pY {@link Float} y touch location.
	 * @return {@link TMXTile} of found location <b>OR</b> returns <code>null</code> 
	 * if not found, or the location is out side the bounds of the tmx file. <br>
	 * <code>null</code> may be returned if {@link #getAllocateTiles()} returns false.
	 */
	public TMXTile getTMXTileAtIsometric(final float pX, final float pY);

	/**
	 * Alternative method to calculating the selected tile at a given location on an isometric map <br> <br>
	 * <b>Usage</b> <br>
	 * From the touch event, execute <code>convertLocalToSceneCoordinates</code> 
	 * on the current scene and pass to this method the returned {@link Float} array
	 * <br><b>Why</b> <br>
	 * {@link #getTMXTIleAtIsometric(float, float)} is very similar to this method,
	 * but calls <code>convertSceneToLocalCoordinates</code> first.
	 * 
	 * @param pTouch {@link Float} array of touch result from  
	 * <code>convertLocalToSceneCoordinates</code> <br> 
	 * <i>element [0]</i> is the X location <br>
	 * <i>element [1]</i> is the Y location 
	 * @return {@link TMXTile} of found location <b>OR</b> returns <code>null</code> 
	 * if not found, or the location is out side the bounds of the tmx file.<br>
	 * <code>null</code> may be returned if {@link #getAllocateTiles()} returns false.
	 */
	public TMXTile getTMXTileAtIsometricAlternative(final float[] pTouch);

	public void addTMXLayerProperty(final TMXLayerProperty pTMXLayerProperty);

	public TMXProperties<TMXLayerProperty> getTMXLayerProperties();

	/**
	 * Are {@link TMXTile} being allocated when creating {@link TMXLayer}
	 * 
	 * @return <code>true</code> if we are allocating, <code>false</code> if
	 *         not.
	 */
	public boolean getAllocateTiles();

	/**
	 * Get the tile centre coordinates for the given column and row. <br>
	 * <b>note</b> Only isometric is currently supported.
	 * 
	 * @param pTileColumn {@link Integer} column of tile
	 * @param pTileRow {@link Integer} row of tile.
	 * @return {@link Float} array <br>
	 * <b>Element[0]</b> is X <br>
	 * <b>Element[1]</b> is Y
	 */
	public float[] getTileCentre(final int pTileColumn, final int pTileRow);

	/**
	 * Get the tile isometric centre coordinates for the given column and row. 
	 * @param pTileColumn {@link Integer} column of tile
	 * @param pTileRow {@link Integer} row of tile.
	 * @return {@link Float} array <br>
	 * <b>Element[0]</b> is X <br>
	 * <b>Element[1]</b> is Y
	 */
	public float[] getIsoTileCentreAt(final int pTileColumn, final int pTileRow);

	public int[] getRowColAtIsometric(final float[] pTouch);

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Deprecated
	public void setRotation(final float pRotation) throws MethodNotSupportedException;

	public void initializeTMXTileFromXML(final Attributes pAttributes,
			final ITMXTilePropertiesListener pTMXTilePropertyListener);

	public void initializeTMXTilesFromDataString(final String pDataString, final String pDataEncoding,
			final String pDataCompression, final ITMXTilePropertiesListener pTMXTilePropertyListener)
			throws IOException, IllegalArgumentException;

	/**
	 * Add a tile to an orthogonal or isometric map. <br>
	 * <br><b>Note </b> 
	 * <br><i>{@link #addTileByGlobalTileIDOrthogonal(int, ITMXTilePropertiesListener)}</i>
	 * does not implement offsets.
	 * <br><i>{@link #addTileByGlobalTileIDIsometric(int, ITMXTilePropertiesListener)}</i>
	 * Does implement offsets so watch out for tile displacements (tiles in the incorrect position)
	 * <br>
	 * <br>
	 * For more information about Isometric tile maps see 
	 * {@link #addTileByGlobalTileIDIsometric(int, int, ITMXTilePropertiesListener)}
	 * <br>
	 * If the map orientation is not supported then {@link #addTileByGlobalTileIDOrthogonal(int, int, ITMXTilePropertiesListener)}
	 * is used instead, but a warning will be thrown to the log.
	 * 
	 * @param pGlobalTileID {@link integer} of the global tile id
	 * @param pTMXTilePropertyListener {@link ITMXTilePropertiesListener} 
	 */
	public void addTileByGlobalTileID(final int pGlobalTileID, final ITMXTilePropertiesListener pTMXTilePropertyListener);

	/**
	 * Add a tile to an orthogonal map. <br>
	 * A slightly modified version of the original implementation in that it can
	 * now have transparent tiles.  <br>
	 * <b>Note </b> This does not implement any offsets!. <br>
	 * <b>Note </b> This does not implement {@link #getAllocateTiles()}
	 * @param pGlobalTileID
	 * @param pTMXTilePropertyListener
	 */
	public void addTileByGlobalTileIDOrthogonal(final int pGlobalTileID,
			final ITMXTilePropertiesListener pTMXTilePropertyListener);

	/**
	 * Add a tile to an isometric map. <br> <br>
	 * 
	 * This can work with maps that use a global tile id of 0 (ie transparent) <br>
	 * Derived from the original code in {@link #addTileByGlobalTileIDOrthogonal(int, int, ITMXTilePropertiesListener)}
	 * <br><br>
	 * <b>NOTE </b>Tileset offsets are implemented! 
	 * <i>Watch out for tile displacements when using offsets!</i>
	 * <br><b>NOTE </b> X is the row on the left hand side.  Y is the columns on the right.
	 * <br><b>NOTE</b> when using X offsets in Tiled, the X offset should be a negative number!
	 * <br><br>
	 * Tiled renders and stores tile positions in the TMX file a certain way.  
	 * The tiles are drawn in rows going left to right, top to bottom. 
	 * In addition, when tiles are larger than the tile grid of the map, 
	 * they are aligned to the bottom-left corner of their cell and will stick 
	 * out to the top and to the right. This is where the offset comes into play
	 * for a tileset. <i>source: somewhere at the tiled github site</i>
	 * <br> <br>
	 * This also determines the draw position (with offsets applied) for the 
	 * {@link SpriteBatch} along with the centre of the tile. This makes life
	 * a bit easier for Paul Robinson! <br> 
	 * 
	 * @param pGlobalTileID {@link integer} of the global tile id
	 * @param pTMXTilePropertyListener {@link ITMXTilePropertiesListener} 
	 */
	public void addTileByGlobalTileIDIsometric(final int pGlobalTileID,
			final ITMXTilePropertiesListener pTMXTilePropertyListener);

	public int getSpriteBatchIndex(final int pColumn, final int pRow);

	public int readGlobalTileID(final DataInputStream pDataIn) throws IOException;

	/**
	 * Call if this if the map is Orthogonal <br>
	 * This is the original unmodified orthogonal render. 
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawOrthogonal(final GLState pGLState, final Camera pCamera);

	/**
	 * Call this if the map is Isometric. <br>
	 * This calls the desired draw method that the user desires.
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawIsometric(final GLState pGLState, final Camera pCamera);

	/**
	 * This will draw all the tiles of an isometric map. <br>
	 * This is the most inefficient way to draw the tiles as no culling occurs
	 * e.g even if the tile isn't on the screen its still being drawn!
	 * <br>
	 * Using this will result in low FPS. So really unsuitable for large maps.
	 * @param pGLState {@link GLState}
	 * @param pCamera {@link Camera}
	 */
	public void drawIsometricAll(final GLState pGLState, final Camera pCamera);

	/**
	 * This loops through all the tiles and checks if the centre location of
	 * the tile is within the screen space.
	 * <br>It doesn't loop through the TMXTile Arrays, instead it calculates the tile centre using maths.
	 * This is not the most efficient way to draw, but FPS is okish.
	 * <br>
	 * This calculates the tile location 
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawIsometricCullingLoop(final GLState pGLState, final Camera pCamera);

	/**
	 * This loops through all the tiles and checks if the centre location of
	 * the tile is within or partly in the screen space.
	 * <br> It doesn't loop through the TMXTile Arrays, instead it calculates the tile centre using maths.
	 * <br>
	 * This is not the most efficient way to draw, but FPS is okish.
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawIsometricCullingLoopExtra(final GLState pGLState, final Camera pCamera);

	/**
	 * Culling method taken from method paintLayer in IsoMapView.java 
	 * from Tiled 0.7.2 source code.  
	 * <br>Not to dissimilar to the function drawTileLayer in isometricrenderer.cpp 
	 * from Tiled 0.8.0 source code.  
	 * <br>Slight performance gain and draws tiles in a different order than the others,
	 * this does not appear to cause any problems.  The tiles original Z order
	 * are unaffected. 
	 * <br>
	 * Copyright 2009-2011, Thorbjørn Lindeijer <thorbjorn@lindeijer.nl>
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawIsometricCullingTiledSource(final GLState pGLState, final Camera pCamera);

	public int[] screenToTileCoords(float x, float y);

}