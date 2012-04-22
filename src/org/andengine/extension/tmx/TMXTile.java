package org.andengine.extension.tmx;

import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.opengl.texture.region.ITextureRegion;

import android.R.integer;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 10:39:48 - 05.08.2010
 */
public class TMXTile {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	int mGlobalTileID;
	private final int mTileRow;
	private final int mTileColumn;
	private final int mTileWidth;
	private final int mTileHeight;
	ITextureRegion mTextureRegion;
	
	private final int mTileZ;
	private final String mOrientation;
	private int mTileXIso = 0;
	private int mTileYIso = 0;
	private int mTileXIsoCentre = 0;
	private int mTileYIsoCentre = 0;

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * 
	 * @param pGlobalTileID
	 * @param pTileZ Z {@link integer} of the Z index
	 * @param pTileColumn
	 * @param pTileRow
	 * @param pTileWidth
	 * @param pTileHeight
	 * @param pTextureRegion
	 */
	public TMXTile(final String pOrientation, final int pGlobalTileID, final int pTileZ, final int pTileColumn, final int pTileRow, final int pTileWidth, final int pTileHeight, final ITextureRegion pTextureRegion) {
		this.mOrientation = pOrientation;
		this.mGlobalTileID = pGlobalTileID;
		this.mTileZ = pTileZ;
		this.mTileRow = pTileRow;
		this.mTileColumn = pTileColumn;
		this.mTileWidth = pTileWidth;
		this.mTileHeight = pTileHeight;
		this.mTextureRegion = pTextureRegion;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getGlobalTileID() {
		return this.mGlobalTileID;
	}

	public int getTileRow() {
		return this.mTileRow;
	}

	public int getTileColumn() {
		return this.mTileColumn;
	}

	public int getTileX() {
		if(this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
			return this.mTileColumn * this.mTileWidth;
		}else if (this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			return this.mTileXIso;
		}else{
			return this.mTileColumn * this.mTileWidth;
		}
	}

	public int getTileY() {
		if(this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
			return this.mTileRow * this.mTileHeight;
		}else if (this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			return this.mTileYIso;
		}else{
			return this.mTileRow * this.mTileHeight;
		}
	}

	public int getTileWidth() {
		return this.mTileWidth;
	}

	public int getTileHeight() {
		return this.mTileHeight;
	}

	public ITextureRegion getTextureRegion() {
		return this.mTextureRegion;
	}
	
	public int getTileZ(){
		return this.mTileZ;
	}
	
	public int getTileXIso() {
		return mTileXIso;
	}

	public void setTileXIso(int mTileXIso) {
		this.mTileXIso = mTileXIso;
	}

	public int getTileYIso() {
		return mTileYIso;
	}

	public void setTileYIso(int mTileYIso) {
		this.mTileYIso = mTileYIso;
	}

	public int getTileXIsoCentre() {
		return mTileXIsoCentre;
	}

	public void setTileXIsoCentre(int mTileXIsoCenter) {
		this.mTileXIsoCentre = mTileXIsoCenter;
	}

	public int getTileYIsoCentre() {
		return mTileYIsoCentre;
	}

	public void setTileYIsoCentre(int mTileYIsoCenter) {
		this.mTileYIsoCentre = mTileYIsoCenter;
	}
	

	/**
	 * Note this will also set the {@link ITextureRegion} with the associated pGlobalTileID of the {@link TMXTiledMap}.
	 * @param pTMXTiledMap
	 * @param pGlobalTileID
	 */
	public void setGlobalTileID(final TMXTiledMap pTMXTiledMap, final int pGlobalTileID) {
		this.mGlobalTileID = pGlobalTileID;
		this.mTextureRegion = pTMXTiledMap.getTextureRegionFromGlobalTileID(pGlobalTileID);
	}

	/**
	 * You'd probably want to call {@link TMXTile#setGlobalTileID(TMXTiledMap, int)} instead.
	 * @param pTextureRegion
	 */
	public void setTextureRegion(final ITextureRegion pTextureRegion) {
		this.mTextureRegion = pTextureRegion;
	}

	public TMXProperties<TMXTileProperty> getTMXTileProperties(final TMXTiledMap pTMXTiledMap) {
		return pTMXTiledMap.getTMXTileProperties(this.mGlobalTileID);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
