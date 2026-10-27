package com.umg.automatas.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representa la quíntupla M = (Q, Σ, δ, q0, F)
 */
public class Automata {
    private final List<Estado> estados;
    private final List<Transicion> transiciones;

    public Automata() {
        this.estados = new ArrayList<>();
        this.transiciones = new ArrayList<>();
    }

    public void agregarEstado(Estado estado) {
        estados.add(estado);
    }

    public void agregarTransicion(Estado origen, Estado destino, String simbolo) {
        Transicion t = new Transicion(origen, destino, simbolo);
        if (!transiciones.contains(t)) {
            transiciones.add(t);
        }
    }

    public Estado getEstadoInicial() {
        for (Estado e : estados) {
            if (e.esInicial()) return e;
        }
        return null;
    }

    public List<Estado> getEstados() { return estados; }
    public List<Transicion> getTransiciones() { return transiciones; }

    public Set<String> getAlfabeto() {
        Set<String> alfabeto = new HashSet<>();
        for (Transicion t : transiciones) {
            if (!t.getSimbolo().equals("ε") && !t.getSimbolo().isEmpty()) {
                alfabeto.add(t.getSimbolo());
            }
        }
        return alfabeto;
    }

    public Set<Estado> obtenerClausuraEpsilon(Set<Estado> conjuntoEstados) {
        Set<Estado> clausura = new HashSet<>(conjuntoEstados);
        List<Estado> porProcesar = new ArrayList<>(conjuntoEstados);

        while (!porProcesar.isEmpty()) {
            Estado actual = porProcesar.remove(0);
            for (Transicion t : transiciones) {
                if (t.getOrigen().equals(actual) && (t.getSimbolo().equals("ε") || t.getSimbolo().isEmpty())) {
                    if (!clausura.contains(t.getDestino())) {
                        clausura.add(t.getDestino());
                        porProcesar.add(t.getDestino());
                    }
                }
            }
        }
        return clausura;
    }

    public List<Estado> obtenerCaminoRecorrido(String cadena) {
        List<Estado> camino = new ArrayList<>();
        Estado inicial = getEstadoInicial();

        if (inicial == null) return camino;

        Set<Estado> actuales = new HashSet<>();
        actuales.add(inicial);
        actuales = obtenerClausuraEpsilon(actuales);

        if (!actuales.isEmpty()) {
            camino.add(actuales.iterator().next());
        }

        if (cadena == null || cadena.trim().isEmpty() || cadena.equals("ε")) {
            return camino;
        }

        for (char c : cadena.toCharArray()) {
            String simbolo = String.valueOf(c);
            Set<Estado> siguientes = new HashSet<>();

            for (Estado e : actuales) {
                for (Transicion t : transiciones) {
                    if (t.getOrigen().equals(e) && t.getSimbolo().equals(simbolo)) {
                        siguientes.add(t.getDestino());
                    }
                }
            }

            actuales = obtenerClausuraEpsilon(siguientes);

            if (!actuales.isEmpty()) {
                camino.add(actuales.iterator().next());
            } else {
                break;
            }
        }

        return camino;
    }

    public boolean evaluarCadena(String cadena) {
        Estado inicial = getEstadoInicial();
        if (inicial == null) return false;

        Set<Estado> actuales = new HashSet<>();
        actuales.add(inicial);
        actuales = obtenerClausuraEpsilon(actuales);

        if (cadena == null || cadena.trim().isEmpty() || cadena.equals("ε")) {
            return contieneEstadoAceptacion(actuales);
        }

        for (char c : cadena.toCharArray()) {
            String simbolo = String.valueOf(c);
            Set<Estado> siguientes = new HashSet<>();

            for (Estado e : actuales) {
                for (Transicion t : transiciones) {
                    if (t.getOrigen().equals(e) && t.getSimbolo().equals(simbolo)) {
                        siguientes.add(t.getDestino());
                    }
                }
            }

            actuales = obtenerClausuraEpsilon(siguientes);

            if (actuales.isEmpty()) {
                return false;
            }
        }

        return contieneEstadoAceptacion(actuales);
    }

    private boolean contieneEstadoAceptacion(Set<Estado> conjunto) {
        for (Estado e : conjunto) {
            if (e.esAceptacion()) return true;
        }
        return false;
    }

    public TipoAutomata determinarTipoAutomata() {
        if (estados.isEmpty()) return TipoAutomata.AFD;

        for (Estado e : estados) {
            Map<String, Integer> contadorSimbolos = new HashMap<>();

            for (Transicion t : transiciones) {
                if (t.getOrigen().equals(e)) {
                    String simbolo = t.getSimbolo().trim();

                    if (simbolo.equals("ε") || simbolo.isEmpty()) {
                        return TipoAutomata.AFN;
                    }

                    contadorSimbolos.put(simbolo, contadorSimbolos.getOrDefault(simbolo, 0) + 1);
                    if (contadorSimbolos.get(simbolo) > 1) {
                        return TipoAutomata.AFN;
                    }
                }
            }
        }
        return TipoAutomata.AFD;
    }

    /**
     * Exporta la estructura del autómata a un DTO para guardar en formato JSON.
     */
    public AutomataDTO exportarDTO() {
        AutomataDTO dto = new AutomataDTO();
        for (Estado e : estados) {
            AutomataDTO.EstadoDTO edto = new AutomataDTO.EstadoDTO();
            edto.nombre = e.getNombre();
            edto.x = e.getPosicionX();
            edto.y = e.getPosicionY();
            edto.esInicial = e.esInicial();
            edto.esAceptacion = e.esAceptacion();
            dto.estados.add(edto);
        }
        for (Transicion t : transiciones) {
            AutomataDTO.TransicionDTO tdto = new AutomataDTO.TransicionDTO();
            tdto.origen = t.getOrigen().getNombre();
            tdto.destino = t.getDestino().getNombre();
            tdto.simbolo = t.getSimbolo();
            dto.transiciones.add(tdto);
        }
        return dto;
    }
}