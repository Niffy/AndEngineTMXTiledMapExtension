package org.andengine.extension.tmx;

import java.util.ArrayList;

import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.extension.tmx.util.constants.TMXIsometricConstants;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

import android.R.integer;
import android.util.SparseArray;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 19:38:11 - 20.07.2010
 */
public class TMXTiledMap implements TMXConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final String mOrientation;
	private final int mTileColumns;
	private final int mTilesRows;
	private final int mTileWidth;
	private final int mTileHeight;
	private int DRAW_METHOD_ISOMETRIC = TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_TILED_SOURCE;

	private final ArrayList<TMXTileSet> mTMXTileSets = new ArrayList<TMXTileSet>();
	private final ArrayList<TMXLayer> mTMXLayers = new ArrayList<TMXLayer>();
	private final ArrayList<TMXObjectGroup> mTMXObjectGroups = new ArrayList<TMXObjectGroup>();

	private final SparseArray<ITextureRegion> mGlobalTileIDToTextureRegionCache = new SparseArray<ITextureRegion>();
	private final SparseArray<TMXProperties<TMXTileProperty>> mGlobalTileIDToTMXTilePropertiesCache = new SparseArray<TMXProperties<TMXTileProperty>>();
	/**
	 * {@link integer} Array cache of offsets and tileset size for global tile id's. 
	 * <br><i>element[0]</i> X offset 
	 * <br><i>element[1]</i> Y offset
	 * <br><i>element[2]</i> Tile size width 
	 * <br><i>element[3]</i> Tile size height
	 */
	private final SparseArray<int[]> mGlobalTileIDMultiCache = new SparseArray<int[]>();
	private final TMXProperties<TMXTiledMapProperty> mTMXTiledMapProperties = new TMXProperties<TMXTiledMapProperty>();
	/**
	 *  Map drawing origin on the X axis. Isometric support only
	 */
	private float mOriginX = 0;
	/**
	 * Map drawing origin on the Y axis. Isometric support only
	 */
	private float mOriginY = 0;

	// ===========================================================
	// Constructors
	// ===========================================================

	TMXTiledMap(final Attributes pAttributes) {
		this.mOrientation = pAttributes.getValue("", TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION);
		if(this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
			//We support this!
		}else if (this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			//We support this!
		}else{
			throw new IllegalArgumentException(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION + ": '" + this.mOrientation + "' is not supported.");
		}

		this.mTileColumns = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_MAP_ATTRIBUTE_WIDTH);
		this.mTilesRows = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_MAP_ATTRIBUTE_HEIGHT);
		this.mTileWidth = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_MAP_ATTRIBUTE_TILEWIDTH);
		this.mTileHeight = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_MAP_ATTRIBUTE_TILEHEIGHT);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public final String getOrientation() {
		return this.mOrientation;
	}

	/**
	 * @deprecated Instead use {@link TMXTiledMap#getTileColumns()} * {@link TMXTiledMap#getTileWidth()}.
	 * @return
	 */
	@Deprecated
	public final int getWidth() {
		return this.mTileColumns;
	}

	public final int getTileColumns() {
		return this.mTileColumns;
	}

	/**
	 * @deprecated Instead use {@link TMXTiledMap#getTileRows()} * {@link TMXTiledMap#getTileHeight()}.
	 * @return
	 */
	@Deprecated
	public final int getHeight() {
		return this.mTilesRows;
	}

	public final int getTileRows() {
		return this.mTilesRows;
	}

	public final int getTileWidth() {
		return this.mTileWidth;
	}

	public final int getTileHeight() {
		return this.mTileHeight;
	}
	
	void addTMXTileSet(final TMXTileSet pTMXTileSet) {
		this.mTMXTileSets.add(pTMXTileSet);
	}

	public ArrayList<TMXTileSet> getTMXTileSets() {
		return this.mTMXTileSets;
	}

	void addTMXLayer(final TMXLayer pTMXLayer) {
		this.mTMXLayers.add(pTMXLayer);
	}

	public ArrayList<TMXLayer> getTMXLayers() {
		return this.mTMXLayers;
	}

	void addTMXObjectGroup(final TMXObjectGroup pTMXObjectGroup) {
		this.mTMXObjectGroups.add(pTMXObjectGroup);
	}

	public ArrayList<TMXObjectGroup> getTMXObjectGroups() {
		return this.mTMXObjectGroups;
	}

	public TMXProperties<TMXTileProperty> getTMXTilePropertiesByGlobalTileID(final int pGlobalTileID) {
		return this.mGlobalTileIDToTMXTilePropertiesCache.get(pGlobalTileID);
	}

	public void addTMXTiledMapProperty(final TMXTiledMapProperty pTMXTiledMapProperty) {
		this.mTMXTiledMapProperties.add(pTMXTiledMapProperty);
	}

	public TMXProperties<TMXTiledMapProperty> getTMXTiledMapProperties() {
		return this.mTMXTiledMapProperties;
	}

	/**
	 * Get the Isometric draw method, as defined in {@link TMXIsometricConstants}
	 * @return
	 */
	public int getIsometricDrawMethod(){
		return this.DRAW_METHOD_ISOMETRIC;
	}
	
	/**
	 * For all layers set the desired render method as defined in {@link TMXIsometricConstants}
	 * <br><b>Available draw methods:</b>
	 * <br> {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_ALL}
	 * <br> {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_SLIM}
	 * <br> {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_PADDING}
	 * <br> <b>Note:</b> If the draw method is not know or supported then 
	 *  {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_ALL} is used.
	 * @param pDrawMethod {@link integer} of the method to use.
	 */
	public void setIsometricDrawMethod(final int pDrawMethod){
		if(this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			this.DRAW_METHOD_ISOMETRIC = pDrawMethod;
			for (TMXLayer layer : this.mTMXLayers) {
				layer.setIsometricDrawMethod(pDrawMethod);
			}
		}
	}
	/**
	 * Set the origin of where the first tile should be drawn.
	 * <br><b>NOTE</b> Currently only Isometric orientation is supported.<br>
	 * When we talk of origin point this is first tile rectangular shape it resides in top left corner.<br>
	 * <b>Important:</b> This should only be called by the TMXLoader, as the origin should be set as we load in the map by calling {@link TMXLoader#loadFromAsset(String, float, float)} or {@link TMXLoader#load(java.io.InputStream, float, float)},
	 * this is because as layers are sprite batches, we cannot later change the draw location (well not to my knowledge)
	 * @param pX {@link Float} of the drawing origin point on the X axis
	 * @param pY {@link Float} of the drawing origin point on the Y axis.
	 */
	public void setMapOrigin(final float pX, final float pY){
		this.mOriginX = pX;
		this.mOriginY = pY;
	}
	/**
	 * Get the map drawing origin point on the X Axis, this is where the first tile is drawn on the X axis
	 * <br><b>NOTE</b> Currently only Isometric orientation is supported.
	 * @return {@link float} of the drawing origin point on the X axis
	 * @see #setMapOrigin(float, float)
	 */
	public float getMapOriginX(){
		return this.mOriginX;
	}
	/**
	 * Get the map drawing origin point on the X Axis, this is where the first tile is drawn on the X axis
	 * <br><b>NOTE</b> Currently only Isometric orientation is supported.
	 * @return {@link float} of the drawing origin point on the X axis
	 * @see #setMapOrigin(float, float)
	 */
	public float getMapOriginY(){
		return this.mOriginY;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	public TMXProperties<TMXTileProperty> getTMXTileProperties(final int pGlobalTileID) {
		final SparseArray<TMXProperties<TMXTileProperty>> globalTileIDToTMXTilePropertiesCache = this.mGlobalTileIDToTMXTilePropertiesCache;

		final TMXProperties<TMXTileProperty> cachedTMXTileProperties = globalTileIDToTMXTilePropertiesCache.get(pGlobalTileID);
		if(cachedTMXTileProperties != null) {
			return cachedTMXTileProperties;
		} else {
			final ArrayList<TMXTileSet> tmxTileSets = this.mTMXTileSets;

			for(int i = tmxTileSets.size() - 1; i >= 0; i--) {
				final TMXTileSet tmxTileSet = tmxTileSets.get(i);
				if(pGlobalTileID >= tmxTileSet.getFirstGlobalTileID()) {
					return tmxTileSet.getTMXTilePropertiesFromGlobalTileID(pGlobalTileID);
				}
			}
			throw new IllegalArgumentException("No TMXTileProperties found for pGlobalTileID=" + pGlobalTileID);
		}
	}

	public ITextureRegion getTextureRegionFromGlobalTileID(final int pGlobalTileID) {
		final SparseArray<ITextureRegion> globalTileIDToTextureRegionCache = this.mGlobalTileIDToTextureRegionCache;

		final ITextureRegion cachedTextureRegion = globalTileIDToTextureRegionCache.get(pGlobalTileID);
		if(cachedTextureRegion != null) {
			return cachedTextureRegion;
		} else {
			final ArrayList<TMXTileSet> tmxTileSets = this.mTMXTileSets;

			for(int i = tmxTileSets.size() - 1; i >= 0; i--) {
				final TMXTileSet tmxTileSet = tmxTileSets.get(i);
				if(pGlobalTileID >= tmxTileSet.getFirstGlobalTileID()) {
					final ITextureRegion textureRegion = tmxTileSet.getTextureRegionFromGlobalTileID(pGlobalTileID);
					/* Add to cache for the all future pGlobalTileIDs with the same value. */
					globalTileIDToTextureRegionCache.put(pGlobalTileID, textureRegion);
					return textureRegion;
				}
			}
			throw new IllegalArgumentException("No TextureRegion found for pGlobalTileID=" + pGlobalTileID);
		}
	}

	/**
	 * Get the offset and tile size of the tile set for a given global tile id. <br>
	 * TODO  This is perhaps not the most efficient way of getting the offset and tile set size,
	 * but it works.  In future perhaps store the range of global tile IDs for 
	 * a tile set and reduce the constant lookup.
	 * 
	 * @param pGlobalTileID {@link integer} of the global tile id
	 * @return {@link integer} array of offset and tile size.
	 * <br><i>element[0]</i> is the X offset. 
	 * <br><i>element[1]</i> is the Y offset.
	 * <br><i>element[2]</i> is the tile width.
	 * <br><i>element[3]</i> is the tile height.
	 */
	public int[] checkTileSetOffsetAndSize(final int pGlobalTileID){
		//implemented by Paul Robinson
		final SparseArray<int[]> globalTileIDMultiCache = this.mGlobalTileIDMultiCache;
		final int[] offset_and_size = globalTileIDMultiCache.get(pGlobalTileID);

		if(offset_and_size != null){
			/* Got a cached offset and size for this tile */
			return offset_and_size;
		}else{
			/* No cached offset, best go and find it! */
			final ArrayList<TMXTileSet> tmxTileSets = this.mTMXTileSets;
			for(int i = tmxTileSets.size() - 1; i >= 0; i--) {
				final TMXTileSet tmxTileSet = tmxTileSets.get(i);
				if(pGlobalTileID >= tmxTileSet.getFirstGlobalTileID()) {
					/* This tile belongs to this set */
					int[] object = {tmxTileSet.getOffsetX(), tmxTileSet.getOffsetY(), tmxTileSet.getTileWidth(), tmxTileSet.getTileHeight() };
					globalTileIDMultiCache.put(pGlobalTileID, object);
					return object;
				}
			}
			throw new IllegalArgumentException(String.format("No Tileset Offset and/or Tileset Size) found for pGlobalTileID: %d", pGlobalTileID));
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
