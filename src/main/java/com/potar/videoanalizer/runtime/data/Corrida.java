package com.potar.videoanalizer.runtime.data;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import com.potar.videoanalizer.utils.Pair;

import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

public class Corrida {
	private Movie mov;
	private boolean rotateMovie;
	private String nombreVideo;
	private TipoTerreno tipoTerreno = TipoTerreno.TERRENO_OPEN_FIELD;

	private float timeStartBackground = -1; // Cuando comenzar a tomar el fondo
	private float timeStartRun = -1; // Cuando comienza la corrida
	private float timeRunDuration = -1; // Cuando finaliza la corrida (opcional, si se deja en 0 termina cuando termina
										// el video)

	// Variables para la obtención de la posición del ratón
	private PImage imagenDeFondo = null;// Imagen con la que se comparará
	private PImage mascaraImg = null; // Imagen con pixeles azules que se ignorarán para el análisis de movimiento
	private boolean[] xyPairsToIgnore;// = new boolean[WIDTH*HEIGHT];//La mascara de pixeles a ignorar
	private Figura figura;
	private PGraphics mascaraExteriorCanvas;

	private Recorrido recorrido = new Recorrido();
	private Estadisticas estadisticas = new Estadisticas();

    //Para el manejo del ignorado de momentos en el video
	private TreeMap<Integer, Boolean> mascaraTemporal = new TreeMap<Integer, Boolean>();
	private boolean ignorando = false; 
	private float tiempoIgnorando = 0; 
	private float tiempoIgnorandoLast = 0; 
	private int cuandoVolverAConsultarMascaraTemporal = -1;
	private PImage imagenActualVideo;

	
	public Corrida() {
	}

	public Movie getMov() {
		return mov;
	}

	public void setMov(Movie mov) {
		this.mov = mov;
	}
	
	public String getNombreVideo() {
		return nombreVideo;
	}
	
	public void setNombreVideo(String nombreVideo) {
		this.nombreVideo = nombreVideo;
	}

	public boolean isRotateMovie() {
		return rotateMovie;
	}

	public void setRotateMovie(boolean rotateMovie) {
		this.rotateMovie = rotateMovie;
	}

	public TipoTerreno getTipoTerreno() {
		return tipoTerreno;
	}

	public void setTipoTerreno(TipoTerreno tipoTerreno) {
		this.tipoTerreno = tipoTerreno;
	}
	public boolean isTerrenoOpenField() {
		return this.tipoTerreno == TipoTerreno.TERRENO_OPEN_FIELD;
	}
	public boolean isTerrenoPlusMAze() {
		return this.tipoTerreno == TipoTerreno.TERRENO_PLUS_MAZE;
	}
	public boolean isTerrenoYMaze() {
		return this.tipoTerreno == TipoTerreno.TERRENO_Y_MAZE;
	}

	public float getTimeStartBackground() {
		return timeStartBackground;
	}

	public void setTimeStartBackground(float timeStartBackground) {
		this.timeStartBackground = timeStartBackground;
	}

	public float getTimeStartRun() {
		return timeStartRun;
	}

	public void setTimeStartRun(float timeStartRun) {
		this.timeStartRun = timeStartRun;
	}

	public float getTimeRunDuration() {
		return timeRunDuration;
	}
	
	/**
	 * Devuelve cuánto tiempo va de análisis de la corrida
	 * @return
	 */
	public float getTiempoCorrida() {
		return getMov().time()-getTimeStartRun()-getTiempoIgnorando();
	}


	public void setTimeRunDuration(float timeRunDuration) {
		this.timeRunDuration = timeRunDuration;
	}

	public PImage getImagenDeFondo() {
		return imagenDeFondo;
	}

	public void setImagenDeFondo(PImage imagenDeFondo) {
		this.imagenDeFondo = imagenDeFondo;
	}

	public PImage getMascaraImg() {
		return mascaraImg;
	}

	public void setMascaraImg(PImage mascaraImg) {
		this.mascaraImg = mascaraImg;
	}

	public boolean[] getXyPairsToIgnore() {
		return xyPairsToIgnore;
	}

	public void setXyPairsToIgnore(boolean[] xyPairsToIgnore) {
		this.xyPairsToIgnore = xyPairsToIgnore;
	}
	public Figura getFigura() {
		return figura;
	}
	public void setFigura(Figura figura) {
		this.figura = figura;
	}
	
	public PGraphics getMascaraExteriorCanvas() {
		return mascaraExteriorCanvas;
	}
	
	public void setMascaraExteriorCanvas(PGraphics mascaraExteriorCanvas) {
		this.mascaraExteriorCanvas = mascaraExteriorCanvas;
	}

	public Recorrido getRecorrido() {
		return recorrido;
	}

	public Estadisticas getEstadisticas() {
		return estadisticas;
	}

	public void resetMovie() {
		recorrido.reset();
		estadisticas.reset();
		setCuandoVolverAConsultarMascaraTemporal(0);
		setIgnorando(false);
		setTiempoIgnorando(0);
		setTiempoIgnorandoLast(0);
		mov.play();
		mov.jump(getTimeStartRun());
	}

	public boolean isIgnorando() {
		return ignorando;
	}

	public void setIgnorando(boolean ignorando) {
		this.ignorando = ignorando;
	}

	public float getTiempoIgnorando() {
		return tiempoIgnorando;
	}

	public void setTiempoIgnorando(float tiempoIgnorando) {
		this.tiempoIgnorando = tiempoIgnorando;
	}

	public float getTiempoIgnorandoLast() {
		return tiempoIgnorandoLast;
	}

	public void setTiempoIgnorandoLast(float tiempoIgnorandoLast) {
		this.tiempoIgnorandoLast = tiempoIgnorandoLast;
	}

	public int getCuandoVolverAConsultarMascaraTemporal() {
		return cuandoVolverAConsultarMascaraTemporal;
	}

	public void setCuandoVolverAConsultarMascaraTemporal(int cuandoVolverAConsultarMascaraTemporal) {
		this.cuandoVolverAConsultarMascaraTemporal = cuandoVolverAConsultarMascaraTemporal;
	}

	public boolean calcIgnorar(float movTime) {
		boolean ignorar;
		
		//Se debe volver a consultar si hay que ignorar o todavía no?
		if (-1 < getCuandoVolverAConsultarMascaraTemporal() && getCuandoVolverAConsultarMascaraTemporal() <= movTime) {
			//Sí, se fija si se debe ignorar
			Pair<Integer, Boolean> mustIgnore = getIgnoreInfo((int)movTime);
			if (mustIgnore != null) {
				//Se setea cuando volver a consultar
				setCuandoVolverAConsultarMascaraTemporal(mustIgnore.getKey());
				ignorar = mustIgnore.getValue();
			} else {
				//Ya no se debe ignorar y no volver a consultar
				setCuandoVolverAConsultarMascaraTemporal(-1);
				ignorar = false;
			}
		} else {
			ignorar = isIgnorando();
		}

		//Se comienza a ignorar a partir de ahora?
		if (ignorar && !isIgnorando()) {
			//Entonces se deja todo listo para cuando se termine de ignorar
			recorrido.softInit(movTime);
			estadisticas.softInit(movTime);
		}

		if (isIgnorando()) {
			tiempoIgnorando += movTime - tiempoIgnorandoLast;
		}
		tiempoIgnorandoLast = movTime;
		
		return ignorar;
	}
	
	/*
		Devuelve hasta que tiempo se debe ignorar o hasta cuando se debe preguntar nuevamente. O null, que no hay mascara temporal y no se debe llamar más.
		Ejemplos:
		-Si devuelve null, no hay mascara temporal y se debe analizar todo el video 
		-Si devuelve Pair<3, true> es que hasta 3 inclusive se debe ignorar el video, y en el tiempo mayor a 3 se debe preguntar nuevamente
		-Si devuelve Pair<10, false> es que hasta 10 inlcusive se puede trabajar normalmente, y en el tiempo mayor a 10 se debe preguntar nuevamente
		-Si devuelve Pair<-1, true> se debe ignorar hasta el final del video
		-Si devuelve Pair<-1, false> se debe analizar hasta el final del video
	 */
	private Pair<Integer, Boolean> getIgnoreInfo(int tiempo) {
		if (mascaraTemporal.isEmpty()) {
			return null;
		}
		Boolean exactValue = mascaraTemporal.get(tiempo);
		Map.Entry<Integer, Boolean> entryAnterior = mascaraTemporal.lowerEntry(tiempo);
		Map.Entry<Integer, Boolean> entryPosterior = mascaraTemporal.higherEntry(tiempo);
		Integer resultTiempo;
		Boolean resultIgnore;

		if (exactValue != null) {
			resultIgnore = exactValue;
			if (entryPosterior == null) {
				resultTiempo = -1;
			} else {
				resultTiempo = entryPosterior.getKey();
			}
		} else if (entryAnterior == null) {
			resultIgnore = Boolean.FALSE;
			resultTiempo = entryPosterior.getKey();
		} else {
			resultIgnore = entryAnterior.getValue();
			if (entryPosterior != null) {
				resultTiempo = entryPosterior.getKey();
			} else {
				resultTiempo = -1;
			}
		}

		return new Pair<>(resultTiempo, resultIgnore);
	}

	
	public boolean mustIgnore(int tiempo) {
		Pair<Integer, Boolean> ignore = getIgnoreInfo(tiempo);
		if (ignore == null) {
			return false;
		}
		return ignore.getValue();
	}

	public void printMascaraTemporal(PrintWriter output) {
		if (mascaraTemporal.size() > 0) {
			output.println("Mascara Temporal");
			output.println("time\ttrue/false");
			for (Map.Entry<Integer, Boolean> entry : mascaraTemporal.entrySet()) {
				Integer time = entry.getKey();
				Boolean ignore = entry.getValue();
				output.println(String.format(Parametros.locale, "%d\t%b", time, ignore));
			}
		} else {
			output.println("No hay máscara temporal");
		}
	}

	public Boolean getIgnorarRaw(Integer segundo) {
		return mascaraTemporal.get(segundo);
	}

	public void setIgnorarRaw(Integer segundo, boolean ignorar) {
		mascaraTemporal.put(segundo, ignorar);
	}

	public void removeIgnorarRaw(Integer segundo) {
		mascaraTemporal.remove(segundo);		
	}

	public void setImagenActualVideo(PImage imagenActualVideo) {
		this.imagenActualVideo = imagenActualVideo;
	}
	
	public PImage getImagenActualVideo() {
		return imagenActualVideo;
	}

}