package com.quiz.javaquiz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

public class QuizApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(QuizApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1020, 740);
        //get scrollpane from fxml by id
        ScrollPane scrollPane = (ScrollPane) scene.lookup("#questionsPane");
        //get button from fxml by id
        Button resultsBtn = (Button) scene.lookup("#resultsBtn");
        Button exitBtn = (Button) scene.lookup("#exitBtn");
        Button submitBtn = (Button) scene.lookup("#submitBtn");
        TextField txtName = (TextField) scene.lookup("#txtName");

        //register event handlers
        resultsBtn.setOnAction(e -> {
            //load results-view fxml
            try {
                FXMLLoader resultsLoader = new FXMLLoader(QuizApplication.class.getResource("results-view.fxml"));
                Scene resultsScene = new Scene(resultsLoader.load(), 1020, 740);
                //get results table from fxml by id
                ScrollPane resultsPane = (ScrollPane) resultsScene.lookup("#resultsPane");

                VBox resultsBox = new VBox();
                resultsBox.getChildren().add(new Label("Name\t\t\t\tAnswers\t\t\t\tTotal Score"));
                try {
                    String results = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("results.txt")));
                    String[] resultsArray = results.split(System.lineSeparator());
                    if (resultsArray.length > 0) {
                        for (String result : resultsArray) {
                            System.out.println(result);
                            String[] resultArray = result.split(" ");
                            if (resultArray.length > 0) {
                                resultsBox.getChildren().add(new Label(resultArray[0] + "\t\t\t\t" + resultArray[1] + "\t\t\t\t" + resultArray[2]));
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                resultsPane.setContent(resultsBox);
                Button btnBack = (Button) resultsScene.lookup("#backBtn");
                btnBack.setOnAction(e1 -> stage.setScene(scene));
                //show results scene
                stage.setScene(resultsScene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        exitBtn.setOnAction(e -> {
            //confirm exit
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                System.exit(0);
            }
        });
        submitBtn.setOnAction(e -> {
            String studentName = txtName.getText();
            if (studentName.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter your name", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            //confirm submit
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to submit?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                //get all questions
                VBox questions = (VBox) scrollPane.getContent();
                int totalPoints = 0;
                StringBuilder chosenAnswers = new StringBuilder();
                //iterate through all questions
                String[] choice = new String[]{"A", "B", "C", "D"};
                ToggleGroup currentGroup = null;
                int choiceIndex = 0;
                String correctAnswerText = "";
                for (int j = 0; j < questions.getChildren().size(); j++) {
                    RadioButton answer = null;


                    //get answer
                    try {
                        answer = (RadioButton) questions.getChildren().get(j);
                        answer.getToggleGroup();

                        if (currentGroup == null) {
                            currentGroup = answer.getToggleGroup();
                        } else if (currentGroup != answer.getToggleGroup()) {
                            currentGroup = answer.getToggleGroup();
                            choiceIndex = 0;
                        }
                        if (!answer.isVisible()) {
                            //get correct answer text
                            correctAnswerText = answer.getText();
                            continue;
                        } else {
                            choiceIndex++;
                        }
                    } catch (Exception ex) {
                        continue;
                    }

                    //check if answer is selected
                    if (answer.isSelected()) {
                        //get answer text
                        String selectedAnswerText = answer.getText();
                        //get answer value
                        if (selectedAnswerText.equals(correctAnswerText)) {
                            totalPoints += 20;
                        } else {
                            totalPoints -= 5;
                        }
                        choiceIndex = Math.min(choiceIndex, 3);
                        chosenAnswers.append(choice[choiceIndex]);
                    }

                }
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION, "Your score is " + totalPoints, ButtonType.OK);

                //write results to file
                FileWriter fileWriter = null;
                String newResult = studentName + " " + chosenAnswers + " " + totalPoints + System.lineSeparator();
                try {
                    //check if file exists
                    if (new java.io.File("results.txt").exists()) {
                        //read file
                        String existingResults = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("results.txt")));
                        //write to file
                        fileWriter = new FileWriter("results.txt");
                        fileWriter.write(existingResults + newResult);
                        fileWriter.close();
                    } else {
                        //write to file
                        fileWriter = new FileWriter("results.txt");
                        fileWriter.write(newResult);
                        fileWriter.close();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        //add questions to the scrollpane
        try {
            addQuestionsToPane(scrollPane);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //set scrollpane to fit the window
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        stage.setTitle("Java Quiz");
        stage.setScene(scene);
        stage.show();
    }

    public void addQuestionsToPane(ScrollPane scrollPane) throws Exception {
        //add questions to the scrollpane
//get questions from file with answers
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("C:\\JAVA\\Assignment2\\src\\main\\java\\com\\quiz\\javaquiz\\exam.txt"));//ENTER YOUR FILE PATH HERE
            JSONArray questions = (JSONArray) obj;
            //shuffle questions
            Collections.shuffle(questions);
            int counter = 1;
            VBox questionBox = new VBox();
            for (Object subject : questions) {
                if (counter > 5) {
                    break;
                }
                JSONObject question = (JSONObject) subject;
                String questionText = (String) question.get("question");
                String answer = (String) question.get("answer");
                JSONArray choices = (JSONArray) question.get("choices");
                //label for question text
                Label questionLabel = new Label(counter + ". " + questionText);
                questionBox.getChildren().add(questionLabel);
                //radio buttons for choices
                ToggleGroup toggleGroup = new ToggleGroup();
                //label for answer
                RadioButton correctAnswer = new RadioButton(answer);
                correctAnswer.setVisible(false);
                correctAnswer.setToggleGroup(toggleGroup);
                questionBox.getChildren().add(correctAnswer);
                for (String choice : (Iterable<String>) choices) {
                    RadioButton choiceBox = new RadioButton(choice);
                    choiceBox.setToggleGroup(toggleGroup);
                    questionBox.getChildren().add(choiceBox);
                }

                counter++;
            }
            scrollPane.setContent(questionBox);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}