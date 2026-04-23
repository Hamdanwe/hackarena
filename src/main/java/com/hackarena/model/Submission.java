package com.hackarena.model;

public class Submission {
    private String username;
    private int challengeId;
    private String flag;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getChallengeId() { return challengeId; }
    public void setChallengeId(int challengeId) { this.challengeId = challengeId; }

    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
}