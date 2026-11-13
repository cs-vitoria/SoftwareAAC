package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class TelaInicialController {

    @FXML
    private Button btn_creditos;

    @FXML
    private Button btn_iniciar;

    @FXML
    void ir_creditos(ActionEvent event) throws IOException {
        Launcher.mudarTela(event, "creditos.fxml");
    }

    @FXML
    void ir_iniciar(ActionEvent event) throws IOException {
        Launcher.mudarTela(event, "prancha.fxml");
    }

}
