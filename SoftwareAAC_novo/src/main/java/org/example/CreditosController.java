package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class CreditosController {

    @FXML
    private Button btn_voltar;

    @FXML
    void voltar(ActionEvent event) throws IOException {
        Launcher.mudarTela(event, "inicial.fxml");
    }

}
