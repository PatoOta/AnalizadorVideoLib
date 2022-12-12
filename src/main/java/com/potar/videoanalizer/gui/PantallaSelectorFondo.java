package com.potar.videoanalizer.gui;

import javax.swing.JOptionPane;

import com.potar.AnalizadorVideo;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.video.Movie;

public class PantallaSelectorFondo extends Pantalla {
	private PImage[] imagenesDeFondo;
	private PImage imagenComienzoCorrida;
	private int lastImagenesDeFondoACargarIndex;
	private float segundosBase;
	private boolean esperandoQueCargueCuadro;
	private long timeSetFondoSeleccionado;
	private int cuadroMouse;
	
	private Movie mov;

	public PantallaSelectorFondo(AnalizadorVideo app, Pantalla pantallaAnterior) {
		super(app, pantallaAnterior);
		reset();
	}

	@Override
	public Pantalla draw() {

		if (lastImagenesDeFondoACargarIndex < imagenesDeFondo.length) {
			cargarImagenesDeFondo();
		}

		cuadroMouse = (int)PApplet.map(app.mouseX, 0, AnalizadorVideo.WIDTH, 0, imagenesDeFondo.length);

		app.background(127);
		if (app.getCorrida().getImagenDeFondo() == null) {
			app.pushStyle();
			app.fill(255);
			app.text("Elija el primer cuadro\ndespués del cartel,\ncon el campo libre\ny sin sombras.\nY haga click", 10, 20);
			app.popStyle();
		} else {
			app.image(app.getCorrida().getImagenDeFondo(), 10, AnalizadorVideo.HEIGHT / 4.0f, AnalizadorVideo.WIDTH / 4.0f, AnalizadorVideo.HEIGHT / 4.0f);
			app.pushStyle();
			app.fill(255);
			app.text("Imagen de fondo\npara comparar\nlos movimientos", 20, AnalizadorVideo.HEIGHT / 4.0f + 10);
			app.popStyle();

			if (imagenComienzoCorrida != null) {
				app.image(imagenComienzoCorrida, AnalizadorVideo.WIDTH * 03.0f / 4.0f, AnalizadorVideo.HEIGHT / 4.0f, AnalizadorVideo.WIDTH / 4.0f, AnalizadorVideo.HEIGHT / 4.0f);
				app.pushStyle();
				app.fill(255);
				app.text("Imagen del comienzo\nde la corrida", AnalizadorVideo.WIDTH * 03.0f / 4.0f + 10, AnalizadorVideo.HEIGHT / 4.0f + 10);
				app.popStyle();
			} else {
				app.pushStyle();
				app.fill(((int) (app.millis() / 1000)) % 2 == 0 ? app.color(255) : app.color(200, 0, 0));
				app.text("Ahora elija el primer\ncuadro de la corrida.\nY haga click", 10, 20);
				app.popStyle();
			}
		}

		app.pushStyle();
		app.fill(255);
		app.text("Si necesita ver más,\nuse las teclas\n + para ir más adelante\n - para retroceder", AnalizadorVideo.WIDTH * 03.0f / 4.0f + 10, 20);
		app.popStyle();

		app.imageMode(PConstants.CENTER);
		for (int dist = 9; dist > -1; dist--) {
			int i1 = cuadroMouse - dist;
			int i2 = cuadroMouse + dist;
			if (0 <= i1 && i1 < lastImagenesDeFondoACargarIndex) {
				app.image(imagenesDeFondo[i1], (1.0f * AnalizadorVideo.WIDTH / imagenesDeFondo.length) * i1, AnalizadorVideo.HEIGHT * 3.0f / 4.0f,
						AnalizadorVideo.WIDTH / 2.2f - (AnalizadorVideo.WIDTH / 28.0f * dist), AnalizadorVideo.HEIGHT / 2.2f - (AnalizadorVideo.HEIGHT / 28.0f * dist));
			}
			if (0 <= i2 && i2 < lastImagenesDeFondoACargarIndex) {
				app.image(imagenesDeFondo[i2], (1.0f * AnalizadorVideo.WIDTH / imagenesDeFondo.length) * i2, AnalizadorVideo.HEIGHT * 3.0f / 4.0f,
						AnalizadorVideo.WIDTH / 2.2f - (AnalizadorVideo.WIDTH / 28.0f * dist), AnalizadorVideo.HEIGHT / 2.2f - (AnalizadorVideo.HEIGHT / 28.0f * dist));
			}
		}

		if (cuadroMouse >= 0 && cuadroMouse < lastImagenesDeFondoACargarIndex) {
			app.image(imagenesDeFondo[cuadroMouse], AnalizadorVideo.WIDTH / 2.0f, AnalizadorVideo.HEIGHT / 4.0f, AnalizadorVideo.WIDTH / 2.0f, AnalizadorVideo.HEIGHT / 2.0f);
			app.text((segundosBase + cuadroMouse) + " seg", AnalizadorVideo.WIDTH / 2.0f - 40.0f, 20);
		}
		app.imageMode(PConstants.CORNER);

		// Ya se tiene la imagen de fondo y el momento del comienzo de la corrida? (y se
		// le da un tiempo para que se muestre la imagen imagenComienzoCorrida)
		if (app.getCorrida().getImagenDeFondo() != null && app.getCorrida().getTimeStartRun() > -1 && timeSetFondoSeleccionado <= app.millis()) {
			// Sí, entonces falta obtener la duración de la corrida
			Object[] possibleValues = { "300 seg (5 min)", "600 seg (10 min)", "Hasta el final del video", "Otro",
					"Cancelar" };
			int selectedValue = JOptionPane.showOptionDialog(null,
					"Elija la opción que corresponda.\n(Debería ser igual o menor a " + (int) (mov.duration() - app.getCorrida().getTimeStartRun()) + "seg)",
					"Duración de la corrida", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, possibleValues, possibleValues[0]);
			switch (selectedValue) {
			case 0: // 300
				app.getCorrida().setTimeRunDuration(300);
				break;
			case 1: // 600
				app.getCorrida().setTimeRunDuration(600);
				break;

			case 2: // Hasta el final del video
				app.getCorrida().setTimeRunDuration(mov.duration());
				break;

			default:// Cancelar
			case -1:// Cancelar
			case 4: // Cancelar
				reset();
				break;
			case 3:// Otro
				boolean repetirPregunta = true;
				do {
					String duracionStr = JOptionPane.showInputDialog(null,
							"Por favor indique los segundos de duración.\n(Debería ser igual o menor a " + (int) (mov.duration() - app.getCorrida().getTimeStartRun()) + "seg)",
							"Duración de la corrida en segundos", JOptionPane.QUESTION_MESSAGE);
					if (duracionStr != null) {
						try {
							int duracion = Integer.parseInt(duracionStr.trim());
							if (duracion <= 0) {
								JOptionPane.showMessageDialog(null, "Duración de la corrida muy corta", "Duración de la corrida", JOptionPane.ERROR_MESSAGE);
							} else if (duracion > mov.duration() - app.getCorrida().getTimeStartRun()) {
								JOptionPane.showMessageDialog(null,
										"Duración corrida muy larga\nDebería ser menor a " + (int) (mov.duration() - app.getCorrida().getTimeStartRun()) + " seg",
										"Duración de la corrida", JOptionPane.ERROR_MESSAGE);
							} else {
								app.getCorrida().setTimeRunDuration(duracion);
								repetirPregunta = false;
							}
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, "La duración de la corrida\ntiene que ser un número.",
									"Duración de la corrida", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						// Se canceló
						repetirPregunta = false;
						reset();
					}
				} while (repetirPregunta);
				break;
			}
			if (app.getCorrida().getTimeStartRun() > -1 && app.getCorrida().getTimeRunDuration() > 0) {
				// Ya se obtubo la imagen de fondo, cuando empieza la corrida y cuánto dura el mismo.
				imagenesDeFondo = null;// Se libera memoria
				return new PantallaCapturaMascara(app, this);
			}
		}

		return this;
	}

	@Override
	public void keyPressed() {
		if (app.key == '+') {
			lastImagenesDeFondoACargarIndex = 0;
			esperandoQueCargueCuadro = false;
			segundosBase += 10;
			if (segundosBase > mov.duration()) {
				segundosBase = 0;
			}
			app.keyPressed = false;
		} else if (app.key == '-') {
			lastImagenesDeFondoACargarIndex = 0;
			esperandoQueCargueCuadro = false;
			segundosBase -= 10;
			if (segundosBase < 0) {
				segundosBase = 0;
			}
			app.keyPressed = false;
		}
	}

	@Override
	public void keyReleased() {
	}

	@Override
	public void mousePressed() {
		if (0 <= cuadroMouse && cuadroMouse < lastImagenesDeFondoACargarIndex) {
			if (app.getCorrida().getImagenDeFondo() == null) {
				// Se selecciona la imagen indicada con el mouse y se la va a usar como la
				// imagen de fondo para detectar movimiento
				app.getCorrida().setImagenDeFondo(imagenesDeFondo[cuadroMouse]);
				app.getCorrida().setTimeStartBackground((cuadroMouse > 0 || segundosBase > 0) ? segundosBase + cuadroMouse : 0.5f);
				mov.play();
				lastImagenesDeFondoACargarIndex = 0;
				esperandoQueCargueCuadro = false;
				segundosBase += 10;
				if (segundosBase > mov.duration()) {
					segundosBase = 0;
				}
			} else if (app.getCorrida().getTimeStartRun() == -1) {
				// Ahora se elige el momento del video en el que comienza la corrida
				app.getCorrida().setTimeStartRun(segundosBase + cuadroMouse); // Cuando comienza la corrida
				imagenComienzoCorrida = imagenesDeFondo[cuadroMouse];
				timeSetFondoSeleccionado = app.millis() + 500;
			}
		}
		app.mousePressed = false;
	}

	/**
	 * Vuelve a foja cero toda esta pantalla y vuelve a pedir todo otra vez
	 */
	public void reset() {
		mov = app.getCorrida().getMov();
		imagenesDeFondo = new PImage[10];
		imagenComienzoCorrida = null;
		lastImagenesDeFondoACargarIndex = 0;
		segundosBase = 0;
		esperandoQueCargueCuadro = false;
		timeSetFondoSeleccionado = 0;
		cuadroMouse = 0;

		app.getCorrida().setImagenDeFondo(null);
		app.getCorrida().setTimeStartRun(-1);
	}

	private void cargarImagenesDeFondo() {
	  if (!esperandoQueCargueCuadro) {
	    esperandoQueCargueCuadro = true;
	    mov.play();
	    if (lastImagenesDeFondoACargarIndex > 0 || segundosBase > 0) {
	      mov.jump(segundosBase + lastImagenesDeFondoACargarIndex);
	    } else {
	      mov.jump(0.5f);//Para no ir exactamente al inicio del video
	    }
	    mov.read();
	  }

	  if (mov.available() && mov.width > 0 && mov.height > 0) {
	    mov.read();
	    imagenesDeFondo[lastImagenesDeFondoACargarIndex] = app.createImage(AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PConstants.RGB);
	    app.getImageFromMov(imagenesDeFondo[lastImagenesDeFondoACargarIndex]);

	    imagenesDeFondo[lastImagenesDeFondoACargarIndex].loadPixels();
	    boolean cargoBien = false;
	    for (int i = 0; i < AnalizadorVideo.WIDTH * app.displayHeight && !cargoBien; i++) {
	      cargoBien = app.brightness(imagenesDeFondo[lastImagenesDeFondoACargarIndex].pixels[i]) > 0.0f;
	    }
	    if (cargoBien) {
	      mov.pause();
	      lastImagenesDeFondoACargarIndex++;
	      esperandoQueCargueCuadro = false;
	    }
	  }
	}
}


