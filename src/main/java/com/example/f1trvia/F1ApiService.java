package com.example.f1trvia;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class F1ApiService {

    public static List<F1ApiData> fetchSession(String session) {
        List<F1ApiData> results = new ArrayList<>();
        String urlStr = "https://f1api.dev/api/2024/1/" + session;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                System.out.println("HTTP error: " + conn.getResponseCode());
                return results;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();

            JSONObject root = new JSONObject(sb.toString());
            JSONObject races = root.getJSONObject("races");

            String resultsKey = session + "Results";
            if (!races.has(resultsKey)) {
                System.out.println("Key " + resultsKey + " not found.");
                return results;
            }

            JSONArray drivers = races.getJSONArray(resultsKey);

            for (int i = 0; i < drivers.length(); i++) {
                JSONObject d = drivers.getJSONObject(i);

                String position = String.valueOf(i + 1);

                String driverName = "N/A";
                if (d.has("driver") && d.get("driver") instanceof JSONObject) {
                    JSONObject dObj = d.getJSONObject("driver");
                    String first = dObj.optString("name", "");
                    String last  = dObj.optString("surname", "");
                    driverName = (first + " " + last).trim();
                    if (driverName.isEmpty()) driverName = "N/A";
                }

                String team = "N/A";
                if (d.has("team") && d.get("team") instanceof JSONObject) {
                    JSONObject tObj = d.getJSONObject("team");
                    team = tObj.optString("teamName", "N/A");
                }

                String time = d.optString("time", "N/A");
                String gap  = d.optString("gap", "N/A");

                if (!driverName.equals("N/A")) {
                    results.add(new F1ApiData(position, driverName, team, time, gap));
                }
            }

            System.out.println("Successfully parsed " + results.size() + " drivers for " + session);

        } catch (Exception e) {
            System.out.println("Error fetching " + session + ": " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }
}