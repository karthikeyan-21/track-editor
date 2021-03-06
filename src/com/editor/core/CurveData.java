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

public class CurveData extends AbstractPrimitiveData implements PrimitiveData {

	//cubic bezier curve
	private int x1,y1;
	private int x2,y2;
	private int x3,y3;
	private int x4,y4;
	
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
	public int getX3() {
		return x3;
	}
	public void setXY3(int x3,int y3) {
		removeFromCache(Math.abs(this.x3)+"+"+Math.abs(this.y3), this);
		this.x3 = x3;
		this.y3 = y3;
		addToCache(Math.abs(this.x3)+"+"+Math.abs(this.y3), this);
	}
	public int getY3() {
		return y3;
	}
	public int getX4() {
		return x4;
	}
	public void setXY4(int x4,int y4) {
		removeFromCache(Math.abs(this.x4)+"+"+Math.abs(this.y4), this);
		this.x4 = x4;
		this.y4 = y4;
		addToCache(Math.abs(this.x4)+"+"+Math.abs(this.y4), this);
	}
	public int getY4() {
		return y4;
	}
}
