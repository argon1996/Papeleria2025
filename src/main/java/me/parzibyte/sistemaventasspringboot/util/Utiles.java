// src/main/java/me/parzibyte/sistemaventasspringboot/util/Utiles.java
package me.parzibyte.sistemaventasspringboot.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utiles {
    public static String obtenerFechaYHoraActual() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
