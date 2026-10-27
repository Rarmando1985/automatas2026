package com.umg.automatas.modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parsea un archivo .txt con la estructura formal del autómata
 * y calcula posiciones geométricas para dibujarlo automáticamente en el lienzo.
 */
public class CargadorAutomataTXT {

    public static Automata cargarDesdeArchivoTXT(File archivo) throws IOException, IllegalArgumentException {
        Automata automata = new Automata();

        Set<String> nombresEstados = new LinkedHashSet<>();
        String estadoInicial = null;
        Set<String> estadosAceptacion = new HashSet<>();
        List<String[]> transicionesTexto = new ArrayList<>();

        boolean leyendoTransiciones = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();

                // Ignorar líneas vacías o comentarios (#)
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                if (linea.toUpperCase().startsWith("Q =") || linea.toUpperCase().startsWith("Q=")) {
                    leyendoTransiciones = false;
                    String[] partes = linea.split("=")[1].split(",");
                    for (String p : partes) {
                        if (!p.trim().isEmpty()) nombresEstados.add(p.trim());
                    }
                } else if (linea.toUpperCase().startsWith("INICIAL =") || linea.toUpperCase().startsWith("INICIAL=")) {
                    leyendoTransiciones = false;
                    estadoInicial = linea.split("=")[1].trim();
                } else if (linea.toUpperCase().startsWith("ACEPTACION =") || linea.toUpperCase().startsWith("ACEPTACION=")) {
                    leyendoTransiciones = false;
                    String[] partes = linea.split("=")[1].split(",");
                    for (String p : partes) {
                        if (!p.trim().isEmpty()) estadosAceptacion.add(p.trim());
                    }
                } else if (linea.toUpperCase().startsWith("TRANSICIONES =") || linea.toUpperCase().startsWith("TRANSICIONES=")) {
                    leyendoTransiciones = true;
                } else if (leyendoTransiciones) {
                    String[] partes = linea.split(",");
                    if (partes.length == 3) {
                        transicionesTexto.add(new String[]{partes[0].trim(), partes[1].trim(), partes[2].trim()});
                    }
                }
            }
        }

        if (nombresEstados.isEmpty()) {
            throw new IllegalArgumentException("El archivo no contiene la definición de estados (Q).");
        }

        // --- Algoritmo de Auto-Layout (Distribución geométrica circular) ---
        double centroX = 380;
        double centroY = 250;
        double radio = Math.max(120, nombresEstados.size() * 35);

        Map<String, Estado> mapaEstados = new HashMap<>();
        List<String> listaNombres = new ArrayList<>(nombresEstados);
        int total = listaNombres.size();

        for (int i = 0; i < total; i++) {
            String nombre = listaNombres.get(i);
            double angulo = 2 * Math.PI * i / total;
            double posX = centroX + radio * Math.cos(angulo);
            double posY = centroY + radio * Math.sin(angulo);

            Estado estado = new Estado(nombre, posX, posY);

            if (nombre.equals(estadoInicial)) {
                estado.setEsInicial(true);
            }
            if (estadosAceptacion.contains(nombre)) {
                estado.setEsAceptacion(true);
            }

            mapaEstados.put(nombre, estado);
            automata.agregarEstado(estado);
        }

        // Si no se especificó estado inicial de forma explícita, tomamos el primero
        if (automata.getEstadoInicial() == null && !automata.getEstados().isEmpty()) {
            automata.getEstados().get(0).setEsInicial(true);
        }

        // Agregar las transiciones
        for (String[] t : transicionesTexto) {
            Estado origen = mapaEstados.get(t[0]);
            Estado destino = mapaEstados.get(t[2]);
            String simbolo = t[1].isEmpty() ? "ε" : t[1];

            if (origen != null && destino != null) {
                automata.agregarTransicion(origen, destino, simbolo);
            }
        }

        return automata;
    }
}