package com.umg.automatas.modelo;

import java.util.Objects;

public class Estado {
    private String nombre;
    private boolean esInicial;
    private boolean esAceptacion;
    
    private double posicionX;
    private double posicionY;

    public Estado(String nombre, double posicionX, double posicionY) {
        this.nombre = nombre;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.esInicial = false;
        this.esAceptacion = false;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean esInicial() {
        return esInicial;
    }

    public void setEsInicial(boolean esInicial) {
        this.esInicial = esInicial;
    }

    public boolean esAceptacion() {
        return esAceptacion;
    }

    public void setEsAceptacion(boolean esAceptacion) {
        this.esAceptacion = esAceptacion;
    }

    public double getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(double posicionX) {
        this.posicionX = posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(double posicionY) {
        this.posicionY = posicionY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estado estado = (Estado) o;
        return Objects.equals(nombre, estado.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
