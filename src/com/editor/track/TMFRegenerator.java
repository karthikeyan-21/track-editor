package com.editor.track;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;

public class TMFRegenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File inputDir = new File("/Users/karthikeyan/sw/rally-tracks/snapshots/uk/"); 
		for(File file : inputDir.listFiles()) {
			if(file.getAbsolutePath().endsWith(".tmf")) {
				TileManifest tileManifest = new TileManifest(file.getAbsolutePath());
				Map<String, List<Point>> trackData = tileManifest.getTrackData();
				for(String key : trackData.keySet()) {
					Collections.reverse(trackData.get(key));
				}
				tileManifest.setTrackData(trackData);
				tileManifest.setFileLocation(file.getAbsolutePath()+".reverse");
				tileManifest.save();
			}
		}
	}

}
