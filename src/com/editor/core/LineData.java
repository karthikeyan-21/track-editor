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

public class LineData extends AbstractPrimitiveData implements PrimitiveData {

	private int x1,y1;
	private int x2,y2;
	
	public int getX1() {
		return x1;
	}

	public void setXY1(int x1,int y1) {
		removeFromCache(Math.abs(this.x1)+"+"+Math.abs(this.y1), this);
		this.x1 = x1;
		this.y1 = y1;
		addToCache(Math.abs(this.x1)+"+"+Math.abs(this.y1), this);
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

	public void setXY2(int x2,int y2) {
		removeFromCache(Math.abs(this.x2)+"+"+Math.abs(this.y2), this);
		this.x2 = x2;
		this.y2 = y2;
		addToCache(Math.abs(this.x2)+"+"+Math.abs(this.y2), this);
	}

	public int getY2() {
		return y2;
	}

	@Override
	public String toString() {
		return "x1: "+x1+",y1: "+y1+",x2: "+x2+",y2: "+y2;
	}
}
