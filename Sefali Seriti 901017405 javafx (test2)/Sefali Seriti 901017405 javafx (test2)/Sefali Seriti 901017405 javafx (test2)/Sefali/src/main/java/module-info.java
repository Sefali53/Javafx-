module com.example.sefali {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires itextpdf;  // Automatic module name from iTextPDF 5.x
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;

    opens com.example.sefali to javafx.fxml;
    exports com.example.sefali;
}