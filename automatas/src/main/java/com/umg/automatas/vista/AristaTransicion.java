package com.umg.automatas.vista;

import com.umg.automatas.modelo.Transicion;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class AristaTransicion extends Group {
    private final Transicion transicion;
    private final Line linea;
    private final Polygon cabezaFlecha;
    private final Text etiquetaSimbolo;

    public AristaTransicion(Transicion transicion, NodoEstado origen, NodoEstado destino, LienzoCanvas lienzo) {
        this.transicion = transicion;

        linea = new Line();
        linea.setStroke(Color.DARKSLATEGRAY);
        linea.setStrokeWidth(2);

        cabezaFlecha = new Polygon();
        cabezaFlecha.setFill(Color.DARKSLATEGRAY);

        etiquetaSimbolo = new Text(transicion.getSimbolo());
        etiquetaSimbolo.setFont(Font.font("System", FontWeight.BOLD, 13));
        etiquetaSimbolo.setFill(Color.DARKRED);

        this.getChildren().addAll(linea, cabezaFlecha, etiquetaSimbolo);

        // Permitir seleccionar/borrar la transición con clic secundario
        this.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                lienzo.eliminarTransicion(this);
                e.consume();
            }
        });

        // La flecha se recalcula automáticamente si el usuario mueve los nodos
        origen.layoutXProperty().addListener((obs, oldVal, newVal) -> actualizarPosicion(origen, destino));
        origen.layoutYProperty().addListener((obs, oldVal, newVal) -> actualizarPosicion(origen, destino));
        destino.layoutXProperty().addListener((obs, oldVal, newVal) -> actualizarPosicion(origen, destino));
        destino.layoutYProperty().addListener((obs, oldVal, newVal) -> actualizarPosicion(origen, destino));

        actualizarPosicion(origen, destino);
    }

    public void actualizarPosicion(NodoEstado origen, NodoEstado destino) {
        double x1 = origen.getLayoutX() + 25;
        double y1 = origen.getLayoutY() + 25;
        double x2 = destino.getLayoutX() + 25;
        double y2 = destino.getLayoutY() + 25;

        double angulo = Math.atan2(y2 - y1, x2 - x1);
        double x2Ajustado = x2 - 25 * Math.cos(angulo);
        double y2Ajustado = y2 - 25 * Math.sin(angulo);

        linea.setStartX(x1);
        linea.setStartY(y1);
        linea.setEndX(x2Ajustado);
        linea.setEndY(y2Ajustado);

        double tamanioPunta = 10.0;
        cabezaFlecha.getPoints().clear();
        cabezaFlecha.getPoints().addAll(
            x2Ajustado, y2Ajustado,
            x2Ajustado - tamanioPunta * Math.cos(angulo - Math.PI / 6), y2Ajustado - tamanioPunta * Math.sin(angulo - Math.PI / 6),
            x2Ajustado - tamanioPunta * Math.cos(angulo + Math.PI / 6), y2Ajustado - tamanioPunta * Math.sin(angulo + Math.PI / 6)
        );

        etiquetaSimbolo.setX((x1 + x2Ajustado) / 2 - 5);
        etiquetaSimbolo.setY((y1 + y2Ajustado) / 2 - 8);
    }

    public Transicion getTransicion() {
        return transicion;
    }
}