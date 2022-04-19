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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractView {

	protected String fText;
	protected boolean isDirty;
	
	public abstract void save(TileManifest manifest);
	public abstract void setInput(TileManifest manifest);
	protected abstract void createControl(CTabItem item);

	public final void createControl(Composite parent) {
		Composite client = new Composite(parent,SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		client.setLayout(gridLayout);
		client.setLayoutData(getControlLayoutData());
		
		CTabFolder tabFolder = new CTabFolder(client,SWT.TOP | SWT.FLAT | SWT.SINGLE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.setSimple(true);
		CTabItem item = new CTabItem(tabFolder,SWT.NONE);
		createControl(item);
		tabFolder.setSelection(item);
	}

	protected Composite createComposite(CTabItem item) {
		Composite client = new Composite(item.getParent(),SWT.NONE);
		client.setLayoutData(new GridData(GridData.FILL_BOTH));
		client.setLayout(new GridLayout());
		return client;
	}

	protected GridData getControlLayoutData() {
		return new GridData(GridData.FILL_BOTH);
	}

	public boolean isDirty() {
		return isDirty;
	}
	
	public void setText(String text) {
		fText = text;
	}
	
}
