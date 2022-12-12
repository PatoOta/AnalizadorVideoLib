package com.potar.videoanalizer.gui.component;

import com.potar.AnalizadorVideo;

import processing.core.PApplet;
import processing.core.PVector;

public class FreeMice extends Component {
	private float miceX;
	private float miceY;

	private float miceDrag = 30;
	private boolean exitMiceJump = false;
	private boolean irAlMouse = false;
	private Boton[] botones;
	private BotonSalida exitDoor = null;

	// La posición (posX,posY) es para la casita del ratón
	public FreeMice(AnalizadorVideo app, float posX, float posY, Boton...botones) {
		super(app, posX, posY);
		miceX = posX;
		miceY = posY;
		this.botones = botones;
		for (int i = 0; i < botones.length && exitDoor == null; i++){
			Boton boton = botones[i];
			if (boton instanceof BotonSalida) {
				exitDoor = (BotonSalida) boton;
			}
		}
	}

	@Override
	public void draw() {
		app.pushStyle();
		// El ratón
		float miceTargetX = app.mouseX;
		float miceTargetY = app.mouseY;
		boolean overAnyButton = false;
		for (int i = 0; i < botones.length && !overAnyButton; i++){
			Boton boton = botones[i];
			overAnyButton |= boton.over(miceTargetX, miceTargetY);
		}
		
		if (!overAnyButton) {
			if (!irAlMouse) {
				miceTargetX = posX;
				miceTargetY = posY;
			}
		} else {
			irAlMouse = true;
		}
		float dx = miceTargetX - miceX;
		miceX += dx / miceDrag;
		float dy = miceTargetY - miceY;
		miceY += dy / miceDrag;

		boolean openFieldOver = false;//Por ahora no está el botón 
		boolean plusMazeOver = false;//Por ahora no está el botón 
		boolean yMazeOver = false;//Por ahora no está el botón 
		if (openFieldOver || plusMazeOver || yMazeOver) {
			exitMiceJump = true;
			miceDrag = 20;
		} else {
			exitMiceJump = false;
			miceDrag = 30;
		}

		if (PApplet.abs(dx) > 5 || PApplet.abs(dy) > 5 || overAnyButton) {
			app.stroke(255);
			app.fill(0);
			app.arc(posX, posY+4, 20, 20, PApplet.PI, PApplet.TWO_PI);
			app.noFill();
			PVector direccion = new PVector(dx, dy);
			miceShape(miceX, miceY, direccion.heading() + PApplet.PI + PApplet.radians(app.random(30) - 15), 50,
					exitMiceJump);
			app.fill(app.g.backgroundColor);
			app.noStroke();
			if (exitDoor != null) {
				app.rect(exitDoor.posX + BotonSalida.exitWidth + 3, exitDoor.posY, BotonSalida.exitWidth, BotonSalida.exitHeight);
			}
			if (PApplet.abs(dx) < 10 && PApplet.abs(dy) < 10 && !overAnyButton) {
				app.fill(app.g.backgroundColor, PApplet.map(PApplet.max(PApplet.abs(dx), PApplet.abs(dy)), 10, 0, 0, 255));
				app.rect(posX - BotonSalida.exitWidth, posY - 115, BotonSalida.exitWidth * 2, 120);
			}
		}

		if (!overAnyButton && irAlMouse) {
			// Está cerca del mouse?
			if (PApplet.abs(app.mouseX - miceX) < 30 && PApplet.abs(app.mouseY - miceY) < 30) {
				// Ahora ir a casita entonces
				irAlMouse = false;
			}
		}
		app.popStyle();
	}

	private void miceShape(float miceX, float miceY, float miceAngle, int miceTailDir, boolean jump) {
		app.pushStyle();
		app.strokeWeight(1);
		app.fill(100);
		app.stroke(0);
		app.pushMatrix();
		app.translate(miceX, miceY);
		if (jump) {
			app.scale(3.0f);
		} else {
			app.scale(1.5f);
		}
		app.rotate(miceAngle);
		app.ellipse(0, 0, 10, 5);
		app.noFill();

		if (miceTailDir > 0) {
			app.arc(7.5f, 0, 5, miceTailDir / 10.0f, 0, PApplet.PI);
			app.arc(12.5f, 0, 5, miceTailDir / 20.0f, -PApplet.PI, 0);
		} else {
			app.arc(7.5f, 0, 5, miceTailDir / -10.0f, -PApplet.PI, 0);
			app.arc(12.5f, 0, 5, miceTailDir / -20.0f, 0, PApplet.PI);
		}
		app.popMatrix();
		app.popStyle();
	}

}
