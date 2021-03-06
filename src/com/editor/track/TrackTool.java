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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import com.editor.track.CanvasView.BrushType;

public class TrackTool implements MouseListener,MouseMoveListener {
/*	
	private static final int CURVE_DELTA = CanvasView.GRID_SIZE * 4;
*/
	private Canvas canvas;
	private BrushType type;
	private Point startPoint;
	
	//start points.
	private List<Point> startPoints;
	private Map<String,List<Point>> layerData;
	private Map<String,List<Point>> cache;
	
	private CanvasView cView;
	private boolean mouseDown;
	private String selectedKey;
	private String layer;
	
	public TrackTool(CanvasView canvasView) {
		this.cView = canvasView;
		layerData = new HashMap<String,List<Point>>();
		startPoints = new ArrayList<Point>();
		cache = new HashMap<String,List<Point>>();
		setLayer(0);
	}
	
	public void init(Canvas canvas) {
		this.canvas = canvas;
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
	}
	
	public void save(TileManifest manifest) {
		manifest.setTrackData(layerData);
	}
	
	public void open(TileManifest manifest) {
		startPoints.clear();
		cache.clear();
		startPoint = null;
		this.layerData = manifest.getTrackData();
	}

	public void moveLeft() {
		for(Point data : getData()) {
			data.x -= CanvasView.GRID_SIZE;
		}
	}
	
	public void moveRight() {
		for(Point data : getData()) {
			data.x += CanvasView.GRID_SIZE;
		}
	}
	
	public void moveUp() {
		for(Point data : getData()) {
			data.y -= CanvasView.GRID_SIZE;
		}
	}
	
	public void moveDown() {
		for(Point data : getData()) {
			data.y += CanvasView.GRID_SIZE;
		}
	}

	public void setBrush(BrushType type) {
		this.type = type;
		if(type == BrushType.SELECTION) {
			startPoint = null;
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}

	@Override
	public void mouseDown(MouseEvent e) {
		mouseDown = true;
		String key = findKey(e);
		if(e.button == 3) {
			switch(type) {
				case CATMULL_ROM_SPLINE:
					if(startPoint != null) {
						Point point = null;
						if(key != null) {
							int index = key.indexOf("+");
							point = new Point(Integer.parseInt(key.substring(0,index)), Integer.parseInt(key.substring(index+1, key.length())));
						} else {
							point = new Point(getCurrentX(e),getCurrentY(e));
						}
						getData().add(point);
						addToCache(point);
						startPoint = point;
					} else {
						startPoint = getStartPoint(e);
						getData().add(startPoint);
						addToCache(startPoint);
					}
					break;
			}
		} else {
			if(key != null) {
				List<Point> points = cache.get(key);
				if(points != null && !points.isEmpty()) {
					selectedKey = key;
				}
			} else {
				selectedKey = null;
			}
		}
		canvas.redraw();
	}

	private List<Point> getData() {
		List<Point> list = layerData.get(layer);
		if(list == null) {
			list = Collections.EMPTY_LIST;
		}
		return list;
	}

	private void addToCache(Point point) {
		String key2 = point.x+"+"+point.y;
		List<Point> list = cache.get(key2);
		if(list == null) {
			list = new ArrayList<Point>();
			cache.put(key2, list);
		}
		list.add(point);
	}

	private Point getStartPoint(MouseEvent e) {
		Point point = new Point(getCurrentX(e),getCurrentY(e));
		String key = findKey(e);
		if(key != null) {
			int index = key.indexOf("+");
			point = new Point(Integer.parseInt(key.substring(0, index)),
						Integer.parseInt(key.substring(index + 1, key.length())));
		}
		if(!startPoints.contains(point) && !(cache.get(point.x+"+"+point.y) != null)) {
			startPoints.add(point);
		}
		return point;
	}

	private String findKey(MouseEvent e) {
		String key = getCurrentX(e)+"+"+getCurrentY(e);
		if(cache.get(key) != null) {
			return key;
		}
		for(int i = 1;i <= 5;i++) {
			int x1 = getCurrentX(e) + i;
			int x2 = getCurrentX(e) - i;
			for(int j = 1;j <= 5;j++) {
				int y = getCurrentY(e) + j;
				if(cache.get(x1+"+"+y) != null) {
					return x1+"+"+y;
				}
				if(cache.get(x2+"+"+y) != null) {
					return x2+"+"+y;
				}
				y = getCurrentY(e) - j;
				if(cache.get(x1+"+"+y) != null) {
					return x1+"+"+y;
				}
				if(cache.get(x2+"+"+y) != null) {
					return x2+"+"+y;
				}
			}
		}
		return null;
	}

	@Override
	public void mouseUp(MouseEvent e) {
		mouseDown = false;
		selectedKey = null;
	}

	public void paint(GC gc) {
		gc.setLineWidth(2);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		int xBias = (cView.getHorizontalTileOffset() * CanvasView.GRID_SIZE);
		int yBias = (cView.getVerticalTileOffset() * CanvasView.GRID_SIZE);
		for(List<Point> data : layerData.values()) {
			if(data.size() <= 4) {
				for(Point point : data) {
					if((point.x/CanvasView.GRID_SIZE > cView.getHorizontalTileOffset() && point.y/CanvasView.GRID_SIZE > cView.getVerticalTileOffset()
							&& point.y/CanvasView.GRID_SIZE < cView.getRows() + cView.getVerticalTileOffset() && point.x/CanvasView.GRID_SIZE < cView.getCols() + cView.getHorizontalTileOffset())) {
						gc.drawOval(point.x - xBias, point.y - yBias, 6, 6);
					}
				}
			} else {
				for(int i = 1; i < (data.size() - 1) ; i++) {
					Point cp0 = data.get(i-1);
					Point cp1 = data.get(i);
					Point cp2 = data.get(i + 1);
					Point cp3 = null;
					if((i + 2 ) == data.size()) {
						if(BrushType.CATMULL_ROM_SPLINE == type) {
							cp3 = Display.getDefault().getCursorLocation();
							cp3.x = cp3.x - (cp3.x % CanvasView.GRID_SIZE);
							cp3.y = cp3.y - (cp3.y % CanvasView.GRID_SIZE);
						} else {
							break;
						}
					} else {
						cp3 = data.get(i + 2);
					}
					if((cp1.x/CanvasView.GRID_SIZE > cView.getHorizontalTileOffset() && cp1.y/CanvasView.GRID_SIZE > cView.getVerticalTileOffset()
							&& cp1.y/CanvasView.GRID_SIZE < cView.getRows() + cView.getVerticalTileOffset() && cp1.x/CanvasView.GRID_SIZE < cView.getCols() + cView.getHorizontalTileOffset())
							|| (cp2.x/CanvasView.GRID_SIZE > cView.getHorizontalTileOffset() && cp2.y/CanvasView.GRID_SIZE > cView.getVerticalTileOffset()
									&& cp2.y/CanvasView.GRID_SIZE < cView.getRows() + cView.getVerticalTileOffset() && cp2.x/CanvasView.GRID_SIZE < cView.getCols() + cView.getHorizontalTileOffset())) {
						/*
							q(t) = 0.5 * ((2 * P1) + (-P0 + P2) * t + (2*P0 - 5*P1 + 4*P2 - P3) * t2 + (-P0 + 3*P1- 3*P2 + P3) * t3)
						 */
						int cx1 = cp0.x,cx2 = cp1.x,cx3 = cp2.x,cx4 = cp3.x;
						int cy1 = cp0.y,cy2 = cp1.y,cy3 = cp2.y,cy4 = cp3.y;
						int x1 = -1, y1 = -1;
						for(float t = 0 ; t < 1 ; t += .05) {
							float t2 = (float) (t * t);
							float t3 = (float) (t * t * t);
							int x2 = (int) (0.5 * ((2 * cx2) + (-cx1 + cx3) * t + (2*cx1 - 5*cx2 + 4*cx3 - cx4) * t2 + (-cx1 + 3*cx2- 3*cx3 + cx4) * t3));
							int y2 = (int) (0.5 * ((2 * cy2) + (-cy1 + cy3) * t + (2*cy1 - 5*cy2 + 4*cy3 - cy4) * t2 + (-cy1 + 3*cy2- 3*cy3 + cy4) * t3));
							if(x1 > -1) {
								gc.drawLine(x1 - xBias, y1 - yBias, x2 - xBias, y2 - yBias);
							}
							x1 = x2;
							y1 = y2;
						}
						gc.drawLine(x1 - xBias, y1 - yBias, cp2.x - xBias, cp2.y - yBias);
						gc.drawOval(cp0.x - xBias, cp0.y - yBias, 6, 6);
						gc.drawOval(cp1.x - xBias, cp1.y - yBias, 6, 6);
						gc.drawOval(cp2.x - xBias, cp2.y - yBias, 6, 6);
						gc.drawOval(cp3.x - xBias, cp3.y - yBias, 6, 6);
					}
				}
			}
		}
		if(startPoint != null) {
			if((startPoint.x/CanvasView.GRID_SIZE > cView.getHorizontalTileOffset() && startPoint.y/CanvasView.GRID_SIZE > cView.getVerticalTileOffset()
					&& startPoint.y/CanvasView.GRID_SIZE < cView.getRows() + cView.getVerticalTileOffset() && startPoint.x/CanvasView.GRID_SIZE < cView.getCols() + cView.getHorizontalTileOffset())) {
				gc.drawOval(startPoint.x - xBias, startPoint.y - yBias, 6, 6);
				Point location = canvas.toControl(Display.getDefault().getCursorLocation());
				gc.drawLine(startPoint.x - xBias, startPoint.y - yBias, location.x - xBias, location.y - yBias);
			}
		}
	}
	
	@Override
	public void mouseMove(MouseEvent e) {
		if(mouseDown) {
			if(selectedKey != null) {
				List<Point> points = cache.get(selectedKey);
				if(points != null) {
					cache.remove(selectedKey);
					for(Point point : points) {
						point.x = getCurrentX(e);
						point.y = getCurrentY(e);
						selectedKey = point.x+"+"+point.y;
					}
					cache.put(selectedKey, points);
				}
			}
		} else {
			String key = findKey(e);
			if(key != null) {
				canvas.getShell().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
			} else {
				canvas.getShell().setCursor(null);
			}
		}
		canvas.redraw();
	}

	private int getCurrentY(MouseEvent e) {
		return e.y - (e.y % CanvasView.GRID_SIZE) /*+ cView.getVerticalTileOffset() * CanvasView.GRID_SIZE*/;
	}

	private int getCurrentX(MouseEvent e) {
		return e.x - (e.x % CanvasView.GRID_SIZE) /*+ cView.getHorizontalTileOffset() * CanvasView.GRID_SIZE*/;
	}

	public void setLayer(int layer) {
		if(layer >= 0) {
			this.layer = String.valueOf(++layer);
			cache.clear();
			startPoint = null;
			if(layerData.containsKey(this.layer)) {
				for(Point point : getData()) {
					addToCache(point);
				}
			} else {
				List<Point> points = new ArrayList<Point>();
				layerData.put(this.layer, points);
			}
		}
	}

	public void cloneTracks() {
		List<Point> list = layerData.get(layer);
		for(int i = 1; i < 4 ; i++) {
			if(!String.valueOf(i).equals(layer)) {
				List<Point> points = new ArrayList<Point>(list.size());
				for(Point point : list) {
					points.add(new Point(point.x,point.y));
				}
				layerData.put(String.valueOf(i),points);
			}
		}
	}

}