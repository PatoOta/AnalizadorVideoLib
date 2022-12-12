package com.potar.videoanalizer.runtime.data;

import java.util.Map;
import java.util.TreeMap;

public class Estadisticas {

	// Para todos los terrenos
	private boolean estadisticasInicializadas = false;
	private RegionData posicionAnt;
	private float posicionAnteriorMomento;

	// Propias de open field
	private TreeMap<Float, Integer> cantidadLineasCruzadasPorTiempo;
	private int ultimaCantidadLineasCruzadas;
	private EstadisticaZona zonaPeriferia;
	private EstadisticaZona zonaEsquina;

	// Propias de PLUS-MAZE y de Y-MAZE
	private EstadisticaZona zona0;
	private EstadisticaZona zona1;
	private EstadisticaZona zona2;
	private EstadisticaZona zona3;
	private EstadisticaZona zona4;

	public void reset() {
		posicionAnt = null;
		posicionAnteriorMomento = -1;

		// Propias de open field
		cantidadLineasCruzadasPorTiempo = new TreeMap<Float, Integer>();
		cantidadLineasCruzadasPorTiempo.put(Float.valueOf(0), Integer.valueOf(0));
		ultimaCantidadLineasCruzadas = 0;
		zonaPeriferia = new EstadisticaZona(true, false);
		zonaEsquina = new EstadisticaZona(false, true);

		// Propias de PLUS-MAZE y de Y-MAZE
		zona0 = new EstadisticaZona(0);
		zona1 = new EstadisticaZona(1);
		zona2 = new EstadisticaZona(2);
		zona3 = new EstadisticaZona(3);
		zona4 = new EstadisticaZona(4);
	}

	@SuppressWarnings("boxing")
	public void jumpTo(float elMomento) {
		this.posicionAnt = null;

		// Se trunca el contador de lineas cruzadas
		cantidadLineasCruzadasPorTiempo.tailMap(elMomento).clear();
		Map.Entry<Float, Integer> lastEntry = cantidadLineasCruzadasPorTiempo.lastEntry();
		if (lastEntry != null) {
			ultimaCantidadLineasCruzadas = lastEntry.getValue();
		} else {
			cantidadLineasCruzadasPorTiempo.put(Float.valueOf(0), 0);
			ultimaCantidadLineasCruzadas = 0;
		}

		// Se truncan el resto de las estadísticas
		zonaPeriferia.jumpTo(elMomento);
		zonaEsquina.jumpTo(elMomento);

		// Propias de PLUS-MAZE y de Y-MAZE
		zona0.jumpTo(elMomento);
		zona1.jumpTo(elMomento);
		zona2.jumpTo(elMomento);
		zona3.jumpTo(elMomento);
		zona4.jumpTo(elMomento);
	}

	/*
	 * Este método se lo utiliza para el momento en el que se comienza el
	 * enmascarado temporal. Se debe dejar la estadística lista como si empezara
	 * desde ahí
	 */
	public void softInit(float elMomento) {
		posicionAnt = null;

		zonaPeriferia.softInit(elMomento);
		zonaEsquina.softInit(elMomento);

		// Propias de PLUS-MAZE y de Y-MAZE
		zona0.softInit(elMomento);
		zona1.softInit(elMomento);
		zona2.softInit(elMomento);
		zona3.softInit(elMomento);
		zona4.softInit(elMomento);
	}

	@SuppressWarnings("boxing")
	public void add(float elMomento, RegionData laPosicion) {
		if (!estadisticasInicializadas) {
			reset();
			estadisticasInicializadas = true;
		}

		zonaPeriferia.actualizar(elMomento, laPosicion);
		zonaEsquina.actualizar(elMomento, laPosicion);
		zona0.actualizar(elMomento, laPosicion);
		zona1.actualizar(elMomento, laPosicion);
		zona2.actualizar(elMomento, laPosicion);
		zona3.actualizar(elMomento, laPosicion);
		zona4.actualizar(elMomento, laPosicion);

		// Se cuenta las veces que cruza una línea (cambia de zona)
		if (posicionAnt == null || (posicionAnt.getIdRegion() != laPosicion.getIdRegion()
				&& (elMomento - posicionAnteriorMomento > 0.3))) {
			if (posicionAnt != null) {
				ultimaCantidadLineasCruzadas++;
				cantidadLineasCruzadasPorTiempo.put(elMomento, ultimaCantidadLineasCruzadas);

			}
			posicionAnt = laPosicion;
			posicionAnteriorMomento = elMomento;
		}
	}

	private class EstadisticaZona {
		private int id;
		private boolean periferia = false;
		private boolean esquina = false;

		// Metrica
		TreeMap<Float, Metrica> metricasPorTiempo = new TreeMap<Float, Metrica>();
		Metrica ultimaMetrica = new Metrica();
		{
			metricasPorTiempo.put(Float.valueOf(0), ultimaMetrica);
		}

		EstadisticaZona(int id) {
			this.id = id;
		}

		EstadisticaZona(boolean periferia, boolean esquina) {
			this.id = -1;
			this.periferia = periferia;
			this.esquina = esquina;

			if (esquina && periferia) {
				throw new RuntimeException("O es periferia o es esquina. Se cuentan por separado");
			}
		}

		@SuppressWarnings("boxing")
		void jumpTo(float elMomento) {
			metricasPorTiempo.tailMap(elMomento).clear();
			Map.Entry<Float, Metrica> lastEntry = metricasPorTiempo.lastEntry();
			if (lastEntry != null) {
				this.ultimaMetrica = lastEntry.getValue();
			} else {
				ultimaMetrica = new Metrica();
				metricasPorTiempo.put(Float.valueOf(0), ultimaMetrica);
			}
		}

		void softInit(float elMomento) {
			RegionData laPosicion = null;
			actualizar(elMomento, laPosicion);
		}

		@SuppressWarnings("boxing")
		void actualizar(float elMomento, RegionData laPosicion) {
			Metrica nuevaUltimaMetrica = null;
			// Se mide el tiempo en la zona
			// Estaba antes en esta zona?
			if (ultimaMetrica.estabaEnZona()) {
				// Sí.
				// Entonces se suma el tiempo que estuvo hasta ahora
				nuevaUltimaMetrica = ultimaMetrica.sumarTiempoEnZona(elMomento);
			}

			// Entró a la zona?
			if (laPosicion != null && (laPosicion.getIdRegion() == id || (periferia && laPosicion.isPeriferia())
					|| (esquina && laPosicion.isEsquina()))) {
				// Sí, se toma el momento en el que entra a la zona (o se actualiza)
				// Pero antes se cuenta la entrada (si es que lo fue)
				if (!ultimaMetrica.estabaEnZona()) {
					nuevaUltimaMetrica = ultimaMetrica.nuevaEntradaEnZona();
				}
				if (nuevaUltimaMetrica == null) {
					// No debería suceder nunca
					// nuevaUltimaMetrica = new Metrica(ultimaMetrica);
					throw new RuntimeException("Nunca debería entrar acá");
				}
				nuevaUltimaMetrica.entradaAZonaTime = elMomento;
			} else {
				// No, salió de la zona (o no estaba de antes).
				nuevaUltimaMetrica = ultimaMetrica.salioDeLaZona();
			}

			metricasPorTiempo.put(elMomento, nuevaUltimaMetrica);
			ultimaMetrica = nuevaUltimaMetrica;
		}

	}

	private class Metrica {
		float entradaAZonaTime = -1;
		float tiempoEnZona = 0;
		int cantEntradas = 0;

		Metrica() {
		}

		Metrica(Metrica original) {
			this.entradaAZonaTime = original.entradaAZonaTime;
			this.cantEntradas = original.cantEntradas;
			this.tiempoEnZona = original.tiempoEnZona;
		}

		boolean estabaEnZona() {
			return entradaAZonaTime > -1;
		}

		Metrica sumarTiempoEnZona(float elMomento) {
			Metrica result = new Metrica(this);
			if (result.entradaAZonaTime > -1) {
				// Sí.
				// Entonces se suma el tiempo que estuvo hasta ahora
				result.tiempoEnZona = result.tiempoEnZona + elMomento - result.entradaAZonaTime;
			}
			return result;
		}

		Metrica nuevaEntradaEnZona() {
			Metrica result = new Metrica(this);
			result.cantEntradas++;
			return result;
		}

		Metrica salioDeLaZona() {
			Metrica result = new Metrica(this);
			result.entradaAZonaTime = -1;
			return result;
		}
	}

	public int getCantidadLineasCruzadas() {
		return ultimaCantidadLineasCruzadas;
	}

	public float getTiempoEnZonaPeriferia() {
		return zonaPeriferia.ultimaMetrica.tiempoEnZona;
	}

	public float getTiempoEnZonaEsquina() {
		return zonaEsquina.ultimaMetrica.tiempoEnZona;
	}

	public float getTiempoEnZona0() {
		return zona0.ultimaMetrica.tiempoEnZona;
	}
	public float getTiempoEnZona1() {
		return zona1.ultimaMetrica.tiempoEnZona;
	}
	public float getTiempoEnZona2() {
		return zona2.ultimaMetrica.tiempoEnZona;
	}
	public float getTiempoEnZona3() {
		return zona3.ultimaMetrica.tiempoEnZona;
	}
	public float getTiempoEnZona4() {
		return zona4.ultimaMetrica.tiempoEnZona;
	}
	
	public int getCantEntradasEnZona0() {
		return zona0.ultimaMetrica.cantEntradas;
	}
	public int getCantEntradasEnZona1() {
		return zona1.ultimaMetrica.cantEntradas;
	}
	public int getCantEntradasEnZona2() {
		return zona2.ultimaMetrica.cantEntradas;
	}
	public int getCantEntradasEnZona3() {
		return zona3.ultimaMetrica.cantEntradas;
	}
	public int getCantEntradasEnZona4() {
		return zona4.ultimaMetrica.cantEntradas;
	}

}