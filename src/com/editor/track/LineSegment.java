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

import org.eclipse.swt.graphics.Point;

public class LineSegment {

	public enum OpaqueSide {
		LEFT,ABOVE,
		RIGHT,BELOW; 
	}
	
	private Point fSrc,fDest;
	private OpaqueSide fSide;
	private List<Point> fPoints;
	
	public LineSegment(Point src,Point dest,OpaqueSide opaqueSide) {
		if(src == null || dest == null) {
			throw new RuntimeException("null points");
		}
		if(opaqueSide == null) {
			throw new RuntimeException("null opaque side");
		}
		fSrc = src;
		fDest = dest;
		fSide = opaqueSide;
		fPoints = LineUtil.lineBresenham(src.x, src.y, dest.x, dest.y);
	}
	
	public Point getSource() {
		return fSrc;
	}
	
	public Point getDestination() {
		return fDest;
	}
	
	public OpaqueSide getSide() {
		return fSide;
	}
	
	public List<Point> getPoints(){
		return fPoints;
	}
}
