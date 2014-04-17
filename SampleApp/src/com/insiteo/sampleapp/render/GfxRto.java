package com.insiteo.sampleapp.render;

import android.graphics.Bitmap;

import com.insiteo.common.rendertouch.GenericRTO;
import com.insiteo.common.utils.geom.Position;

/**
 * @author Insiteo
 *
 */
public class GfxRto extends GenericRTO{
	

	public GfxRto(int id) {
		super(id);
	}

	public GfxRto(int id, Position pos, String name) {
		super(id, pos, name);
	}
	
	public GfxRto(int id, Position pos, String name, int pinResId) {
		super(id, pos, name, pinResId);
	}


}
