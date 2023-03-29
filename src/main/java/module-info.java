module com.quiz.javaquiz {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;


    opens com.quiz.javaquiz to javafx.fxml;
    exports com.quiz.javaquiz;
}