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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.editor.track.LineSegment.OpaqueSide;


public class TileInfoDialog extends Dialog {
	
	private TileInfo info;
	private Combo fSrcXCombo;
	private Combo fSrcYCombo;
	private Combo fDestXCombo;
	private Combo fDestYCombo;
	private Text fClassNameText;
	private Combo fOpaqueSideCombo;
	
	public TileInfoDialog(Shell parentShell,TileInfo info) {
		super(parentShell);
		parentShell.setText("Configure settings");
		this.info = info;
	}
	
	protected void createContents(Composite parent) {
		parent.setLayout(new GridLayout());
		
		createConfigurationFactoryUI(parent);
		createCollisionDetectionUI(parent);
		createButtonBar(parent);
	}

	private void createCollisionDetectionUI(Composite parent) {
		Group group = new Group(parent,SWT.NONE);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("Collision Detection");
		group.setLayout(new GridLayout(5,false));
		
		Label sourceLabel = new Label(group,SWT.NONE);
		sourceLabel.setText("Source point:");
		sourceLabel.setLayoutData(new GridData());
		
		Label srcX = new Label(group,SWT.NONE);
		srcX.setText("X*:");
		srcX.setLayoutData(new GridData());
		
		fSrcXCombo = new Combo(group,SWT.DROP_DOWN | SWT.READ_ONLY);
		fSrcXCombo.setLayoutData(new GridData());
		
		Label srcY = new Label(group,SWT.NONE);
		srcY.setText("Y*:");
		srcY.setLayoutData(new GridData());
		
		fSrcYCombo = new Combo(group,SWT.DROP_DOWN | SWT.READ_ONLY);
		fSrcYCombo.setLayoutData(new GridData());
		
		Label destinationLabel = new Label(group,SWT.NONE);
		destinationLabel.setText("Destination point:");
		destinationLabel.setLayoutData(new GridData());
		
		srcX = new Label(group,SWT.NONE);
		srcX.setText("X*:");
		srcX.setLayoutData(new GridData());
		
		fDestXCombo = new Combo(group,SWT.DROP_DOWN | SWT.READ_ONLY);
		fDestXCombo.setLayoutData(new GridData());
		
		srcY = new Label(group,SWT.NONE);
		srcY.setText("Y*:");
		srcY.setLayoutData(new GridData());
		
		fDestYCombo = new Combo(group,SWT.DROP_DOWN | SWT.READ_ONLY);
		fDestYCombo.setLayoutData(new GridData());
		
		Label opaqueSide = new Label(group,SWT.NONE);
		opaqueSide.setText("Opaque Side*:");
		opaqueSide.setLayoutData(new GridData());
		
		fOpaqueSideCombo = new Combo(group,SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData();
		data.horizontalSpan = 4;
		fOpaqueSideCombo.setLayoutData(data);
		
		for(int i = 0;i <= 32;i++) {
			String value = String.valueOf(i);
			fSrcXCombo.add(value);
			fSrcYCombo.add(value);
			fDestXCombo.add(value);
			fDestYCombo.add(value);
		}
		
		for(OpaqueSide side : OpaqueSide.values()) {
			fOpaqueSideCombo.add(side.name());
		}
	}

	private void createButtonBar(Composite parent) {
		Composite buttonBar = new Composite(parent,SWT.NONE);
		buttonBar.setLayout(new GridLayout(2,false));
		buttonBar.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_END | GridData.HORIZONTAL_ALIGN_END));
		
		final Button okButton = new Button(buttonBar,SWT.PUSH);
		okButton.setText("OK");
		okButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		final Button deleteButton = new Button(buttonBar,SWT.PUSH);
		deleteButton.setText("Cancel");
		deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(okButton == e.getSource()) {
					okPressed();
				} else if(deleteButton == e.getSource()) {
					deletePressed();
				}
			}
		};
		okButton.addSelectionListener(listener);
		deleteButton.addSelectionListener(listener);
	}

	protected void deletePressed() {
		getParent().dispose();
	}

	protected void okPressed() {
		String className = fClassNameText.getText();
//		if(className != null && className.length() > 0) {
//			info.setClassName(className);
//		} else {
//			info.setClassName(null);
//		}
		
		String srcX = fSrcXCombo.getText();
		String srcY = fSrcYCombo.getText();
		String destX = fDestXCombo.getText();
		String destY = fDestYCombo.getText();
		String side = fOpaqueSideCombo.getText();
		
//		if(srcX.length() > 0 || srcY.length() > 0 || side.length() > 0 
//				|| destX.length() > 0 || destY.length() > 0) {
//			Point src = new Point(Integer.parseInt(srcX),Integer.parseInt(srcY));
//			Point dest = new Point(Integer.parseInt(destX),Integer.parseInt(destY));
//			OpaqueSide opSide = OpaqueSide.valueOf(side);
//			info.setCollisionRay(new LineSegment(src,dest,opSide));
//		} else {
//			info.setCollisionRay(null);
//		}
		getParent().dispose();
	}

	private void createConfigurationFactoryUI(Composite parent) {
		Group client = new Group(parent,SWT.NONE);
		client.setText("Configuration Factory");
		client.setLayout(new GridLayout(2,false));
		client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label  = new Label(client,SWT.NONE);
		label.setText("Class Name:");
		label.setLayoutData(new GridData());
		
		fClassNameText = new Text(client,SWT.BORDER);
		fClassNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
 	public void open () {
 		Shell parent = getParent();
 		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		shell.setText("Tile Properties");
 		shell.setSize(400, 400);
 		shell.setText(getText());
 		createContents(shell);
 		shell.open();
 		Display display = parent.getDisplay();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) display.sleep();
 		}
 	}
}
