module com.example.bisayaplusplus {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bisayaplusplus to javafx.fxml;
    exports com.example.bisayaplusplus;
}