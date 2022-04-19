package com.editor.track;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SplineTest implements PaintListener {

	public static void main(String... h) {
		final Display display = new Display();
		final Shell shell = new Shell(display,SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		
		SplineTest splineTest = new SplineTest();
		Canvas canvas = new Canvas(shell,SWT.DOUBLE_BUFFERED);
		canvas.addPaintListener(splineTest);
		canvas.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	@Override
	public void paintControl(PaintEvent e) {
		Point cp0 = new Point(50,50);
		Point cp1 = new Point(150,100);
		Point cp2 = new Point(100,150);
		Point cp3 = new Point(150,150);
		
		GC gc = e.gc;
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		gc.setLineWidth(2);
		
		int cx1 = cp0.x,cx2 = cp1.x,cx3 = cp2.x,cx4 = cp3.x;
		int cy1 = cp0.y,cy2 = cp1.y,cy3 = cp2.y,cy4 = cp3.y;
		int x1 = -1, y1 = -1;
		for(float t = 0 ; t < 1 ; t += .1) {
			float t2 = (float) (t * t);
			float t3 = (float) (t * t * t);
			int x2 = (int) (0.5 * ((2 * cx2) + (-cx1 + cx3) * t + (2*cx1 - 5*cx2 + 4*cx3 - cx4) * t2 + (-cx1 + 3*cx2- 3*cx3 + cx4) * t3));
			int y2 = (int) (0.5 * ((2 * cy2) + (-cy1 + cy3) * t + (2*cy1 - 5*cy2 + 4*cy3 - cy4) * t2 + (-cy1 + 3*cy2- 3*cy3 + cy4) * t3));
			if(x1 > -1) {
				gc.drawLine(x1, y1, x2, y2);
			}
			x1 = x2;
			y1 = y2;
		}
		gc.drawOval(cp0.x, cp0.y, 6, 6);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		gc.drawOval(cp1.x, cp1.y, 6, 6);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		gc.drawOval(cp2.x, cp2.y, 6, 6);
		gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		gc.drawOval(cp3.x, cp3.y, 6, 6);

	}

	
}
