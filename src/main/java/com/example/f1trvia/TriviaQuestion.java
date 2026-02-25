package com.example.f1trvia;

import java.util.List;

public class TriviaQuestion {
    private String questionText;
    private List<String> choices;
    private int correctIndex;

    public TriviaQuestion(String questionText, List<String> choices, int correctIndex) {
        this.questionText = questionText;
        this.choices = choices;
        this.correctIndex = correctIndex;
    }

    public String getQuestionText() { return questionText; }
    public List<String> getChoices() { return choices; }
    public int getCorrectIndex() { return correctIndex; }
    public String getCorrectAnswer() { return choices.get(correctIndex); }
}