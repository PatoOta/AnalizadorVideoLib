package com.potar.videoanalizer.gui;

import com.potar.AnalizadorVideo;

public abstract class Pantalla {
	protected AnalizadorVideo app;
	private Pantalla pantallaAnterior;

	public Pantalla(AnalizadorVideo app, Pantalla pantallaAnterior) {
		super();
		this.app = app;
		this.pantallaAnterior = pantallaAnterior != null ? pantallaAnterior : this;
	}

	public Pantalla getPrev() {
		pantallaAnterior.reset();
		return pantallaAnterior;
	}

	public abstract Pantalla draw();
	public abstract void reset();
	public abstract void keyPressed();
	public abstract void keyReleased();
	public abstract void mousePressed();
}
