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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.editor.track.TileInfo.TileBag;


public class TileView extends AbstractView {

	private int idCounter;
	
	private ToolItem item;
	private ToolBar palette;
	private CanvasView canvasView;
	
	private HashMap<Integer,TileInfo> tiles;
	private SelectionListener paletteSelectionListener;

	private ToolItem fNullToolItem;
	
	public TileView() {
		tiles = new HashMap<Integer,TileInfo>();
		idCounter = 1;
	}
	
	@Override
	public void save(TileManifest location) {
		location.setTileInfo(tiles);
		isDirty = false;
	}

	@Override
	protected GridData getControlLayoutData() {
		return new GridData(GridData.FILL_VERTICAL);
	}
	
	@Override
	protected void createControl(CTabItem item) {
		item.setText("Tile View: ");
		Color systemColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		
		Composite client = createComposite(item);
		client.setBackground(systemColor);
		
		Label label = new Label(client,SWT.NONE);
		label.setText("Select a Tile and Drop it into Canvas");
		label.setBackground(systemColor);
		
		palette = new ToolBar(client, SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		palette.setLayout(new GridLayout(5,true));
		palette.setLayoutData(new GridData(GridData.FILL_BOTH));
		palette.setBackground(systemColor);
		
		ToolBar toolbar = new ToolBar(client,SWT.FLAT | SWT.HORIZONTAL | SWT.CENTER);
		toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		
//		final ToolItem openTile = TileMapEditor.createToolItem(toolbar,"load from existing tileset","/icons/editor/opents.gif");
		final ToolItem newTile = TileMapEditor.createToolItem(toolbar,"Add a Tile","/icons/editor/newts.gif");
		final ToolItem removeTile = TileMapEditor.createToolItem(toolbar, "Remove Selected Tile", "/icons/editor/clear.gif");
		final ToolItem tileSettings = TileMapEditor.createToolItem(toolbar, "Configure Tile Properties", "/icons/editor/opents.gif");
		
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				if(newTile == source) {
					openTile();
				} else if(removeTile == source) {
					removeTile();
				} else if(tileSettings == source) {
					configureSettings();
				}
				isDirty = true;
			}

		};
		newTile.addSelectionListener(listener);
		removeTile.addSelectionListener(listener);
		tileSettings.addSelectionListener(listener);
		item.setControl(client);
		paletteSelectionListener = new TileSelectionListener();
		fNullToolItem = createToolItem(null);
	}

	private void configureSettings() {
		if(item != null) {
			new TileInfoDialog(new Shell(palette.getShell()),(TileInfo)item.getData()).open();
			canvasView.redraw();
		}
	}
	
	public void setCanvasView(CanvasView canvasView){
		this.canvasView = canvasView;
	}
	
	private void removeTile() {
		if(item != null) {
			if(item.getData() instanceof Tile) {
				removeToolItem(item);
				palette.layout();
				canvasView.setTile(null);
				item = null;
			}
		}
	}

	private void removeToolItem(ToolItem item) {
		TileInfo tile = (TileInfo) item.getData();
		item.removeSelectionListener(paletteSelectionListener);
		item.dispose();
		tiles.remove(tile.getId());
	}

	@Override
	public void setInput(TileManifest directory) {
		for(ToolItem item : palette.getItems()) {
			if(item == fNullToolItem) {
				continue;
			}
			removeToolItem(item);
		}
		if(directory != null) {
			for(Entry<Integer,TileInfo> entry : directory.getTileInfo().entrySet()) {
				TileInfo tileInfo = entry.getValue();
				tileInfo.setImage(tileInfo.getImage());
				createToolItem(tileInfo);
			}
		}
		palette.layout();
		isDirty = false;
	}
	
	private void openTile() {
		FileDialog fileDialog = new FileDialog(palette.getShell(),SWT.MULTI);
		fileDialog.setFilterExtensions(new String[]{"*.gif;*.png;*.jpg"});
		fileDialog.setFilterNames(new String[]{"gif or png or jpg files"});
		String open = fileDialog.open();
		if(open != null && open.length() > 0) {
			String path = fileDialog.getFilterPath();
			for(String file : fileDialog.getFileNames()) {
				String imgLocation = path+"/"+file;
				createTool(imgLocation.replaceAll("\\\\", "/"));
			}
			palette.layout(true,true);
		}
	}

	private int getId() {
		while(tiles.containsKey(idCounter)) {
			++idCounter;
		}
		return idCounter;
	}

	public TileInfo getTile(int id) {
		return tiles.get(id);
	}
	
	private void createTool(String imgLocation) {
		Image baseImage = ImageRegistry.createImage(imgLocation);
		Rectangle bounds = baseImage.getBounds();
		int wx = bounds.width / 32;
		int hx = bounds.height / 32;
		TileBag bag = new TileBag();
//		int y = 0;
//		for(int i = 0;i < wx; i++) {
//			int x = 0;
//			for(int j = 0;j < hx; j++) {
//				Image image = new Image(Display.getDefault(),CanvasView.GRID_SIZE,CanvasView.GRID_SIZE);
//				GC gc = new GC(image);
//				gc.drawImage(baseImage, x, y,CanvasView.GRID_SIZE,CanvasView.GRID_SIZE,0,0,CanvasView.GRID_SIZE,CanvasView.GRID_SIZE);
//				gc.dispose();
//				TileInfo tile = new TileInfo(getId(),image);
//				tile.bag = bag;
//				List<TileInfo> list = bag.infoList.get(i);
//				if(list == null) {
//					list = new ArrayList<TileInfo>();
//					bag.infoList.put(i, list);
//				}
//				list.add(tile);
//				ImageData imageData = image.getImageData();
//				imageData.transparentPixel = image.getImageData().palette.getPixel(new RGB(255,255,255));
//				tile.setImage(new Image(Display.getDefault(),imageData));
//				image.dispose();
//				createToolItem(tile);
//				x += 32;
//			}
//			y += 32;
//		}

		int y = 0;
		for(int i = 0;i < hx; i++) {
			int x = (wx - 1) * 32;
			for(int j = 0;j < wx; j++) {
				Image image = new Image(Display.getDefault(),CanvasView.GRID_SIZE,CanvasView.GRID_SIZE);
				GC gc = new GC(image);
				gc.drawImage(baseImage, x, y,CanvasView.GRID_SIZE,CanvasView.GRID_SIZE,0,0,CanvasView.GRID_SIZE,CanvasView.GRID_SIZE);
				gc.dispose();
				TileInfo tile = new TileInfo(getId(),image);
				tile.bag = bag;
				List<TileInfo> list = bag.infoList.get(i);
				if(list == null) {
					list = new ArrayList<TileInfo>();
					bag.infoList.put(i, list);
				}
				list.add(tile);
				ImageData imageData = image.getImageData();
				imageData.transparentPixel = image.getImageData().palette.getPixel(new RGB(255,255,255));
				tile.setImage(new Image(Display.getDefault(),imageData));
				image.dispose();
				createToolItem(tile);
				x -= 32;
			}
			y += 32;
		}

	}

	private ToolItem createToolItem(TileInfo info) {
		ToolItem toolItem = new ToolItem(palette,SWT.RADIO);
		if(info != null) {
			toolItem.setImage(info.getImage());
			toolItem.setData(info);
			tiles.put(info.getId(), info);
		}
		toolItem.addSelectionListener(paletteSelectionListener);
		return toolItem;
	}

	private class TileSelectionListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if(e.getSource() instanceof ToolItem) {
				ToolItem toolItem = (ToolItem) e.getSource();
				if(toolItem.getData() instanceof TileInfo) {
					TileInfo tile = (TileInfo) toolItem.getData();
					item = toolItem;
					canvasView.setTile(tile);
				} else if(fNullToolItem == e.getSource()) {
					canvasView.setTile(null);
				}
			}
		}
	}
	
}
