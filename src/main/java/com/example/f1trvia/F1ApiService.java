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

    public static List<F1ApiData> fetchSession(int round, String session) {
        List<F1ApiData> results = new ArrayList<>();
        String urlStr = "https://f1api.dev/api/2024/" + round + "/" + session;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("HTTP error: " + responseCode + " for " + urlStr);
                return results;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            JSONObject root = new JSONObject(sb.toString());

            // The API wraps results — try common key names
            JSONArray drivers = null;
            if (root.has("drivers")) {
                drivers = root.getJSONArray("drivers");
            } else if (root.has("results")) {
                drivers = root.getJSONArray("results");
            } else if (root.has("fp1")) {
                drivers = root.getJSONArray("fp1");
            } else {
                // Try first array key found
                for (String key : root.keySet()) {
                    if (root.get(key) instanceof JSONArray) {
                        drivers = root.getJSONArray(key);
                        break;
                    }
                }
            }

            if (drivers == null) {
                System.out.println("Could not find results array in response from: " + urlStr);
                return results;
            }

            for (int i = 0; i < drivers.length(); i++) {
                JSONObject d = drivers.getJSONObject(i);

                String position = getStringSafe(d, "position", "classifiedPosition", "pos");
                String driverName = getStringSafe(d, "driver", "driverName", "name", "Driver");
                String team = getStringSafe(d, "team", "constructor", "teamName", "Team");
                String time = getStringSafe(d, "time", "lapTime", "lap_time", "Time");
                String gap = getStringSafe(d, "gap", "interval", "Gap");

                // Handle nested driver objects
                if (d.has("driver") && d.get("driver") instanceof JSONObject) {
                    JSONObject driverObj = d.getJSONObject("driver");
                    driverName = getStringSafe(driverObj, "name", "fullName", "surname", "familyName");
                }
                if (d.has("team") && d.get("team") instanceof JSONObject) {
                    JSONObject teamObj = d.getJSONObject("team");
                    team = getStringSafe(teamObj, "name", "teamName", "constructorName");
                }

                if (!driverName.equals("N/A")) {
                    results.add(new F1ApiData(position, driverName, team, time, gap));
                }
            }

        } catch (Exception e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }

        return results;
    }

    private static String getStringSafe(JSONObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                return obj.get(key).toString();
            }
        }
        return "N/A";
    }
}