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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ImageRegistry {

	private static Map<String,Image> fCache;
	
	static {
		fCache = new HashMap<String,Image>();
	}
	
	public static Image createImage(String location) {
		if(fCache.containsKey(location)) {
			return fCache.get(location);
		}
		Image image = new Image(Display.getDefault(),location);
		fCache.put(location, image);
		return image;
	}
	
	private static final int DEFAULT_IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;

    public static BufferedImage bufferImage(java.awt.Image image) {
        return bufferImage(image, DEFAULT_IMAGE_TYPE);
    }

    public static  BufferedImage bufferImage(java.awt.Image image, int type) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(image, null, null);
        waitForImage(bufferedImage);
        return bufferedImage;
    }

    private static void waitForImage(BufferedImage bufferedImage) {
        final ImageLoadStatus imageLoadStatus = new ImageLoadStatus();
        bufferedImage.getHeight(new ImageObserver() {
            public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y, int width, int height) {
                if (infoflags == ALLBITS) {
                    imageLoadStatus.heightDone = true;
                    return true;
                }
                return false;
            }
        });
        bufferedImage.getWidth(new ImageObserver() {
            public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y, int width, int height) {
                if (infoflags == ALLBITS) {
                    imageLoadStatus.widthDone = true;
                    return true;
                }
                return false;
            }
        });
//        while (!imageLoadStatus.widthDone && !imageLoadStatus.heightDone) {
//            try {
////                Thread.sleep(300);
//            } catch (InterruptedException e) {
//
//            }
//        }
    }

    private static class ImageLoadStatus {

        public boolean widthDone = false;
        public boolean heightDone = false;
    }

	
	public static BufferedImage convertToAWT(ImageData data) {
	    ColorModel colorModel = null;
	    PaletteData palette = data.palette;
	    if (palette.isDirect) {
	      colorModel = new DirectColorModel(data.depth, palette.redMask,palette.greenMask, palette.blueMask);
	      BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
	      WritableRaster raster = bufferedImage.getRaster();
	      int[] pixelArray = new int[3];
	      for (int y = 0; y < data.height; y++) {
	        for (int x = 0; x < data.width; x++) {
	          int pixel = data.getPixel(x, y);
	          RGB rgb = palette.getRGB(pixel);
	          pixelArray[0] = rgb.red;
	          pixelArray[1] = rgb.green;
	          pixelArray[2] = rgb.blue;
	          raster.setPixels(x, y, 1, 1, pixelArray);
	        }
	      }
	      return bufferedImage;
	    } else {
	      RGB[] rgbs = palette.getRGBs();
	      byte[] red = new byte[rgbs.length];
	      byte[] green = new byte[rgbs.length];
	      byte[] blue = new byte[rgbs.length];
	      for (int i = 0; i < rgbs.length; i++) {
	        RGB rgb = rgbs[i];
	        red[i] = (byte) rgb.red;
	        green[i] = (byte) rgb.green;
	        blue[i] = (byte) rgb.blue;
	      }
	      if (data.transparentPixel != -1) {
	        colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
	      } else {
	        colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
	      }
	      BufferedImage bufferedImage = new BufferedImage(colorModel,colorModel.createCompatibleWritableRaster(data.width,data.height), false, null);
	      WritableRaster raster = bufferedImage.getRaster();
	      int[] pixelArray = new int[1];
	      for (int y = 0; y < data.height; y++) {
	        for (int x = 0; x < data.width; x++) {
	          int pixel = data.getPixel(x, y);
	          pixelArray[0] = pixel;
	          raster.setPixel(x, y, pixelArray);
	        }
	      }
	      return bufferedImage;
	    }
	  }

	public static ImageData convertToSWT(BufferedImage bufferedImage) {
	    if (bufferedImage.getColorModel() instanceof DirectColorModel) {
	      DirectColorModel colorModel = (DirectColorModel) bufferedImage
	          .getColorModel();
	      PaletteData palette = new PaletteData(colorModel.getRedMask(),
	          colorModel.getGreenMask(), colorModel.getBlueMask());
	      ImageData data = new ImageData(bufferedImage.getWidth(),
	          bufferedImage.getHeight(), colorModel.getPixelSize(),
	          palette);
	      WritableRaster raster = bufferedImage.getRaster();
	      int[] pixelArray = new int[3];
	      for (int y = 0; y < data.height; y++) {
	        for (int x = 0; x < data.width; x++) {
	          raster.getPixel(x, y, pixelArray);
	          int pixel = palette.getPixel(new RGB(pixelArray[0],
	              pixelArray[1], pixelArray[2]));
	          data.setPixel(x, y, pixel);
	        }
	      }
	      return data;
	    } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
	      IndexColorModel colorModel = (IndexColorModel) bufferedImage
	          .getColorModel();
	      int size = colorModel.getMapSize();
	      byte[] reds = new byte[size];
	      byte[] greens = new byte[size];
	      byte[] blues = new byte[size];
	      colorModel.getReds(reds);
	      colorModel.getGreens(greens);
	      colorModel.getBlues(blues);
	      RGB[] rgbs = new RGB[size];
	      for (int i = 0; i < rgbs.length; i++) {
	        rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
	            blues[i] & 0xFF);
	      }
	      PaletteData palette = new PaletteData(rgbs);
	      ImageData data = new ImageData(bufferedImage.getWidth(),
	          bufferedImage.getHeight(), colorModel.getPixelSize(),
	          palette);
	      data.transparentPixel = colorModel.getTransparentPixel();
	      WritableRaster raster = bufferedImage.getRaster();
	      int[] pixelArray = new int[1];
	      for (int y = 0; y < data.height; y++) {
	        for (int x = 0; x < data.width; x++) {
	          raster.getPixel(x, y, pixelArray);
	          data.setPixel(x, y, pixelArray[0]);
	        }
	      }
	      return data;
	    }
	    return null;
	  }

}
