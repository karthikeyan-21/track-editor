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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PaintTool implements MouseListener,PaintListener,MouseMoveListener {
	
	private static final String PRIMITIVES = "primitives";
	private static final String CURVE = "curve";
	private static final String LINE = "line";
	private static final String START_POINT = "start-points";

	public enum BrushType {
		LINE,
		CURVE;
	}

	private Canvas canvas;
	private BrushType type;
	
	private Point startPoint;
	
	int ix = 0, iy = 0;
	
	private List<PrimitiveData> data;
	
	//start points.
	private List<Point> startPoints;
	
	private boolean mouseDown;
	private String selectedKey;
	private Map<String,List<PrimitiveData>> cache;
	
	private String currentFile;
	private Image image;
	
	public PaintTool() {
		type = BrushType.LINE;
		data = new ArrayList<PrimitiveData>();
		cache = new HashMap<String,List<PrimitiveData>>();
		startPoints = new ArrayList<Point>();
	}
	
	public void init(Canvas canvas) {
		this.canvas = canvas;
		// Set up the image canvas scroll bars.
		ScrollBar horizontal = canvas.getHorizontalBar();
		horizontal.setVisible(true);
		horizontal.setMinimum(0);
		horizontal.setEnabled(false);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar)event.widget);
			}
		});
		ScrollBar vertical = canvas.getVerticalBar();
		vertical.setVisible(true);
		vertical.setMinimum(0);
		vertical.setEnabled(false);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar)event.widget);
			}
		});

		canvas.addMouseListener(this);
		canvas.addPaintListener(this);
		canvas.addMouseMoveListener(this);
	}
	
	public void createNew() {
		startPoints.clear();
		cache.clear();
		startPoint = null;
		currentFile = null;
		resetScrollBars();
		canvas.redraw();
	}
	
	public void save() {
		/*
		 * 1) start points.
		 * 2) track path.
		 * 3) list of coords.
		 */
		if(currentFile == null && !startPoints.isEmpty()) {
			FileDialog dialog = new FileDialog(canvas.getShell(),SWT.SAVE);
			dialog.setFilterExtensions(new String[]{"*.cord"});
			currentFile = dialog.open();
		}
		if(currentFile != null) {
			FileWriter writer = null;
			try {
				Properties prop = new Properties();
				StringBuilder buffer = new StringBuilder();
				for(Point point : startPoints) {
					buffer.append(point.x).append(",").append(point.y);
				}
				prop.put(START_POINT, buffer.toString());
				buffer.setLength(0);
				for(PrimitiveData data : this.data){
					if(data instanceof LineData) {
						LineData lineData = (LineData) data;
						buffer.append(LINE).append(":").append(lineData.x1+","+lineData.y1+","+lineData.x2+","+lineData.y2);
					} else if(data instanceof CurveData) {
						CurveData curveData = (CurveData) data;
						buffer.append(CURVE).append(":")
							.append(curveData.x1+","+curveData.y1+","+(curveData.x2+curveData.x1)+","+(curveData.y2+curveData.y1)+","+(curveData.x3+curveData.x1)+","+(curveData.y3+curveData.y1)+","+(curveData.x4+curveData.x1)+","+(curveData.y4+curveData.y1));
					}
					buffer.append("\n");
				}
				prop.put(PRIMITIVES, buffer.toString());
				writer = new FileWriter(currentFile);
				prop.store(writer, null);
				writer.close();
			} catch (Exception e) {
				showError(e,"Error saving file");
			} finally {
				if(writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void showError(Exception e,String text) {
		MessageBox messageBox = new MessageBox(canvas.getShell());
		messageBox.setText(text);
		messageBox.setMessage(e.getMessage());
		messageBox.open();
	}
	
	public void open() {
		save();
		createNew();
		FileDialog dialog = new FileDialog(canvas.getShell(),SWT.OPEN);
		dialog.setFilterExtensions(new String[]{"*.cord"});
		String open = dialog.open();
		if(open != null) {
			currentFile = open;
		}
		if(currentFile != null) {
			Properties prop = new Properties();
			FileReader reader = null;
			try {
				reader = new FileReader(currentFile);
				prop.load(reader);
				String property = prop.getProperty(START_POINT);
				if(property != null) {
					StringTokenizer tokenizer = new StringTokenizer(property);
					while(tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						String[] split = token.split(",");
						startPoints.add(new Point(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
					}
				}
				property = prop.getProperty(PRIMITIVES);
				if(property != null) {
					StringTokenizer tokenizer = new StringTokenizer(property);
					while(tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						String[] split = token.split(":");
						if(split[0].equals("line")) {
							LineData line = new LineData();
							String[] data = split[1].split(",");
							line.x1 = Integer.parseInt(data[0]);
							line.y1 = Integer.parseInt(data[1]);
							line.x2 = Integer.parseInt(data[2]);
							line.y2 = Integer.parseInt(data[3]);
							addToCache(line.x1+"+"+line.y1,line);
							addToCache(line.x2+"+"+line.y2, line);
							this.data.add(line);
						} else if(split[0].equals("curve")) {
							CurveData curve = new CurveData();
							String[] data = split[1].split(",");
							curve.x1 = Integer.parseInt(data[0]);
							curve.y1 = Integer.parseInt(data[1]);
							curve.x2 = Integer.parseInt(data[2]) - curve.x1;
							curve.y2 = Integer.parseInt(data[3]) - curve.y1;
							curve.x3 = Integer.parseInt(data[4]) - curve.x1;
							curve.y3 = Integer.parseInt(data[5]) - curve.y1;
							curve.x4 = Integer.parseInt(data[6]) - curve.x1;
							curve.y4 = Integer.parseInt(data[7]) - curve.y1;
							addToCache(curve.x1+"+"+curve.y1, curve);
							addToCache(curve.x2+"+"+curve.y2, curve);
							addToCache(curve.x3+"+"+curve.y3, curve);
							addToCache(curve.x4+"+"+curve.y4, curve);
							this.data.add(curve);
						}
					}
				}
			} catch (Exception e) {
				showError(e,"Error loading file");
			} finally {
				if(reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void addToCache(String key,PrimitiveData line) {
		List<PrimitiveData> list = cache.get(key);
		if(list == null) {
			list = new ArrayList<PrimitiveData>();
			cache.put(key, list);
		}
		if(!list.contains(line)) {
			list.add(line);
		}
	}
	
	public void setBrush(BrushType type) {
		this.type = type;
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
			case LINE:
				if(startPoint != null) {
					LineData data = new LineData();
					data.x1 = startPoint.x;
					data.y1 = startPoint.y;
					startPoint = null;
					if(key != null) {
						int index = key.indexOf("+");
						data.x2 = Integer.parseInt(key.substring(0,index));
						data.y2 = Integer.parseInt(key.substring(index+1, key.length()));
					} else {
						data.x2 = getCurrentX(e);
						data.y2 = getCurrentY(e);
					}
					this.data.add(data);
					addToCache(data.x1+"+"+data.y1,data);
					addToCache(data.x2 + "+" + data.y2, data);
				} else {
					startPoint = getStartPoint(e);
				}
				break;
			case CURVE:
				if(startPoint != null) {
					CurveData data = new CurveData();
					data.x1 = startPoint.x;
					data.y1 = startPoint.y;
					int endx = getCurrentX(e);
					int endy = getCurrentY(e);
					if(key != null) {
						int index = key.indexOf("+");
						endx = Integer.parseInt(key.substring(0,index));
						endy = Integer.parseInt(key.substring(index+1, key.length()));
					}
					data.x4 = endx - startPoint.x;
					data.y4 = endy - startPoint.y;
					startPoint = null;
					data.x2 = data.x3 = 100;
					data.y2 = -100;
					data.y3 = 100;
					this.data.add(data);
					addToCache(data.x1 + "+" + data.y1, data);
					addToCache((data.x1 + data.x2) + "+" + (data.y1 + data.y2), data);
					addToCache((data.x1 + data.x3) + "+" + (data.y1 + data.y3), data);
					addToCache((data.x1 + data.x4) + "+" + (data.y1 + data.y4), data);
				} else {
					startPoint = getStartPoint(e);
				}
				break;
			}
		} else {
			if(key != null) {
				List<PrimitiveData> primitiveData = cache.get(key);
				if(primitiveData != null) {
					selectedKey = key;
				}
			} else {
				selectedKey = null;
			}
		}
		canvas.redraw();
	}

	private Point getStartPoint(MouseEvent e) {
		Point point = new Point(getCurrentX(e),getCurrentY(e));
		String key = findKey(e);
		if(key != null) {
			int index = key.indexOf("+");
			point = new Point(Integer.parseInt(key.substring(0, index)),
						Integer.parseInt(key.substring(index + 1, key.length())));
		}
		if(!startPoints.contains(point) && !cache.containsKey(point.x+"+"+point.y)) {
			startPoints.add(point);
		}
		return point;
	}

	private String findKey(MouseEvent e) {
		String key = getCurrentX(e)+"+"+getCurrentY(e);
		if(cache.containsKey(key)) {
			return key;
		}
		for(int i = 1;i <= 5;i++) {
			int x1 = getCurrentX(e) + i;
			int x2 = getCurrentX(e) - i;
			for(int j = 1;j <= 5;j++) {
				int y = getCurrentY(e) + j;
				if(cache.containsKey(x1+"+"+y)) {
					return x1+"+"+y;
				}
				if(cache.containsKey(x2+"+"+y)) {
					return x2+"+"+y;
				}
				y = getCurrentY(e) - j;
				if(cache.containsKey(x1+"+"+y)) {
					return x1+"+"+y;
				}
				if(cache.containsKey(x2+"+"+y)) {
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

	@Override
	public void paintControl(PaintEvent e) {
		
		Rectangle imageBounds = canvas.getBounds();
		
		image = new Image(e.gc.getDevice(),imageBounds);
		GC gc = new GC(image);
		gc.setAntialias(SWT.ON);
		
		gc.setLineWidth(2);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		for(PrimitiveData _data : data) {
			if(_data instanceof LineData) {
				LineData lineData = (LineData) _data;
				gc.drawLine(lineData.x1, lineData.y1, lineData.x2, lineData.y2);
				gc.drawOval(lineData.x1 - 3, lineData.y1 - 3, 6, 6);
				gc.drawOval(lineData.x2 - 3, lineData.y2 - 3, 6, 6);
			} else if(_data instanceof CurveData) {
				CurveData curveData = (CurveData) _data;
				Transform transform = new Transform(gc.getDevice());
				transform.translate(curveData.x1,curveData.y1);
				gc.setTransform(transform);
				Path path = new Path(gc.getDevice());
				path.cubicTo(curveData.x2, curveData.y2, curveData.x3, curveData.y3, curveData.x4, curveData.y4);
				gc.drawPath(path);
				path.dispose();
				gc.setTransform(null);
				gc.drawOval(curveData.x1, curveData.y1, 6, 6);
				gc.drawOval(curveData.x1 + curveData.x2 - 3, curveData.y1 + curveData.y2 - 3, 6, 6);
				gc.drawOval(curveData.x1 + curveData.x3 - 3, curveData.y1 + curveData.y3 - 3, 6, 6);
				gc.drawOval(curveData.x1 + curveData.x4 - 3, curveData.y1 + curveData.y4 - 3, 6, 6);
			}
		}
		if(startPoint != null) {
			gc.drawOval(startPoint.x - 3, startPoint.y - 3, 6, 6);
			Point location = canvas.toControl(Display.getDefault().getCursorLocation());
			gc.drawLine(startPoint.x, startPoint.y, location.x, location.y);
		}
		
		gc.dispose();
		
		ImageData imageData = image.getImageData();
		e.gc.drawImage(
				image,
				0,
				0,
				imageData.width,
				imageData.height,
				ix + imageData.x,
				iy + imageData.y,
				imageData.width,
				imageData.height);
		image.dispose();
	}
	
	void resizeShell(ControlEvent event) {
		if (canvas.getShell().isDisposed())
			return;
		resizeScrollBars();
	}

	// Reset the scroll bars to 0.
	void resetScrollBars() {
		ix = 0; iy = 0;
		resizeScrollBars();
		canvas.getHorizontalBar().setSelection(0);
		canvas.getVerticalBar().setSelection(0);
	}

	void resizeScrollBars() {
		// Set the max and thumb for the image canvas scroll bars.
		ScrollBar horizontal = canvas.getHorizontalBar();
		ScrollBar vertical = canvas.getVerticalBar();
		Rectangle canvasBounds = canvas.getClientArea();
//		if (width > canvasBounds.width) {
			// The image is wider than the canvas.
			horizontal.setEnabled(true);
			horizontal.setMaximum(canvasBounds.width);
			horizontal.setThumb(canvasBounds.width);
			horizontal.setPageIncrement(canvasBounds.width);
//		}
	/* else {
			// The canvas is wider than the image.
			horizontal.setEnabled(false);
			if (ix != 0) {
				// Make sure the image is completely visible.
				ix = 0;
				canvas.redraw();
			}
		}*/
//		int height = Math.round(imageData != null ? imageData.height : 0);
//		if (height > canvasBounds.height) {
			// The image is taller than the canvas.
			vertical.setEnabled(true);
			vertical.setMaximum(canvasBounds.height);
			vertical.setThumb(canvasBounds.height);
			vertical.setPageIncrement(canvasBounds.height);
//		} else {
//			// The canvas is taller than the image.
//			vertical.setEnabled(false);
//			if (iy != 0) {
//				// Make sure the image is completely visible.
//				iy = 0;
//				canvas.redraw();
//			}
//		}
	}

	/*
	 * Called when the image canvas' horizontal scrollbar is selected.
	 */
	void scrollHorizontally(ScrollBar scrollBar) {
//		if (backgroundImage == null) return;
		Rectangle canvasBounds = canvas.getClientArea();
		ImageData imageData = image.getImageData();
		int width = Math.round(imageData.width );
		int height = Math.round(imageData.height );
		if (width > canvasBounds.width) {
			// Only scroll if the image is bigger than the canvas.
			int x = -scrollBar.getSelection();
			if (x + width < canvasBounds.width) {
				// Don't scroll past the end of the image.
				x = canvasBounds.width - width;
			}
			canvas.scroll(x, iy, ix, iy, width, height, false);
			ix = x;
		}
	}
	
	/*
	 * Called when the image canvas' vertical scrollbar is selected.
	 */
	void scrollVertically(ScrollBar scrollBar) {
//		if (backgroundImage == null) return;
		Rectangle canvasBounds = canvas.getClientArea();
		ImageData imageData = image.getImageData();
		int width = Math.round(imageData.width );
		int height = Math.round(imageData.height );
		if (height > canvasBounds.height) {
			// Only scroll if the image is bigger than the canvas.
			int y = -scrollBar.getSelection();
			if (y + height < canvasBounds.height) {
				// Don't scroll past the end of the image.
				y = canvasBounds.height - height;
			}
			canvas.scroll(ix, y, ix, iy, width, height, false);
			iy = y;
		}
	}

	@Override
	public void mouseMove(MouseEvent e) {
		if(mouseDown) {
			if(selectedKey != null) {

				int index = selectedKey.indexOf("+");
				Point point = new Point(Integer.parseInt(selectedKey.substring(0,index)),
						Integer.parseInt(selectedKey.substring(index + 1, selectedKey.length())));
				if(startPoints.contains(point)){
					startPoints.remove(point);
					startPoints.add(new Point(getCurrentX(e),getCurrentY(e)));
				}
				
				List<PrimitiveData> datas = cache.get(selectedKey);
				List<PrimitiveData> remove = new ArrayList<PrimitiveData>();
				String newSelectedKey = null;
				for(PrimitiveData data : datas) {
					if(data instanceof LineData) {
						LineData lineData = (LineData) data;
						String key = lineData.x1+"+"+lineData.y1;
						if(key.equals(selectedKey)) {
							lineData.x1 = getCurrentX(e);
							lineData.y1 = getCurrentY(e);
							remove.add(lineData);
							newSelectedKey = lineData.x1+"+"+lineData.y1;
							addToCache(newSelectedKey, lineData);
						}
						key = lineData.x2+"+"+lineData.y2;
						if(key.equals(selectedKey)) {
							lineData.x2 = getCurrentX(e);
							lineData.y2 = getCurrentY(e);
							remove.add(lineData);
							newSelectedKey = lineData.x2+"+"+lineData.y2;
							addToCache(newSelectedKey, lineData);
						}
					} else if(data instanceof CurveData) {
						CurveData curveData = (CurveData) data;
						String key = curveData.x1+"+"+curveData.y1;
						if(key.equals(selectedKey)) {
							int diffx = curveData.x1 - getCurrentX(e);
							int diffy = curveData.y1 - getCurrentY(e);
							curveData.x1 = getCurrentX(e);
							curveData.y1 = getCurrentY(e);
							remove.add(curveData);
							newSelectedKey = curveData.x1+"+"+curveData.y1;
							addToCache(newSelectedKey, curveData);
							removeFromCache(curveData.x2+"+"+curveData.y2,curveData);
							curveData.x2 += diffx;
							curveData.y2 += diffy;
							addToCache(curveData.x2+"+"+curveData.y2,curveData);
							removeFromCache(curveData.x3+"+"+curveData.y3,curveData);
							curveData.x3 += diffx;
							curveData.y3 += diffy;
							addToCache(curveData.x3+"+"+curveData.y3,curveData);
							removeFromCache(curveData.x4+"+"+curveData.y4,curveData);
							curveData.x4 += diffx;
							curveData.y4 += diffy;
							addToCache(curveData.x4+"+"+curveData.y4,curveData);
						}
						
						key = (curveData.x1+curveData.x2)+"+"+(curveData.y1+curveData.y2);
						if(key.equals(selectedKey)) {
							curveData.x2 = getCurrentX(e) - curveData.x1;
							curveData.y2 =  getCurrentY(e) - curveData.y1;
							remove.add(curveData);
							newSelectedKey = (curveData.x1+curveData.x2)+"+"+(curveData.y1+curveData.y2);
							addToCache(newSelectedKey, curveData);
						}
						
						key = (curveData.x1+curveData.x3)+"+"+(curveData.y1+curveData.y3);
						if(key.equals(selectedKey)) {
							curveData.x3 = getCurrentX(e) - curveData.x1;
							curveData.y3 = getCurrentY(e) - curveData.y1;
							remove.add(curveData);
							newSelectedKey = (curveData.x1+curveData.x3)+"+"+(curveData.y1+curveData.y3);
							addToCache(newSelectedKey, curveData);
						}
						
						key = (curveData.x1+curveData.x4)+"+"+(curveData.y1+curveData.y4);
						if(key.equals(selectedKey)) {
							curveData.x4 = getCurrentX(e) - curveData.x1;
							curveData.y4 = getCurrentY(e) - curveData.y1;
							remove.add(curveData);
							newSelectedKey = (curveData.x1+curveData.x4)+"+"+(curveData.y1+curveData.y4);
							addToCache(newSelectedKey, curveData);
						}
					}
				}
				selectedKey = newSelectedKey;
				datas.removeAll(remove);
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

	private void removeFromCache(String selectedKey, PrimitiveData data) {
		List<PrimitiveData> list = cache.get(selectedKey);
		if(list != null) {
			list.remove(data);
			if(list.isEmpty()) {
				cache.remove(selectedKey);
			}
		}
	}

	private int getCurrentY(MouseEvent e) {
		return e.y + canvas.getVerticalBar().getSelection();
	}

	private int getCurrentX(MouseEvent e) {
		return e.x + canvas.getHorizontalBar().getSelection();
	}

	public void render() {
		final class RenderDialog extends Dialog implements PaintListener,KeyListener {
			private static final int HEIGHT = 320;
			private static final int WIDTH = 240;
			private Canvas canvas;
			private Rectangle rect;
			private int translateX,translateY;
			public RenderDialog() {
				super(PaintTool.this.canvas.getShell());
			}
		 	public Object open () {
		 		Shell parent = getParent();
		 		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		 		shell.setText(getText());
		 		
		 		canvas = new Canvas(shell,SWT.DOUBLE_BUFFERED);
		 		canvas.addPaintListener(this);
		 		canvas.addKeyListener(this);
		 		canvas.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		 		canvas.setSize(WIDTH , HEIGHT );
		 		
		 		shell.pack();
		 		shell.open();
		 		canvas.redraw();
		 		
		 		Display display = parent.getDisplay();
		 		while (!shell.isDisposed()) {
		 			if (!display.readAndDispatch()) display.sleep();
		 		}
		 		return null;
		 	}
			@Override
			public void paintControl(PaintEvent e) {
				
				int x = WIDTH / 2;
				int y = HEIGHT / 2;

				List<List<PrimitiveData>> datas = new ArrayList<List<PrimitiveData>>();
				for(Point point : startPoints) {
					List<PrimitiveData> data = new ArrayList<PrimitiveData>();
					if(rect == null) {
						rect = new Rectangle(point.x - x,point.y - y,WIDTH,HEIGHT);
					}
					populateCurrentData(point.x,point.y,data);
					if(!data.isEmpty()) {
						datas.add(data);
					}
				}
				e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

				for(List<PrimitiveData> listData : datas) {
					Path path = new Path(Display.getDefault());
					PrimitiveData data = listData.get(0);
					int _x = 0,_y = 0;
					if(data instanceof LineData) {
						LineData lineData = (LineData) data;
						_x = lineData.x1;
						_y = lineData.y2;
					} else if(data instanceof CurveData) {
						CurveData curveData = (CurveData) data;
						_x = curveData.x1;
						_y = curveData.y2;
					}
					for(PrimitiveData _data : listData) {
						if(data instanceof LineData) {
							LineData lineData = (LineData) _data;
							path.lineTo(lineData.x2 - _x, lineData.y2 - _y);
						} else if(data instanceof CurveData) {
							CurveData curveData = (CurveData) _data;
							path.cubicTo(curveData.x2 - _x, curveData.y2 - _y, curveData.x3 - _x,
									curveData.y3 - _y, curveData.x4 - _x, curveData.y4 - _y);
						}
					}
					Transform t = new Transform(Display.getDefault());
					t.translate(_x + translateX, _y + translateY);
					System.out.println("PaintTool.render().RenderDialog.paintControl(): "+datas);
					e.gc.setTransform(t);
					e.gc.drawPath(path);
					t.dispose();
				}
			}
			
			private void populateCurrentData(int x,int y,List<PrimitiveData> currentData) {
				List<PrimitiveData> list = cache.get(x+"+"+y);
				if(list != null) {
					for(PrimitiveData data : list) {
						if(currentData.contains(data)) {
							continue;
						}
						if(data instanceof LineData) {
							LineData lineData = (LineData) data;
							if(lineData.x1 == x &&  lineData.y1 == y) {
//								if(rect.contains(lineData.x1, lineData.y1)){
//									addData(lineData,currentData);
//									populateCurrentData(lineData.x2, lineData.y2,currentData);
//								}
//								if(rect.contains(lineData.x2, lineData.y2)) {
									addData(lineData,currentData);
									populateCurrentData(lineData.x2, lineData.y2,currentData);
//								}
							}
						} else if(data instanceof CurveData) {
							CurveData curveData = (CurveData) data;
							if(curveData.x1 == x &&  curveData.y1 == y) {
//								if(rect.contains(curveData.x1, curveData.y1)){
//									addData(curveData,currentData);
//								}
//								if(rect.contains(curveData.x2, curveData.y2)) {
									addData(curveData,currentData);
									populateCurrentData(curveData.x2, curveData.y2,currentData);
//								}
							}
						}
					}
				}
			}
			private void addData(PrimitiveData data,List<PrimitiveData> currentData) {
				if(!currentData.contains(data)) {
					currentData.add(data);
				}
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.ARROW_LEFT) {
					translateX += 5;
				} else if(e.keyCode == SWT.ARROW_RIGHT) {
					translateX -= 5;
				} else if(e.keyCode == SWT.ARROW_UP) {
					translateY += 5;
				} else if(e.keyCode == SWT.ARROW_DOWN) {
					translateY -= 5;
				}
				canvas.redraw();
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
		try {
			new RenderDialog().open(); 
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public void size() {
		final class SizeDialog extends Dialog implements ModifyListener {
			private Rectangle bounds;
			private Text widthText;
			private Text heightText;
			public SizeDialog(Shell parent,Rectangle bounds) {
				super(parent);
				this.bounds = bounds;
			}
			public Object open () {
				Shell parent = getParent();
				Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				shell.setSize(200, 200);
				shell.setText(getText());
				shell.setLayout(new GridLayout());
				Composite composite = new Composite(shell,SWT.NONE);
				composite.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,true,false));
				composite.setLayout(new GridLayout(2,true));
				new Label(composite,SWT.NONE).setText("Width: ");
				widthText = new Text(composite,SWT.BORDER);
				widthText.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,true,false));
				new Label(composite,SWT.NONE).setText("Height: ");
				heightText = new Text(composite,SWT.BORDER);
				heightText.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,true,false));
				widthText.setText(String.valueOf(bounds.width));
				heightText.setText(String.valueOf(bounds.height));
				
				widthText.addModifyListener(this);
				heightText.addModifyListener(this);
				// Your code goes here (widget creation, set result, etc).
				shell.open();
				Display display = parent.getDisplay();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) display.sleep();
				}
				return bounds;
			}
			@Override
			public void modifyText(ModifyEvent e) {
				if(widthText.getText() != null && widthText.getText().length() > 0) {
					bounds.width = Integer.parseInt(widthText.getText());
				}
				if(heightText.getText() != null && heightText.getText().length() > 0) {
					bounds.height = Integer.parseInt(heightText.getText());
				}
			}
		}
		SizeDialog dialog = new SizeDialog(this.canvas.getShell(),this.canvas.getBounds());
		Object result = dialog.open();
		if(result instanceof Rectangle) {
			Rectangle rectangle = (Rectangle) result;
			canvas.setBounds(rectangle);
			resizeScrollBars();
		}
	}

}
