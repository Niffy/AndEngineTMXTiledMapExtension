package org.andengine.extension.tmx;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.entity.sprite.batch.SpriteBatchLowMemoryVBO;
import org.andengine.entity.sprite.batch.vbo.HighPerformanceSpriteBatchVertexBufferObject;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.extension.tmx.util.constants.TMXIsometricConstants;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.SAXUtils;
import org.andengine.util.StreamUtils;
import org.andengine.util.algorithm.collision.RectangularShapeCollisionChecker;
import org.andengine.util.base64.Base64;
import org.andengine.util.base64.Base64InputStream;
import org.andengine.util.color.Color;
import org.andengine.util.exception.AndEngineRuntimeException;
import org.andengine.util.exception.MethodNotSupportedException;
import org.andengine.util.math.MathUtils;
import org.xml.sax.Attributes;

import android.opengl.GLES20;
import android.util.Log;

/**
 * A TMXLayer which extends {@link SpriteBatch} which in itself uses a 
 * {@link HighPerformanceSpriteBatchVertexBufferObject}
 * 
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 20:27:31 - 20.07.2010
 */
public class TMXLayerHighPerformanceSpriteBatch extends SpriteBatchLowMemoryVBO implements TMXConstants, TMXLayer {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final TMXTiledMap mTMXTiledMap;
	private final String TAG = "TMXLayer";
	private final String mName;
	private final int mTileColumns;
	private final int mTileRows;
	protected final TMXTile[][] mTMXTiles;

	private int mTilesAdded;
	private final int mGlobalTileIDsExpected;

	private final float[] mCullingVertices = new float[2 * Sprite.VERTICES_PER_SPRITE];

	private final TMXProperties<TMXLayerProperty> mTMXLayerProperties = new TMXProperties<TMXLayerProperty>();

	private final int mWidth;
	private final int mHeight;

	private double tileratio = 0;
	/**
	 * Half the width of the isometric tile
	 */
	protected int mIsoHalfTileWidth = 0;
	/**
	 * Half the height of the isometric tile
	 */
	protected int mIsoHalfTileHeight = 0;
	/**
	 * Count how many tiles on the row axis has been added.
	 */
	private int mAddedTilesOnRow = 0;
	/**
	 * Count how many tile on the columns axis has been added. 
	 */
	private int mAddedRows = 0;
	/**
	 * What draw method to use for Isometric layers.<br>
	 * Default draw method is defined in the TMXTiledMap
	 * {@link TMXTiledMap#getIsometricDrawMethod()}
	 */
	private int DRAW_METHOD_ISOMETRIC = TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_ALL;
	/**
	 * Are we allocating TMXTiles when creating a layer
	 */
	private boolean mAllocateTMXTiles = false;

	// ===========================================================
	// Constructors
	// ===========================================================

	public TMXLayerHighPerformanceSpriteBatch(final TMXTiledMap pTMXTiledMap, final Attributes pAttributes, final VertexBufferObjectManager pVertexBufferObjectManager, boolean pAllocateTiles) {
		super(null, SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_WIDTH) * SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_HEIGHT), pVertexBufferObjectManager);

		this.mTMXTiledMap = pTMXTiledMap;
		this.mName = pAttributes.getValue("", TMXConstants.TAG_LAYER_ATTRIBUTE_NAME);
		this.mTileColumns = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_WIDTH);
		this.mTileRows = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_HEIGHT);
		if(pAllocateTiles){
			this.mAllocateTMXTiles = true;
			this.mTMXTiles = new TMXTile[this.mTileRows][this.mTileColumns];
		}else{
			this.mAllocateTMXTiles = false;
			this.mTMXTiles = null;
		}
	
		this.mWidth = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		this.mHeight = pTMXTiledMap.getTileHeight() * this.mTileRows;

		this.mRotationCenterX = this.mWidth * 0.5f;
		this.mRotationCenterY = this.mHeight * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;

		this.mGlobalTileIDsExpected = this.mTileColumns * this.mTileRows;

		this.setVisible(SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_VISIBLE, TMXConstants.TAG_LAYER_ATTRIBUTE_VISIBLE_VALUE_DEFAULT) == 1);
		this.setAlpha(SAXUtils.getFloatAttribute(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_OPACITY, TMXConstants.TAG_LAYER_ATTRIBUTE_OPACITY_VALUE_DEFAULT));
		
		if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			//Paul Robinson
			//Calculate the half of the tile height and width, saves doing it later
			this.mIsoHalfTileHeight = this.mTMXTiledMap.getTileHeight() / 2;
			this.mIsoHalfTileWidth = this.mTMXTiledMap.getTileWidth() /2;
			this.tileratio = this.mTMXTiledMap.getTileWidth() / this.mTMXTiledMap.getTileHeight();
			this.setIsometricDrawMethod(this.mTMXTiledMap.getIsometricDrawMethod());
		}
	}
	
	public TMXLayerHighPerformanceSpriteBatch(final TMXTiledMap pTMXTiledMap, final VertexBufferObjectManager pVertexBufferObjectManager, final TMXObjectGroup pTMXObjectGroup, boolean pAllocateTiles) {
		super(null, pTMXTiledMap.getTileWidth() * pTMXTiledMap.getTileHeight(), pVertexBufferObjectManager);

		this.mTMXTiledMap = pTMXTiledMap;
		this.mName = pTMXObjectGroup.getName();
		this.mTileColumns = pTMXTiledMap.getTileColumns();
		this.mTileRows = pTMXTiledMap.getTileRows();
		if(pAllocateTiles){
			this.mAllocateTMXTiles = true;
			this.mTMXTiles = new TMXTile[this.mTileRows][this.mTileColumns];
		}else{
			this.mAllocateTMXTiles = false;
			this.mTMXTiles = null;
		}

		this.mWidth = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		this.mHeight = pTMXTiledMap.getTileHeight() * this.mTileRows;

		this.mRotationCenterX = this.mWidth * 0.5f;
		this.mRotationCenterY = this.mHeight * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;

		this.mGlobalTileIDsExpected = this.mTileColumns * this.mTileRows;

		this.setVisible(true);
		this.setAlpha(1.0f);

		if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			//Paul Robinson
			//Calculate the half of the tile height and width, saves doing it later
			this.mIsoHalfTileHeight = this.mTMXTiledMap.getTileHeight() / 2;
			this.mIsoHalfTileWidth = this.mTMXTiledMap.getTileWidth() /2;
			this.tileratio = this.mTMXTiledMap.getTileWidth() / this.mTMXTiledMap.getTileHeight();
			this.setIsometricDrawMethod(this.mTMXTiledMap.getIsometricDrawMethod());
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getName()
	 */
	@Override
	public String getName() {
		return this.mName;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getWidth()
	 */
	@Override
	public int getWidth() {
		return this.mWidth;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getHeight()
	 */
	@Override
	public int getHeight() {
		return this.mHeight;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTileColumns()
	 */
	@Override
	public int getTileColumns() {
		return this.mTileColumns;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTileRows()
	 */
	@Override
	public int getTileRows() {
		return this.mTileRows;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXTiles()
	 */
	@Override
	public TMXTile[][] getTMXTiles() {
		return this.mTMXTiles;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXTile(int, int)
	 */
	@Override
	public TMXTile getTMXTile(final int pTileColumn, final int pTileRow) throws ArrayIndexOutOfBoundsException {
		if(this.mAllocateTMXTiles){
			return this.mTMXTiles[pTileRow][pTileColumn];
		}else{
			return null;
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXTileCanReturnNull(int, int)
	 */
	@Override
	public TMXTile getTMXTileCanReturnNull(final int pTileColumn, final int pTileRow) {
		if(this.mAllocateTMXTiles){
			if(pTileColumn >= 0 && pTileColumn < this.mTileColumns 
					&& pTileRow >= 0 && pTileRow < this.mTileRows){
				return this.mTMXTiles[pTileRow][pTileColumn];
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#setIsometricDrawMethod(int)
	 */
	@Override
	public void setIsometricDrawMethod(final int pMethod){
		this.DRAW_METHOD_ISOMETRIC = pMethod;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXTileAt(float, float)
	 */
	@Override
	public TMXTile getTMXTileAt(final float pX, final float pY) {
		if(this.mAllocateTMXTiles){
			//Modification by Paul Robinson
			if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
				return this.getTMXTileAtOrthogonal(pX, pY);
			}else if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
				return this.getTMXTileAtIsometric(pX, pY);
			}else{
				Log.w(TAG, String.format("Orientation not supported: '%s'. " +
						"Will use normal Orthogonal getTMXTileAt method", this.mTMXTiledMap.getOrientation()));	
				return this.getTMXTileAtOrthogonal(pX, pY);
			}
		}else{
			return null;
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXTileAtOrthogonal(float, float)
	 */
	@Override
	public TMXTile getTMXTileAtOrthogonal(final float pX, final float pY){
		if(this.mAllocateTMXTiles){
			final float[] localCoords = this.convertSceneToLocalCoordinates(pX, pY);
			final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;

			final int tileColumn = (int)(localCoords[SpriteBatch.VERTEX_INDEX_X] / tmxTiledMap.getTileWidth());
			if(tileColumn < 0 || tileColumn > this.mTileColumns - 1) {
				return null;
			}
			final int tileRow = (int)(localCoords[SpriteBatch.VERTEX_INDEX_Y] / tmxTiledMap.getTileWidth());
			if(tileRow < 0 || tileRow > this.mTileRows - 1) {
				return null;
			}

			return this.mTMXTiles[tileRow][tileColumn];
		}else{
			return null;
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXTileAtIsometric(float, float)
	 */
	@Override
	public TMXTile getTMXTileAtIsometric(final float pX, final float pY){
		/*
		 * Implemented by Paul Robinson
		 * Referenced work Christian Knudsen of Laserbrain Studios - "The basics of isometric programming" 
		 * http://laserbrainstudios.com/2010/08/the-basics-of-isometric-programming/
		 */
		if(this.mAllocateTMXTiles){
			float[] localCoords = this.convertSceneToLocalCoordinates(pX, pY);
			/*
			 * Since we can now have a map origin, we subject the map origin from the X(localCoords[0]) and Y(localCoords[1]) coordinates 
			 */
			localCoords[0] -= this.mTMXTiledMap.getMapOriginX();
			localCoords[1] -= this.mTMXTiledMap.getMapOriginY();
			final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;
			
			float screenX = localCoords[SpriteBatch.VERTEX_INDEX_X] -  this.mTMXTiledMap.getTileHeight();
			float tileColumn = (localCoords[SpriteBatch.VERTEX_INDEX_Y] / tmxTiledMap.getTileHeight()) + (screenX / tmxTiledMap.getTileWidth());
			float tileRow = (localCoords[SpriteBatch.VERTEX_INDEX_Y] / tmxTiledMap.getTileHeight()) - (screenX / tmxTiledMap.getTileWidth());
			if(tileColumn < 0 || tileColumn > this.mTileColumns) {
				return null;
			}
			if(tileRow < 0 || tileRow > this.mTileRows ) {
				return null;
			}
			return this.mTMXTiles[(int) tileRow][(int) tileColumn];
		}else{
			return null;
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXTileAtIsometricAlternative(float[])
	 */
	@Override
	public TMXTile getTMXTileAtIsometricAlternative(final float[] pTouch){
		/*
		 * Implemented by Paul Robinson
		 * Referenced work Christian Knudsen of Laserbrain Studios - "The basics of isometric programming" 
		 * http://laserbrainstudios.com/2010/08/the-basics-of-isometric-programming/
		 */
		/*
		 * Since we can now have a map origin, we subject the map origin from the screenX and Y coordinates 
		 */
		if(this.mAllocateTMXTiles){
			float pX = pTouch[0];
			float pY = pTouch[1];
			float screenX = pX - this.mTMXTiledMap.getMapOriginX() - this.mTMXTiledMap.getTileHeight();
			float screenY = pY - this.mTMXTiledMap.getMapOriginY();
			float tileColumn = (screenY / this.mTMXTiledMap.getTileHeight()) + (screenX / this.mTMXTiledMap.getTileWidth());
			float tileRow =  (screenY / this.mTMXTiledMap.getTileHeight()) - (screenX / this.mTMXTiledMap.getTileWidth());

			if(tileColumn < 0 || tileColumn > this.mTileColumns) {
				return null;
			}
			if(tileRow < 0 || tileRow > this.mTileRows) {
				return null;
			}
			return this.mTMXTiles[(int) tileRow][(int) tileColumn];
		}else{
			return null;
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#addTMXLayerProperty(org.andengine.extension.tmx.TMXLayerProperty)
	 */
	@Override
	public void addTMXLayerProperty(final TMXLayerProperty pTMXLayerProperty) {
		this.mTMXLayerProperties.add(pTMXLayerProperty);
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTMXLayerProperties()
	 */
	@Override
	public TMXProperties<TMXLayerProperty> getTMXLayerProperties() {
		return this.mTMXLayerProperties;
	}
	
	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getAllocateTiles()
	 */
	@Override
	public boolean getAllocateTiles() {
		return this.mAllocateTMXTiles;
	}
	
	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getTileCentre(int, int)
	 */
	@Override
	public float[] getTileCentre(final int pTileColumn, final int pTileRow){
		if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
			return null;
		}else if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			return this.getIsoTileCentreAt(pTileColumn, pTileRow);
		}else{
			Log.w(TAG, String.format("getTileCentre: Orientation not supported: '%s'. " +
					"will return null.", this.mTMXTiledMap.getOrientation()));
			return null;
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getIsoTileCentreAt(int, int)
	 */
	@Override
	public float[] getIsoTileCentreAt(final int pTileColumn, final int pTileRow){
		/*
		 * Get the first tile with the draw origin as well.
		 * Get the first tile iso X and Y for the given pTileRow
		 * Then do the adding to get the required tile in pTileColumn.
		 */
		float firstTileXCen = this.mTMXTiledMap.getMapOriginX() + this.mIsoHalfTileWidth;
		float firstTileYCen = this.mTMXTiledMap.getMapOriginY() + this.mIsoHalfTileHeight;
		float isoX = 0;
		float isoY = 0;
		
		isoX = firstTileXCen - (pTileRow * this.mIsoHalfTileWidth);
		isoY = firstTileYCen + (pTileRow * this.mIsoHalfTileHeight);
		
		isoX = isoX + (pTileColumn * this.mIsoHalfTileWidth);
		isoY = isoY + (pTileColumn * this.mIsoHalfTileHeight);
		return new float[] { isoX, isoY };
	}
	
	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getRowColAtIsometric(float[])
	 */
	@Override
	public int[] getRowColAtIsometric(final float[] pTouch){
		float pX = pTouch[0];
		float pY = pTouch[1];
		float screenX = pX - this.mTMXTiledMap.getMapOriginX() - this.mTMXTiledMap.getTileHeight();
		float screenY = pY - this.mTMXTiledMap.getMapOriginY();
		float tileColumn = (screenY / this.mTMXTiledMap.getTileHeight()) + (screenX / this.mTMXTiledMap.getTileWidth());
		float tileRow =  (screenY / this.mTMXTiledMap.getTileHeight()) - (screenX / this.mTMXTiledMap.getTileWidth());

		if(tileColumn < 0 || tileColumn > this.mTileColumns) {
			return null;
		}
		if(tileRow < 0 || tileRow > this.mTileRows) {
			return null;
		}
		return new int[] { (int) tileRow, (int) tileColumn };
	}
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void initBlendFunction(final ITexture pTexture) {

	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#setRotation(float)
	 */
	@Override
	@Deprecated
	public void setRotation(final float pRotation) throws MethodNotSupportedException {
		throw new MethodNotSupportedException();
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		/* Nothing. */
	}

	/**
	 * Modified to take in account the map orientation.
	 * <br> If orientation is not supported then a warning will be thrown to the
	 * log and will call {@link #drawOrthogonal(GLState, Camera)}
	 * 
	 */
	@Override
	protected void draw(final GLState pGLState, final Camera pCamera) {
		//Modified by Paul Robinson
		if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
			this.drawOrthogonal(pGLState, pCamera);
		}else if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			this.drawIsometric(pGLState, pCamera);
		}else{
			Log.w(TAG, String.format("Orientation not supported: '%s'. " +
					"Will use normal Orthogonal draw method", this.mTMXTiledMap.getOrientation()));	
			this.drawOrthogonal(pGLState, pCamera);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#initializeTMXTileFromXML(org.xml.sax.Attributes, org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener)
	 */
	@Override
	public void initializeTMXTileFromXML(final Attributes pAttributes, final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		this.addTileByGlobalTileID(SAXUtils.getIntAttributeOrThrow(pAttributes,TMXConstants.TAG_TILE_ATTRIBUTE_GID), pTMXTilePropertyListener);
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#initializeTMXTilesFromDataString(java.lang.String, java.lang.String, java.lang.String, org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener)
	 */
	@Override
	public void initializeTMXTilesFromDataString(final String pDataString, final String pDataEncoding, final String pDataCompression, final ITMXTilePropertiesListener pTMXTilePropertyListener) throws IOException, IllegalArgumentException {
		DataInputStream dataIn = null;
		try{
			InputStream in = new ByteArrayInputStream(pDataString.getBytes("UTF-8"));

			/* Wrap decoding Streams if necessary. */
			if(pDataEncoding != null && pDataEncoding.equals(TMXConstants.TAG_DATA_ATTRIBUTE_ENCODING_VALUE_BASE64)) {
				in = new Base64InputStream(in, Base64.DEFAULT);
			}
			if(pDataCompression != null){
				if(pDataCompression.equals(TMXConstants.TAG_DATA_ATTRIBUTE_COMPRESSION_VALUE_GZIP)) {
					in = new GZIPInputStream(in);
				} else {
					throw new IllegalArgumentException("Supplied compression '" + pDataCompression + "' is not supported yet.");
				}
			}
			dataIn = new DataInputStream(in);

			while(this.mTilesAdded < this.mGlobalTileIDsExpected) {
				final int globalTileID = this.readGlobalTileID(dataIn);
				this.addTileByGlobalTileID(globalTileID,pTMXTilePropertyListener);
			}
		} finally {
			StreamUtils.close(dataIn);
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#addTileByGlobalTileID(int, org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener)
	 */
	@Override
	public void addTileByGlobalTileID(final int pGlobalTileID, final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
			this.addTileByGlobalTileIDOrthogonal(pGlobalTileID, pTMXTilePropertyListener);
		}else if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			this.addTileByGlobalTileIDIsometric(pGlobalTileID, pTMXTilePropertyListener);
		}else{
			Log.w(TAG, String.format("Orientation not supported: '%s'. " +
					"Will use original addTileByGlobalTileIDOriginal method ", this.mTMXTiledMap.getOrientation()));
			this.addTileByGlobalTileIDOrthogonal(pGlobalTileID, pTMXTilePropertyListener);
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#addTileByGlobalTileIDOrthogonal(int, org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener)
	 */
	@Override
	public void addTileByGlobalTileIDOrthogonal(final int pGlobalTileID, final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;

		final int tilesHorizontal = this.mTileColumns;

		final int column = this.mTilesAdded % tilesHorizontal;
		final int row = this.mTilesAdded / tilesHorizontal;
		
		final TMXTile[][] tmxTiles = this.mTMXTiles;

		final ITextureRegion tmxTileTextureRegion;
		if(pGlobalTileID == 0) {
			tmxTileTextureRegion = null;
		} else {
			tmxTileTextureRegion = tmxTiledMap.getTextureRegionFromGlobalTileID(pGlobalTileID);
		}
		final int tileHeight = this.mTMXTiledMap.getTileHeight();
		final int tileWidth = this.mTMXTiledMap.getTileWidth();

		if (tmxTileTextureRegion != null) {
			// Unless this is a transparent tile, setup the texture
			if (this.mTexture == null) {
				this.mTexture = tmxTileTextureRegion.getTexture();
				super.initBlendFunction(this.mTexture);
			} else {
				if (this.mTexture != tmxTileTextureRegion.getTexture()) {
					throw new AndEngineRuntimeException("All TMXTiles in a TMXLayer ("+ mName + ") need to be in the same TMXTileSet.");
				}
			}
		}

		final TMXTile tmxTile = new TMXTile(this.mTMXTiledMap.getOrientation(), pGlobalTileID, this.mTilesAdded, column, row, tileWidth, tileHeight, tmxTileTextureRegion);
		tmxTiles[row][column] = tmxTile;

		if(pGlobalTileID != 0) {
			this.setIndex(this.getSpriteBatchIndex(column, row));
			this.drawWithoutChecks(tmxTileTextureRegion, tmxTile.getTileX(), tmxTile.getTileY(), tileWidth, tileHeight, Color.WHITE_ABGR_PACKED_FLOAT);
			this.submit(); // TODO Doesn't need to be called here, but should rather be called in a "init" step, when parsing the XML is complete.

			// Notify the ITMXTilePropertiesListener if it exists. 
			if(pTMXTilePropertyListener != null) {
				final TMXProperties<TMXTileProperty> tmxTileProperties = tmxTiledMap.getTMXTileProperties(pGlobalTileID);
				if(tmxTileProperties != null) {
					pTMXTilePropertyListener.onTMXTileWithPropertiesCreated(tmxTiledMap, this, tmxTile, tmxTileProperties);
					//Log.i(TAG, "tmxTileProperties created, size " + tmxTileProperties.size());
				}
			}
		}

		this.mTilesAdded++;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#addTileByGlobalTileIDIsometric(int, org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener)
	 */
	@Override
	public void addTileByGlobalTileIDIsometric(final int pGlobalTileID, final ITMXTilePropertiesListener pTMXTilePropertyListener){
		/*
		 * Implemented by - Paul Robinson
		 * Referenced work - athanazio - "Working with Isometric Maps"
		 * http://www.athanazio.com/2008/02/21/working-with-isometric-maps/
		 * http://www.athanazio.com/wp-content/uploads/2008/02/isomapjava.txt
		 */
		final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;
		final int tilesHorizontal = this.mTileColumns; 
		//Tile height and width of the map not the tileset!
		final int tileHeight = this.mTMXTiledMap.getTileHeight();
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int column = this.mTilesAdded % tilesHorizontal;
		final int row = this.mTilesAdded / tilesHorizontal;
		
		TMXTile[][] tmxTiles = null;
		if(this.mAllocateTMXTiles){
			tmxTiles = this.mTMXTiles;
		}
		
		final ITextureRegion tmxTileTextureRegion;

		if(pGlobalTileID == 0) {
			tmxTileTextureRegion = null;
		} else {
			tmxTileTextureRegion = tmxTiledMap.getTextureRegionFromGlobalTileID(pGlobalTileID);
		}

		if (tmxTileTextureRegion != null) {
			// Unless this is a transparent tile, setup the texture
			if (this.mTexture == null) {
				this.mTexture = tmxTileTextureRegion.getTexture();
				super.initBlendFunction(this.mTexture);
			} else {
				if (this.mTexture != tmxTileTextureRegion.getTexture()) {
					throw new AndEngineRuntimeException("All TMXTiles in a TMXLayer ("+ mName + ") need to be in the same TMXTileSet.");
				}
			}
		}		
		TMXTile tmxTile = null;
		if(this.mAllocateTMXTiles){
			tmxTile = new TMXTile(this.mTMXTiledMap.getOrientation(), pGlobalTileID, this.mTilesAdded, column, row, tileWidth, tileHeight, tmxTileTextureRegion);
		}
		
		//Get the offset for the tileset and the tileset size
		/*
		 * element[0] is the X offset.
		 * element[1] is the Y offset.
		 * element[2] is the tile width.
		 * element[3] is the tile height.
		 */
		int[] offset_tilesize = {0,0,tileWidth,tileHeight};
		if(pGlobalTileID == 0){
			//tile is transparent so there is no offset, and use default map tile size
		}else{
			offset_tilesize = this.mTMXTiledMap.checkTileSetOffsetAndSize(pGlobalTileID);
		}

		/*
		 * Work out where the "perfect" isometric tile should go.
		 * Perfect meaning a tile from a tileset of the correct height and 
		 * width matching the map tile height and width.
		 * Now with the map origin taken into account.
		 */	
		float xRealIsoPos = this.mTMXTiledMap.getMapOriginX() + (this.mAddedTilesOnRow * this.mIsoHalfTileWidth);
		xRealIsoPos = xRealIsoPos - (this.mAddedRows * this.mIsoHalfTileWidth);
		float yRealIsoPos = this.mTMXTiledMap.getMapOriginY() +(this.mAddedTilesOnRow * this.mIsoHalfTileHeight);
		yRealIsoPos = yRealIsoPos + (this.mAddedRows * this.mIsoHalfTileHeight);
		float yOffsetPos = yRealIsoPos - ((offset_tilesize[3] - tileHeight) - offset_tilesize[1]);
		/*
		 * Fixes #1
		 */
		float xOffsetPos = 0;
		if(offset_tilesize[0] > 0){
			xOffsetPos = xRealIsoPos + Math.abs(offset_tilesize[0]);
		}else{
			xOffsetPos = xRealIsoPos - Math.abs(offset_tilesize[0]);
		}
		float tileXIso = xOffsetPos;
		float tileYIso = yOffsetPos;
		if(this.mAllocateTMXTiles){
			tmxTile.setTileXIso(xOffsetPos);
			tmxTile.setTileYIso(yOffsetPos);
		}
		float xCentre = xRealIsoPos + this.mIsoHalfTileWidth;
		float yCentre = yRealIsoPos + this.mIsoHalfTileHeight;
		float tileXIsoC = xCentre;
		float tileYIsoX = yCentre;
		
		if(this.mAllocateTMXTiles){
			tmxTile.setTileXIsoCentre(xCentre);
			tmxTile.setTileYIsoCentre(yCentre);	
			tmxTiles[row][column] = tmxTile;
			
			this.mTMXTiles[row][column] = tmxTile;
		}
		
		this.mAddedTilesOnRow++;
		
		if(this.mAddedTilesOnRow == this.mTMXTiledMap.getTileColumns()){
			//Reset the tiles added to a row
			this.mAddedTilesOnRow = 0;
			//Increase the numbers of rows added
			this.mAddedRows++;
		}

		if(pGlobalTileID != 0) {
			this.setIndex(this.getSpriteBatchIndex(column, row));
			//Before we were drawing to the map tile size, not the tileset size
			this.drawWithoutChecks(tmxTileTextureRegion,tileXIso , tileYIso, offset_tilesize[2], offset_tilesize[3], Color.WHITE_ABGR_PACKED_FLOAT);
			this.submit(); // TODO Doesn't need to be called here, but should rather be called in a "init" step, when parsing the XML is complete.
			// Notify the ITMXTilePropertiesListener if it exists. 
			if(pTMXTilePropertyListener != null) {
				final TMXProperties<TMXTileProperty> tmxTileProperties = tmxTiledMap.getTMXTileProperties(pGlobalTileID);
				if(tmxTileProperties != null) {
					if(this.mAllocateTMXTiles){
						pTMXTilePropertyListener.onTMXTileWithPropertiesCreated(tmxTiledMap, this, tmxTile, tmxTileProperties);
					}else{
						pTMXTilePropertyListener.onTMXTileWithPropertiesCreated(tmxTiledMap, this, null, tmxTileProperties);
					}
					//Log.i(TAG, "tmxTileProperties created, size " + tmxTileProperties.size());
				}
			}
		}
		this.mTilesAdded++;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#getSpriteBatchIndex(int, int)
	 */
	@Override
	public int getSpriteBatchIndex(final int pColumn, final int pRow) {
		return pRow * this.mTileColumns + pColumn;
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#readGlobalTileID(java.io.DataInputStream)
	 */
	@Override
	public int readGlobalTileID(final DataInputStream pDataIn) throws IOException {
		final int lowestByte = pDataIn.read();
		final int secondLowestByte = pDataIn.read();
		final int secondHighestByte = pDataIn.read();
		final int highestByte = pDataIn.read();

		if(lowestByte < 0 || secondLowestByte < 0 || secondHighestByte < 0 || highestByte < 0) {
			throw new IllegalArgumentException("Couldn't read global Tile ID.");
		}

		return lowestByte | secondLowestByte <<  8 |secondHighestByte << 16 | highestByte << 24;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	// ===========================================================
	// Drawing options
	// ===========================================================

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#drawOrthogonal(org.andengine.opengl.util.GLState, org.andengine.engine.camera.Camera)
	 */
	@Override
	public void drawOrthogonal(final GLState pGLState, final Camera pCamera){
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int tileHeight = this.mTMXTiledMap.getTileHeight();

		final float scaledTileWidth = tileWidth * this.mScaleX;
		final float scaledTileHeight = tileHeight * this.mScaleY;

		final float[] cullingVertices = this.mCullingVertices;
		RectangularShapeCollisionChecker.fillVertices(0, 0, this.mWidth, this.mHeight, this.getLocalToSceneTransformation(), cullingVertices);

		final float layerMinX = cullingVertices[SpriteBatch.VERTEX_INDEX_X];
		final float layerMinY = cullingVertices[SpriteBatch.VERTEX_INDEX_Y];

		final float cameraMinX = pCamera.getXMin();
		final float cameraMinY = pCamera.getYMin();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();

		/* Determine the area that is visible in the camera. */
		final float firstColumnRaw = (cameraMinX - layerMinX) / scaledTileWidth;
		final int firstColumn = MathUtils.bringToBounds(0, tileColumns - 1, (int)Math.floor(firstColumnRaw));
		final int lastColumn = MathUtils.bringToBounds(0, tileColumns - 1, (int)Math.ceil(firstColumnRaw + cameraWidth / scaledTileWidth));

		final float firstRowRaw = (cameraMinY - layerMinY) / scaledTileHeight;
		final int firstRow = MathUtils.bringToBounds(0, tileRows - 1, (int)Math.floor(firstRowRaw));
		final int lastRow = MathUtils.bringToBounds(0, tileRows - 1, (int)Math.floor(firstRowRaw + cameraHeight / scaledTileHeight));

		for(int row = firstRow; row <= lastRow; row++) {
			for(int column = firstColumn; column <= lastColumn; column++) {
				this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP, this.getSpriteBatchIndex(column, row) * SpriteBatch.VERTICES_PER_SPRITE, SpriteBatch.VERTICES_PER_SPRITE);
			}
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#drawIsometric(org.andengine.opengl.util.GLState, org.andengine.engine.camera.Camera)
	 */
	@Override
	public void drawIsometric(final GLState pGLState, final Camera pCamera){ 
		if(this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_ALL){
			this.drawIsometricAll(pGLState, pCamera);
		}else if(this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_SLIM){
			this.drawIsometricCullingLoop(pGLState, pCamera);
		}else if(this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_PADDING){
			this.drawIsometricCullingLoopExtra(pGLState, pCamera);	
		}else if(this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_TILED_SOURCE){
			this.drawIsometricCullingTiledSource(pGLState, pCamera);
		}else{
			Log.w(TAG, String.format("Draw method %d is currently not supported or an unknown draw method. Will use the default draw method."
					, this.DRAW_METHOD_ISOMETRIC));
			this.DRAW_METHOD_ISOMETRIC = TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_ALL;
			this.drawIsometricAll(pGLState, pCamera);
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#drawIsometricAll(org.andengine.opengl.util.GLState, org.andengine.engine.camera.Camera)
	 */
	@Override
	public void drawIsometricAll(final GLState pGLState, final Camera pCamera){
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;
		for (int j = 0; j < tileRows; j++) {
			for (int i = 0; i < tileColumns; i++) {
				this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP, 
						this.getSpriteBatchIndex(i, j) * SpriteBatch.VERTICES_PER_SPRITE, 
						SpriteBatch.VERTICES_PER_SPRITE);
			}
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#drawIsometricCullingLoop(org.andengine.opengl.util.GLState, org.andengine.engine.camera.Camera)
	 */
	@Override
	public void drawIsometricCullingLoop(final GLState pGLState, final Camera pCamera){
		final float cameraMinX = pCamera.getXMin();
		final float cameraMinY = pCamera.getYMin();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;

		final int yWholeMax = (int) (cameraMinY + cameraHeight);
		final int yWholeMin = (int) cameraMinY;
		final int xWholeMax = (int) (cameraMinX + cameraWidth);
		final int xWholeMin = (int) cameraMinX;

		for (int j = 0; j < tileRows; j++) {
			for (int i = 0; i < tileColumns; i++) {
				float[] isoCen = this.getIsoTileCentreAt(i, j);
				if(isoCen[1] < yWholeMax &&
						isoCen[1] > yWholeMin){				
					if(isoCen[0] < xWholeMax
							&& isoCen[0] > xWholeMin) {
						this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP, 
								this.getSpriteBatchIndex(i, j) * SpriteBatch.VERTICES_PER_SPRITE, 
								SpriteBatch.VERTICES_PER_SPRITE);
					}
				}
			}
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#drawIsometricCullingLoopExtra(org.andengine.opengl.util.GLState, org.andengine.engine.camera.Camera)
	 */
	@Override
	public void drawIsometricCullingLoopExtra(final GLState pGLState, final Camera pCamera){
		final float cameraMinX = pCamera.getXMin();
		final float cameraMinY = pCamera.getYMin();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;
		final float tileHeight = this.mTMXTiledMap.getTileHeight();
		final float tileWidth = this.mTMXTiledMap.getTileWidth();

		final int yWholeMax = (int) (cameraMinY + cameraHeight);
		final int yWholeMin = (int) cameraMinY;
		final int yPartialMax = (int) (yWholeMax + tileHeight);
		final int yPartialMin = (int) (yWholeMin - tileHeight);

		final int xWholeMax = (int) (cameraMinX + cameraWidth);
		final int xWholeMin = (int) cameraMinX;
		final int xPartialMax = (int) (xWholeMax + tileWidth);
		final int xPartialMin =(int) (xWholeMin - tileWidth);

		final float[] cullingVertices = this.mCullingVertices;
		RectangularShapeCollisionChecker.fillVertices(0, 0, this.mWidth, this.mHeight, this.getLocalToSceneTransformation(), cullingVertices);

		for (int j = 0; j < tileRows; j++) {
			for (int i = 0; i < tileColumns; i++) {
				float[] isoCen = this.getIsoTileCentreAt(i, j);
				if(isoCen[1] < yWholeMax &&
						isoCen[1] > yWholeMin ||
						isoCen[1] < yPartialMax && 
						isoCen[1] > yPartialMin
						){				
					if(isoCen[0] < xWholeMax
							&& isoCen[0]  > xWholeMin ||
							isoCen[0]  < xPartialMax
							&& isoCen[0]  > xPartialMin
							){
						this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP, 
								this.getSpriteBatchIndex(i, j) * SpriteBatch.VERTICES_PER_SPRITE, 
								SpriteBatch.VERTICES_PER_SPRITE);
					}
				}
			}
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#drawIsometricCullingTiledSource(org.andengine.opengl.util.GLState, org.andengine.engine.camera.Camera)
	 */	
	@Override
	public void drawIsometricCullingTiledSource(final GLState pGLState, final Camera pCamera){
		/*
		 * Copyright 2009-2011, Thorbjørn Lindeijer <thorbjorn@lindeijer.nl>
		 * <br><a href="http://sourceforge.net/projects/tiled/files/Tiled/0.7.2/tiled-0.7.2-src.zip/">Tiled 0.7.2 source code zip</a>
		 * <br><a href="https://github.com/bjorn/tiled/blob/master/src/libtiled/isometricrenderer.cpp">Tiled 0.8.0 source code - isometricrenderer.cpp on Github</a>
		 * Copied across and changed slightly by Paul Robinson.
		 * Changes being using an int array rather than Point object, 
		 * The original Tiled Java source code used Point objects.
		 */
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int tileHeight = this.mTMXTiledMap.getTileHeight();
		//We also subtract the map origin, other wise culling takes place on screen
		final float cameraMinX = pCamera.getXMin() - this.mTMXTiledMap.getMapOriginX();
		final float cameraMinY = pCamera.getYMin() - this.mTMXTiledMap.getMapOriginY();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();

		int tileStepY = tileHeight / 2 == 0 ? 1 : tileHeight / 2;
		int[] rowItr = screenToTileCoords(cameraMinX, cameraMinY);
		rowItr[0]--;

		// Determine area to draw from clipping rectangle
		int columns = (int) (cameraWidth / tileWidth + 3);
		int rows = (int) ((cameraHeight + tileHeight * 0) / tileStepY + 4);
		// Draw this map layer
		for (int y = 0; y < rows; y++) {
			int[] columnItr = {rowItr[0],rowItr[1]};
			for (int x = 0; x < columns; x++) {
				if(columnItr[0] >= 0 && columnItr[0] < this.mTileColumns){
					if(columnItr[1] >= 0 && columnItr[1] < this.mTileRows){
						this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP, 
								this.getSpriteBatchIndex(columnItr[0], columnItr[1]) * SpriteBatch.VERTICES_PER_SPRITE, 
								SpriteBatch.VERTICES_PER_SPRITE);
					}
				}
				// Advance to the next tile
				columnItr[0]++;
				columnItr[1]--;
			}
			if ((y & 1) > 0) {
				rowItr[0]++;
			} else {
				rowItr[1]++;
			}
		}
	}

	/**
	 * @see org.andengine.extension.tmx.TMXLayer#screenToTileCoords(float, float)
	 */
	@Override
	public int[] screenToTileCoords(float x, float y) {
		/*
		 * Copyright 2009-2011, Thorbjørn Lindeijer <thorbjorn@lindeijer.nl>
		 * <br><a href="http://sourceforge.net/projects/tiled/files/Tiled/0.7.2/tiled-0.7.2-src.zip/">Tiled 0.7.2 source code zip</a>
		 * <br><a href="https://github.com/bjorn/tiled/blob/master/src/libtiled/isometricrenderer.cpp">Tiled 0.8.0 source code - isometricrenderer.cpp on Github</a>
		 * Copied across and changed slightly by Paul Robinson.
		 * Changes being returning an int array rather than Point, 
		 * I didn't want to keep creating a Point object every time 
		 */
		// Translate origin to top-center
		//Do we need this? Paul Robinson
		//x -= this.getTileRows() * (this.mIsoHalfTileWidth);
		int mx = (int) (y + (int)(x / this.tileratio));
		int my = (int) (y - (int)(x / this.tileratio));
		// be square in normal projection)
		return new int[] { 
				(mx < 0 ? mx - this.mTMXTiledMap.getTileHeight() : mx) / this.mTMXTiledMap.getTileHeight(),
				(my < 0 ? my - this.mTMXTiledMap.getTileHeight() : my) / this.mTMXTiledMap.getTileHeight()};
	}
}
