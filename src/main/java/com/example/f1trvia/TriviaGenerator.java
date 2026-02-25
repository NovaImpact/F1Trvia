package com.example.f1trvia;

import java.util.*;

public class TriviaGenerator {

    private List<F1ApiData> fp1Data;
    private List<F1ApiData> fp2Data;
    private List<F1ApiData> fp3Data;
    private Random random = new Random();

    public TriviaGenerator(List<F1ApiData> fp1, List<F1ApiData> fp2, List<F1ApiData> fp3) {
        this.fp1Data = fp1;
        this.fp2Data = fp2;
        this.fp3Data = fp3;
    }

    public List<TriviaQuestion> generateQuestions(int count) {
        List<TriviaQuestion> pool = new ArrayList<>();

        addQuestionsForSession(pool, fp1Data, "FP1");
        addQuestionsForSession(pool, fp2Data, "FP2");
        addQuestionsForSession(pool, fp3Data, "FP3");

        Collections.shuffle(pool);
        if (pool.size() > count) {
            return pool.subList(0, count);
        }
        return pool;
    }

    private void addQuestionsForSession(List<TriviaQuestion> pool, List<F1ApiData> data, String sessionName) {
        if (data == null || data.size() < 4) return;

        TriviaQuestion q1 = makeDriverForPositionQuestion(data, sessionName, "1", "first");
        if (q1 != null) pool.add(q1);

        TriviaQuestion qLast = makeDriverForPositionQuestion(data, sessionName,
                String.valueOf(data.size()), "last");
        if (qLast != null) pool.add(qLast);

        TriviaQuestion q3 = makeTeamForDriverQuestion(data, sessionName);
        if (q3 != null) pool.add(q3);

        TriviaQuestion q4 = makePositionForDriverQuestion(data, sessionName);
        if (q4 != null) pool.add(q4);

        TriviaQuestion q5 = makeDriverForTeamQuestion(data, sessionName);
        if (q5 != null) pool.add(q5);

        TriviaQuestion q6 = makeFastestLapQuestion(data, sessionName);
        if (q6 != null) pool.add(q6);

        TriviaQuestion q7 = makeDriverCountQuestion(data, sessionName);
        if (q7 != null) pool.add(q7);
    }

    private TriviaQuestion makeDriverForPositionQuestion(List<F1ApiData> data,
                                                         String session, String pos, String posLabel) {
        F1ApiData correct = getByPosition(data, pos);
        if (correct == null) return null;

        List<String> choices = new ArrayList<>();
        choices.add(correct.getDriverName());
        addRandomDrivers(choices, data, correct.getDriverName(), 3);
        Collections.shuffle(choices);

        int correctIndex = choices.indexOf(correct.getDriverName());
        String question = "Who finished " + posLabel + " (P" + pos + ") in " + session + "?";
        return new TriviaQuestion(question, choices, correctIndex);
    }

    private TriviaQuestion makeTeamForDriverQuestion(List<F1ApiData> data, String session) {
        F1ApiData driver = data.get(random.nextInt(data.size()));
        if (driver.getTeam().equals("N/A")) return null;

        List<String> choices = new ArrayList<>();
        choices.add(driver.getTeam());
        addRandomTeams(choices, data, driver.getTeam(), 3);
        Collections.shuffle(choices);

        int correctIndex = choices.indexOf(driver.getTeam());
        String question = "Which team did " + driver.getDriverName() + " race for in " + session + "?";
        return new TriviaQuestion(question, choices, correctIndex);
    }

    private TriviaQuestion makePositionForDriverQuestion(List<F1ApiData> data, String session) {
        F1ApiData driver = data.get(random.nextInt(data.size()));
        if (driver.getPosition().equals("N/A")) return null;

        Set<String> posSet = new LinkedHashSet<>();
        posSet.add(driver.getPosition());
        List<Integer> allPos = new ArrayList<>();
        for (int i = 1; i <= data.size(); i++) allPos.add(i);
        Collections.shuffle(allPos);
        for (int p : allPos) {
            String ps = String.valueOf(p);
            if (!ps.equals(driver.getPosition())) posSet.add(ps);
            if (posSet.size() == 4) break;
        }

        List<String> choices = new ArrayList<>(posSet);
        choices.sort(Comparator.comparingInt(s -> Integer.parseInt(s.replaceAll("[^0-9]", "0"))));
        int correctIndex = choices.indexOf(driver.getPosition());
        if (correctIndex == -1) return null;

        String question = "What position did " + driver.getDriverName() + " finish in " + session + "?";
        return new TriviaQuestion(question, choices, correctIndex);
    }

    private TriviaQuestion makeDriverForTeamQuestion(List<F1ApiData> data, String session) {
        F1ApiData entry = data.get(random.nextInt(data.size()));
        if (entry.getTeam().equals("N/A")) return null;

        List<String> choices = new ArrayList<>();
        choices.add(entry.getDriverName());
        addRandomDrivers(choices, data, entry.getDriverName(), 3);
        Collections.shuffle(choices);

        int correctIndex = choices.indexOf(entry.getDriverName());
        String question = "Which driver raced for " + entry.getTeam() + " in " + session + "?";
        return new TriviaQuestion(question, choices, correctIndex);
    }

    private TriviaQuestion makeFastestLapQuestion(List<F1ApiData> data, String session) {
        F1ApiData p1 = getByPosition(data, "1");
        if (p1 == null) return null;

        List<String> choices = new ArrayList<>();
        choices.add(p1.getDriverName());
        addRandomDrivers(choices, data, p1.getDriverName(), 3);
        Collections.shuffle(choices);

        int correctIndex = choices.indexOf(p1.getDriverName());
        String question = "Who set the fastest lap time in " + session + "?";
        return new TriviaQuestion(question, choices, correctIndex);
    }

    private TriviaQuestion makeDriverCountQuestion(List<F1ApiData> data, String session) {
        int correct = data.size();
        Set<Integer> nums = new LinkedHashSet<>();
        nums.add(correct);
        int[] offsets = {-2, -1, 1, 2, 3, -3};
        for (int off : offsets) {
            int candidate = correct + off;
            if (candidate > 0) nums.add(candidate);
            if (nums.size() == 4) break;
        }

        List<String> choices = new ArrayList<>();
        for (int n : nums) choices.add(String.valueOf(n));
        Collections.sort(choices, Comparator.comparingInt(Integer::parseInt));

        int correctIndex = choices.indexOf(String.valueOf(correct));
        String question = "How many drivers competed in " + session + "?";
        return new TriviaQuestion(question, choices, correctIndex);
    }



    private F1ApiData getByPosition(List<F1ApiData> data, String position) {
        for (F1ApiData d : data) {
            if (d.getPosition().trim().equals(position.trim())) return d;
        }
        try {
            int idx = Integer.parseInt(position) - 1;
            if (idx >= 0 && idx < data.size()) return data.get(idx);
        } catch (NumberFormatException e) { /* ignore */ }
        return null;
    }

    private void addRandomDrivers(List<String> list, List<F1ApiData> data, String exclude, int count) {
        List<F1ApiData> shuffled = new ArrayList<>(data);
        Collections.shuffle(shuffled);
        for (F1ApiData d : shuffled) {
            if (!d.getDriverName().equals(exclude) && !list.contains(d.getDriverName())) {
                list.add(d.getDriverName());
                if (list.size() - 1 == count) break;
            }
        }
    }

    private void addRandomTeams(List<String> list, List<F1ApiData> data, String exclude, int count) {
        List<F1ApiData> shuffled = new ArrayList<>(data);
        Collections.shuffle(shuffled);
        for (F1ApiData d : shuffled) {
            if (!d.getTeam().equals(exclude) && !list.contains(d.getTeam())) {
                list.add(d.getTeam());
                if (list.size() - 1 == count) break;
            }
        }
    }
}