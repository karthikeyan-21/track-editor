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
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

public class CurveTranslator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File inputDir = new File("/Users/karthikeyan/workspaces/libgdx-workspace/RR/data/uk"); 
		for(File file : inputDir.listFiles()) {
			if(file.getAbsolutePath().endsWith(".tmf")) {
				TileManifest tileManifest = new TileManifest(file.getAbsolutePath());
				Map<String, List<Point>> trackData = tileManifest.getTrackData();
				for(String key : trackData.keySet()) {
					for(Point point : trackData.get(key)) {
						point.x = Math.abs(point.x) + 100;
						point.y = Math.abs(point.y) + 100;
					}
				}
				tileManifest.setTrackData(trackData);
				tileManifest.save();
				System.out.println("Processed: "+file.getAbsolutePath());
			}
		}
	}

}
