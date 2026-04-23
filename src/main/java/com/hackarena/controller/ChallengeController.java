package com.hackarena.controller;

import com.hackarena.model.Challenge;
import com.hackarena.model.Submission;
import com.hackarena.service.ScoreService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChallengeController {

    private List<Challenge> challenges;

    @Autowired
    private ScoreService scoreService;

    public ChallengeController() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/data/challenges.json");
        challenges = mapper.readValue(is, new TypeReference<List<Challenge>>() {});
        System.out.println("[HackArena] Loaded " + challenges.size() + " challenges.");
    }

    // ─── GET ALL CHALLENGES (no flag exposed) ───
    @GetMapping("/challenges")
    public List<Map<String, Object>> getChallenges() {
        List<Map<String, Object>> safe = new ArrayList<>();
        for (Challenge c : challenges) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          c.getId());
            m.put("name",        c.getName());
            m.put("category",    c.getCategory());
            m.put("description", c.getDescription());
            m.put("points",      c.getPoints());
            m.put("difficulty",  c.getDifficulty());
            m.put("solves",      c.getSolves());
            safe.add(m);
        }
        return safe;
    }

    // ─── GET HINT ───
    @GetMapping("/challenges/{id}/hint")
    public Map<String, Object> getHint(@PathVariable int id,
                                       @RequestParam String username) {
        Map<String, Object> resp = new HashMap<>();
        Optional<Challenge> found = challenges.stream()
            .filter(c -> c.getId() == id).findFirst();

        if (found.isEmpty()) {
            resp.put("error", "Challenge not found");
            return resp;
        }

        // Deduct 25 points for hint (minimum 0)
        int current = scoreService.getScore(username);
        if (current > 0) {
            scoreService.addScore(username, -1, -25); // -1 = hint penalty ID
        }

        resp.put("hint",       found.get().getHint());
        resp.put("penalty",    -25);
        resp.put("totalScore", scoreService.getScore(username));
        return resp;
    }

    // ─── SUBMIT FLAG ───
    @PostMapping("/submit")
    public Map<String, Object> submitFlag(@RequestBody Submission sub) {
        Map<String, Object> resp = new HashMap<>();
        String user = sub.getUsername().trim();
        int    id   = sub.getChallengeId();
        String flag = sub.getFlag().trim();

        // Find challenge
        Optional<Challenge> found = challenges.stream()
            .filter(c -> c.getId() == id).findFirst();

        if (found.isEmpty()) {
            resp.put("correct", false);
            resp.put("message", "Challenge not found.");
            return resp;
        }

        Challenge chall = found.get();

        // Already solved?
        if (scoreService.hasSolved(user, id)) {
            resp.put("correct", false);
            resp.put("message", "Already solved! No extra points.");
            return resp;
        }

        // Check flag
        if (flag.equalsIgnoreCase(chall.getFlag())) {
            scoreService.addScore(user, id, chall.getPoints());
            chall.setSolves(chall.getSolves() + 1);

            resp.put("correct",    true);
            resp.put("message",    "Correct flag! +" + chall.getPoints() + " pts");
            resp.put("points",     chall.getPoints());
            resp.put("totalScore", scoreService.getScore(user));
        } else {
            resp.put("correct", false);
            resp.put("message", "Wrong flag. Try again!");
        }
        return resp;
    }

    // ─── LEADERBOARD ───
    @GetMapping("/leaderboard")
    public List<Map<String, Object>> getLeaderboard() {
        return scoreService.getLeaderboard();
    }

    // ─── PLAYER SCORE ───
    @GetMapping("/score/{username}")
    public Map<String, Object> getScore(@PathVariable String username) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("username", username);
        resp.put("score",    scoreService.getScore(username));
        resp.put("solves",   scoreService.getSolved(username).size());
        resp.put("solved",   scoreService.getSolved(username));
        return resp;
    }
}