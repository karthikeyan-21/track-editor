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

import java.io.File;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.editor.track.CanvasView.BrushType;

public class TileMapEditor {

	private Shell fShell;
	
	private String fileLocation;
	private TileView tileView;
	private CanvasView canvasView;

	private TileManifest tileManifest;
	
	public TileMapEditor() {
		tileView = new TileView();
		canvasView = new CanvasView();
		tileView.setCanvasView(canvasView);
	}
	
	public void createControl(Shell shell) {
		fShell = shell;
		shell.setText("Editor");
		
		createMenuBar(shell);
		createToolBar(shell);

		SashForm composite = new SashForm(shell,SWT.HORIZONTAL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createEditorUI(composite);
		composite.setWeights(new int[]{30,70});
	}

	private void createEditorUI(Composite parent) {
		tileView.createControl(parent);
		canvasView.createControl(parent);
	}

	private void createMenuBar(Shell shell) {
		Menu menu = new Menu(shell,SWT.BAR);
		shell.setMenuBar(menu);
		
		createFileMenu(menu);
		createEditMenu(menu);
	}

	private void createEditMenu(Menu menu) {
		MenuItem item = new MenuItem(menu,SWT.PUSH);
		item.setText("Edit");
	}

	private void createFileMenu(Menu menu) {
		MenuItem item = createMenuItem(menu,"File");
		
		Menu fMenu = new Menu(item);
		item.setMenu(fMenu);
		
		final MenuItem newFile = createMenuItem(fMenu,"New");
		final MenuItem openFile = createMenuItem(fMenu,"Open...");
		final MenuItem saveFile = createMenuItem(fMenu,"Save");
		final MenuItem saveAsFile = createMenuItem(fMenu,"Save As...");
		new MenuItem(fMenu,SWT.SEPARATOR);
		final MenuItem exit = createMenuItem(fMenu,"Exit");
		
		final class SelectionHandler extends SelectionAdapter {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				if(source == newFile) {
					createNewFile();
				} else if(source == openFile) {
					openFile();
				} else if(source == saveFile) {
					saveFile();
				} else if(source == saveAsFile ){
					saveAsFile();
				} else if(source == exit) {
					fShell.close();
				}
			}
		}
		SelectionHandler listener = new SelectionHandler(); 
		newFile.addSelectionListener(listener);
		openFile.addSelectionListener(listener);
		saveFile.addSelectionListener(listener);
		saveAsFile.addSelectionListener(listener);
		exit.addSelectionListener(listener);
	}

	public static MenuItem createMenuItem(Menu menu,String text) {
		MenuItem item  = new MenuItem(menu,SWT.CASCADE);
		item.setText(text);
		return item;
	}

	private void saveAsFile() {
		getFileToSave(SWT.SAVE);
		saveViews();
	}

	private void saveFile() {
		if(fileLocation == null) {
			getFileToSave(SWT.SAVE);
		}
		saveViews();
	}

	private void saveViews() {
		if(fileLocation != null) {
			if(tileManifest == null) {
				tileManifest = new TileManifest();
			}
			tileManifest.setFileLocation(fileLocation);
			tileView.save(tileManifest);
			canvasView.save(tileManifest);
			tileManifest.save();
		}
	}

	private void getFileToSave(int style) {
		FileDialog dialog = new FileDialog(getShell(),style);
		dialog.setText("Select a Tile Manifest file");
		dialog.setFilterExtensions(new String[]{"tmf"});
		dialog.setFilterNames(new String[]{"Tile Manifest(tmf)"});
		String result = dialog.open();
		if(result != null ) {
			fileLocation = result;
		}
	}

	private void openFile() {
		if(tileView.isDirty() || canvasView.isDirty()) {
			boolean openQuestion = MessageDialog.openQuestion(getShell(), "Save Required", "Save Current or cancel to discard changes ?");
			if(openQuestion) {
				saveFile();
			}
		}
		getFileToSave(SWT.OPEN);
		loadViews();
	}

	private void loadViews() {
		if(fileLocation != null) {
			tileManifest = new TileManifest(fileLocation);
			tileView.setInput(tileManifest);
			canvasView.setInput(tileManifest);
		}
	}

	private void createNewFile() {
		if(tileView.isDirty() || canvasView.isDirty()) {
			boolean openQuestion = MessageDialog.openQuestion(getShell(), "Save Required", "Save Current or cancel to discard changes ?");
			if(openQuestion) {
				saveFile();
			}
		}
		tileView.setInput(null);
		canvasView.setInput(null);
	}

	protected Shell getShell() {
		return Display.getDefault().getActiveShell();
	}
	
	private void createToolBar(Composite composite) {
		ToolBar toolbar = new ToolBar(composite,SWT.FLAT | SWT.HORIZONTAL);
		final ToolItem grid = createToolItem(toolbar,"Show/Hide grid","/icons/editor/grid.gif");
		new ToolItem(toolbar,SWT.SEPARATOR);
		final ToolItem incGridWidthR = createToolItem(toolbar,"Increase Grid Width","/icons/editor/increaseWidth.gif");
		final ToolItem incGridHeightB = createToolItem(toolbar,"Increase Grid Height","/icons/editor/increaseHeight.gif");
		final ToolItem decGridWidthR = createToolItem(toolbar,"Decrease Grid Width","/icons/editor/decreaseWidth.gif");
		final ToolItem decGridHeightB = createToolItem(toolbar,"Decrease Grid Height","/icons/editor/decreaseHeight.gif");
		new ToolItem(toolbar,SWT.SEPARATOR);
		final ToolItem incGridWidthL = createToolItem(toolbar,"Increase Grid Width","/icons/editor/increaseWidth.gif");
		final ToolItem incGridHeightT = createToolItem(toolbar,"Increase Grid Height","/icons/editor/increaseHeight.gif");
		final ToolItem decGridWidthL = createToolItem(toolbar,"Decrease Grid Width","/icons/editor/decreaseWidth.gif");
		final ToolItem decGridHeightT = createToolItem(toolbar,"Decrease Grid Height","/icons/editor/decreaseHeight.gif");
		new ToolItem(toolbar,SWT.SEPARATOR);
		final ToolItem layer1 = createToolItem(toolbar, "Layer 1", "/icons/editor/top.gif",SWT.RADIO);
		final ToolItem layer2 = createToolItem(toolbar, "Layer 2", "/icons/editor/mid.gif",SWT.RADIO);
		final ToolItem layer3 = createToolItem(toolbar, "Layer 3", "/icons/editor/bottom.gif",SWT.RADIO);
		final ToolItem layer4 = createToolItem(toolbar, "All Layers", "/icons/editor/grid.gif",SWT.RADIO);
		layer1.setSelection(true);
		new ToolItem(toolbar,SWT.SEPARATOR);
		final ToolItem selectItem = new ToolItem(toolbar,SWT.RADIO);
		selectItem.setText("Selection");
		selectItem.setSelection(true);
		final ToolItem splineItem = new ToolItem(toolbar,SWT.RADIO);
		splineItem.setText("Spline");
		new ToolItem(toolbar,SWT.SEPARATOR);
		final ToolItem fillItem = new ToolItem(toolbar,SWT.PUSH);
		fillItem.setText("fill");
		final ToolItem cloneItem = new ToolItem(toolbar,SWT.PUSH);
		cloneItem.setText("clone");
		final ToolItem bgItem = new ToolItem(toolbar,SWT.PUSH);
		bgItem.setText("BG");
		final ToolItem colorItem = new ToolItem(toolbar,SWT.PUSH);
		colorItem.setText("color");
		final ToolItem fillTilesItem = new ToolItem(toolbar,SWT.PUSH);
		fillTilesItem.setText("fill-tiles");
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				if(source == grid) {
					canvasView.showGrid(!canvasView.isGridVisible());
				} else if(source == incGridWidthR) {
					canvasView.addColumnToRight(); 
				} else if(source == incGridHeightB) {
					canvasView.addRowToBottom();
				} else if(source == decGridWidthR) {
					canvasView.removeColumnFromRight();
				} else if(source == decGridHeightB) {
					canvasView.removeRowFromBottom();
				} else if(source == incGridWidthL) {
					canvasView.addColumnToLeft();
				} else if(source == incGridHeightT){
					canvasView.addRowToTop();
				} else if(source == decGridWidthL) {
					canvasView.removeColumnFromLeft();
				} else if(source == decGridHeightT) {
					canvasView.removeRowFromTop();
				} else if(source == layer1) {
					canvasView.showLayer(0);
				} else if(source == layer2) {
					canvasView.showLayer(1);
				} else if(source == layer3) {
					canvasView.showLayer(2);
				} else if(source == layer4) {
					canvasView.showLayer(-1);
				} else if(e.getSource() == splineItem) {
					canvasView.setBrush(BrushType.CATMULL_ROM_SPLINE);
				} else if(e.getSource() == selectItem) {
					canvasView.setBrush(BrushType.SELECTION);
				} else if(e.getSource() == fillItem) {
					try {
						InputDialog dialog = new InputDialog(Display.getDefault().getActiveShell(), "Fill rows and columns", "Enter [row,column]", null, null);
						dialog.open();
						String value = dialog.getValue();
						if(value != null) {
							String[] split = value.split(",");
							int x = Integer.parseInt(split[0]);
							int y = Integer.parseInt(split[1]);
							canvasView.fill(x,y);
						}
					} catch(Throwable t) {
						t.printStackTrace();
					}
				} else if(e.getSource() == cloneItem) {
					canvasView.cloneTrack();
				} else if(e.getSource() == bgItem) {
					FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell());
					dialog.setFilterExtensions(new String[]{"jpeg","jpg"});
					String file = dialog.open();
					if(file != null) {
						canvasView.setBackgroundImage(new Image(Display.getDefault(),file));
					}
				} else if(e.getSource() == colorItem) {
					ColorDialog dialog = new ColorDialog(Display.getDefault().getActiveShell());
					RGB open = dialog.open();
					if(open != null) {
						canvasView.setBackgroundColor(open);
					}
				} else if(e.getSource() == fillTilesItem) {
					canvasView.fillTiles();
				}
				canvasView.redraw();
			}
		};
		grid.addSelectionListener(listener);
		incGridWidthR.addSelectionListener(listener);
		incGridHeightB.addSelectionListener(listener);
		decGridHeightB.addSelectionListener(listener);
		decGridWidthR.addSelectionListener(listener);
		incGridWidthL.addSelectionListener(listener);
		incGridHeightT.addSelectionListener(listener);
		decGridWidthL.addSelectionListener(listener);
		decGridHeightT.addSelectionListener(listener);
		layer1.addSelectionListener(listener);
		layer2.addSelectionListener(listener);
		layer3.addSelectionListener(listener);
		layer4.addSelectionListener(listener);
		splineItem.addSelectionListener(listener);
		selectItem.addSelectionListener(listener);
		fillItem.addSelectionListener(listener);
		cloneItem.addSelectionListener(listener);
		bgItem.addSelectionListener(listener);
		colorItem.addSelectionListener(listener);
		fillTilesItem.addSelectionListener(listener);
	}

	public static ToolItem createToolItem(ToolBar toolbar,String tooltip,String imagePath) {
		ToolItem toolItem = createToolItem(toolbar, tooltip, imagePath, SWT.PUSH);
		return toolItem;
	}

	public static ToolItem createToolItem(ToolBar toolbar,String tooltip,String imagePath,int style) {
		ToolItem toolItem = new ToolItem(toolbar,style);
		toolItem.setImage(getImage(imagePath));
		toolItem.setToolTipText(tooltip);
		return toolItem;
	}

	private static Image getImage(String string) {
		String location = new File("").getAbsolutePath()+string;
		return new Image(Display.getDefault(),location);
	}

}
