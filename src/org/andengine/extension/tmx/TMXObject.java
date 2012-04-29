package org.andengine.extension.tmx;

import org.andengine.extension.tmx.util.SAXUtilsObject;
import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 11:21:01 - 29.07.2010
 */
public class TMXObject implements TMXConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final String mName;
	private final String mType;
	private final int mX;
	private final int mY;
	private final int mWidth;
	private final int mHeight;
	private final TMXProperties<TMXObjectProperty> mTMXObjectProperties = new TMXProperties<TMXObjectProperty>();
	private int[][] mPolygon_points;
	private int[][] mPolyline_points;
	private final int mGID;

	// ===========================================================
	// Constructors
	// ===========================================================

	public TMXObject(final Attributes pAttributes) {
		this.mName = pAttributes.getValue("", TMXConstants.TAG_OBJECT_ATTRIBUTE_NAME);
		this.mType = pAttributes.getValue("", TMXConstants.TAG_OBJECT_ATTRIBUTE_TYPE);
		this.mX = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_OBJECT_ATTRIBUTE_X);
		this.mY = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_OBJECT_ATTRIBUTE_Y);
		this.mWidth = SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_OBJECT_ATTRIBUTE_WIDTH, 0);
		this.mHeight = SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_OBJECT_ATTRIBUTE_HEIGHT, 0);
		this.mPolygon_points = null;
		this.mPolyline_points = null;
		this.mGID = SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_OBJECT_ATTRIBUTE_GID, -1);
	}

	/**
	 * Add a polygon to the object.
	 * @param pAttributes {@link Attributes} to parse.
	 */
	public void addPolygon(final Attributes pAttributes){
		this.mPolygon_points = SAXUtilsObject.getIntPoints(pAttributes, TMXConstants.TAG_OBJECT_ATTRIBUTE_POLY_POINTS);
	}

	/**
	 * Add a polyline to the object
	 * @param pAttributes
	 */
	public void addPolyline(final Attributes pAttributes){
		this.mPolyline_points = SAXUtilsObject.getIntPoints(pAttributes, TMXConstants.TAG_OBJECT_ATTRIBUTE_POLY_POINTS);
	}


	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getName() {
		return this.mName;
	}

	public String getType() {
		return this.mType;
	}

	public int getX() {
		return this.mX;
	}

	public int getY() {
		return this.mY;
	}

	public int getWidth() {
		return this.mWidth;
	}

	public int getHeight() {
		return this.mHeight;
	}
	
	/**
	 * Get GID of this tile object.
	 * @return {@link Integer} of the GID of what tile to draw. <b>OR</b>
	 * returns <code>-1</code> if this is not a tile object.
	 */
	public int getGID(){
		return this.mGID;
	}
	
	/**
	 * Get the Polygons pixel point coordinates for this object.
	 * The points are relative to the object X and Y pixel coordinates.
	 * <br>
	 * The first element in the 2D array [i][j] I is the order of the 
	 * point as it is read in. While J is [0] = X [1] = Y pixel location.
	 * 
	 * @return {@link Integer} 2 Dimensional array of the points or <code>NULL</code>
	 * if there is none or they could not be parsed correctly.
	 */
	public int[][] getPolygonPoints(){
		return this.mPolygon_points;
	}
	
	/**
	 * Get the Polylines pixel point coordinates for this object.
	 * The points are relative to the object X and Y pixel coordinates.
	 * <br>
	 * The first element in the 2D array [i][j] I is the order of the 
	 * point as it is read in. While J is [0] = X [1] = Y pixel location.
	 * 
	 * @return {@link Integer} 2 Dimensional array of the points or <code>NULL</code>
	 * if there is none or they could not be parsed correctly.
	 */
	public int[][] getPolylinePoints(){
		return this.mPolyline_points;
	}

	public void addTMXObjectProperty(final TMXObjectProperty pTMXObjectProperty) {
		this.mTMXObjectProperties.add(pTMXObjectProperty);
	}

	public TMXProperties<TMXObjectProperty> getTMXObjectProperties() {
		return this.mTMXObjectProperties;
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
