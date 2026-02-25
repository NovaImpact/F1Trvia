package com.example.f1trvia;

public class F1ApiData {
    private String position;
    private String driverName;
    private String team;
    private String time;
    private String gap;

    public F1ApiData(String position, String driverName, String team, String time, String gap) {
        this.position = position;
        this.driverName = driverName;
        this.team = team;
        this.time = time;
        this.gap = gap;
    }

    public String getPosition() { return position; }
    public String getDriverName() { return driverName; }
    public String getTeam() { return team; }
    public String getTime() { return time; }
    public String getGap() { return gap; }

    @Override
    public String toString() {
        return position + ". " + driverName + " - " + team + " [" + time + "]";
    }
}