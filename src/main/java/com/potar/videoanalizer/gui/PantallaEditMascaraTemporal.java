package com.potar.videoanalizer.gui;

import javax.swing.JOptionPane;

import com.potar.AnalizadorVideo;
import com.potar.videoanalizer.runtime.data.Corrida;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.video.Movie;

public class PantallaEditMascaraTemporal extends Pantalla {
	private PImage[] muestreo;
	private int lastMuestreoIndex;
	private int tiempoBase;
	private float hastaMomentoAnalizado;
	private boolean mostrarMensajeAyudasMascaraTemporal;
	private boolean esperandoQueCargueCuadroMuestreo;

	private Movie mov;
	private int cuadroMouse;
	private int returnTime;

	public PantallaEditMascaraTemporal(AnalizadorVideo app, Pantalla pantallaAnterior) {
		super(app, pantallaAnterior);
		reset();
	}

	@Override
	public Pantalla draw() {
		if (returnTime > -1) {
			Corrida corrida = app.getCorrida();
			corrida.getEstadisticas().jumpTo(returnTime);
			corrida.getRecorrido().jumpTo(returnTime);
			corrida.setCuandoVolverAConsultarMascaraTemporal(0);
			corrida.setIgnorando(false);
			mov.jump(returnTime);
			mov.play();
			mov.volume(1.0f);

			tiempoBase = -1;
			returnTime = -1;

			return this.getPrev();
		}
		app.background(127);

		if (tiempoBase < 0) {
			hastaMomentoAnalizado = mov.time();
			int segundosReferencia = (int)hastaMomentoAnalizado;
			tiempoBase = segundosReferencia - (muestreo.length/2);
			if (tiempoBase < 0 ) {
				tiempoBase = 0;
			}
		}

		if (lastMuestreoIndex < muestreo.length) {
			esperandoQueCargueCuadroMuestreo = cargarMuestreoDeImagenes(tiempoBase, muestreo, lastMuestreoIndex, esperandoQueCargueCuadroMuestreo);
			if (!esperandoQueCargueCuadroMuestreo) {
				lastMuestreoIndex++;
			}
		}

		cuadroMouse = (int)PApplet.map(app.mouseX, 0, AnalizadorVideo.WIDTH, 0, muestreo.length);

		app.pushStyle();
		app.imageMode(PConstants.CENTER);
		app.rectMode(PConstants.CENTER);
		for (int dist = muestreo.length-1; dist > -1; dist--) {
			int i1 = cuadroMouse - dist;
			int i2 = cuadroMouse + dist;
			if (0 <= i1 && i1 < lastMuestreoIndex) {
				float x = (1.0f * AnalizadorVideo.WIDTH / muestreo.length) * i1;
				float y = PApplet.lerp(0, (AnalizadorVideo.HEIGHT * 3.0f / 4.0f), PApplet.cos(PApplet.radians(PApplet.map(dist, 0, muestreo.length-1, 0, 90))));
				float imgWidth = AnalizadorVideo.WIDTH/2.2f - (AnalizadorVideo.WIDTH / 98.0f * dist);
				float imgHeight = AnalizadorVideo.HEIGHT / 2.2f - (AnalizadorVideo.HEIGHT / 98.0f * dist);
				app.fill(app.getCorrida().mustIgnore(tiempoBase + i1) ? app.color(255,0,0) : 255);
				app.pushMatrix();
				app.translate(x, y);
				app.shearX(-PApplet.radians(PApplet.map(dist, 0, muestreo.length-1, 0, 45)));
				app.rect(0,0,imgWidth+4, imgHeight+4);
				app.image(muestreo[i1], 0, 0, imgWidth, imgHeight);
				app.popMatrix();
			}
			if (0 <= i2 && i2 < lastMuestreoIndex) {
				float x = (1.0f * AnalizadorVideo.WIDTH / muestreo.length) * i2;
				float y = PApplet.lerp(0, (AnalizadorVideo.HEIGHT * 3.0f / 4.0f), PApplet.cos(PApplet.radians(PApplet.map(dist, 0, muestreo.length-1, 0, 90))));
				float imgWidth = AnalizadorVideo.WIDTH/2.2f - (AnalizadorVideo.WIDTH / 98.0f * dist);
				float imgHeight = AnalizadorVideo.HEIGHT / 2.2f - (AnalizadorVideo.HEIGHT / 98.0f * dist);
				app.fill(app.getCorrida().mustIgnore(tiempoBase + i2) ? app.color(255,0,0) : 255);
				app.pushMatrix();
				app.translate(x, y);
				app.shearX(PApplet.radians(PApplet.map(dist, 0, muestreo.length-1, 0, 45)));
				app.rect(0,0,imgWidth+4, imgHeight+4);
				app.image(muestreo[i2], 0, 0, imgWidth, imgHeight);
				app.popMatrix();
			}
		}

		if (cuadroMouse >= 0 && cuadroMouse < lastMuestreoIndex) {
			app.image(muestreo[cuadroMouse], AnalizadorVideo.WIDTH / 2.0f, AnalizadorVideo.HEIGHT / 4.0f, AnalizadorVideo.WIDTH / 2.0f, AnalizadorVideo.HEIGHT / 2.0f);
			app.fill(255);
			app.text((tiempoBase + cuadroMouse) + " seg", AnalizadorVideo.WIDTH / 2.0f - 40.0f, 20);
		}
		app.rectMode(PConstants.CORNER);
		app.imageMode(PConstants.CORNER);
		app.popStyle();


		if (mostrarMensajeAyudasMascaraTemporal) {
			app.pushStyle();
			app.fill(255,190);
			app.strokeWeight(4);
			int margen = 20;
			app.rect(margen, margen, AnalizadorVideo.WIDTH-2*margen, AnalizadorVideo.HEIGHT-2*margen);
			app.fill(0);
			app.text("Edición del enmascarado temporal del video (momentos a ignorar)\n"+
					"Tecla 'A' para volver a mostrar este mensaje de Ayuda\n" +
					"Teclas '+' y '-' para desplazarse en el video más adelante o hacia atrás\n"+
					"Tecla 'D' para indicar o quitar que Desde ahí en adelante ignorar lo que suceda en el video\n" +
					"Tecla 'H' para indicar o quitar que Hasta ahí NO inclusive ignorar lo que suceda en el video\n    O sea, a partir de aquí se retoma el análisis del video\n" +
					"Tecla 'P' para poner Play al video y comenzar el análisis de los movimientos\n    Se debe elegir el momento para retomar el análisis.\n" +
					"La Barra Espaciadora Durante la reproducción del video para volver a esta pantalla.", 
					margen*1.8f, margen*1.8f);
			app.popStyle();

		}

		return null;
	}


	private boolean cargarMuestreoDeImagenes(int tiempoBase, PImage[] muestreo, int lastMuestreoIndex, boolean esperandoQueCargueCuadro) {
		if (!esperandoQueCargueCuadro) {
			esperandoQueCargueCuadro = true;
			mov.play();

			if (lastMuestreoIndex > 0 || tiempoBase > 0) {
				mov.jump(tiempoBase + lastMuestreoIndex);
			} else {
				mov.jump(0.5f);//Para no ir exactamente al inicio del video
			}
			mov.read();
		}

		if (mov.available() && mov.width > 0 && mov.height > 0) {
			mov.read();
			muestreo[lastMuestreoIndex] =  app.getImageFromMov(null);
			muestreo[lastMuestreoIndex].loadPixels();
			boolean cargoBien = false;
			for (int i = 0; i < AnalizadorVideo.WIDTH * app.displayHeight && !cargoBien; i++) {
				cargoBien = app.brightness(muestreo[lastMuestreoIndex].pixels[i]) > 0;
			}
			if (cargoBien) {
				mov.pause();
				lastMuestreoIndex++;
				esperandoQueCargueCuadro = false;
			}
		}
		return esperandoQueCargueCuadro;
	}

	@Override
	public void reset() {
		returnTime = -1;
		muestreo = new PImage[30];
		lastMuestreoIndex = 0;
		tiempoBase = -1;
		hastaMomentoAnalizado = 0;
		mostrarMensajeAyudasMascaraTemporal = true;
		esperandoQueCargueCuadroMuestreo = false;
		mov = app.getCorrida().getMov();
	}

	@Override
	public void keyPressed() {
		if (app.key == '+') {
			lastMuestreoIndex = 0;
			esperandoQueCargueCuadroMuestreo = false;
			tiempoBase += 30;
			if (tiempoBase > mov.duration()) {
				tiempoBase = (int)mov.duration();
			}
			app.keyPressed = false;
			return;
		}

		if (app.key == '-') {
			lastMuestreoIndex = 0;
			esperandoQueCargueCuadroMuestreo = false;
			tiempoBase -= 30;
			if (tiempoBase < 0) {
				tiempoBase = 0;
			}
			app.keyPressed = false;
			return;
		}

		Corrida corrida = app.getCorrida();
		if ((app.key == 'i' || app.key == 'I') && cuadroMouse >= 0 && cuadroMouse < lastMuestreoIndex) {
			Integer segundo = tiempoBase + cuadroMouse;
			Boolean ignore = corrida.getIgnorarRaw(segundo);
			if (ignore == null) {
				corrida.setIgnorarRaw(segundo, true);
				Boolean ignoreFin = corrida.getIgnorarRaw(segundo+1);
				if (ignoreFin == null) {
					corrida.setIgnorarRaw(segundo + 1, false);
				}
			} else {
				if (ignore) {
					corrida.setIgnorarRaw(segundo, false);
				} else {
					corrida.setIgnorarRaw(segundo, true);
					Boolean ignoreFin = corrida.getIgnorarRaw(segundo+1);
					if (ignoreFin == null) {
						corrida.setIgnorarRaw(segundo + 1, false);
					}
				}
			}
			app.keyPressed = false;
			return;
		}

		if ((app.key == 'd' || app.key == 'D') && cuadroMouse >= 0 && cuadroMouse < lastMuestreoIndex) {
			Integer segundo = tiempoBase + cuadroMouse;
			Boolean ignore = corrida.getIgnorarRaw(segundo);
			if (ignore == null) {
				corrida.setIgnorarRaw(segundo, true);
			} else {
				corrida.removeIgnorarRaw(segundo);
			}
			app.keyPressed = false;
			return;
		}

		if ((app.key == 'h' || app.key == 'H') && cuadroMouse >= 0 && cuadroMouse < lastMuestreoIndex) {
			Integer segundo = tiempoBase + cuadroMouse;
			Boolean ignore = corrida.getIgnorarRaw(segundo);
			if (ignore == null) {
				corrida.setIgnorarRaw(segundo, false);
			} else {
				corrida.removeIgnorarRaw(segundo);
			}
			app.keyPressed = false;
			return;
		}

		if ((app.key == 'p' || app.key == 'P') && cuadroMouse >= 0 && cuadroMouse < lastMuestreoIndex) {
			returnTime = tiempoBase + cuadroMouse;
			if (corrida.mustIgnore(returnTime)) {
				JOptionPane.showMessageDialog(null, "Debe volver a un cuadro que no esté ignorado", "Retorno al análisis", JOptionPane.ERROR_MESSAGE);
				returnTime = -1;
				return;
			}
			if (returnTime > hastaMomentoAnalizado) {
				JOptionPane.showMessageDialog(null, "Debe volver a un momento previo o al mismo momento al que se había llegado antes ("+((int)hastaMomentoAnalizado)+"seg)", "Retorno al análisis", JOptionPane.ERROR_MESSAGE);
				returnTime = -1;
				return;
			}

			if (returnTime < corrida.getTimeStartRun()) {
				JOptionPane.showMessageDialog(null, "Debe volver a un momento posterior o igual al comienzo de la corrida ("+((int)corrida.getTimeStartRun())+"seg)", "Retorno al análisis", JOptionPane.ERROR_MESSAGE);
				returnTime = -1;
				return;
			}

			app.keyPressed = false;
			return;
		}
	}

	@Override
	public void keyReleased() {
		mostrarMensajeAyudasMascaraTemporal = false;
	}

	@Override
	public void mousePressed() {
		mostrarMensajeAyudasMascaraTemporal = false;
	}

}
