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

import java.util.ArrayList;
import java.util.List;


public class TileMatrix {

	public static class Grid {
		
		private Row fRow;
		private TileInfo[] tile;
		private Column fColumn;
		
		public Grid(Row row,Column column) {
			if(row == null || column == null) {
				throw new RuntimeException();
			}
			fRow = row;
			fColumn = column;
			fRow.add(this);
			fColumn.add(this);
			tile = new TileInfo[3];
		}
		public int getX() {
			return fRow.indexOf(fColumn);
		}
		public int getY() {
			return fColumn.indexOf(fRow);
		}
		public void setTileInfo(int zIndex,TileInfo tile) {
			this.tile[zIndex] = tile;
		}
		public TileInfo getTileInfo(int zIndex) {
			return tile[zIndex];
		}
		public void remove() {
			fRow.remove(this);
			fColumn.remove(this);
		}
	}
	
	private class GridInfo<T> {
		
		private List<T> elements;
		private List<Grid> grids;
		
		public GridInfo() {
			elements = new ArrayList<T>();
			grids = new ArrayList<Grid>();
		}
		
		public void add(int index,T row) {
			elements.add(index,row);
		}
		
		public void remove(T row) {
			elements.remove(row);
		}
		
		public T get(int index){
			try {
				return elements.get(index);
			} catch(IndexOutOfBoundsException e) {
			}
			return null;
		}
		
		public int indexOf(T row) {
			return elements.indexOf(row);
		}
		
		public List<T> getElements() {
			return elements;
		}
		
		public void add(Grid grid) {
			grids.add(grid);
		}
		
		public void remove(Grid grid) {
			grids.remove(grid);
		}
		
		public Grid getGrid(int index) {
			return grids.get(index);
		}
		
		public List<Grid> getGrids() {
			return grids;
		}
	}
	
	private class Row extends GridInfo<Column> {}
	private class Column extends GridInfo<Row> {}
	
	private Row fRow; //first row
	private Column fColumn; //first column

	public TileMatrix(int rows,int columns) {
		fRow = new Row();
		fColumn = new Column();
		setup(0,fRow,0,fColumn);
		fillGrids(rows, columns);
	}

	private void setup(int rowIndex, Row row,int colIndex,Column column) {
		column.add(rowIndex,row);
		row.add(colIndex,column);
	}

	public void fillGrids(int rows, int columns) {
		for(int i = 0;i < rows;i++) {
			Row row = fColumn.get(i);
			if(row == null) {
				row = new Row();
				fColumn.add(i,row);
			}
			for(int j = 0;j < columns;j++) {
				Column column = fRow.get(j);
				if(column == null) {
					column = new Column();
					fRow.add(j,column);
				}
				if(column != fColumn) {
					column.add(i,row);
				}
				if(row != fRow) {
					row.add(j,column);
				}
				new Grid(row,column);
			}
		}
	}

	public void add(int x,int y,int z,TileInfo tile) {
		Grid grid = getGrid(x, y);
		grid.setTileInfo(z,tile);
	}
	
	public TileInfo get(int x,int y,int z) {
		Grid grid = getGrid(x, y);
		if(grid != null) {
			return grid.getTileInfo(z);
		}
		return null;
	}

	public Grid getGrid(int x, int y) {
		Column column = this.fRow.get(x);
		if(column != null) {
			Row row = column.get(y);
			if(row != null) {
				return row.getGrid(x);
			}
		}
		return null;
	}

	public List<Grid> getGrids() {
		List<Grid> grids = new ArrayList<Grid>();
		for(Row row : fColumn.getElements()) {
			grids.addAll(row.getGrids());
		}
		return grids;
	}
	
	public void addRow(int index) {
		Row row = new Row();
		List<Column> elements = this.fRow.getElements();
		for(int i = 0;i < elements.size();i++) {
			Column column = elements.get(i);
			setup(index,row,i,column);
			new Grid(row,column);
		}
	}

	public void addColumn(int index) {
		Column column = new Column();
		List<Row> elements = this.fColumn.getElements();
		for(int i = 0;i < elements.size();i++) {
			Row row = elements.get(i); 
			setup(i,row,index,column);
			new Grid(row,column);
		}
	}

	public void removeColumn(int index) {
		Column column = fRow.get(index);
		for(Row row : column.getElements()) {
			Grid grid = row.getGrid(index);
			grid.remove();
			row.getElements().remove(index);
		}
	}
	
	public void removeRow(int index) {
		Row row = fColumn.get(index);
		for(Column column : row.getElements()) {
			Grid grid = column.getGrid(index);
			grid.remove();
			column.getElements().remove(index);
		}
	}
	
	public void clear() {
		fRow = null;
		fColumn = null;
		fRow = new Row();
		fColumn = new Column();
		setup(0,fRow,0,fColumn);
	}
}
