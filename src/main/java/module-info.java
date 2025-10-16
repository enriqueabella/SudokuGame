module com.project.sudokugame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.project.sudokugame to javafx.fxml;
    exports com.project.sudokugame;
}