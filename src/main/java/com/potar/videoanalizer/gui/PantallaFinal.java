package com.potar.videoanalizer.gui;

import java.io.PrintWriter;
import java.util.Date;

import com.potar.AnalizadorVideo;
import com.potar.videoanalizer.gui.component.BotonNuevoAnalisis;
import com.potar.videoanalizer.gui.component.BotonSalida;
import com.potar.videoanalizer.gui.component.FreeMice;
import com.potar.videoanalizer.runtime.data.Corrida;
import com.potar.videoanalizer.runtime.data.Estadisticas;
import com.potar.videoanalizer.runtime.data.Recorrido;
import com.potar.videoanalizer.runtime.data.TipoTerreno;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

public class PantallaFinal extends Pantalla {
	private final float botonNuevoAnalisisX = 560;
	private final float botonNuevoAnalisisY = 240;

	private BotonSalida botonSalida;
	private BotonNuevoAnalisis botonNuevoAnalisis; 
	private FreeMice freeMice;

	private boolean informacionGuardada = false;
	private PGraphics pgImagenFinal = null;

	private Movie mov;
	private Corrida corrida;
	private Estadisticas estadisticas;
	private Recorrido recorrido;

	public PantallaFinal(AnalizadorVideo app, Pantalla pantallaAnterior) {
		super(app, pantallaAnterior);
		reset();
	}

	@Override
	public Pantalla draw() {
		//Guardar información obtenida en archivo de texto cvs
		//Mostrar los datos obtenidos
		//Mostrar Recorrido
		//Informar archivos guardados
		//Mostrar Botón de salir
		//Mostrar Botón Nuevo Análisis?

		if (!informacionGuardada && mov != null) {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm");
			Date ahora = new Date();
			String dateStr = sdf.format(ahora);
			String dataInfo = "Video: \t" + mov.filename + "\t Ubicación y Nombre\n" +
					"Toma del fondo: \t" + (int)corrida.getTimeStartBackground() + "\tseg\n" +
					"Comienzo de la corrida: \t" + (int)corrida.getTimeStartRun() + "\tseg\n" +
					"Duración de la corrida: \t" + (int)corrida.getTiempoCorrida() + "\tseg\n" +
					"Tiempo ignorado: \t" + (int)corrida.getTiempoIgnorando() + "\tseg\n\n";

			if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_OPEN_FIELD) {
				dataInfo +=     "Tipo Terreno: \tOPEN FIELD\tCampo abierto\n"+
						"Líneas cruzadas: \t" + estadisticas.getCantidadLineasCruzadas() + "\tcantidad\n"+
						"Tiempo en Periferia: \t" + (int)estadisticas.getTiempoEnZonaPeriferia() + "\tseg\n"+
						"Tiempo en Esquinas: \t" + (int)estadisticas.getTiempoEnZonaEsquina() + "\tseg";
			} else if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_PLUS_MAZE) {
				dataInfo +=     "Tipo Terreno: \tPlus-Maze\tLaberinto en Cruz (Zona 1 es la central)\n" +
						"Tiempo en Zona 0: \t" + (int)estadisticas.getTiempoEnZona0() + "\tseg\n"+
						"Tiempo en Zona 1: \t" + (int)estadisticas.getTiempoEnZona1() + "\tseg\n"+
						"Tiempo en Zona 2: \t" + (int)estadisticas.getTiempoEnZona2() + "\tseg\n"+
						"Tiempo en Zona 3: \t" + (int)estadisticas.getTiempoEnZona3() + "\tseg\n"+
						"Tiempo en Zona 4: \t" + (int)estadisticas.getTiempoEnZona4() + "\tseg\n";                      
				dataInfo +=     "Zona 0: \t" + (int)estadisticas.getCantEntradasEnZona0() + "\tentradas\n"+
						"Zona 1: \t" + (int)estadisticas.getCantEntradasEnZona1() + "\tentradas\n"+
						"Zona 2: \t" + (int)estadisticas.getCantEntradasEnZona2() + "\tentradas\n"+
						"Zona 3: \t" + (int)estadisticas.getCantEntradasEnZona3() + "\tentradas\n"+
						"Zona 4: \t" + (int)estadisticas.getCantEntradasEnZona4() + "\tentradas";
			} else if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_Y_MAZE) {
				dataInfo +=     "Tipo Terreno: \tY-Maze\tLaberinto en Y (Zona 1 es la central)\n" +
						"Tiempo en Zona 0: \t" + (int)estadisticas.getTiempoEnZona0() + "\tseg\n"+
						"Tiempo en Zona 1: \t" + (int)estadisticas.getTiempoEnZona1() + "\tseg\n"+
						"Tiempo en Zona 2: \t" + (int)estadisticas.getTiempoEnZona2() + "\tseg\n"+
						"Tiempo en Zona 3: \t" + (int)estadisticas.getTiempoEnZona3() + "\tseg\n";
				dataInfo +=     "Zona 0: \t" + (int)estadisticas.getCantEntradasEnZona0() + "\tentradas\n"+
						"Zona 1: \t" + (int)estadisticas.getCantEntradasEnZona1() + "\tentradas\n"+
						"Zona 2: \t" + (int)estadisticas.getCantEntradasEnZona2() + "\tentradas\n"+
						"Zona 3: \t" + (int)estadisticas.getCantEntradasEnZona3() + "\tentradas";
			}
			mov.stop();
			PrintWriter output = app.createWriter(mov.filename+"_analisis_"+dateStr+".cvs");
			output.println("Resultados del análisis del video Open Field");

			output.println("");
			output.println(dataInfo);
			System.out.println(dataInfo);

			output.println("");
			recorrido.print(output);

			output.println("");
			corrida.printMascaraTemporal(output);

			output.println("");
			corrida.getFigura().print(output);


			output.flush(); // Writes the remaining data to the file
			output.close(); // Finishes the file

			PImage imagenActualVideo = corrida.getImagenActualVideo();
			if (imagenActualVideo != null) {
				app.image(imagenActualVideo, 0, 0);
				//Se dibuja el recorrido y la psoición actual
				recorrido.drawRecorrido(app.getGraphics());
				app.save(mov.filename+"_recorrido_"+dateStr+".jpg");
			}

			if (corrida.getMascaraImg() != null) {
				app.image(corrida.getMascaraImg(), 0, 0);
				app.save(mov.filename+"_mascara_"+dateStr+".jpg");
			}
			informacionGuardada = true;
		}

		app.background(127);
		app.text("Resultados del análisis de Open Field (información almacenada en la misma carpeta del video)", 20, 20);
		app.text("Video: " + (mov != null ? mov.filename : ""), 20, 40);
		app.text("Corrida duración: " + (int)corrida.getTiempoCorrida() + "seg   Tiempo ignorado: " + (int)corrida.getTiempoIgnorando() + "seg", 20, 60);
		if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_OPEN_FIELD) {
			app.text("Líneas cruzadas: " + estadisticas.getCantidadLineasCruzadas(), 20, 80);
			app.text("Tiempo en Periferia: " + (int)estadisticas.getTiempoEnZonaPeriferia() + "seg", 20, 100);
			app.text("Tiempo en Esquinas: " + (int)estadisticas.getTiempoEnZonaEsquina() + "seg", 20, 120);
		} else if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_PLUS_MAZE) {
			app.text("Zona 0: " + (int)estadisticas.getTiempoEnZona0() + "seg #" + estadisticas.getCantEntradasEnZona0(), 20, 80);
			app.text("Zona 1: " + (int)estadisticas.getTiempoEnZona1() + "seg #" + estadisticas.getCantEntradasEnZona1(), 20, 95);
			app.text("Zona 2: " + (int)estadisticas.getTiempoEnZona2() + "seg #" + estadisticas.getCantEntradasEnZona2(), 20, 110);
			app.text("Zona 3: " + (int)estadisticas.getTiempoEnZona3() + "seg #" + estadisticas.getCantEntradasEnZona3(), 20, 125);
			app.text("Zona 4: " + (int)estadisticas.getTiempoEnZona4() + "seg #" + estadisticas.getCantEntradasEnZona4(), 20, 140);
		} else if (corrida.getTipoTerreno() == TipoTerreno.TERRENO_Y_MAZE) {
			app.text("Zona 0: " + (int)estadisticas.getTiempoEnZona0() + "seg #" + estadisticas.getCantEntradasEnZona0(), 20, 80);
			app.text("Zona 1: " + (int)estadisticas.getTiempoEnZona1() + "seg #" + estadisticas.getCantEntradasEnZona1(), 20, 100);
			app.text("Zona 2: " + (int)estadisticas.getTiempoEnZona2() + "seg #" + estadisticas.getCantEntradasEnZona2(), 20, 120);
			app.text("Zona 3: " + (int)estadisticas.getTiempoEnZona3() + "seg #" + estadisticas.getCantEntradasEnZona3(), 20, 140);
		}

		boolean overBotonNuevoAnalisis = botonNuevoAnalisis.over(app.mouseX, app.mouseY);
		boolean overBotonSalida = botonSalida.over(app.mouseX, app.mouseY);

		botonSalida.draw();
		botonNuevoAnalisis.draw();
		freeMice.draw();


		if (overBotonSalida && app.mousePressed == true) {
			app.exit();
		}

		if (pgImagenFinal == null) {
			pgImagenFinal = app.createGraphics(AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT);
			pgImagenFinal.beginDraw();
			if (corrida.getImagenActualVideo() != null) {
				pgImagenFinal.image(corrida.getImagenActualVideo(), 0, 0);
			}
			if (corrida.getMascaraExteriorCanvas() != null) {
				corrida.getMascaraExteriorCanvas().loadPixels();
				pgImagenFinal.loadPixels();
				for (int i=0; i < AnalizadorVideo.WIDTH * AnalizadorVideo.HEIGHT; i++) {
					if (app.blue(corrida.getMascaraExteriorCanvas().pixels[i]) > 0) {
						pgImagenFinal.pixels[i] = app.getGraphics().backgroundColor;
					}
				}
				pgImagenFinal.updatePixels();
			}

			if (corrida.getMascaraImg() != null) {
				corrida.getMascaraImg().loadPixels();
				pgImagenFinal.loadPixels();
				for (int i=0; i < AnalizadorVideo.WIDTH * AnalizadorVideo.HEIGHT; i++) {
					if (i % AnalizadorVideo.WIDTH == 0 || i % AnalizadorVideo.WIDTH == (AnalizadorVideo.WIDTH-1) || i < AnalizadorVideo.WIDTH || i > AnalizadorVideo.WIDTH * (AnalizadorVideo.HEIGHT-1)) {
						pgImagenFinal.pixels[i] = 255;
					} else if (app.blue(corrida.getMascaraImg().pixels[i]) > 0) {
						pgImagenFinal.pixels[i] = app.getGraphics().backgroundColor;
					}
				}
				pgImagenFinal.updatePixels();
			}

			//Se dibuja el recorrido y la psoición actual
			recorrido.drawRecorrido(pgImagenFinal);
			pgImagenFinal.endDraw();
		}

		app.pushMatrix();
		app.translate(30, 150);
		app.scale(0.5f);
		app.image(pgImagenFinal, 0, 0);
		app.popMatrix();

		//Se deja todo listo para otro análisis nuevo
		if(overBotonNuevoAnalisis && app.mousePressed == true) {
			app.reset();
		}
		
		return null;
	}

	@Override
	public void reset() {
		informacionGuardada = false;
		pgImagenFinal = null;
		corrida = app.getCorrida();
		mov = corrida.getMov();
		estadisticas = corrida.getEstadisticas();
		recorrido = corrida.getRecorrido();
		botonNuevoAnalisis = new BotonNuevoAnalisis(app, botonNuevoAnalisisX, botonNuevoAnalisisY);
		botonSalida = new BotonSalida(app, AnalizadorVideo.WIDTH*7/8.0f, AnalizadorVideo.HEIGHT/12.0f);
		freeMice = new FreeMice(app, AnalizadorVideo.WIDTH/4f, AnalizadorVideo.HEIGHT-5, botonSalida, botonNuevoAnalisis);
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
