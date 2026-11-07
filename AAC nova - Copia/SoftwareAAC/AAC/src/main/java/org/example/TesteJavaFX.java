package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TesteJavaFX extends Application {

    @Override
    public void start(Stage stage) {
        Label texto = new Label("JavaFX funcionando!");
        stage.setScene(new Scene(texto, 300, 100));
        stage.setTitle("Teste");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}