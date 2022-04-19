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
package com.editor.track;

import java.util.List;

import org.eclipse.swt.graphics.GC;

public class Tile {

	private Vector fPosition;
	private List<TileInfo> tileInfo;
	
	public Tile(List<TileInfo> info) {
		tileInfo = info;
	}
	
	public void setPosition(Vector position) {
		fPosition = position;
	}
	
	public void draw(GC gc) {
		if(fPosition != null ) {
			for(int i = tileInfo.size()-1;i >= 0;i--) {
				TileInfo info = tileInfo.get(i);
				if(info != null) {
					gc.drawImage(info.getImage(), (int)fPosition.x, (int)fPosition.y);
				}
			}
		}
	}

}
