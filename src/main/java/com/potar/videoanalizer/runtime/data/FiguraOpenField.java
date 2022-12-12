package com.potar.videoanalizer.runtime.data;

import java.util.HashSet;
import java.util.Set;

import com.potar.AnalizadorVideo;

import processing.core.PVector;

public class FiguraOpenField extends Figura {
	private static final float openFieldShapeX = 10;
	private static final float openFieldShapeY = 0;
	private static final float openFieldShapeWidth = AnalizadorVideo.WIDTH * 0.9f;
	private static final float openFieldShapeHeight = AnalizadorVideo.HEIGHT * 0.9f;

	private final PVector[] openFieldShapePoints;
	private LineaInterna[] openFieldShapeInternalLines;
	private int rows = 5;
	private int columns = 6;

	private Set<Integer> idRegionesPeriferia;
	private Set<Integer> idRegionesEsquina;
	
	public FiguraOpenField() {
		openFieldShapePoints = new PVector[] { new PVector(openFieldShapeX + 100, openFieldShapeY + 50),
				new PVector(openFieldShapeX + openFieldShapeWidth - 100, openFieldShapeY + 50),
				new PVector(openFieldShapeX + openFieldShapeWidth, openFieldShapeY + openFieldShapeHeight),
				new PVector(openFieldShapeX, openFieldShapeY + openFieldShapeHeight) };
		resetInternalLines();
	}

	@Override
	public PVector[] getPuntosFigura() {
		return openFieldShapePoints;
	}

	@Override
	public LineaInterna[] getLineasInternas() {
		return openFieldShapeInternalLines;
	}

	public void resetInternalLines() {
		float columnsIncrement = 1.0f / columns;
		float rowsIncrement = 1.0f / rows;
		openFieldShapeInternalLines = new LineaInterna[columns + rows - 2];
		for (int i = 0; i < columns - 1; i++) {
			openFieldShapeInternalLines[i] = new LineaInterna(openFieldShapePoints, 0, 3, columnsIncrement * (i + 1), true);
		}
		for (int i = 0; i < rows - 1; i++) {
			openFieldShapeInternalLines[columns - 1 + i] = new LineaInterna(openFieldShapePoints, 1, 0,
					rowsIncrement * (i + 1), true);
		}
		
		canvasPorIdRegion = null;
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getColumns() {
		return columns;
	}
	
	public void addRow() {
		rows++;
	}

	public void addColumn() {
		columns++;
	}

	public void deleteRow() {
		rows--;
		if (rows < 1) {
			rows = 1;
		}
	}

	public void deleteColumn() {
		columns--;
		if (columns < 1) {
			columns = 1;
		}
	}

	@Override
	protected void extraRegionCalcInits() {
		idRegionesPeriferia = new HashSet<Integer>();
		idRegionesEsquina = new HashSet<Integer>();
	}

	@Override
	protected void extraRegionCalcs(AnalizadorVideo app, Integer idRegion) {
		if (app.getCorrida().getFigura() instanceof FiguraOpenField) {
			FiguraOpenField figuraOpenField = (FiguraOpenField)app.getCorrida().getFigura();
			if (isPeriferia(idRegion.intValue(), figuraOpenField.getRows(), figuraOpenField.getColumns())) {
				idRegionesPeriferia.add(idRegion);
				if (isEsquina(idRegion.intValue(), figuraOpenField.getRows(), figuraOpenField.getColumns())) {
					idRegionesEsquina.add(idRegion);
				}
			}
		}
	}

	private boolean isPeriferia(int idRegion, int rows, int columns) {
		if (idRegion % rows == 0) {
			//La fila de arriba
			return true;
		}
		if (idRegion % rows == rows-1) {
			//La fila de abajo
			return true;
		}

		if (idRegion < rows) {
			//La primera columna
			return true;
		}

		if (idRegion / rows == (columns-1) ) {
			//La columna de la derecha
			return true;
		}
		return false;
	}

	private boolean isEsquina(int idRegion, int rows, int columns) {
		if (idRegion == 0) {
			//La esquina de arriba a la izquierda
			return true;
		}

		if (idRegion == rows-1) {
			//La esquina de abajo a la izquierda
			return true;
		}

		if (idRegion == rows * (columns - 1)) {
			//La esquina de arriba a la derecha
			return true;
		}

		if (idRegion == rows * columns - 1) {
			//La esquina de abajo a la derecha
			return true;
		}
		return false;
	}

	@Override
	protected void extraRegionData(RegionData result, Integer idDeLaRegion) {
		result.setPeriferia(idRegionesPeriferia.contains(idDeLaRegion));
		result.setEsquina(idRegionesEsquina.contains(idDeLaRegion));
	}

	public Integer[] getIdRegionesPeriferia() {
		Integer[] result = new Integer[idRegionesPeriferia.size()];
		return idRegionesPeriferia.toArray(result);
	}

	public boolean isRegionEsquina(Integer idRegion) {
		return idRegionesEsquina.contains(idRegion);
	}

}
