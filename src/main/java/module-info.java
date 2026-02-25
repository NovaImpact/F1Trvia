module com.example.f1trvia {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens com.example.f1trvia to javafx.fxml;
    exports com.example.f1trvia;
}