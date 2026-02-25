package com.example.f1trvia;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.util.*;

public class HelloController {

    @FXML private VBox menuScreen;
    @FXML private VBox loadingScreen;
    @FXML private VBox quizScreen;
    @FXML private VBox resultScreen;

    @FXML private ComboBox<String> difficultyBox;
    @FXML private Label highScoreLabel;


    @FXML private Label loadingLabel;


    @FXML private Label questionNumberLabel;
    @FXML private Label scoreLabel;
    @FXML private Label questionLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button choiceA;
    @FXML private Button choiceB;
    @FXML private Button choiceC;
    @FXML private Button choiceD;
    @FXML private Label feedbackLabel;
    @FXML private Button nextButton;

    // Result
    @FXML private Label finalScoreLabel;
    @FXML private Label accuracyLabel;
    @FXML private Label resultMessageLabel;
    @FXML private Label leaderboardLabel;

    private List<TriviaQuestion> questions;
    private int currentIndex = 0;
    private int score = 0;
    private int correctCount = 0;
    private boolean answered = false;
    private int totalQuestions = 10;
    private List<Integer> highScores = new ArrayList<>();

    @FXML
    public void initialize() {
        difficultyBox.getItems().addAll("Easy (10 Q)", "Medium (15 Q)", "Hard (20 Q)");
        difficultyBox.setValue("Easy (10 Q)");
        showScreen("menu");
        updateHighScoreLabel();
    }


    @FXML
    private void onStartQuiz() {
        String diff = difficultyBox.getValue();
        if (diff.startsWith("Easy"))        totalQuestions = 10;
        else if (diff.startsWith("Medium")) totalQuestions = 15;
        else                                totalQuestions = 20;

        int round = 1;

        showScreen("loading");
        loadingLabel.setText("Loading 2024 Bahrain GP Practice data...");

        Thread fetchThread = new Thread(() -> {
            List<F1ApiData> fp1 = F1ApiService.fetchSession(round, "fp1");
            List<F1ApiData> fp2 = F1ApiService.fetchSession(round, "fp2");
            List<F1ApiData> fp3 = F1ApiService.fetchSession(round, "fp3");

            Platform.runLater(() -> {
                boolean hasData = !fp1.isEmpty() || !fp2.isEmpty() || !fp3.isEmpty();
                if (!hasData) {
                    fp1.addAll(getDemoData("FP1"));
                    fp2.addAll(getDemoData("FP2"));
                    fp3.addAll(getDemoData("FP3"));
                    showAlert("Note", "Could not reach the F1 API. Using demo data for this session.");
                }

                TriviaGenerator gen = new TriviaGenerator(fp1, fp2, fp3);
                questions = gen.generateQuestions(totalQuestions);

                if (questions.isEmpty()) {
                    showAlert("Error", "Not enough data to generate questions. Please try again.");
                    showScreen("menu");
                    return;
                }

                currentIndex = 0;
                score = 0;
                correctCount = 0;
                showScreen("quiz");
                loadQuestion();
            });
        });
        fetchThread.setDaemon(true);
        fetchThread.start();
    }


    private void loadQuestion() {
        if (currentIndex >= questions.size()) {
            showResult();
            return;
        }

        answered = false;
        feedbackLabel.setText("");
        nextButton.setVisible(false);

        TriviaQuestion q = questions.get(currentIndex);

        questionNumberLabel.setText("Question " + (currentIndex + 1) + " / " + questions.size());
        scoreLabel.setText("Score: " + score);
        progressBar.setProgress((double) currentIndex / questions.size());
        questionLabel.setText(q.getQuestionText());

        List<String> choices = q.getChoices();
        Button[] buttons = {choiceA, choiceB, choiceC, choiceD};

        for (int i = 0; i < 4; i++) {
            if (i < choices.size()) {
                buttons[i].setText(choices.get(i));
                buttons[i].setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white;");
                buttons[i].setDisable(false);
                buttons[i].setVisible(true);
            } else {
                buttons[i].setVisible(false);
            }
        }
    }

    @FXML private void onChoiceA() { checkAnswer(0); }
    @FXML private void onChoiceB() { checkAnswer(1); }
    @FXML private void onChoiceC() { checkAnswer(2); }
    @FXML private void onChoiceD() { checkAnswer(3); }

    private void checkAnswer(int selectedIndex) {
        if (answered) return;
        answered = true;

        TriviaQuestion q = questions.get(currentIndex);
        Button[] buttons = {choiceA, choiceB, choiceC, choiceD};

        for (int i = 0; i < 4; i++) {
            buttons[i].setDisable(true);
        }

        if (selectedIndex == q.getCorrectIndex()) {
            buttons[selectedIndex].setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            feedbackLabel.setText("✅ Correct! +" + getPointsForDifficulty());
            feedbackLabel.setStyle("-fx-text-fill: #27ae60;");
            score += getPointsForDifficulty();
            correctCount++;
        } else {
            buttons[selectedIndex].setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            buttons[q.getCorrectIndex()].setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            feedbackLabel.setText("❌ Wrong! The answer was: " + q.getCorrectAnswer());
            feedbackLabel.setStyle("-fx-text-fill: #e74c3c;");
            score -= getPointsForDifficulty() / 2;
        }

        nextButton.setVisible(true);
    }

    private int getPointsForDifficulty() {
        String diff = difficultyBox.getValue();
        if (diff.startsWith("Easy"))        return 10;
        else if (diff.startsWith("Medium")) return 15;
        else                                return 20;
    }

    @FXML
    private void onNextQuestion() {
        currentIndex++;
        loadQuestion();
    }


    private void showResult() {
        showScreen("result");

        highScores.add(score);
        Collections.sort(highScores, Collections.reverseOrder());
        if (highScores.size() > 5) highScores = highScores.subList(0, 5);

        double accuracy = questions.isEmpty() ? 0 : (double) correctCount / questions.size() * 100;

        finalScoreLabel.setText("Final Score: " + score);
        accuracyLabel.setText(String.format("Accuracy: %.0f%% (%d/%d correct)", accuracy, correctCount, questions.size()));

        if (accuracy >= 80)      resultMessageLabel.setText("🏆 Champion! You really know your F1!");
        else if (accuracy >= 60) resultMessageLabel.setText("🏅 Solid performance! Keep studying!");
        else if (accuracy >= 40) resultMessageLabel.setText("🔧 Not bad, but there's room to improve.");
        else                     resultMessageLabel.setText("💥 Back to the garage for more practice!");

        StringBuilder lb = new StringBuilder("🏁 Top Scores This Session:\n");
        for (int i = 0; i < highScores.size(); i++) {
            lb.append("  ").append(i + 1).append(". ").append(highScores.get(i)).append(" pts\n");
        }
        leaderboardLabel.setText(lb.toString());
        updateHighScoreLabel();
    }

    @FXML
    private void onPlayAgain() {
        showScreen("menu");
    }

    @FXML
    private void onMainMenu() {
        showScreen("menu");
    }



    private void showScreen(String name) {
        menuScreen.setVisible(false);
        loadingScreen.setVisible(false);
        quizScreen.setVisible(false);
        resultScreen.setVisible(false);

        switch (name) {
            case "menu":    menuScreen.setVisible(true); break;
            case "loading": loadingScreen.setVisible(true); break;
            case "quiz":    quizScreen.setVisible(true); break;
            case "result":  resultScreen.setVisible(true); break;
        }
    }

    private void updateHighScoreLabel() {
        if (highScores.isEmpty()) {
            highScoreLabel.setText("High Score: --");
        } else {
            highScoreLabel.setText("High Score: " + highScores.get(0) + " pts");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private List<F1ApiData> getDemoData(String session) {
        List<F1ApiData> list = new ArrayList<>();
        String[][] drivers = {
                {"1","Max Verstappen","Red Bull","1:29.979","--"},
                {"2","Fernando Alonso","Aston Martin","1:30.187","+0.208"},
                {"3","Carlos Sainz","Ferrari","1:30.265","+0.286"},
                {"4","Lewis Hamilton","Mercedes","1:30.401","+0.422"},
                {"5","Charles Leclerc","Ferrari","1:30.512","+0.533"},
                {"6","Sergio Perez","Red Bull","1:30.611","+0.632"},
                {"7","Lance Stroll","Aston Martin","1:30.789","+0.810"},
                {"8","George Russell","Mercedes","1:30.900","+0.921"},
                {"9","Lando Norris","McLaren","1:31.012","+1.033"},
                {"10","Oscar Piastri","McLaren","1:31.120","+1.141"},
                {"11","Esteban Ocon","Alpine","1:31.234","+1.255"},
                {"12","Pierre Gasly","Alpine","1:31.345","+1.366"},
                {"13","Valtteri Bottas","Alfa Romeo","1:31.456","+1.477"},
                {"14","Zhou Guanyu","Alfa Romeo","1:31.567","+1.588"},
                {"15","Yuki Tsunoda","AlphaTauri","1:31.678","+1.699"},
                {"16","Daniel Ricciardo","AlphaTauri","1:31.789","+1.810"},
                {"17","Kevin Magnussen","Haas","1:31.900","+1.921"},
                {"18","Nico Hulkenberg","Haas","1:32.011","+2.032"},
                {"19","Alex Albon","Williams","1:32.122","+2.143"},
                {"20","Logan Sargeant","Williams","1:32.233","+2.254"}
        };
        for (String[] d : drivers) {
            list.add(new F1ApiData(d[0], d[1], d[2], d[3], d[4]));
        }
        return list;
    }
}