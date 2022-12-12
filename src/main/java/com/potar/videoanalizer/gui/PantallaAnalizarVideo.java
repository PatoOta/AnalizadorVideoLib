package com.potar.videoanalizer.gui;

import com.potar.AnalizadorVideo;
import com.potar.videoanalizer.runtime.data.Corrida;
import com.potar.videoanalizer.runtime.data.Estadisticas;
import com.potar.videoanalizer.runtime.data.Parametros;
import com.potar.videoanalizer.runtime.data.Recorrido;
import com.potar.videoanalizer.runtime.data.RegionData;
import com.potar.videoanalizer.runtime.data.TipoTerreno;

import milchreis.imageprocessing.Comparison;
import milchreis.imageprocessing.Grayscale;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;

public class PantallaAnalizarVideo extends Pantalla {
	boolean mostrarRecorrido;
	long mostrarMensajeCapturaGrabadaTime;
	boolean mostrarAyuda;
	private boolean isReturnToEdit;
	private boolean isGoToEditMascaraTemporal;

	public PantallaAnalizarVideo(AnalizadorVideo app, Pantalla pantallaAnterior) {
		super(app, pantallaAnterior);

		reset();
	}

	@Override
	public Pantalla draw() {
		Corrida corrida = app.getCorrida();
		Movie mov = corrida.getMov();

		if (isReturnToEdit) {
			mov.volume(0);
			mov.pause();
			return this.getPrev();
		}

		if (isGoToEditMascaraTemporal) {
			mov.volume(0);
			mov.pause();
			isGoToEditMascaraTemporal = false;
			return new PantallaEditMascaraTemporal(app, this);
		}

		//Si el video no está disponible, no hay mucho para hacer
		if (!mov.available()) {
			return null;
		}

		//Se lee la imagen del video en el momento actual
		mov.read();

		//Si lo que se leyó aún no tiene dimensión
		if (mov.width == 0 || mov.height == 0) {
			return null;
		}

		Recorrido recorrido = corrida.getRecorrido();
		Estadisticas estadisticas = corrida.getEstadisticas();

		//Se fija si se debe ignorar este momento del video
		float movTime = mov.time();
		boolean ignorar = corrida.calcIgnorar(movTime);//Hacer todo lo que sigue sobre el ignorar en un método de Corrida

		PImage edgeDiff = null;
		PImage imagenActualVideo = null;
		if (!ignorar) {
			//Ahora se calcula dónde hubo movimientos
			edgeDiff = Grayscale.apply(Comparison.calculateDifferenceImage(corrida.getImagenDeFondo(), app.getImageFromMov(null)));//SobelEdgeDetector.apply();
			edgeDiff.loadPixels();
			PGraphics mascaraExteriorCanvas = corrida.getMascaraExteriorCanvas();
			if (mascaraExteriorCanvas == null) {
				corrida.getFigura().calcRegionesYExtrasParticulares(app, true);
			}
			mascaraExteriorCanvas.loadPixels();
			int xSum = 0;
			int ySum = 0;
			int count = 0;
			for (int x = 0; x < AnalizadorVideo.WIDTH; x++) {
				for (int y = 0; y < AnalizadorVideo.HEIGHT; y++) {
					int mascPix = mascaraExteriorCanvas.pixels[x+ y * AnalizadorVideo.WIDTH];
					if (app.blue(mascPix) > 0) {
						continue;
					}
					int pix = edgeDiff.pixels[x+ y * AnalizadorVideo.WIDTH];
					if (app.red(pix) > Parametros.limite) {
						boolean ignore = corrida.getXyPairsToIgnore()[x+y*AnalizadorVideo.WIDTH];
						if (ignore) {
							edgeDiff.pixels[x+y*AnalizadorVideo.WIDTH] = app.color(0, 0, 255);
						} else {
							xSum += x;
							ySum += y;
							count++;
							edgeDiff.pixels[x+y*AnalizadorVideo.WIDTH] = app.color(255, 0, 0);
						}
					}
				}
			}
			edgeDiff.updatePixels();

			if (count > 0) {
				imagenActualVideo = app.getImageFromMov(null);
				recorrido.agregarPosicion(mov.time(), xSum/count, ySum/count);
			}
		} else {
			//Se ignora, pero se muestra lo que se está ignorando
			imagenActualVideo = app.getImageFromMov(null);
		}
		
		if (imagenActualVideo != null) {
			corrida.setImagenActualVideo(imagenActualVideo);
		}

		app.pushMatrix();
		boolean showPosition = true;
		boolean showAll = false;
		if (app.mouseX > AnalizadorVideo.WIDTH/2.0 && app.mouseY < AnalizadorVideo.HEIGHT/2.0) {
			if (imagenActualVideo != null) {
				app.image(imagenActualVideo, 0, 0);
			} else {
				app.image(mov, 0, 0);
			}
		} else if (app.mouseX < AnalizadorVideo.WIDTH/4.0 && app.mouseY < AnalizadorVideo.HEIGHT/4.0 && corrida.getImagenDeFondo() != null) {
			showPosition = false;
			app.image(corrida.getImagenDeFondo(), 0, 0);
		} else if (app.mouseY > AnalizadorVideo.HEIGHT - (AnalizadorVideo.HEIGHT/8.0) && corrida.getMascaraImg() != null && edgeDiff != null) {
			showPosition = false;
			app.image(edgeDiff, 0, 0);
			app.blend(corrida.getMascaraImg(), 0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, 0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PConstants.ADD);
			app.blend(corrida.getMascaraExteriorCanvas(), 0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, 0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PConstants.ADD);
		} else {
			showAll= true;
			app.scale(0.5f);
			if (corrida.getImagenDeFondo() != null) {
				app.image(corrida.getImagenDeFondo(), 0, 0);
			}
			if (imagenActualVideo != null) {
				app.image(imagenActualVideo, AnalizadorVideo.WIDTH, 0);
			} else {
				app.image(mov, AnalizadorVideo.WIDTH, 0);
			}
			if (corrida.getMascaraImg() != null) {
				app.image(corrida.getMascaraImg(), 0, AnalizadorVideo.HEIGHT);
				app.blend(corrida.getMascaraExteriorCanvas(), 0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, 0, AnalizadorVideo.HEIGHT/2, AnalizadorVideo.WIDTH/2, AnalizadorVideo.HEIGHT/2, PConstants.ADD);
			}
			if (edgeDiff != null) {
				app.image(edgeDiff, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT);
			}

			//Para que lo que se dibuje después vaya encima de la imagen imagenActualVideo
			app.translate(AnalizadorVideo.WIDTH, 0);
		}

		//Se dibuja el recorrido y la psoición actual
		if (mostrarRecorrido && showPosition) {
			recorrido.drawRecorridoYUltimaPosicion(app.getGraphics());
		}

		//Se obtiene la región actual y se actualizan las estadísticas
		if (!ignorar) {
			RegionData posicionActual = corrida.getFigura().getRegionData(app, recorrido.getUltimaPosicionX(), recorrido.getUltimaPosicionY());
			if (posicionActual != null) {
				estadisticas.add(movTime, posicionActual);

				if (showPosition) {
					app.pushStyle();
					app.stroke(app.color(255, 64));
					app.fill(app.color(255, 64));

					app.beginShape();
					for (PVector aPoint : posicionActual.getRegion()) {
						app.vertex(aPoint.x, aPoint.y);
					}
					app.endShape(PConstants.CLOSE);
					app.popStyle();
				}
			}
		}

		corrida.setIgnorando(ignorar);
		app.popMatrix();

		app.fill(255);
		if (showAll) {
			app.text("Fondo con el que se compara", 10, 20);
			app.text("mascara", 10, AnalizadorVideo.HEIGHT/2.0f + 20);
			app.text("Detección (pixeles azules ignorados)", AnalizadorVideo.WIDTH/2.0f + 10, AnalizadorVideo.HEIGHT/2.0f + 20);
		}
		if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_OPEN_FIELD) {
			app.text("# Lineas Cruzadas: " + estadisticas.getCantidadLineasCruzadas(), 10, 40);
			app.text("T Periferia: " + String.format(Parametros.locale, "%.2f", estadisticas.getTiempoEnZonaPeriferia()) +"s", 10, 60);
			app.text("T Esquinas: " + String.format(Parametros.locale, "%.2f", estadisticas.getTiempoEnZonaEsquina()) +"s", 10, 80);
		} else if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_PLUS_MAZE) {
			app.text(String.format(Parametros.locale, "T Zona 0: %.2fs", estadisticas.getTiempoEnZona0()), 10, 40);
			app.text(String.format(Parametros.locale, "T Zona 1: %.2fs", estadisticas.getTiempoEnZona1()), 10, 60);
			app.text(String.format(Parametros.locale, "T Zona 2: %.2fs", estadisticas.getTiempoEnZona2()), 10, 80);
			app.text(String.format(Parametros.locale, "T Zona 3: %.2fs", estadisticas.getTiempoEnZona3()), 10, 100);
			app.text(String.format(Parametros.locale, "T Zona 4: %.2fs", estadisticas.getTiempoEnZona4()), 10, 120);

			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona0()), 140, 40);
			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona1()), 140, 60);
			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona2()), 140, 80);
			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona3()), 140, 100);
			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona4()), 140, 120);

		} else if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_Y_MAZE) {
			app.text(String.format(Parametros.locale, "T Zona 0: %.2fs", estadisticas.getTiempoEnZona0()), 10, 40);
			app.text(String.format(Parametros.locale, "T Zona 1: %.2fs", estadisticas.getTiempoEnZona1()), 10, 60);
			app.text(String.format(Parametros.locale, "T Zona 2: %.2fs", estadisticas.getTiempoEnZona2()), 10, 80);
			app.text(String.format(Parametros.locale, "T Zona 3: %.2fs", estadisticas.getTiempoEnZona3()), 10, 100);

			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona0()), 140, 40);
			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona1()), 140, 60);
			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona2()), 140, 80);
			app.text(String.format(Parametros.locale, "#%d", estadisticas.getCantEntradasEnZona3()), 140, 100);
		}
		app.text(corrida.getNombreVideo(), AnalizadorVideo.WIDTH/2 + 10, 15);
		app.text("T Video: " + (int)mov.time() + "s T Corrida: " + (int)(corrida.getTiempoCorrida()) + "s T Ignorado: " + (int)corrida.getTiempoIgnorando() + "s", AnalizadorVideo.WIDTH/2 + 10, 35);
		app.drawVersion(AnalizadorVideo.WIDTH - 100, AnalizadorVideo.HEIGHT - 10);

		if (ignorar) {
			//Se pinta la pantalla semitransparente para indicar que se está ignorando
			app.pushStyle();
			app.fill(0,0,255,100);
			app.rect(0, 0, AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT);
			app.popStyle();
		}

		if (mostrarMensajeCapturaGrabadaTime > 0) {
			app.pushStyle();
			app.fill(255,127);
			app.strokeWeight(4);

			app.rect(10, AnalizadorVideo.HEIGHT - 30, 115, 20);
			app.fill(0);
			app.text("Captura tomada", 20, AnalizadorVideo.HEIGHT - 15);
			app.popStyle();

			if (mostrarMensajeCapturaGrabadaTime < app.millis()) {
				mostrarMensajeCapturaGrabadaTime = -1;
			}
		}

		if (mostrarAyuda) {
			app.pushStyle();
			app.fill(255,190);
			app.strokeWeight(4);
			int margen = 40;
			app.rect(margen, margen, AnalizadorVideo.WIDTH-2*margen, AnalizadorVideo.HEIGHT-2*margen);
			app.fill(0);
			app.text("Tecla 'A' muestra esta Ayuda\n" +
					"La Barra Espaciadora pone en Pausa el video para poder marcar momentos a ignorar\n" +
					"Tecla 'C' toma una Captura (se guardan en la misma ubicación que el video)\n" +
					"Tecla 'E' vuelve a Edición de la grilla y máscara\n    (Vuelve los contadores a 0 y el video al principio de la corrida)\n" +
					"Tecla 'R' alterna entre mostrar y deja de mostrar el Recorrido", 
					margen*1.5f, margen*1.5f);
			app.popStyle();
			mostrarAyuda = false;
		}

		//Finalizó? (llegó al final? o se indicó cuando termina la corrida?)
		if((mov.duration() - 0.1) <= mov.time() || (corrida.getTimeRunDuration() > 0 && corrida.getTiempoCorrida() >= corrida.getTimeRunDuration())) {
			//Sí, entonces paso a modo Final
			return new PantallaFinal(app, this);
		}
		
		return null;
	}



	@Override
	public void reset() {
		mostrarRecorrido = true;
		mostrarMensajeCapturaGrabadaTime = -1;
		mostrarAyuda = false;
		isReturnToEdit = false;
	}

	@Override
	public void keyPressed() {
		Movie mov = app.getCorrida().getMov();;
		//C toma una Captura
		//E vuelve a Edición de la grilla y máscara
		//R muestra o deja de mostrar el Recorrido
		if (app.key == 'a' || app.key == 'A') {
			//A muestra Ayuda
			mostrarAyuda = true;//Se muestra al final, para que quede por artriba de todo lo demás
		} else if (app.key == ' ' || app.key == ' ') {
			//Barra Espaciadora pone en Pausa la corrida y permite marcar momentos del video a ignorar
			isGoToEditMascaraTemporal = true;
			app.keyPressed = false;
			return;
		} else if (app.key == 'c' || app.key == 'C') {
			app.saveFrame(mov.filename+"-######.jpg");
			mostrarMensajeCapturaGrabadaTime = app.millis()+1000;
			app.keyPressed = false;
		} else if (app.key == 'e' || app.key == 'E') {
			isReturnToEdit = true;
			app.keyPressed = false;
			return;
		} else if (app.key == 'r' || app.key == 'R') {
			mostrarRecorrido = !mostrarRecorrido;
			app.keyPressed = false;
		}
	}

	@Override
	public void keyReleased() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed() {
		// TODO Auto-generated method stub

	}

}
