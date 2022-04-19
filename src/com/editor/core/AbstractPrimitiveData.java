/**
 * BSD Zero Clause License
 *
 * Copyright (c) 2012 Karthikeyan Natarajan (karthikeyan21@gmail.com)
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */
package com.editor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;

public class AbstractPrimitiveData implements PrimitiveData {

	private static final Map<String,List<PrimitiveData>> cache = new HashMap<String,List<PrimitiveData>>();
	
	public static final void clearCache() {
		cache.clear();
	}
	
	protected static final void addToCache(String key,PrimitiveData data) {
		List<PrimitiveData> list = cache.get(key);
		if(list == null) {
			list = new ArrayList<PrimitiveData>();
			cache.put(key, list);
		}
		if(!list.contains(data)) {
			list.add(data);
		}
	}
	
	protected static final void removeFromCache(String key,PrimitiveData data) {
		List<PrimitiveData> list = cache.get(key);
		if(list != null) {
			list.remove(data);
			if(list.isEmpty()) {
				cache.remove(key);
			}
		}
	}
	
	public static final List<PrimitiveData> getData(String key) {
		return cache.get(key);
	}
	
	private Color color;
	private OpaqueSide side;
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public OpaqueSide getOpaqueSide() {
		return side;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void setOpaqueSide(OpaqueSide side) {
		this.side = side;
	}

}
