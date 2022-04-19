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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;

import com.editor.track.TileMatrix.Grid;

public class CanvasView extends AbstractView {

	public enum BrushType {
		CATMULL_ROM_SPLINE,
		SELECTION;
	}
	
	public static final int GRID_SIZE = 32;
	
	private int zIndex;
	private int rows,cols;
	private int x1,y1,x2,y2;
	private int vTileOffset,hTileOffset;
	private boolean showGrid;
	
	private Image image;
	private Label status;
	private Canvas canvas;
	private BrushType type;
	private ScrolledComposite fScrolledComposite;

	private TileInfo tile;
	private TrackTool trackTool;
	private TileMatrix tileMatrix;
	private Map<Point,TileInfo> copyMap;
	
	private Image backgroundImage;

	private Color background;
	
	public CanvasView() {
		rows = cols = 20;
		showGrid = true;
		tileMatrix = new TileMatrix(rows,cols);
		copyMap = new LinkedHashMap<Point,TileInfo>();
		trackTool = new TrackTool(this);
		setBrush(BrushType.SELECTION);
	}
	
	public void setBackgroundImage(Image image) {
		if(backgroundImage != null) {
			backgroundImage.dispose();
		}
		this.backgroundImage = image;
	}
	
	public int getVerticalTileOffset() {
		return vTileOffset;
	}

	public int getHorizontalTileOffset() {
		return hTileOffset;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	@Override
	public void save(TileManifest manifest) {
		if(background != null) {
			manifest.setBackgroundColor(background.getRGB());
		}
		manifest.setGridDimension(rows, cols);
		manifest.setGrids(tileMatrix);
		trackTool.save(manifest);
		isDirty = false;
	}

	@Override
	protected void createControl(CTabItem item) {
		item.setText("Canvas View: ");
		
		Color systemColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		Composite client = createComposite(item);
		client.setBackground(systemColor);
		
		fScrolledComposite = new ScrolledComposite(client,SWT.H_SCROLL | SWT.V_SCROLL);
		fScrolledComposite.setBackground(systemColor);
		fScrolledComposite.setAlwaysShowScrollBars(true);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		fScrolledComposite.setLayout(gridLayout);
		fScrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		canvas = new Canvas(fScrolledComposite,SWT.DOUBLE_BUFFERED);
		canvas.setBackground(systemColor);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		canvas.setSize(canvas.computeSize(SWT.FILL, SWT.FILL));
		fScrolledComposite.setContent(canvas);
		trackTool.init(canvas);
		
		status = new Label(client,SWT.NONE);
		status.setBackground(systemColor);
		status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		item.setControl(client);
		
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				paint(e.gc);
			}
		});
		CanvasMouseListener listener = new CanvasMouseListener();
		canvas.addMouseMoveListener(listener);
		canvas.addMouseListener(listener);
		canvas.addDragDetectListener(listener);
		ScrollBar verticalBar = fScrolledComposite.getVerticalBar();
		verticalBar.setPageIncrement(GRID_SIZE);
		ScrollBar horizontalBar = fScrolledComposite.getHorizontalBar();
		horizontalBar.setPageIncrement(GRID_SIZE);
		ScrollBarListener _listener = new ScrollBarListener();
		verticalBar.addSelectionListener(_listener);
		horizontalBar.addSelectionListener(_listener);
	}

	private class ScrollBarListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {

			Point origin = fScrolledComposite.getOrigin();
			vTileOffset = (origin.y / GRID_SIZE);
			hTileOffset = (origin.x / GRID_SIZE);
			fScrolledComposite.getHorizontalBar().setSelection(hTileOffset * GRID_SIZE); 
			fScrolledComposite.getVerticalBar().setSelection(vTileOffset * GRID_SIZE);
			redraw();
		}
	}
	
	private class CanvasMouseListener implements DragDetectListener,MouseMoveListener,MouseListener {

		private boolean selection;{
			resetSelection();
		}
		
		@Override
		public void dragDetected(DragDetectEvent e) {
			if(BrushType.SELECTION != type) {
				return;
			}
			if(e.button == 1) {
				selection = true;
				x1 = e.x/GRID_SIZE;
				y1 = e.y/GRID_SIZE;
			}
		}

		@Override
		public void mouseMove(MouseEvent e) {
			int col = e.x/GRID_SIZE + 1;
			int row = e.y/GRID_SIZE + 1;
			if(row <= rows && col <= cols) {
				status.setText("Mouse at row: "+row+", col: "+col);
			} else {
				status.setText("Mouse at x: "+e.x+", y: "+e.y);
			}
			if(selection) {
				x2 = e.x / GRID_SIZE;
				y2 = e.y / GRID_SIZE;
				redraw();
			}
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if(BrushType.SELECTION != type) {
				return;
			}
			if(x2 > -1 && y2 > -1) {
				int x = e.x / GRID_SIZE;
				int y = e.y / GRID_SIZE;
				if(x1 < x2 && (x < x1 || x > x2 )) {
					resetSelection();
				} else if(x1 > x2 && (x < x2 || x > x1)) {
					resetSelection();
				}
				if(y1 < y2 && (y < y1 || y > y2)) {
					resetSelection();
				} else if(y1 > y2 && (y < y2 || y > y1)) {
					resetSelection();
				}
			}
		}

		private void resetSelection() {
			x1 = x2 = y1 = y2 = -1;
		}

		@Override
		public void mouseUp(MouseEvent e) {
			if(BrushType.SELECTION != type) {
				return;
			}
			if(e.button == 1) {
				if(!copyMap.isEmpty()) {
					copy(e);
				} else {
					setTiles(e);
				}
				if(selection) {
					x2 = e.x / GRID_SIZE;
					y2 = e.y / GRID_SIZE;
					selection = false;
				}
			} else if(e.button == 3 && (x1 > -1 && x2 > -1 && y1 > -1 && y2 > -1)) {
				showMenu();
			}
		}
	}
	
	private void copy(MouseEvent e) {
		int x = e.x/GRID_SIZE;
		int y = e.y/GRID_SIZE;
		int _x = -1,_y = -1;
		int i = 0,j = 0;
		for(Entry<Point,TileInfo> entry : copyMap.entrySet()) {
			Point point = entry.getKey();
			if(_x != -1 && _y != -1) {
				if(point.x > _x) {
					i++;
				} else if(point.x < _x) {
					i = 0;
				}
				if(point.y > _y) {
					j++;
				} else {
					j = 0;
				}
			}
			tileMatrix.add(x + i,y + j,zIndex,entry.getValue());
			_x = point.x;
			_y = point.y;
		}
		redraw();
		isDirty = true;
	}
	
	private void showMenu() {
		Menu menu = new Menu(canvas);
		MenuItem item = new MenuItem(menu,SWT.NONE);
		item.setText("Copy");
		menu.setVisible(true);
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copyMap.clear();
				if(x1 <= x2) {
					if(y1 <= y2) {
						fillCopyMap(x1,y1,x2,y2);
					} else if(y1 > y2) {
						fillCopyMap(x1, y2, x2, y1);
					}
				} else if(x1 > x2) {
					if(y1 <= y2) {
						fillCopyMap(x2, y1, x1, y2);
					} else if(y1 > y2) {
						fillCopyMap(x2, y2, x1, y1);
					}
				}
			}
			private void fillCopyMap(int x1, int y1,int x2, int y2) {
				for(int i = x1 ; i <= x2 ; i++) {
					for(int j = y1 ; j <= y2 ; j++) {
						copyMap.put(new Point(i,j), tileMatrix.get(i,j,zIndex));
					}
				}
			}
		});
	}

	private void setTiles(MouseEvent e) {
		if(x1 > -1 && y1 > -1 && x2 > -1 && y2 > -1) {
			if(x1 <= x2) {
				if(y1 <= y2) {
					fillTiles(x1,y1,x2,y2);
				} else if(y1 > y2) {
					fillTiles(x1, y2, x2, y1);
				}
			} else if(x1 > x2) {
				if(y1 <= y2) {
					fillTiles(x2, y1, x1, y2);
				} else if(y1 > y2) {
					fillTiles(x2, y2, x1, y1);
				}
			}
		} else {
			int x = e.x/GRID_SIZE;
			int y = e.y/GRID_SIZE;
			if(tile != null && tile.bag != null) {
				for(int i=0;i < tile.bag.infoList.size();i++) {
					List<TileInfo> list = tile.bag.infoList.get(i);
					for(int j = 0;j < list.size();j++) {
						TileInfo tInfo = list.get(j);
						tileMatrix.add(x + j, y + i,zIndex, tInfo);
					}
				}
			} else {
				tileMatrix.add(x, y,zIndex, tile);
			}
		}
		redraw();
		isDirty = true;
	}

	private void fillTiles(int x1, int y1, int x2, int y2) {
		for(int i = x1 ; i <= x2 ; i++) {
			for(int j = y1 ; j <= y2 ; j++) {
				tileMatrix.add(i,j,zIndex,tile);
			}
		}
	}
	
	public void setTile(TileInfo tile) {
		this.tile = tile;
		copyMap.clear();
	}
	
	public void redraw() {
		canvas.redraw();
		canvas.update();
	}

	private void paint(GC gc) {
		Color color = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		Rectangle clientArea = fScrolledComposite.getClientArea();
		int visibleCols = clientArea.width/GRID_SIZE;
		int visibleRows = (clientArea.height/GRID_SIZE) ;
		visibleCols = (visibleCols > cols) ? cols : visibleCols;
		visibleRows = (visibleRows >  rows) ? rows : visibleRows;
		int width = visibleCols * GRID_SIZE;
		int height = visibleRows * GRID_SIZE;
		if(image != null) {
			image.dispose();
		}
		image =	new Image(gc.getDevice(),new Rectangle(0,0,width + 1,height + 1));
		GC _gc = new GC(image);
		if(background != null) {
			_gc.setBackground(background);
			_gc.fillRectangle(0, 0, width + 1, height + 1);
		}
		if(backgroundImage != null) {
			Rectangle bounds = backgroundImage.getBounds();
			int srcX = hTileOffset * GRID_SIZE;
			int srcY = vTileOffset * GRID_SIZE;
			int srcWidth = Math.min(bounds.width - srcX, width);
			int srcHeight = Math.min(bounds.height - srcY, height);
			if(srcWidth > 0 && srcHeight > 0) {
				_gc.drawImage(backgroundImage,srcX,srcY,srcWidth,srcHeight,0,0,srcWidth,srcHeight);
			}
		}
		if(showGrid) {
			_gc.setForeground(color);
			_gc.drawRectangle(0, 0, width, height);
			for(int i = 0;i != width ; i += GRID_SIZE) {
				if(i >= width) {
					i = width;
				}	
				_gc.drawLine(i, 0, i, height);
			}
			for(int i = 0;i != height ; i += GRID_SIZE) {
				_gc.drawLine(0, i, width, i);
			}
		}
//		System.out.println("Rendering from x["+hTileOffset+"-"+(hTileOffset+visibleCols)+",y["+vTileOffset+"-"+(vTileOffset+visibleRows)+"]");
		for(int x = 0;x < visibleCols;x++) {
			for(int y = 0;y < visibleRows;y++) {
				Grid grid = null;
				try {
					grid = tileMatrix.getGrid(x + hTileOffset,y + vTileOffset);
				} catch (Exception e) {
					break;
				}
				if(grid == null) {
					break;
				}
				if(zIndex == -1) {
					for(int i = 2;i >= 0;i--) {
						TileInfo tile2 = grid.getTileInfo(i);
						if(tile2 != null) {
							_gc.drawImage(tile2.getImage(), x * GRID_SIZE, y * GRID_SIZE);
						}
					}
				} else {
					TileInfo tile2 = grid.getTileInfo(zIndex);
					if(tile2 != null) {
						int _x = x * GRID_SIZE;
						int _y = y * GRID_SIZE;
						_gc.drawImage(tile2.getImage(),_x,_y);
					}
				}
			}
		}
		if(showGrid && x1 > -1 && y1 > -1 && x2 > -1 && y2 > -1) {
			if(x1 <= x2) {
				if(y1 <= y2) {
					drawGrids(_gc,x1,y1,x2,y2);
				} else if(y1 > y2) {
					drawGrids(_gc,x1, y2, x2, y1);
				}
			} else if(x1 > x2) {
				if(y1 <= y2) {
					drawGrids(_gc,x2, y1, x1, y2);
				} else if(y1 > y2) {
					drawGrids(_gc,x2, y2, x1, y1);
				}
			}
		}
		trackTool.paint(_gc);
		_gc.dispose();
		gc.drawImage(image, fScrolledComposite.getOrigin().x,fScrolledComposite.getOrigin().y);
		canvas.setSize(cols * GRID_SIZE + 1,rows * GRID_SIZE + 1);
		canvas.getParent().layout();
	}

	private void drawGrids(GC gc,int x1, int y1, int x2, int y2) {
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		for(int i = x1 ; i <= x2 ; i++) {
			for(int j = y1 ; j <= y2 ; j++) {
				gc.drawRectangle(i * GRID_SIZE,j * GRID_SIZE,GRID_SIZE,GRID_SIZE);
			}
		}
	}

	@Override
	public void setInput(TileManifest manifest) {
		tileMatrix.clear();
		if(manifest != null) {
			tileMatrix = manifest.getGrids();
			int[] dimension = manifest.getGridDimension();
			rows = dimension[0];
			cols = dimension[1];
			setBackgroundColor(manifest.getBackgroundColor());
		}
		trackTool.open(manifest);
		redraw();
	}

	public boolean isGridVisible() {
		return showGrid;
	}

	public void showGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	public void addColumnToRight() {
		tileMatrix.addColumn(cols);
		cols++;
	}
	
	public void addColumnToLeft() {
		tileMatrix.addColumn(0);
		trackTool.moveRight();
		cols++;
	}

	public void removeColumnFromRight() {
		tileMatrix.removeColumn(cols-1);
		cols--;
	}
	
	public void removeColumnFromLeft() {
		tileMatrix.removeColumn(0);
		trackTool.moveLeft();
		cols--;
	}

	public void addRowToBottom() {
		tileMatrix.addRow(rows);
		rows++;
	}
	
	public void addRowToTop() {
		tileMatrix.addRow(0);
		trackTool.moveDown();
		rows++;
	}

	public void removeRowFromBottom() {
		tileMatrix.removeRow(rows-1);
		rows--;
	}
	
	public void removeRowFromTop() {
		tileMatrix.removeRow(0);
		trackTool.moveUp();
		rows--;
	}

	public void showLayer(int i) {
		zIndex = i;
		trackTool.setLayer(i);
	}

	public void setBrush(BrushType type) {
		this.type = type;
		trackTool.setBrush(type);
	}

	public void fill(int x, int y) {
		x = Math.abs(rows - x);
		y = Math.abs(cols - y);
		for(int i = 0; i < x ; i++) {
			if(i < x/2) {
				addRowToTop();
			} else {
				addRowToBottom();
			}
		}
		for(int i = 0; i < y ; i++) {
			if(i < y/2) {
				addColumnToLeft();
			} else {
				addColumnToRight();
			}
		}
		tileMatrix.fillGrids(x, y);
	}

	public void cloneTrack() {
		trackTool.cloneTracks();
	}

	public void setBackgroundColor(RGB rgb) {
		if(background != null) {
			background.dispose();
		}
		if(rgb != null) {
			background = new Color(Display.getDefault(), rgb);
		}
	}

	public void fillTiles() {
		if(tile != null && zIndex > -1) {
			List<Grid> grids = tileMatrix.getGrids();
			for(Grid grid : grids) {
				grid.setTileInfo(zIndex, tile);
			}
		}
	}

}