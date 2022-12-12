package com.potar.videoanalizer.gui.component;

import java.io.File;

import javax.swing.JOptionPane;

import com.potar.AnalizadorVideo;

import processing.core.PApplet;
import processing.video.Movie;

public class BotonAbrirVideo extends Component implements Boton {
	private static final int width = 100;
	private static final int height = 70;

	private boolean overAbrirVideo;

	//Data
	private boolean dialogoAbrirMovieLanzado = false;
	private long archivoAbiertoTime = 0;
	private String nombreVideo = null;
	private Movie mov = null;
	private boolean rotateMovie;
	private boolean movieReady;

	public BotonAbrirVideo(AnalizadorVideo app, float posX, float posY) {
		super(app, posX - width / 2f, posY - height / 2f);
	}
	
	public boolean isMovieReady() {
		return movieReady;
	}
	
	public Movie getMovie() {
		if (!movieReady) {
			throw new RuntimeException("El video no está listo");
		}
		return mov;
	}
	
	public boolean isRotateMovie() {
		if (!movieReady) {
			throw new RuntimeException("El video no está listo");
		}
		return rotateMovie;
	}
	
	public String getNombreVideo() {
		if (!movieReady) {
			throw new RuntimeException("El video no está listo");
		}
		return nombreVideo;
	}


	@Override
	public void draw() {
		overAbrirVideo = over(app.mouseX, app.mouseY);
		app.pushStyle();

		// El rectángulo fondo del botón
		app.fill(overAbrirVideo ? 200 : 255);
		app.stroke(0);
		app.strokeWeight(1);
		app.rect(posX, posY, width, height, 9);

		// El triángulo
		app.fill(0);
		app.strokeWeight(9);
		app.strokeJoin(PApplet.ROUND);
		app.beginShape();
		app.vertex(posX + width / 2f + 5 - 15, posY + height / 2f - 15);
		app.vertex(posX + width / 2f + 5 + 15, posY + height / 2f);
		app.vertex(posX + width / 2f + 5 - 15, posY + height / 2f + 15);
		app.endShape(PApplet.CLOSE);

		if (mov != null && mov.available() && mov.width > 0 && mov.height > 0) {
			mov.read();
			if (mov.duration() < 10) {
				JOptionPane.showMessageDialog(null, "El video dura muy poco.\nPor favor elija otro archivo.", "Video muy corto", JOptionPane.ERROR_MESSAGE);
				dialogoAbrirMovieLanzado = false;
				archivoAbiertoTime = -1;
				mov = null;
				return;
			}
			mov.volume(0);
			mov.pause();
			if (AnalizadorVideo.WIDTH == mov.width && AnalizadorVideo.HEIGHT == mov.height) {
				rotateMovie = false;
				// Cargó el video correctamente
				movieReady = true;
			} else {
				if (mov.width < mov.height) {
					rotateMovie = true;
				}

				// Cargó el video correctamente
				movieReady = true;
			}
		}

		if (!movieReady && archivoAbiertoTime > 0 && archivoAbiertoTime + 5000 < app.millis()) {
			// Pasó mucho tiempo y aún no hay imagen del video
			// Entonces no se pudo abrir correctamente el archivo
			Object[] possibleValues = { "Esperar más tiempo", "Seleccionar otro video", "Salir de la aplicación" };
			int selectedValue = JOptionPane.showOptionDialog(null,
					"El archivo con el video no se pudo leer o está tardando demasiado.", "Leyendo archivo de video",
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, possibleValues, possibleValues[0]);
			switch (selectedValue) {
			case 0: // Esperar más tiempo
				archivoAbiertoTime = app.millis();
				break;

			case 1: // Seleccionar otro archivo
				dialogoAbrirMovieLanzado = false;
				archivoAbiertoTime = -1;
				mov = null;
				lanzarDialogoAperturaVideo();
				return;
			case 2:
				dialogoAbrirMovieLanzado = false;
				archivoAbiertoTime = -1;
				mov = null;
				app.exit();
				break;
			}
		}

		app.popStyle();
	}

	public boolean over(float x, float y) {
		return (posX <= x && x <= posX + width && posY <= y && y <= posY + height);
	}

	public void mousePressed() {
		if (overAbrirVideo && !dialogoAbrirMovieLanzado && mov == null) {
			lanzarDialogoAperturaVideo();
		}
	}

	private void lanzarDialogoAperturaVideo() {
		app.selectInput("Seleccione un archivo de video para procesar Open Field:", "fileSelected", null, this);
		dialogoAbrirMovieLanzado = true;
	}
	
	public void fileSelected(File selection) {
		dialogoAbrirMovieLanzado = false;
		if (selection != null) {
			try {
				archivoAbiertoTime = app.millis();
				mov = new Movie(app, selection.getAbsolutePath());
				nombreVideo = selection.getName();
				mov.play();
				mov.read();
			} catch (Exception ex) {
			}
		}
	}

}
