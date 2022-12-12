package com.potar.videoanalizer.gui;

import com.potar.AnalizadorVideo;
import com.potar.videoanalizer.gui.component.BotonAbrirVideo;
import com.potar.videoanalizer.gui.component.BotonSalida;
import com.potar.videoanalizer.gui.component.FreeMice;

public class PantallaInicial extends Pantalla {
	//GUI components
	private BotonAbrirVideo botonAbrirVideo;
	private BotonSalida botonSalida;
	private FreeMice freeMice;
	
	public PantallaInicial(AnalizadorVideo app) {
		super(app, null);
		botonSalida = new BotonSalida(app, AnalizadorVideo.WIDTH*7/8.0f, AnalizadorVideo.HEIGHT/12.0f);
		freeMice = new FreeMice(app, AnalizadorVideo.WIDTH/4f, AnalizadorVideo.HEIGHT-5, botonSalida);
		reset();
	}
	
	@Override
	public Pantalla draw() {
		app.background(127);
		botonAbrirVideo.draw();
		botonSalida.draw();
		freeMice.draw();
		
		if (botonAbrirVideo.isMovieReady()) {
			app.getCorrida().setMov(botonAbrirVideo.getMovie());
			app.getCorrida().setRotateMovie(botonAbrirVideo.isRotateMovie());
			app.getCorrida().setNombreVideo(botonAbrirVideo.getNombreVideo());
			
			return new PantallaSelectorFondo(app, this);
		} else {
			return this;
		}
	}
	
	@Override
	public void reset() {
		app.getCorrida().setMov(null);
		app.getCorrida().setRotateMovie(false);
		app.getCorrida().setNombreVideo(null);
		botonAbrirVideo = new BotonAbrirVideo(app, AnalizadorVideo.WIDTH/2.0f, AnalizadorVideo.HEIGHT/2.0f);
	}

	@Override
	public void keyPressed() {
		if (app.key == 'e') {
			app.fill((float)(Math.random()*255), (float)(Math.random()*255), (float)(Math.random()*255));
		}
	}

	@Override
	public void keyReleased() {
	}

	@Override
	public void mousePressed() {
		botonSalida.mousePressed();
		botonAbrirVideo.mousePressed();
	}

}
