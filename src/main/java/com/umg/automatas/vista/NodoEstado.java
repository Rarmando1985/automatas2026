package com.umg.automatas.vista;

import com.umg.automatas.modelo.Estado;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class NodoEstado extends Group {
    private final Estado estado;
    private final Circle circuloExterior;
    private final Circle circuloInterior;
    private final Text textoEtiqueta;
    private final Polygon flechaInicial;

    public NodoEstado(Estado estado) {
        this.estado = estado;

        circuloExterior = new Circle(25);
        circuloExterior.setFill(Color.DODGERBLUE);
        circuloExterior.setStroke(Color.BLACK);
        circuloExterior.setStrokeWidth(2);

        circuloInterior = new Circle(20);
        circuloInterior.setFill(Color.TRANSPARENT);
        circuloInterior.setStroke(Color.BLACK);
        circuloInterior.setStrokeWidth(1.5);
        circuloInterior.setVisible(false);

        textoEtiqueta = new Text(estado.getNombre());
        textoEtiqueta.setFont(Font.font("System", FontWeight.BOLD, 14));
        textoEtiqueta.setFill(Color.WHITE);
        textoEtiqueta.setX(-textoEtiqueta.getLayoutBounds().getWidth() / 2);
        textoEtiqueta.setY(5);

        flechaInicial = new Polygon(-45.0, 0.0, -28.0, -8.0, -28.0, 8.0);
        flechaInicial.setFill(Color.ROYALBLUE);
        flechaInicial.setVisible(false);

        this.getChildren().addAll(circuloExterior, circuloInterior, textoEtiqueta, flechaInicial);

        this.setLayoutX(estado.getPosicionX());
        this.setLayoutY(estado.getPosicionY());

        actualizarVisualizacion();
    }

    public void actualizarVisualizacion() {
        circuloInterior.setVisible(estado.esAceptacion());
        flechaInicial.setVisible(estado.esInicial());
        
        circuloExterior.setFill(Color.DODGERBLUE);
    }

    public void resaltar(boolean activar) {
        if (activar) {
            circuloExterior.setFill(Color.GOLD);
        } else {
            circuloExterior.setFill(Color.DODGERBLUE);
        }
    }

    public void resaltarAnimacion(Color color) {
        circuloExterior.setFill(color);
    }

    public Estado getEstado() {
        return estado;
    }
}   