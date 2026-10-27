package com.umg.automatas.modelo;

import java.util.ArrayList;
import java.util.List;


public class AutomataDTO {
    public static class EstadoDTO {
        public String nombre;
        public double x;
        public double y;
        public boolean esInicial;
        public boolean esAceptacion;
    }

    public static class TransicionDTO {
        public String origen;
        public String destino;
        public String simbolo;
    }

    public List<EstadoDTO> estados = new ArrayList<>();
    public List<TransicionDTO> transiciones = new ArrayList<>();
}