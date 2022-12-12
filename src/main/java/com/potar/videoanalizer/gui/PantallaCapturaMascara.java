package com.potar.videoanalizer.gui;

import javax.swing.JOptionPane;

import com.potar.AnalizadorVideo;

import milchreis.imageprocessing.Comparison;
import milchreis.imageprocessing.Grayscale;
import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Movie;

public class PantallaCapturaMascara extends Pantalla {
	private boolean capturaMascaraComenzada;
	private PImage mascaraImg;
	private boolean[] xyPairsToIgnore;
	private boolean mascaraCompleta;
	private Movie mov;

	public PantallaCapturaMascara(AnalizadorVideo app, Pantalla pantallaAnterior) {
		super(app, pantallaAnterior);
		reset();
	}

	@Override
	public Pantalla draw() {
		if (!mascaraCompleta) {
			if (!capturaMascaraComenzada) {
				mov.play();
				mov.jump(app.getCorrida().getTimeStartBackground());
				mov.read();
				capturaMascaraComenzada = true;
			}

			PImage imagenDeFondo = app.getCorrida().getImagenDeFondo();
			if (imagenDeFondo != null && mov.available() && mov.width > 0 && mov.height > 0) {
				mov.read();

				PImage edgeDiff = Grayscale.apply(Comparison.calculateDifferenceImage(imagenDeFondo, app.getImageFromMov(null)));

				edgeDiff.loadPixels();
				mascaraImg.loadPixels();
				for (int x = 0; x < AnalizadorVideo.WIDTH; x++) {
					for (int y = 0; y < AnalizadorVideo.HEIGHT; y++) {
						int pix = edgeDiff.pixels[x + y * AnalizadorVideo.WIDTH];
						float brightness = app.red(pix);
						if (brightness > 10.0) {
							xyPairsToIgnore[x + y * AnalizadorVideo.WIDTH] = true;
							mascaraImg.pixels[x + y * AnalizadorVideo.WIDTH] = app.color(0, 0, 255.0f);
						}
					}
				}
				mascaraImg.updatePixels();
				
				if (mov.time() > app.getCorrida().getTimeStartBackground() + 3) {
					mascaraCompleta = true;
					mov.pause();
				}
			}

			app.image(mascaraImg, 0, 0);
		}
		// Informar que se finalizó
		if (mascaraCompleta) {
			JOptionPane.showMessageDialog(null,
					"Máscara obtenida automáticamente.\nMás adelante podrá editarla manualmente.",
					"Máscara para ignorar movimientos en las partes azules", JOptionPane.INFORMATION_MESSAGE);

			// Ahora se para a modo edición para ajustar la máscara y otros
			return new PantallaEdicion(app, this);
		} else {
			return this;
		}
	}

	@Override
	public void reset() {
		mov = app.getCorrida().getMov();
		capturaMascaraComenzada = false;
		mascaraCompleta = false;
		mascaraImg = app.createImage(AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PApplet.RGB);
		app.getCorrida().setMascaraImg(mascaraImg);
		mascaraImg.loadPixels();
		for (int i = 0; i < AnalizadorVideo.WIDTH * AnalizadorVideo.HEIGHT; i++) {
			mascaraImg.pixels[i] = app.color(0, 0, 0);
		}
		mascaraImg.updatePixels();
		xyPairsToIgnore = new boolean[AnalizadorVideo.WIDTH * AnalizadorVideo.HEIGHT];
		app.getCorrida().setXyPairsToIgnore(xyPairsToIgnore);
	}

	@Override
	public void keyPressed() {
	}

	@Override
	public void keyReleased() {
	}

	@Override
	public void mousePressed() {
	}

}
