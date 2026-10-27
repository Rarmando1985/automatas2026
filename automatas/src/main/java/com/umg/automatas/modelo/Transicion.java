package com.umg.automatas.modelo;

public class Transicion {
private final Estado origen;
private final Estado destino;
private final String simbolo;

public Transicion(Estado origen, Estado destino, String simbolo) {
    this.origen = origen;
    this.destino = destino;
    this.simbolo = simbolo;
}

public Estado getOrigen() {
    return origen;
}

public Estado getDestino() {
    return destino;
}

public String getSimbolo() {
    return simbolo;
}

@Override
public String toString() {
    return origen.getNombre() + " --" + simbolo + "--> " + destino.getNombre();
}

@Override
public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }

    if (!(obj instanceof Transicion)) {
        return false;
    }

    Transicion otra = (Transicion) obj;

    return origen.equals(otra.origen)
            && destino.equals(otra.destino)
            && simbolo.equals(otra.simbolo);
}

@Override
public int hashCode() {
    return java.util.Objects.hash(origen, destino, simbolo);
}


}