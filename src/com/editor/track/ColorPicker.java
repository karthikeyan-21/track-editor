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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ColorPicker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display,SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		
		shell.setText("Editor");
		
		ToolBar toolbar = new ToolBar(shell,SWT.FLAT | SWT.HORIZONTAL);
		final ToolItem colorItem = new ToolItem(toolbar,SWT.PUSH);
		colorItem.setText("color");
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog dialog = new ColorDialog(Display.getDefault().getActiveShell());
				RGB open = dialog.open();
				System.out.println("RGB: "+open);
			}
		};
		colorItem.addSelectionListener(listener);

		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public static ToolItem createToolItem(ToolBar toolbar,String tooltip,String imagePath) {
		ToolItem toolItem = createToolItem(toolbar, tooltip, imagePath, SWT.PUSH);
		return toolItem;
	}

	public static ToolItem createToolItem(ToolBar toolbar,String tooltip,String imagePath,int style) {
		ToolItem toolItem = new ToolItem(toolbar,style);
		toolItem.setToolTipText(tooltip);
		return toolItem;
	}

}
