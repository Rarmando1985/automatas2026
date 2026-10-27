package com.umg.automatas.vista;

import com.umg.automatas.modelo.Automata;
import com.umg.automatas.modelo.AutomataDTO;
import com.umg.automatas.modelo.Estado;
import com.umg.automatas.modelo.Transicion;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lienzo interactivo (Pane JavaFX) con cuadrícula de fondo profesional
 * donde se dibujan, conectan, editan y animan los estados y transiciones.
 */
public class LienzoCanvas extends Pane {
    private final Automata automata;
    private final Map<Estado, NodoEstado> mapaNodos;
    private final List<AristaTransicion> aristas;
    private final List<Line> lineasCuadricula;
    private int contadorEstados;

    private NodoEstado nodoOrigenSeleccionado;
    private NodoEstado nodoActivoFocus;

    private double offsetMouseX;
    private double offsetMouseY;

    public LienzoCanvas(Automata automata) {
        this.automata = automata;
        this.mapaNodos = new HashMap<>();
        this.aristas = new ArrayList<>();
        this.lineasCuadricula = new ArrayList<>();
        this.contadorEstados = 0;
        this.nodoOrigenSeleccionado = null;
        this.nodoActivoFocus = null;

        // Estilo base del lienzo
        this.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #DEE2E6; -fx-border-width: 1px;");

        // Dibujar retícula visual de fondo
        dibujarCuadricula();

        // Habilitar recepción de eventos de teclado (Tecla Supr / Delete)
        this.setFocusTraversable(true);
        this.setOnKeyPressed(e -> {
            if ((e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE) && nodoActivoFocus != null) {
                eliminarEstado(nodoActivoFocus);
                nodoActivoFocus = null;
            }
        });

        // Clic en el área libre del lienzo para crear un nuevo estado
        this.setOnMouseClicked(evento -> {
            this.requestFocus();
            if (evento.isStillSincePress() && evento.getTarget() == this) {
                if (nodoOrigenSeleccionado != null) {
                    nodoOrigenSeleccionado.resaltar(false);
                    nodoOrigenSeleccionado = null;
                } else {
                    crearNuevoEstado(evento.getX(), evento.getY());
                }
            }
        });
    }

    /**
     * Dibuja líneas de guía tipo retícula en el fondo del lienzo.
     */
    private void dibujarCuadricula() {
        double espacio = 25;
        double maxAncho = 2500;
        double maxAlto = 2000;

        for (double x = 0; x < maxAncho; x += espacio) {
            Line lineaV = new Line(x, 0, x, maxAlto);
            lineaV.setStroke(Color.web("#F1F3F5"));
            lineaV.setStrokeWidth(1);
            lineasCuadricula.add(lineaV);
            this.getChildren().add(lineaV);
        }

        for (double y = 0; y < maxAlto; y += espacio) {
            Line lineaH = new Line(0, y, maxAncho, y);
            lineaH.setStroke(Color.web("#F1F3F5"));
            lineaH.setStrokeWidth(1);
            lineasCuadricula.add(lineaH);
            this.getChildren().add(lineaH);
        }
    }

    public void crearNuevoEstado(double x, double y) {
        String nombre = "q" + contadorEstados++;
        Estado nuevoEstado = new Estado(nombre, x, y);

        if (automata.getEstados().isEmpty()) {
            nuevoEstado.setEsInicial(true);
        }

        automata.agregarEstado(nuevoEstado);
        NodoEstado nodo = new NodoEstado(nuevoEstado);

        configurarEventosNodo(nodo);

        mapaNodos.put(nuevoEstado, nodo);
        this.getChildren().add(nodo);
    }

    private void configurarEventosNodo(NodoEstado nodo) {
        // Evento presionar ratón (prepara arrastre y selecciona focus)
        nodo.setOnMousePressed(evento -> {
            offsetMouseX = evento.getSceneX() - nodo.getLayoutX();
            offsetMouseY = evento.getSceneY() - nodo.getLayoutY();
            nodoActivoFocus = nodo;
            this.requestFocus();
            evento.consume();
        });

        // Evento arrastrar ratón (mueve el estado libremente por el lienzo)
        nodo.setOnMouseDragged(evento -> {
            double nuevaX = evento.getSceneX() - offsetMouseX;
            double nuevaY = evento.getSceneY() - offsetMouseY;

            nodo.setLayoutX(nuevaX);
            nodo.setLayoutY(nuevaY);

            nodo.getEstado().setPosicionX(nuevaX);
            nodo.getEstado().setPosicionY(nuevaY);

            evento.consume();
        });

        // Clic en el nodo (Izquierdo: Seleccionar/Conectar | Derecho: Alternar Estado Final)
        nodo.setOnMouseClicked(evento -> {
            if (evento.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (nodoOrigenSeleccionado == null) {
                    nodoOrigenSeleccionado = nodo;
                    nodo.resaltar(true);
                } else {
                    crearTransicionEntre(nodoOrigenSeleccionado, nodo);
                    nodoOrigenSeleccionado.resaltar(false);
                    nodoOrigenSeleccionado = null;
                }
                evento.consume();
            } else if (evento.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                Estado e = nodo.getEstado();
                e.setEsAceptacion(!e.esAceptacion());
                nodo.actualizarVisualizacion();
                evento.consume();
            }
        });
    }

    private void crearTransicionEntre(NodoEstado origen, NodoEstado destino) {
        TextInputDialog dialogo = new TextInputDialog("a");
        dialogo.setTitle("Nueva Transición");
        dialogo.setHeaderText("Transición de " + origen.getEstado().getNombre() + " a " + destino.getEstado().getNombre());
        dialogo.setContentText("Ingrese el símbolo (deje EN BLANCO para 'ε'):");

        Optional<String> resultado = dialogo.showAndWait();
        resultado.ifPresent(simbolo -> {
            String simboloLimpio = simbolo.trim();
            if (simboloLimpio.isEmpty()) {
                simboloLimpio = "ε";
            }

            Transicion t = new Transicion(origen.getEstado(), destino.getEstado(), simboloLimpio);
            automata.agregarTransicion(origen.getEstado(), destino.getEstado(), simboloLimpio);

            AristaTransicion arista = new AristaTransicion(t, origen, destino, this);
            aristas.add(arista);
            // Insertar la arista por detrás de los nodos para mantener buen aspecto visual
            int indiceInsercion = Math.min(lineasCuadricula.size(), this.getChildren().size());
            this.getChildren().add(indiceInsercion, arista);
        });
    }

    public void eliminarEstado(NodoEstado nodo) {
        Estado e = nodo.getEstado();
        List<AristaTransicion> aBorrar = new ArrayList<>();
        for (AristaTransicion arista : aristas) {
            if (arista.getTransicion().getOrigen().equals(e) || arista.getTransicion().getDestino().equals(e)) {
                aBorrar.add(arista);
            }
        }
        for (AristaTransicion arista : aBorrar) {
            eliminarTransicion(arista);
        }

        automata.getEstados().remove(e);
        mapaNodos.remove(e);
        this.getChildren().remove(nodo);
    }

    public void eliminarTransicion(AristaTransicion arista) {
        automata.getTransiciones().remove(arista.getTransicion());
        aristas.remove(arista);
        this.getChildren().remove(arista);
    }

    public void restaurarColoresNodos() {
        for (NodoEstado nodo : mapaNodos.values()) {
            nodo.actualizarVisualizacion();
        }
    }

    public void limpiarLienzo() {
        this.getChildren().clear();
        this.mapaNodos.clear();
        this.aristas.clear();
        this.lineasCuadricula.clear();
        this.automata.getEstados().clear();
        this.automata.getTransiciones().clear();
        this.contadorEstados = 0;
        this.nodoOrigenSeleccionado = null;
        this.nodoActivoFocus = null;

        // Redibujar cuadrícula
        dibujarCuadricula();
    }

    /**
     * Construye y dibuja en el lienzo un autómata cargado desde un archivo .txt.
     */
    public void cargarDesdeAutomata(Automata nuevoAutomata) {
        limpiarLienzo();

        // 1. Reconstruir nodos de estados
        for (Estado e : nuevoAutomata.getEstados()) {
            this.automata.agregarEstado(e);
            NodoEstado nodo = new NodoEstado(e);
            configurarEventosNodo(nodo);
            mapaNodos.put(e, nodo);
            this.getChildren().add(nodo);

            try {
                int num = Integer.parseInt(e.getNombre().replace("q", ""));
                if (num >= contadorEstados) {
                    contadorEstados = num + 1;
                }
            } catch (Exception ignored) {}
        }

        // 2. Reconstruir aristas de transiciones
        for (Transicion t : nuevoAutomata.getTransiciones()) {
            this.automata.agregarTransicion(t.getOrigen(), t.getDestino(), t.getSimbolo());
            NodoEstado nodoOrigen = mapaNodos.get(t.getOrigen());
            NodoEstado nodoDestino = mapaNodos.get(t.getDestino());

            if (nodoOrigen != null && nodoDestino != null) {
                AristaTransicion arista = new AristaTransicion(t, nodoOrigen, nodoDestino, this);
                aristas.add(arista);
                int indiceInsercion = Math.min(lineasCuadricula.size(), this.getChildren().size());
                this.getChildren().add(indiceInsercion, arista);
            }
        }
    }

    /**
     * Carga un autómata desde la estructura JSON DTO.
     */
    public void cargarDesdeDTO(AutomataDTO dto) {
        limpiarLienzo();
        Map<String, Estado> mapaEstadosTemp = new HashMap<>();

        for (AutomataDTO.EstadoDTO edto : dto.estados) {
            Estado e = new Estado(edto.nombre, edto.x, edto.y);
            e.setEsInicial(edto.esInicial);
            e.setEsAceptacion(edto.esAceptacion);

            automata.agregarEstado(e);
            NodoEstado nodo = new NodoEstado(e);
            configurarEventosNodo(nodo);

            mapaNodos.put(e, nodo);
            mapaEstadosTemp.put(e.getNombre(), e);
            this.getChildren().add(nodo);

            int num = 0;
            try {
                num = Integer.parseInt(edto.nombre.replace("q", ""));
            } catch (Exception ignored) {}
            if (num >= contadorEstados) {
                contadorEstados = num + 1;
            }
        }

        for (AutomataDTO.TransicionDTO tdto : dto.transiciones) {
            Estado origen = mapaEstadosTemp.get(tdto.origen);
            Estado destino = mapaEstadosTemp.get(tdto.destino);

            if (origen != null && destino != null) {
                Transicion t = new Transicion(origen, destino, tdto.simbolo);
                automata.agregarTransicion(origen, destino, tdto.simbolo);

                NodoEstado nodoOrigen = mapaNodos.get(origen);
                NodoEstado nodoDestino = mapaNodos.get(destino);

                AristaTransicion arista = new AristaTransicion(t, nodoOrigen, nodoDestino, this);
                aristas.add(arista);
                int indiceInsercion = Math.min(lineasCuadricula.size(), this.getChildren().size());
                this.getChildren().add(indiceInsercion, arista);
            }
        }
    }
    /**
 * Cambia dinámicamente la paleta del lienzo según el modo de tema activo.
 */
public void aplicarTemaLienzo(boolean esModoOscuro) {
    if (esModoOscuro) {
        this.setStyle("-fx-background-color: #121212; -fx-border-color: #2D2D2D; -fx-border-width: 1px;");
        for (Line linea : lineasCuadricula) {
            linea.setStroke(Color.web("#222222"));
        }
    } else {
        this.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #DEE2E6; -fx-border-width: 1px;");
        for (Line linea : lineasCuadricula) {
            linea.setStroke(Color.web("#F1F3F5"));
        }
    }
}

    public void exportarImagenPNG(File archivoDestino) throws IOException {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        WritableImage snapshot = this.snapshot(params, null);

        int ancho = (int) snapshot.getWidth();
        int alto = (int) snapshot.getHeight();
        BufferedImage bufferedImage = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = snapshot.getPixelReader();

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                bufferedImage.setRGB(x, y, reader.getArgb(x, y));
            }
        }

        ImageIO.write(bufferedImage, "png", archivoDestino);
    }

    public NodoEstado getNodo(Estado estado) { return mapaNodos.get(estado); }
    public Automata getAutomata() { return automata; }
}