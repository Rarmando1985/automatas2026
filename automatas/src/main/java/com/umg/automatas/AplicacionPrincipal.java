package com.umg.automatas;

import com.umg.automatas.modelo.Automata;
import com.umg.automatas.modelo.AutomataDTO;
import com.umg.automatas.modelo.CargadorAutomataTXT;
import com.umg.automatas.modelo.Estado;
import com.umg.automatas.modelo.TipoAutomata;
import com.umg.automatas.modelo.Transicion;
import com.umg.automatas.vista.LienzoCanvas;
import com.umg.automatas.vista.NodoEstado;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Aplicación Principal - Automaton Studio UMG
 * Incluye alternancia de tema (Modo Claro / Modo Oscuro), evaluación múltiple
 * y persistencia completa.
 */
public class AplicacionPrincipal extends Application {

    private static final Color[] COLORES_CADENAS = {
        Color.web("#0D6EFD"), Color.web("#FD7E14"), Color.web("#198754"),
        Color.web("#6F42C1"), Color.web("#DC3545"), Color.web("#20C997"),
        Color.web("#D63384"), Color.web("#795548"), Color.web("#84CC16"),
        Color.web("#8B5CF6")
    };

    private static final String[] COLORES_HEX = {
        "#0D6EFD", "#FD7E14", "#198754", "#6F42C1", "#DC3545",
        "#20C997", "#D63384", "#795548", "#84CC16", "#8B5CF6"
    };

    private final List<FilaCadena> filasCadenas = new ArrayList<>();
    private VBox contenedorFilas;
    private boolean esModoOscuro = false;

    private static class FilaCadena {
        int indice;
        TextField campoTexto;
        Label lblColor;
        Label lblResultado;
        Color colorAsignado;

        public FilaCadena(int indice, Color color, String colorHex) {
            this.indice = indice;
            this.colorAsignado = color;
            this.campoTexto = new TextField();
            this.campoTexto.setPromptText("Cadena " + (indice + 1) + " (vacío = ε)");
            this.campoTexto.setPrefWidth(110);

            this.lblColor = new Label(" " + (indice + 1) + " ");
            this.lblColor.setFont(Font.font("System", FontWeight.BOLD, 11));
            this.lblColor.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-background-radius: 3;");

            this.lblResultado = new Label("•");
            this.lblResultado.setFont(Font.font("System", FontWeight.BOLD, 11));
            this.lblResultado.setStyle("-fx-text-fill: #6C757D;");
        }
    }

    @Override
    public void start(Stage stage) {
        Automata automata = new Automata();
        LienzoCanvas lienzo = new LienzoCanvas(automata);

        // --- Panel Superior: Menú de Acciones ---
        Label lblTipo = new Label("Tipo: AFD");
        lblTipo.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblTipo.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-padding: 4 8 4 8; -fx-background-radius: 4;");

        Button btnGuardar = new Button("Guardar (JSON)");
        btnGuardar.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnCargarJSON = new Button("Cargar (JSON)");
        btnCargarJSON.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnCargarTXT = new Button("Cargar (TXT)");
        btnCargarTXT.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnTablaTransiciones = new Button("Tabla");
        btnTablaTransiciones.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnGenerarEjemplos = new Button("Generar Ejemplos");
        btnGenerarEjemplos.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnExportarPNG = new Button("Exportar PNG");
        btnExportarPNG.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold;");

        // BOTÓN NUEVO: Alternar Tema (Claro / Oscuro)
        Button btnTema = new Button("Modo Oscuro 🌙");
        btnTema.setStyle("-fx-background-color: #343A40; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox topBox = new HBox(10, lblTipo, btnGuardar, btnCargarJSON, btnCargarTXT, btnTablaTransiciones, btnGenerarEjemplos, btnExportarPNG, btnTema);
        topBox.getStyleClass().add("panel-top");
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setStyle("-fx-background-color: #E9ECEF;");

        // --- Panel Lateral Derecho: Evaluación Múltiple ---
        Label lblTituloCadenas = new Label("Evaluación Múltiple (10 Cadenas)");
        lblTituloCadenas.setFont(Font.font("System", FontWeight.BOLD, 13));

        contenedorFilas = new VBox(6);
        for (int i = 0; i < 10; i++) {
            FilaCadena fila = new FilaCadena(i, COLORES_CADENAS[i], COLORES_HEX[i]);
            filasCadenas.add(fila);

            HBox cajaFila = new HBox(6, fila.lblColor, fila.campoTexto, fila.lblResultado);
            cajaFila.setAlignment(Pos.CENTER_LEFT);
            contenedorFilas.getChildren().add(cajaFila);
        }

        ScrollPane scrollCadenas = new ScrollPane(contenedorFilas);
        scrollCadenas.setFitToWidth(true);
        scrollCadenas.setPrefHeight(380);
        scrollCadenas.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Label lblVelocidad = new Label("Velocidad de Animación:");
        lblVelocidad.setFont(Font.font("System", FontWeight.BOLD, 11));
        Slider sliderVelocidad = new Slider(0.2, 2.0, 0.8);
        sliderVelocidad.setShowTickMarks(true);

        Button btnEvaluarTodas = new Button("Simular Cadenas");
        btnEvaluarTodas.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEvaluarTodas.setMaxWidth(Double.MAX_VALUE);

        Button btnConvertirRegex = new Button("Convertir a Regex");
        btnConvertirRegex.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold;");
        btnConvertirRegex.setDisable(true);
        btnConvertirRegex.setMaxWidth(Double.MAX_VALUE);

        Button btnLimpiar = new Button("Limpiar Lienzo");
        btnLimpiar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);

        VBox rightBox = new VBox(10, lblTituloCadenas, scrollCadenas, lblVelocidad, sliderVelocidad, btnEvaluarTodas, btnConvertirRegex, btnLimpiar);
        rightBox.getStyleClass().add("panel-right");
        rightBox.setPadding(new Insets(12));
        rightBox.setPrefWidth(290);
        rightBox.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #CED4DA; -fx-border-width: 0 0 0 1px;");

        // --- Panel Inferior: Estado Global ---
        Label lblResultadoGlobal = new Label("Estado: Esperando evaluación");
        lblResultadoGlobal.setFont(Font.font("System", FontWeight.BOLD, 13));

        HBox bottomBox = new HBox(lblResultadoGlobal);
        bottomBox.getStyleClass().add("panel-bottom");
        bottomBox.setPadding(new Insets(10, 15, 10, 15));
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setStyle("-fx-background-color: #F1F3F5; -fx-border-color: #CED4DA; -fx-border-width: 1px 0 0 0;");

        lienzo.setOnMouseReleased(e -> actualizarTipoAutomata(lienzo.getAutomata(), lblTipo));

        // --- EVENTO: Alternar Tema Claro / Oscuro ---
        BorderPane root = new BorderPane();
        btnTema.setOnAction(e -> {
            esModoOscuro = !esModoOscuro;
            if (esModoOscuro) {
                root.getStyleClass().add("dark-theme");
                btnTema.setText("Modo Claro ☀️");
                btnTema.setStyle("-fx-background-color: #FFC107; -fx-text-fill: #000000; -fx-font-weight: bold;");
                topBox.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #2D2D2D; -fx-border-width: 0 0 1px 0;");
                rightBox.setStyle("-fx-background-color: #181818; -fx-border-color: #2D2D2D; -fx-border-width: 0 0 0 1px;");
                bottomBox.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #2D2D2D; -fx-border-width: 1px 0 0 0;");
            } else {
                root.getStyleClass().remove("dark-theme");
                btnTema.setText("Modo Oscuro 🌙");
                btnTema.setStyle("-fx-background-color: #343A40; -fx-text-fill: white; -fx-font-weight: bold;");
                topBox.setStyle("-fx-background-color: #E9ECEF;");
                rightBox.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #CED4DA; -fx-border-width: 0 0 0 1px;");
                bottomBox.setStyle("-fx-background-color: #F1F3F5; -fx-border-color: #CED4DA; -fx-border-width: 1px 0 0 0;");
            }
            lienzo.aplicarTemaLienzo(esModoOscuro);
        });

        // --- Eventos de Menú y Operaciones ---
        btnGuardar.setOnAction(e -> {
            Automata autActual = lienzo.getAutomata();
            if (autActual.getEstados().isEmpty()) {
                mostrarAviso("Atención", "Lienzo Vacío", "No hay ningún autómata en el lienzo para guardar.", Alert.AlertType.WARNING);
                return;
            }
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar Autómata (JSON)");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo JSON (*.json)", "*.json"));
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    AutomataDTO dto = autActual.exportarDTO();
                    writer.write(convertirDTOaJSON(dto));
                    mostrarAviso("Guardado Exitoso", "Autómata Guardado",
                            "El autómata se guardó correctamente en: " + file.getName(), Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    mostrarAviso("Error al Guardar", "No se pudo guardar el archivo", ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        btnCargarJSON.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Cargar Autómata desde JSON");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo JSON (*.json)", "*.json"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    StringBuilder jsonSB = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) jsonSB.append(line);
                    AutomataDTO dto = convertirJSONaDTO(jsonSB.toString());
                    lienzo.cargarDesdeDTO(dto);
                    actualizarTipoAutomata(lienzo.getAutomata(), lblTipo);
                    mostrarAviso("Carga Exitosa", "Autómata Cargado",
                            "Se cargó la estructura y posiciones desde: " + file.getName(), Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    mostrarAviso("Error de Carga", "No se pudo leer el archivo JSON", ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        btnCargarTXT.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Cargar Autómata desde Archivo TXT");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de Texto (*.txt)", "*.txt"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                try {
                    Automata autCargado = CargadorAutomataTXT.cargarDesdeArchivoTXT(file);
                    lienzo.cargarDesdeAutomata(autCargado);
                    actualizarTipoAutomata(lienzo.getAutomata(), lblTipo);
                    mostrarAviso("Carga Exitosa", "Autómata Dibujado",
                            "El autómata se construyó correctamente desde: " + file.getName(),
                            Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    mostrarAviso("Error de Lectura", "No se pudo interpretar el archivo",
                            ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        btnTablaTransiciones.setOnAction(e -> mostrarVentanaTablaTransiciones(lienzo.getAutomata()));

        btnGenerarEjemplos.setOnAction(e -> {
            Automata autActual = lienzo.getAutomata();
            if (autActual.getEstados().isEmpty()) {
                mostrarAviso("Atención", "Lienzo Vacío", "Dibuja o carga un autómata antes de generar ejemplos.", Alert.AlertType.WARNING);
                return;
            }

            Map<String, List<String>> ejemplos = generarEjemplosCadenas(autActual);
            List<String> aceptadas = ejemplos.get("Aceptadas");
            List<String> rechazadas = ejemplos.get("Rechazadas");

            int idx = 0;
            for (String cad : aceptadas) {
                if (idx < filasCadenas.size()) {
                    filasCadenas.get(idx++).campoTexto.setText(cad);
                }
            }
            for (String cad : rechazadas) {
                if (idx < filasCadenas.size()) {
                    filasCadenas.get(idx++).campoTexto.setText(cad);
                }
            }

            mostrarAviso("Ejemplos Generados", "Cadenas de Prueba Insertadas",
                    "Cadenas Aceptadas: " + aceptadas + "\n\nCadenas Rechazadas: " + rechazadas +
                    "\n\nSe han copiado automáticamente a las casillas laterales para simularlas.",
                    Alert.AlertType.INFORMATION);
        });

        btnExportarPNG.setOnAction(e -> {
            Automata autActual = lienzo.getAutomata();
            if (autActual.getEstados().isEmpty()) {
                mostrarAviso("Atención", "Lienzo Vacío", "Dibuja un autómata antes de exportar la imagen.", Alert.AlertType.WARNING);
                return;
            }
            FileChooser fc = new FileChooser();
            fc.setTitle("Exportar Grafo a Imagen PNG");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen PNG (*.png)", "*.png"));
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try {
                    lienzo.exportarImagenPNG(file);
                    mostrarAviso("Exportación Exitosa", "Imagen Exportada",
                            "La imagen PNG del autómata se guardó en: " + file.getName(), Alert.AlertType.INFORMATION);
                } catch (Exception ex) {
                    mostrarAviso("Error al Exportar", "No se pudo crear la imagen", ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });

        btnLimpiar.setOnAction(e -> {
            lienzo.limpiarLienzo();
            lblTipo.setText("Tipo: AFD");
            lblTipo.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-padding: 4 8 4 8; -fx-background-radius: 4;");
            lblResultadoGlobal.setText("Estado: Lienzo reajustado");
            lblResultadoGlobal.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 13px;");

            for (FilaCadena fila : filasCadenas) {
                fila.campoTexto.clear();
                fila.lblResultado.setText("•");
                fila.lblResultado.setStyle("-fx-text-fill: #6C757D;");
            }
        });

        btnEvaluarTodas.setOnAction(e -> {
            Automata autActual = lienzo.getAutomata();
            lienzo.restaurarColoresNodos();

            if (autActual.getEstados().isEmpty()) {
                mostrarAviso("Atención", "No hay estados", "Crea o carga un autómata antes de simular.", Alert.AlertType.WARNING);
                return;
            }

            if (autActual.getEstadoInicial() == null) {
                mostrarAviso("Atención", "Falta Estado Inicial", "El autómata no tiene un estado inicial definido.", Alert.AlertType.WARNING);
                return;
            }

            List<FilaCadena> filasAProcesar = new ArrayList<>();
            for (FilaCadena fila : filasCadenas) {
                if (!fila.campoTexto.getText().trim().isEmpty()) {
                    filasAProcesar.add(fila);
                }
            }

            if (filasAProcesar.isEmpty()) {
                mostrarAviso("Atención", "Sin Cadenas", "Ingresa al menos una cadena en el panel lateral para simular.", Alert.AlertType.WARNING);
                return;
            }

            btnEvaluarTodas.setDisable(true);
            btnLimpiar.setDisable(true);

            ejecutarSimulacionSecuencial(autActual, lienzo, filasAProcesar, 0, sliderVelocidad.getValue(), btnEvaluarTodas, btnLimpiar, lblResultadoGlobal);
        });

        root.setTop(topBox);
        root.setCenter(lienzo);
        root.setRight(rightBox);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1180, 650);
        
        // Cargar hoja de estilos CSS opcional
        try {
            scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());
        } catch (Exception ignored) {}

        stage.setTitle("Automaton Studio - UMG");
        stage.setScene(scene);
        stage.show();
    }

    private void ejecutarSimulacionSecuencial(Automata automata, LienzoCanvas lienzo, List<FilaCadena> filas,
                                               int indiceActual, double velocidad, Button btnEvaluar,
                                               Button btnLimpiar, Label lblResultadoGlobal) {
        if (indiceActual >= filas.size()) {
            btnEvaluar.setDisable(false);
            btnLimpiar.setDisable(false);
            lienzo.restaurarColoresNodos();
            lblResultadoGlobal.setText("Simulación finalizada para todas las cadenas.");
            lblResultadoGlobal.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
            return;
        }

        FilaCadena fila = filas.get(indiceActual);
        String entrada = fila.campoTexto.getText().trim();
        List<Estado> camino = automata.obtenerCaminoRecorrido(entrada);
        boolean esAceptada = automata.evaluarCadena(entrada);

        TipoAutomata tipoActual = automata.determinarTipoAutomata();
        String etiquetaTipo = " [" + tipoActual.name() + "]";

        lblResultadoGlobal.setText("Evaluando Cadena " + (fila.indice + 1) + " ('" + entrada + "')...");
        lblResultadoGlobal.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");

        Timeline timeline = new Timeline();

        for (int i = 0; i < camino.size(); i++) {
            final int pos = i;
            KeyFrame frame = new KeyFrame(Duration.seconds(i * velocidad), evt -> {
                lienzo.restaurarColoresNodos();
                Estado actual = camino.get(pos);
                NodoEstado nodo = lienzo.getNodo(actual);
                if (nodo != null) {
                    nodo.resaltarAnimacion(fila.colorAsignado);
                }
            });
            timeline.getKeyFrames().add(frame);
        }

        timeline.setOnFinished(evt -> {
            if (!camino.isEmpty()) {
                Estado ultimo = camino.get(camino.size() - 1);
                NodoEstado nodoFinal = lienzo.getNodo(ultimo);

                if (esAceptada && nodoFinal != null) {
                    fila.lblResultado.setText("✔ Aceptada" + etiquetaTipo);
                    fila.lblResultado.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
                } else {
                    fila.lblResultado.setText("✘ Rechazada" + etiquetaTipo);
                    fila.lblResultado.setStyle("-fx-text-fill: #DC3545; -fx-font-weight: bold;");
                }
            }

            ejecutarSimulacionSecuencial(automata, lienzo, filas, indiceActual + 1, velocidad, btnEvaluar, btnLimpiar, lblResultadoGlobal);
        });

        timeline.play();
    }

    private void actualizarTipoAutomata(Automata automata, Label lblTipo) {
        TipoAutomata tipo = automata.determinarTipoAutomata();
        lblTipo.setText("Tipo: " + tipo.name());
        if (tipo == TipoAutomata.AFN) {
            lblTipo.setStyle("-fx-background-color: #6F42C1; -fx-text-fill: white; -fx-padding: 4 8 4 8; -fx-background-radius: 4;");
        } else {
            lblTipo.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-padding: 4 8 4 8; -fx-background-radius: 4;");
        }
    }

    private void mostrarVentanaTablaTransiciones(Automata automata) {
        Stage modalStage = new Stage();
        modalStage.setTitle("Tabla de Transiciones");

        Set<String> alfabeto = new HashSet<>(automata.getAlfabeto());
        alfabeto.add("ε");
        List<String> columnas = new ArrayList<>(alfabeto);
        Collections.sort(columnas);

        TableView<Map<String, String>> tabla = new TableView<>();

        TableColumn<Map<String, String>, String> colEstado = new TableColumn<>("Estado (Q)");
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("Estado")));
        tabla.getColumns().add(colEstado);

        for (String simbolo : columnas) {
            TableColumn<Map<String, String>, String> colSimbolo = new TableColumn<>("Transición (" + simbolo + ")");
            colSimbolo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(simbolo)));
            tabla.getColumns().add(colSimbolo);
        }

        for (Estado e : automata.getEstados()) {
            Map<String, String> fila = new HashMap<>();
            String prefix = "";
            if (e.esInicial()) prefix += "→ ";
            if (e.esAceptacion()) prefix += "* ";
            fila.put("Estado", prefix + e.getNombre());

            for (String simbolo : columnas) {
                Set<String> destinos = new HashSet<>();
                for (Transicion t : automata.getTransiciones()) {
                    if (t.getOrigen().equals(e) && t.getSimbolo().equals(simbolo)) {
                        destinos.add(t.getDestino().getNombre());
                    }
                }
                fila.put(simbolo, destinos.isEmpty() ? "-" : String.join(", ", destinos));
            }
            tabla.getItems().add(fila);
        }

        VBox layout = new VBox(10, new Label("Tabla de Transiciones del Autómata"), tabla);
        layout.setPadding(new Insets(15));
        modalStage.setScene(new Scene(layout, 520, 350));
        modalStage.show();
    }

    private Map<String, List<String>> generarEjemplosCadenas(Automata automata) {
        Map<String, List<String>> ejemplos = new HashMap<>();
        List<String> aceptadas = new ArrayList<>();
        List<String> rechazadas = new ArrayList<>();

        List<String> alfabeto = new ArrayList<>(automata.getAlfabeto());
        if (alfabeto.isEmpty()) {
            alfabeto.add("a");
            alfabeto.add("b");
        }

        List<String> candidatas = new ArrayList<>();
        candidatas.add("");
        for (String s1 : alfabeto) {
            candidatas.add(s1);
            for (String s2 : alfabeto) {
                candidatas.add(s1 + s2);
                for (String s3 : alfabeto) {
                    candidatas.add(s1 + s2 + s3);
                }
            }
        }

        for (String c : candidatas) {
            if (automata.evaluarCadena(c)) {
                if (aceptadas.size() < 3 && !aceptadas.contains(c)) {
                    aceptadas.add(c);
                }
            } else {
                if (rechazadas.size() < 3 && !rechazadas.contains(c)) {
                    rechazadas.add(c);
                }
            }
        }

        ejemplos.put("Aceptadas", aceptadas);
        ejemplos.put("Rechazadas", rechazadas);
        return ejemplos;
    }

    private String convertirDTOaJSON(AutomataDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"estados\": [\n");
        for (int i = 0; i < dto.estados.size(); i++) {
            AutomataDTO.EstadoDTO e = dto.estados.get(i);
            sb.append(String.format("    {\"nombre\": \"%s\", \"x\": %.2f, \"y\": %.2f, \"esInicial\": %b, \"esAceptacion\": %b}",
                    e.nombre, e.x, e.y, e.esInicial, e.esAceptacion));
            if (i < dto.estados.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("  \"transiciones\": [\n");
        for (int i = 0; i < dto.transiciones.size(); i++) {
            AutomataDTO.TransicionDTO t = dto.transiciones.get(i);
            sb.append(String.format("    {\"origen\": \"%s\", \"destino\": \"%s\", \"simbolo\": \"%s\"}",
                    t.origen, t.destino, t.simbolo));
            if (i < dto.transiciones.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    private AutomataDTO convertirJSONaDTO(String json) {
        AutomataDTO dto = new AutomataDTO();

        int indexEstados = json.indexOf("\"estados\":");
        int indexTransiciones = json.indexOf("\"transiciones\":");

        if (indexEstados != -1 && indexTransiciones != -1) {
            String subEstados = json.substring(indexEstados, indexTransiciones);
            String[] bloquesEstados = subEstados.split("\\}");
            for (String b : bloquesEstados) {
                if (b.contains("\"nombre\"")) {
                    AutomataDTO.EstadoDTO e = new AutomataDTO.EstadoDTO();
                    e.nombre = extraerValor(b, "nombre");
                    e.x = Double.parseDouble(extraerValor(b, "x"));
                    e.y = Double.parseDouble(extraerValor(b, "y"));
                    e.esInicial = Boolean.parseBoolean(extraerValor(b, "esInicial"));
                    e.esAceptacion = Boolean.parseBoolean(extraerValor(b, "esAceptacion"));
                    dto.estados.add(e);
                }
            }

            String subTransiciones = json.substring(indexTransiciones);
            String[] bloquesTrans = subTransiciones.split("\\}");
            for (String b : bloquesTrans) {
                if (b.contains("\"origen\"")) {
                    AutomataDTO.TransicionDTO t = new AutomataDTO.TransicionDTO();
                    t.origen = extraerValor(b, "origen");
                    t.destino = extraerValor(b, "destino");
                    t.simbolo = extraerValor(b, "simbolo");
                    dto.transiciones.add(t);
                }
            }
        }
        return dto;
    }

    private String extraerValor(String bloque, String clave) {
        int posClave = bloque.indexOf("\"" + clave + "\"");
        if (posClave == -1) return "";
        int posDosPuntos = bloque.indexOf(":", posClave);
        int posComa = bloque.indexOf(",", posDosPuntos);
        if (posComa == -1) posComa = bloque.indexOf("\n", posDosPuntos);
        if (posComa == -1) posComa = bloque.length();

        String val = bloque.substring(posDosPuntos + 1, posComa).trim();
        return val.replace("\"", "").replace("}", "").replace("]", "").trim();
    }

    private void mostrarAviso(String titulo, String encabezado, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}