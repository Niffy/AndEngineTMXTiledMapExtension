/**
 * 
 */
package org.andengine.extension.tmx.util.constants;

/**
 * Constants for Isometric maps
 * @author Paul Robinson
 */
public interface TMXIsometricConstants {

	// ===========================================================
	// Drawing Options
	// For use to help determine the draw method (for isometric tiles)
	// ===========================================================
	/**
	 * No culling takes place, draws every single tile on the map.
	 * <br>Alternatives
	 * <br> {@link #DRAW_METHOD_ISOMETRIC_CULLING_SLIM}
	 * <br> {@link #DRAW_METHOD_ISOMETRIC_CULLING_PADDING}
	 */
	public static final int DRAW_METHOD_ISOMETRIC_ALL = 1;
	/**
	 * Culling does take place, but only whole tiles are visible on the screen.
	 * <br>This is inefficient, as it loops through every single tile and checks
	 * that its centre point is within the space of the camera.
	 * <br>
	 * If you also want to draw a tile that is partly on the screen, use
	 * {@link #DRAW_METHOD_ISOMETRIC_CULLING_PADDING}
	 */
	public static final int DRAW_METHOD_ISOMETRIC_CULLING_SLIM = 2;
	/**
	 * Culling does take place, tiles which are partly on the screen are also drawn.
	 * <br>This is inefficient, as it loops through every single tile and checks
	 * that its centre point is within the space of the camera.
	 * <br>
	 * If you also want to draw complete tiles on the screen, use
	 * {@link #DRAW_METHOD_ISOMETRIC_CULLING_SLIM}
	 */
	public static final int DRAW_METHOD_ISOMETRIC_CULLING_PADDING = 3;
}
