package com.hackarena.model;

public class Challenge {
    private int id;
    private String name;
    private String category;
    private String description;
    private String hint;
    private int points;
    private String difficulty;
    private String flag;        // never sent to frontend!
    private int solves;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public int getSolves() { return solves; }
    public void setSolves(int solves) { this.solves = solves; }
}