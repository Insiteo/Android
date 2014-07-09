package com.insiteo.sampleapp.render;

import com.insiteo.lbs.common.utils.geometry.Position;
import com.insiteo.lbs.map.render.GenericRTO;

/**
 * @author Insiteo
 *
 */
public class GfxRto extends GenericRTO {
	

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
