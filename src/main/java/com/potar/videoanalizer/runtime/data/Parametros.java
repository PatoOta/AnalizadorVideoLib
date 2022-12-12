package com.potar.videoanalizer.runtime.data;

import java.util.Locale;

/*
1.4.1
Usando la tecla Backspace (tecla Retroceso o "borrar para atrás") permite volver la pantalla anterior. De esta forma está se puede elegir otro video, elegir otro fondo, otra duración, etc.
Ahora si se ignora una parte del video, ese tiempo no se suma al tiempo de la corrida
Al apretar escape, preguntar si está seguro de querer salir

1.4.0
Se agregó la posibilidad de marcar distintos momentos del video para ignorar. Por ejemplo cuando se cae el ratón y hay que intervenir, se "enmascara" ese tiempo para que no se lo tome en cuenta en el análisis
Se arma automáticamente la máscara y ya no deja estar o no conforme con la misma. Después se edita manualmente.
Se agregó el nombre del video a la pantalla mientras se hace el análisis del mismo.
Se puede en la edición de la grilla, engrosar las líneas para indicar que se enmascaren.

1.3.1
En esta versión se agregó que las estadísticas (tiempo en zonas, cantidad de entradas, etc) se acumulan por tiempo. Esto permitirá volver el tiempo atrás y retomar desde un momento dado del video.
También se agregó la posibilidad de ver em grande la imagen de fondo, la máscara y la imagen de detección
Se arregló un pequeño bug cuando se ponía que uno no estaba de acuerdo con la mascara generada automáticamente
*/

public class Parametros {
  public static final String appVersion = "1.4.1";
  public static final float limite = 28;              //El límite para tomar un punto que hubo movimiento o no
  public static final int WIDTH = 640;      //Ancho del video en pixeles
  public static final int HEIGHT = 360;     //Altura del video en pixeles
  public static final Locale locale = new Locale("es", "AR");
}