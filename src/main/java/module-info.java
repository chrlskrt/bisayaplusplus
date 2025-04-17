module com.example.bisayaplusplus {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bisayaplusplus to javafx.fxml;
    exports com.example.bisayaplusplus;
    exports com.example.bisayaplusplus.parser;
    exports com.example.bisayaplusplus.exception;
    exports com.example.bisayaplusplus.lexer;
    exports com.example.bisayaplusplus.interpreter;
    opens com.example.bisayaplusplus.interpreter to javafx.fxml;
}