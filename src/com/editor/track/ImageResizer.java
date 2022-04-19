package com.editor.track;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class ImageResizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		File imageDir = new File("/Users/karthikeyan/sw/textures/level-textures/snapshot1/raw");
		String outputDir = "/Users/karthikeyan/sw/textures/level-textures/snapshot1/modified";
		int trackCount = 1;
		for(File img : imageDir.listFiles()) {
			Image image = new Image(Display.getDefault(),img.getAbsolutePath());
			Image newImage = new Image(Display.getDefault(),256,256);
			GC gc = new GC(newImage);
			Rectangle bounds = image.getBounds();
			gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, 256, 256);
			gc.dispose();
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { newImage.getImageData() };
			loader.save(outputDir+"/level-texture"+trackCount+".png", SWT.IMAGE_PNG);
			image.dispose();
			newImage.dispose();
			trackCount++;
		}

	}

}
