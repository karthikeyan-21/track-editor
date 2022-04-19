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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.editor.core.PaintTool;
import com.editor.core.PaintTool.BrushType;

public class Editor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Display display = new Display();
		
		Shell shell = new Shell(display,SWT.RESIZE | SWT.TITLE | SWT.MAX | SWT.MIN);
		shell.setText("Editor");
		shell.setMaximized(true);
		shell.setLayout(new GridLayout());

		PaintTool paintTool = new PaintTool();
		
		initializeMenu(shell,paintTool);
		initializeToolbar(shell,paintTool);

		Composite client = new Composite(shell,SWT.NONE);
		client.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		client.setLayout(new GridLayout());
		initializeCanvas(client,paintTool);
		
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private static void initializeToolbar(Composite client,final PaintTool paintTool) {
		ToolBar toolbar = new ToolBar(client,SWT.FLAT | SWT.HORIZONTAL);
		
		final ToolItem lineItem = new ToolItem(toolbar,SWT.RADIO);
		lineItem.setText("Line");
		lineItem.setSelection(true);
		
		final ToolItem curveItem = new ToolItem(toolbar,SWT.RADIO);
		curveItem.setText("Curve");
		
		new ToolItem(toolbar,SWT.SEPARATOR);
		
		final ToolItem renderItem = new ToolItem(toolbar,SWT.PUSH); 
		renderItem.setText("Render");
		
		SelectionAdapter selectionHandler = new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.getSource() == lineItem) {
					paintTool.setBrush(BrushType.LINE);
				} else if(e.getSource() == curveItem) {
					paintTool.setBrush(BrushType.CURVE);
				}
			}
		};
		lineItem.addSelectionListener(selectionHandler);
		curveItem.addSelectionListener(selectionHandler);
		renderItem.addSelectionListener(selectionHandler);
	}

	private static void initializeCanvas(Composite client,final PaintTool paintTool) {
		Canvas canvas = new Canvas(client, SWT.DOUBLE_BUFFERED | SWT.H_SCROLL | SWT.V_SCROLL); 
		canvas.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		canvas.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		paintTool.init(canvas);
	}

	private static void initializeMenu(Shell shell,final PaintTool paintTool) {
		Menu menuBar = new Menu(shell,SWT.BAR);
		menuBar.setVisible(true);
		shell.setMenuBar(menuBar);

		MenuItem fileItems = createMenuItem(menuBar, "File");
		Menu fileMenu = new Menu(fileItems);
		fileItems.setMenu(fileMenu);
		
		final MenuItem newTrack = new MenuItem(fileMenu,SWT.PUSH);
		newTrack.setText("New");
		
		final MenuItem openFile = new MenuItem(fileMenu,SWT.PUSH);
		openFile.setText("Open...");
		
		final MenuItem saveFile = new MenuItem(fileMenu,SWT.PUSH);
		saveFile.setText("Save...");
		
//		MenuItem backgroundItems = createMenuItem(menuBar, "Background");
//		Menu backgroundMenu = new Menu(backgroundItems);
//		backgroundItems.setMenu(backgroundMenu);
//		
//		final MenuItem loadImage = new MenuItem(backgroundMenu,SWT.PUSH);
//		loadImage.setText("Load Image...");
		
		SelectionAdapter selectionAdapter = new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.getSource() == newTrack) {
					paintTool.createNew();
				} else if(e.getSource() == openFile) {
					paintTool.open();
				} else if(e.getSource() == saveFile) {
					paintTool.save();
				}
			}
		};
		newTrack.addSelectionListener(selectionAdapter);
		openFile.addSelectionListener(selectionAdapter);
		saveFile.addSelectionListener(selectionAdapter);
//		loadImage.addSelectionListener(selectionAdapter);
	}

	private static MenuItem createMenuItem(Menu menu,String text) {
		MenuItem item  = new MenuItem(menu,SWT.CASCADE);
		item.setText(text);
		return item;
	}

}