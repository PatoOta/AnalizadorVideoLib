package com.potar.videoanalizer.gui;

import java.io.PrintWriter;
import java.util.List;

import com.potar.AnalizadorVideo;
import com.potar.videoanalizer.runtime.data.Figura;
import com.potar.videoanalizer.runtime.data.FiguraOpenField;
import com.potar.videoanalizer.runtime.data.FiguraPlusMaze;
import com.potar.videoanalizer.runtime.data.LineaInterna;
import com.potar.videoanalizer.runtime.data.Parametros;
import com.potar.videoanalizer.runtime.data.RegionData;
import com.potar.videoanalizer.runtime.data.TipoTerreno;
import com.potar.videoanalizer.utils.Pair;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class PantallaEdicion extends Pantalla {
	//Figura donde se limitará la búsqueda de movimientos
	private boolean figuraInicializada;

	private float grosorMostrarTiempo;
	private boolean mostrarMensajeAyudasEdicion;
	private boolean isEditingPoint;
	private boolean isEditingInternalLine;
	private boolean isEditingMask;

	public PantallaEdicion(AnalizadorVideo app, Pantalla pantallaAnterior) {
		super(app, pantallaAnterior);
		reset();
	}

	@Override
	public Pantalla draw() {
		app.background(127);
		if (!figuraInicializada) {
			switch (app.getCorrida().getTipoTerreno()) {
			case TERRENO_OPEN_FIELD:
				app.getCorrida().setFigura(new FiguraOpenField());
				break;
			case TERRENO_PLUS_MAZE:
				app.getCorrida().setFigura(new FiguraPlusMaze());
				break;
			case TERRENO_Y_MAZE:
				app.getCorrida().setFigura(new FiguraYMaze());
				break;
			}
			figuraInicializada = true;
		}
		if (app.keyPressed == true) {
			if (app.key == 'p' || app.key == 'P') {
				//Se pasa a analizar el video (Play the movie)
				app.getCorrida().resetMovie();
				app.getCorrida().getMov().volume(1);
				app.keyPressed = false;
				return new PantallaAnalizarVideo(app, this);
			}
		}

		app.image(app.getCorrida().getImagenDeFondo(), 0, 0);


		Figura figuraActual = app.getCorrida().getFigura();
		figuraActual.draw(app);

		if (grosorMostrarTiempo > app.millis()) {
			app.pushStyle();
			app.fill(255, 190);
			app.strokeWeight(4);
			int margen = 20;
			app.rect(margen, margen, 200, margen * 2);
			app.fill(0);
			app.text("Grosor líneas enmascarado: " + figuraActual.getGrosorLineasEnmascarado(), margen * 1.5f, margen * 2.3f);
			app.popStyle();
		}
		
		if (isEditingPoint) {
			PVector editingPoint = figuraActual.getNearestPoint(app.mouseX, app.mouseY);
			if (editingPoint != null) {
				if (app.mousePressed == true) {
					float newX = app.mouseX;
					float newY = app.mouseY;

					if (figuraActual.verificarFigura(editingPoint, newX, newY)) {
						editingPoint.x = newX;
						editingPoint.y = newY;
						figuraActual.calcRegionesYExtrasParticulares(app, true);
					}
				}

				app.pushStyle();
				app.fill(255, 127);
				app.strokeWeight(1);
				app.stroke(0, 100, 0);
				app.ellipse(editingPoint.x, editingPoint.y, 20, 20);
				app.popStyle();
			}

			app.pushStyle();
			app.fill(255, 127);
			app.strokeWeight(1);
			app.stroke(0, 100, 0);
			//Ahora se resaltan los otros puntos de la figura que no se están editando
			for (PVector puntoFigura : figuraActual.getPuntosFigura()) {
				if (editingPoint == puntoFigura) {
					continue;
				}
				app.ellipse(puntoFigura.x, puntoFigura.y, 10, 10);
			}
			app.popStyle();

		} else if (isEditingMask) {
			// Marca zonas para enmascarar los movimientos en las mismas (o desmarca con click derecho)
			
			// Se "dibuja" la mascara por fuera de la figura
			app.pushStyle();
			app.stroke(0, 0, 255, 64);
			app.fill(0, 0, 255, 64);
			app.beginShape();
			app.vertex(0, 0);
			app.vertex(AnalizadorVideo.WIDTH, 0);
			app.vertex(AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT);
			app.vertex(0, AnalizadorVideo.WIDTH);

			app.beginContour();
			for (int i = figuraActual.getPuntosFigura().length - 1; i >= 0; i--) {
				// Se recorre antihorario para generar el hueco
				PVector aPoint = figuraActual.getPuntosFigura()[i];
				app.vertex(aPoint.x, aPoint.y);
			}
			app.endContour();
			app.endShape(PConstants.CLOSE);

			app.popStyle();

			RegionData regionData = figuraActual.getRegionData(app, app.mouseX, app.mouseY);
			app.pushStyle();
			app.stroke(app.color(255, 64));
			app.fill(app.color(255, 64));

			if (regionData != null) {
				app.beginShape();
				for (PVector aPoint : regionData.getRegion()) {
					app.vertex(aPoint.x, aPoint.y);
				}
				app.endShape(PConstants.CLOSE);
				if (app.getCorrida().getTipoTerreno() == TipoTerreno.TERRENO_PLUS_MAZE || app.getCorrida().getTipoTerreno() == TipoTerreno.TERRENO_Y_MAZE) {
					app.fill(app.color(255));
					app.text("Zona: " + regionData.getIdRegion(), app.mouseX, app.mouseY);
				}
			}
			if (figuraActual instanceof FiguraOpenField) {
				FiguraOpenField figuraOpenField = (FiguraOpenField)figuraActual;
				app.stroke(app.color(0, 255, 0, 127));
				for (Integer idRegion : figuraOpenField.getIdRegionesPeriferia()) {
					List<PVector> region = figuraOpenField.getRegiones().get(idRegion);
					if (region != null) {
						boolean esEsquina = figuraOpenField.isRegionEsquina(idRegion);
						app.fill(esEsquina ? app.color(255, 211, 0, 64) : app.color(0, 255, 0, 64));
						app.beginShape();
						for (PVector aPoint : region) {
							app.vertex(aPoint.x, aPoint.y);
						}
						app.endShape(PConstants.CLOSE);
					}
				}
			}
			app.popStyle();

			if (app.getCorrida().getMascaraImg() != null) {
				app.pushStyle();
				app.blend(app.getCorrida().getMascaraImg(), 0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, 0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PConstants.ADD);
				app.popStyle();
			}

			if (regionData != null && app.mousePressed == true) {
				boolean remove;
				if (app.mouseButton == PConstants.RIGHT) {
					remove = true;
				} else {
					remove = false;
				}

				PGraphics canvas = app.createGraphics(AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PConstants.JAVA2D);
				canvas.beginDraw();
				canvas.background(0);
				canvas.pushStyle();
				canvas.stroke(app.color(255));
				canvas.fill(app.color(255));

				canvas.beginShape();
				for (PVector aPoint : regionData.getRegion()) {
					canvas.vertex(aPoint.x, aPoint.y);
				}
				canvas.endShape(PConstants.CLOSE);
				canvas.popStyle();
				canvas.endDraw();
				app.getCorrida().getMascaraImg().loadPixels();
				for (int i = 0; i < AnalizadorVideo.WIDTH * AnalizadorVideo.HEIGHT; i++) {
					int canvasColor = canvas.pixels[i];
					if (app.brightness(canvasColor) > 0) {
						app.getCorrida().getXyPairsToIgnore()[i] = (remove ? false : true);
						app.getCorrida().getMascaraImg().pixels[i] = (remove ? app.color(0, 0, 0) : app.color(0, 0, 255));
					}
				}
				app.getCorrida().getMascaraImg().updatePixels();
			}
		} else if (isEditingInternalLine) {
			//Selecciona la Línea interna más cercana al mouse (hacer click para moverlo)
			PVector vectorMouse = new PVector(app.mouseX, app.mouseY);
			LineaInterna editingLine = figuraActual.getNearestInternalLine(vectorMouse);
			if (editingLine != null) {
				boolean drawPorcentage = false;
				if (app.mousePressed == true) {
					PVector pointInLineNearestMouse = figuraActual.orthogonalProjection(editingLine.getA(), editingLine.getB(), vectorMouse);
					PVector pointInTowardsLineNearestMouse = figuraActual.orthogonalProjection(editingLine.getAHacia(), editingLine.getBHacia(), vectorMouse);
					float totalDist = pointInTowardsLineNearestMouse.dist(pointInLineNearestMouse);
					float mouseDistToLine = pointInLineNearestMouse.dist(vectorMouse);
					float mouseDistToTowardsLine = pointInTowardsLineNearestMouse.dist(vectorMouse);

					drawPorcentage = true;
					float nuevoPorcentage = mouseDistToLine < totalDist && mouseDistToTowardsLine < totalDist ? mouseDistToLine / totalDist : mouseDistToLine < mouseDistToTowardsLine ? 0 : 1;

					if (nuevoPorcentage == 0) {
						nuevoPorcentage = 0.01f;
					}

					if (figuraActual.checkShape(editingLine, nuevoPorcentage)) {
						//Si está correcto todo, se actualiza el porcentaje de las líneas no editables (heredan el porcentaje de la línea que se edita)
						for (LineaInterna lineaInterna : figuraActual.getLineasInternas()) {
							if (!lineaInterna.isEditable()) {
								lineaInterna.setPorcentage(nuevoPorcentage);
							}
						}
						editingLine.setPorcentage(nuevoPorcentage);
						figuraActual.calcRegionesYExtrasParticulares(app, true);
					}
				}

				Pair<PVector, PVector> points = editingLine.getABDesplazamiento();
				PVector aPointDiff = points.getKey();
				PVector bPointDiff = points.getValue();

				app.pushStyle();
				app.fill(255, 127);
				app.strokeWeight(1);
				app.stroke(0, 100, 0);
				app.ellipse(aPointDiff.x, aPointDiff.y, 20, 20);
				app.ellipse(bPointDiff.x, bPointDiff.y, 20, 20);
				if (drawPorcentage) {
					app.text(String.format(Parametros.locale, "%d%%", Integer.valueOf((int) (editingLine.getPorcentage() * 100))), app.mouseX, app.mouseY);
				}
				app.popStyle();
			}

			app.pushStyle();
			app.fill(255, 127);
			app.strokeWeight(1);
			app.stroke(0, 100, 0);

			for (LineaInterna lineaInterna : figuraActual.getLineasInternas()) {
				if (editingLine == lineaInterna || !lineaInterna.isEditable()) {
					continue;
				}
				Pair<PVector, PVector> puntosDesplazados = lineaInterna.getABDesplazamiento();
				PVector aPuntoDesplazado = puntosDesplazados.getKey();
				PVector bPuntoDesplazado = puntosDesplazados.getValue();

				app.ellipse(aPuntoDesplazado.x, aPuntoDesplazado.y, 10, 10);
				app.ellipse(bPuntoDesplazado.x, bPuntoDesplazado.y, 10, 10);
			}
			app.popStyle();
		}

		if (mostrarMensajeAyudasEdicion) {
			app.pushStyle();
			app.fill(255, 190);
			app.strokeWeight(4);
			int margen = 20;
			app.rect(margen, margen, AnalizadorVideo.WIDTH - 2 * margen, AnalizadorVideo.HEIGHT - 2 * margen);
			app.fill(0);
			app.text("Edición de los límites\n" + "Tecla 'A' para volver a mostrar este mensaje de Ayuda\n"
					+ "Tecla 'T' para cambiar el Tipo de terreno (Open Field, Plus-Maze o Y-Maze)\n"
					+ "Tecla 'E' para editar los vértices que forman el terreno\n"
					+ "Tecla 'L' para mover las líneas internas. Mientras se presiona la tecla 'L' hacer click con\n    el mouse y moverlo para ajustar como se desee.\n"
					+ "Tecla 'J' se edita las líneas internas todas Juntas (se edita una, afectando a las demás)\n"
					+ "Tecla 'I' se edita las líneas internas Independientemente cada una\n"
					+ "Tecla 'M' muestra la Máscara (azul), la periferia y esquinas (verde y amarillo)\n    Hacer click para enmascarar o click con el botón derecho para desenmascarar.\n"
					+ "Tecla 'G' cambia el grosor de las líneas internas para usarse en la máscara.\n"
					+ "Teclas '+' y '-' para agregar o quitar filas (sólo en Open Field)\n"
					+ "Teclas '*' y '/' para agregar o quitar columnas (sólo en Open Field)\n"
					+ "Tecla 'P' para poner Play al video y comenzar el análisis de los movimientos\n"
					+ "Tecla 'E' Durante la reproducción del video para volver a esta pantalla y volver a Editar\n    (vuelve todos los contadores a 0 y el video al comienzo de la corrida)",
					margen * 1.8f, margen * 1.8f);
			app.popStyle();
		}

		return null;
	}

	@Override
	public void reset() {
		if (!figuraInicializada) {
			app.getCorrida().setFigura(null);
			app.getCorrida().setMascaraExteriorCanvas(null);

			mostrarMensajeAyudasEdicion = true;
			grosorMostrarTiempo = 0;
			
			isEditingPoint = false;
			isEditingInternalLine = false;
			isEditingMask = false;
		}
	}

	@Override
	public void keyPressed() {
		
		if (app.key == 't' || app.key == 'T') {
			//Para cambiar el tipo de terreno
			switch (app.getCorrida().getTipoTerreno()) {
			case TERRENO_OPEN_FIELD:
				app.getCorrida().setTipoTerreno(TipoTerreno.TERRENO_PLUS_MAZE);
				break;
			case TERRENO_PLUS_MAZE:
				app.getCorrida().setTipoTerreno(TipoTerreno.TERRENO_Y_MAZE);
				break;
			case TERRENO_Y_MAZE:
				app.getCorrida().setTipoTerreno(TipoTerreno.TERRENO_OPEN_FIELD);
				break;
			}
			
			figuraInicializada = false;
			
			app.keyPressed = false;
		} else {
			Figura figuraActual = app.getCorrida().getFigura();
			if (figuraActual instanceof FiguraOpenField && (app.key == '+' || app.key == '-' || app.key == '*' || app.key == '/')) {
				//Solamente para open field. Agrega o quita filas y/o columnas
				FiguraOpenField figuraOpenField = (FiguraOpenField)figuraActual;
				switch (app.key) {
				case '+':
					figuraOpenField.addRow();
					break;
				case '-':
					figuraOpenField.deleteRow();
					break;
				case '*':
					figuraOpenField.addColumn();
					break;
				case '/':
					figuraOpenField.deleteColumn();
					break;
				}
				figuraOpenField.resetInternalLines();
				app.keyPressed = false;
			} else if ((app.getCorrida().getTipoTerreno() == TipoTerreno.TERRENO_PLUS_MAZE || app.getCorrida().getTipoTerreno() == TipoTerreno.TERRENO_Y_MAZE)
					&& (app.key == 'i' || app.key == 'I' || app.key == 'j' || app.key == 'J')) {
				switch (app.key) {
				case 'i':
				case 'I':
					// Se desea editar las líneas independientemente
					for (LineaInterna lineaInterna : figuraActual.getLineasInternas()) {
						lineaInterna.setEditable(true);
					}
					break;
				case 'j':
				case 'J':
					// Se desea editar las líneas todas juntas (se edita una, pero afecta a todas por igual)
					LineaInterna firstLine = null;
					for (LineaInterna lineaInterna : figuraActual.getLineasInternas()) {
						if (firstLine == null) {
							// La primera se la deja editable
							lineaInterna.setEditable(true);
							firstLine = lineaInterna;
						} else {
							// Las demás copiarán a la primera
							lineaInterna.setEditable(false);
							lineaInterna.setPorcentage(firstLine.getPorcentage());
						}
					}
					break;
				}
			} else if (app.key == 'e' || app.key == 'E') {
				// Edita el punto más cercano al mouse (hacer click para moverlo)
				isEditingPoint = true;
			} else if (app.key == 'r' || app.key == 'R') {
				// Resetea la forma a la original
				figuraInicializada = false;
			} else if (app.key == 'm' || app.key == 'M') {
				// Marca zonas para enmascarar los movimientos en las mismas (o desmarca con click derecho)
				isEditingMask = true;
			} else if (app.key == 'g' || app.key == 'G') {
				//Cambia el grosor de las líneas para enmascarar
				figuraActual.cambiarGrosorLineasEnmascarado();
				figuraActual.calcRegionesYExtrasParticulares(app, true);
				grosorMostrarTiempo = app.millis() + 3000;
				app.keyPressed = false;
			} else if (app.key == 'l' || app.key == 'L') {
				//Selecciona la Línea interna más cercana al mouse (hacer click para moverlo)
				isEditingInternalLine = true;
			} else if (app.key == 'a' || app.key == 'A') {
				//Para mostrar el mensaje de ayuda
				mostrarMensajeAyudasEdicion = true;
			}
		}
	}

	@Override
	public void keyReleased() {
		mostrarMensajeAyudasEdicion = false;
		isEditingPoint = false;
		isEditingMask = false;
		isEditingInternalLine = false;
	}

	@Override
	public void mousePressed() {
		mostrarMensajeAyudasEdicion = false;
	}


	public void print(PrintWriter output) {
		if (app.getCorrida().getFigura() == null) {
			return;
		}
		output.print("Terreno\t");
		switch (app.getCorrida().getTipoTerreno()) {
		case TERRENO_OPEN_FIELD:
			output.println("Open Field");
			break;
		case TERRENO_PLUS_MAZE:
			output.println("Plus-Maze");
			break;
		case TERRENO_Y_MAZE:
			output.println("Y-Maze");
			break;
		}
		
		app.getCorrida().getFigura().print(output);
	}
}
