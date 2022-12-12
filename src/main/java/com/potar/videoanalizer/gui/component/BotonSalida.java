package com.potar.videoanalizer.gui.component;

import com.potar.AnalizadorVideo;

import processing.core.PApplet;

public class BotonSalida extends Component implements Boton{
	public static final int exitWidth = 45;
	public static final int exitHeight = 90; 

	private int exitDoorAngle = 0;
	private boolean exitOver = false;

	public BotonSalida(AnalizadorVideo app, float posX, float posY) {
		super(app, posX, posY);
	}

	public void draw() {
		exitOver = over(app.mouseX, app.mouseY);

		// Marco negro
		app.pushStyle();
		app.stroke(0);
		app.strokeWeight(1);
		app.rect(posX - 2, posY - 2, exitWidth + 4, exitHeight + 4);

		// Relleno prado y cielo
		app.noStroke();
		app.fill(app.color(154, 203, 255));
		app.rect(posX, posY, exitWidth, exitHeight / 3.0f);
		app.fill(app.color(0, 200, 0));
		app.rect(posX, posY + exitHeight / 3.0f, exitWidth, exitHeight * 2.0f / 3.0f);
		for (int i = 0; i < 50; i++) {
			app.stroke(app.color(0, PApplet.map(i, 0, 50, 100, 200), 0));
			app.line(posX, posY + i + exitHeight / 3.0f, posX + exitWidth, posY + i + exitHeight / 3.0f);
		}

		app.noStroke();
		for (int r = (int) (exitWidth * 4.0 / 5.0); r > 0; --r) {
			app.fill(PApplet.map(r, (int) (exitWidth * 4.0 / 5.0), 0, 240, 255), 204, 0);
			app.arc(posX + exitWidth * 2.0f / 3.0f, posY, r, r, 0, PApplet.PI);
		}

		// Marco blanco
		app.stroke(255);
		app.strokeWeight(3);
		app.noFill();
		app.rect(posX, posY, exitWidth, exitHeight);
		app.strokeWeight(1);

		// La puerta
		app.pushMatrix();
		app.translate(posX + exitWidth + 2, posY);
		app.shearY(PApplet.radians(exitDoorAngle));
		app.fill(255);
		app.stroke(0);
		app.strokeWeight(1);
		app.rect((exitDoorAngle / 2) - exitWidth - 3, -2, exitWidth - (exitDoorAngle / 2) + 3, exitHeight + 4);

		exitDoorAngle += exitOver ? 1 : -1;
		if (exitDoorAngle > 80) {
			exitDoorAngle = 80;
		} else if (exitDoorAngle < 0) {
			exitDoorAngle = 0;
		}
		app.popMatrix();

		app.popStyle();
	}

	public boolean over(float x, float y) {
		return (posX <= x && x <= posX + exitWidth && posY <= y && y <= posY + exitHeight);
	}

	public void mousePressed() {
		if (exitOver) {
			app.exit();
		}
	}

}
