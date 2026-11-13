module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires ollama4j;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;

    opens org.example to javafx.fxml;
    exports org.example;
}
