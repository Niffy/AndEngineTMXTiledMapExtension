package org.andengine.extension.tmx;

import org.xml.sax.Attributes;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 11:19:44 - 29.07.2010
 */
public class TMXObjectProperty extends TMXProperty {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public TMXObjectProperty(final Attributes pAttributes) {
		super(pAttributes);
	}
	
	/**
	 * Copy constructor
	 * @param pTMXObjectProperty {@link TMXObjectProperty} to copy
	 */
	public TMXObjectProperty(final TMXObjectProperty pTMXObjectProperty){
		super(pTMXObjectProperty);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

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
