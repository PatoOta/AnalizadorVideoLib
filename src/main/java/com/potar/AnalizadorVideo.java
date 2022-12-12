package com.potar;

import javax.swing.JOptionPane;

import com.potar.videoanalizer.gui.Pantalla;
import com.potar.videoanalizer.gui.PantallaAnalizarVideo;
import com.potar.videoanalizer.gui.PantallaEditMascaraTemporal;
import com.potar.videoanalizer.gui.PantallaFinal;
import com.potar.videoanalizer.gui.PantallaInicial;
import com.potar.videoanalizer.runtime.data.Corrida;
import com.potar.videoanalizer.runtime.data.Parametros;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

public class AnalizadorVideo extends PApplet {
	public static final int WIDTH = 640;
	public static final int HEIGHT = 360;
	
	private Corrida corrida = new Corrida();

	private Pantalla pantallaActual = new PantallaInicial(this);
	private PGraphics internalCanvas = null;

	public static void main(String[] args) {
		main("com.potar.AnalizadorVideo");
	}

	public void settings() {
		size(WIDTH, HEIGHT);
	}

	public void setup() {
		background(127);
	}

	public void draw() {
		Pantalla pantallaNueva = pantallaActual.draw();
		if (pantallaNueva != null && pantallaNueva != pantallaActual) {
			pantallaActual = pantallaNueva;
		}
	}
	
	public void reset() {
		corrida = new Corrida();
		pantallaActual = new PantallaInicial(this);
		internalCanvas = null;
	}
	
	public Corrida getCorrida() {
		return corrida;
	}

	public void keyPressed() {
		if (key == ESC) {
			//Confirma si quiere salir y sale, o sigue la ejecución
			this.exit();
			key = 0;
			keyPressed = false;
		} else if ( key == PConstants.BACKSPACE || 
				   (key == PConstants.CODED  && (keyCode == PConstants.BACKSPACE || keyCode == com.jogamp.newt.event.KeyEvent.VK_BACK_SPACE)) ) {
			if (!(pantallaActual instanceof PantallaAnalizarVideo) && 
				!(pantallaActual instanceof PantallaFinal) && 
				!(pantallaActual instanceof PantallaEditMascaraTemporal) &&
				!(pantallaActual instanceof PantallaInicial)) {
				pantallaActual = pantallaActual.getPrev();
				pantallaActual.reset();
				keyPressed = false;
			}
		} else if (pantallaActual != null) {
			pantallaActual.keyPressed();
		}
	}

	public void keyReleased() {
		pantallaActual.keyReleased();
	}

	public void mousePressed() {
		pantallaActual.mousePressed();
	}
	
	@Override
	public void exit() {
		int resp = JOptionPane.showConfirmDialog(null, "¿Está seguro de salir de la aplicación?",
				"Salir de la aplicación", JOptionPane.YES_NO_OPTION);

		if (resp == JOptionPane.YES_OPTION) {
			super.exit();
		}
	}
	
	public PImage getImageFromMov(PImage toImage) {
		if (toImage == null) {
			toImage = createImage(WIDTH, HEIGHT, RGB);
		}

		Movie mov = corrida.getMov();
		if (!corrida.isRotateMovie()) {
			toImage.copy(mov, 0, 0, mov.width, mov.height, 0, 0, WIDTH, HEIGHT);
			return toImage;
		} else {
			if (internalCanvas == null) {
				internalCanvas = createGraphics(mov.height, mov.width, JAVA2D);
			}

			internalCanvas.beginDraw();
			internalCanvas.pushMatrix();
			internalCanvas.translate(mov.height / 2.0f, mov.width / 2.0f);
			internalCanvas.rotate(radians(90));
			internalCanvas.translate(mov.width / -2.0f, mov.height / -2.0f);
			internalCanvas.image(mov, 0, 0);
			internalCanvas.popMatrix();
			internalCanvas.endDraw();
			toImage.copy(internalCanvas, 0, 0, mov.height, mov.width, 0, 0, WIDTH, HEIGHT);
			return toImage;
		}
	}
	
	public void drawVersion(float x, float y) {
		pushStyle();
		fill(255);
		text("Ver " + Parametros.appVersion, x, y);
		popStyle();
	}
}
