package com.umg.automatas.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AlgoritmoEliminacionEstados {

    private final Automata automataOriginal;
    private final Map<String, Map<String, String>> matrizTransiciones;
    private final List<Estado> estadosIntermedios;
    private Estado qStart;
    private Estado qAccept;

    public AlgoritmoEliminacionEstados(Automata automata) {
        this.automataOriginal = automata;
        this.matrizTransiciones = new HashMap<>();
        this.estadosIntermedios = new ArrayList<>();
    }

    public void normalizarGrafo() {
        matrizTransiciones.clear();
        estadosIntermedios.clear();

        qStart = new Estado("q_start", 80, 250);
        qStart.setEsInicial(true);

        qAccept = new Estado("q_accept", 750, 250);
        qAccept.setEsAceptacion(true);

        for (Estado e : automataOriginal.getEstados()) {
            estadosIntermedios.add(e);
        }

        for (Transicion t : automataOriginal.getTransiciones()) {
            agregarTransicionMatriz(t.getOrigen().getNombre(), t.getDestino().getNombre(), t.getSimbolo());
        }

        Estado inicialOriginal = automataOriginal.getEstadoInicial();
        if (inicialOriginal != null) {
            agregarTransicionMatriz(qStart.getNombre(), inicialOriginal.getNombre(), "ε");
        }

        for (Estado e : automataOriginal.getEstados()) {
            if (e.esAceptacion()) {
                agregarTransicionMatriz(e.getNombre(), qAccept.getNombre(), "ε");
            }
        }
    }

    private void agregarTransicionMatriz(String origen, String destino, String expresion) {
        matrizTransiciones.putIfAbsent(origen, new HashMap<>());
        Map<String, String> destinos = matrizTransiciones.get(origen);

        if (destinos.containsKey(destino)) {
            String expExistente = destinos.get(destino);
            destinos.put(destino, simplificarUnion(expExistente, expresion));
        } else {
            destinos.put(destino, expresion);
        }
    }

    public String eliminarEstadoPaso(Estado estadoAEliminar) {
        String qj = estadoAEliminar.getNombre();

        String r_jj = obtenerTransicion(qj, qj);

        List<String> predecesores = new ArrayList<>();
        List<String> sucesores = new ArrayList<>();

        for (String origen : obtenerTodosLosNodos()) {
            if (!origen.equals(qj) && contieneTransicion(origen, qj)) {
                predecesores.add(origen);
            }
        }

        for (String destino : obtenerTodosLosNodos()) {
            if (!destino.equals(qj) && contieneTransicion(qj, destino)) {
                sucesores.add(destino);
            }
        }

        for (String qi : predecesores) {
            String r_ij = obtenerTransicion(qi, qj);

            for (String qk : sucesores) {
                String r_jk = obtenerTransicion(qj, qk);
                String r_ik_actual = obtenerTransicion(qi, qk);

                String nuevaExpresion = construirExpresionReducida(r_ik_actual, r_ij, r_jj, r_jk);
                matrizTransiciones.get(qi).put(qk, nuevaExpresion);
            }
        }

        matrizTransiciones.remove(qj);
        for (Map<String, String> mapa : matrizTransiciones.values()) {
            mapa.remove(qj);
        }
        estadosIntermedios.remove(estadoAEliminar);

        return obtenerTransicion(qStart.getNombre(), qAccept.getNombre());
    }

    public String generarRegexCompleta() {
        return obtenerTransicion(qStart.getNombre(), qAccept.getNombre());
    }

    private String construirExpresionReducida(String r_ik, String r_ij, String r_jj, String r_jk) {
        StringBuilder sb = new StringBuilder();

        if (!r_ij.equals("ε")) {
            sb.append(formatearSubexpresion(r_ij));
        }

        if (r_jj != null && !r_jj.isEmpty()) {
            if (r_jj.length() > 1 && !r_jj.startsWith("(")) {
                sb.append("(").append(r_jj).append(")*");
            } else {
                sb.append(r_jj).append("*");
            }
        }

        if (!r_jk.equals("ε")) {
            sb.append(formatearSubexpresion(r_jk));
        }

        String caminoNuevo = sb.toString();
        if (caminoNuevo.isEmpty()) {
            caminoNuevo = "ε";
        }

        if (r_ik == null || r_ik.isEmpty()) {
            return caminoNuevo;
        } else {
            return simplificarUnion(r_ik, caminoNuevo);
        }
    }

    private String formatearSubexpresion(String exp) {
        if (exp.contains("+") && !exp.startsWith("(")) {
            return "(" + exp + ")";
        }
        return exp;
    }

    private String simplificarUnion(String exp1, String exp2) {
        if (exp1.equals(exp2)) return exp1;
        if (exp1.equals("∅")) return exp2;
        if (exp2.equals("∅")) return exp1;
        return exp1 + "+" + exp2;
    }

    private String obtenerTransicion(String origen, String destino) {
        if (matrizTransiciones.containsKey(origen)) {
            return matrizTransiciones.get(origen).get(destino);
        }
        return null;
    }

    private boolean contieneTransicion(String origen, String destino) {
        return obtenerTransicion(origen, destino) != null;
    }

    private List<String> obtenerTodosLosNodos() {
        List<String> nodos = new ArrayList<>();
        nodos.add(qStart.getNombre());
        for (Estado e : estadosIntermedios) {
            nodos.add(e.getNombre());
        }
        nodos.add(qAccept.getNombre());
        return nodos;
    }

    public List<Estado> getEstadosIntermedios() {
        return estadosIntermedios;
    }

    public Estado getqStart() {
        return qStart;
    }

    public Estado getqAccept() {
        return qAccept;
    }
}