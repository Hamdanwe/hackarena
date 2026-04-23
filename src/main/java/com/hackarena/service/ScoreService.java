package com.hackarena.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class ScoreService {

    // CSV file will be saved in project root folder
    private static final String CSV_FILE = "scores.csv";

    // In-memory storage (Topic 14: HashMap + HashSet)
    private Map<String, Integer>    scores  = new HashMap<>();
    private Map<String, Set<Integer>> solved = new HashMap<>();

    // Load scores from CSV when server starts
    public ScoreService() {
        loadFromCSV();
    }

    // ─── ADD SCORE ───
    public void addScore(String username, int challengeId, int points) {
        scores.merge(username, points, Integer::sum);
        solved.putIfAbsent(username, new HashSet<>());
        solved.get(username).add(challengeId);
        saveToCSV(); // persist immediately
    }

    // ─── GETTERS ───
    public int getScore(String username) {
        return scores.getOrDefault(username, 0);
    }

    public Set<Integer> getSolved(String username) {
        return solved.getOrDefault(username, new HashSet<>());
    }

    public boolean hasSolved(String username, int challengeId) {
        return getSolved(username).contains(challengeId);
    }

    // ─── LEADERBOARD ───
    public List<Map<String, Object>> getLeaderboard() {
        List<Map<String, Object>> board = new ArrayList<>();

        for (String user : scores.keySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank",     0); // filled after sorting
            row.put("username", user);
            row.put("score",    scores.get(user));
            row.put("solves",   solved.getOrDefault(user, new HashSet<>()).size());
            board.add(row);
        }

        // Topic 8: Sort by score descending
        board.sort((a, b) -> (int) b.get("score") - (int) a.get("score"));

        // Assign ranks
        for (int i = 0; i < board.size(); i++) {
            board.get(i).put("rank", i + 1);
        }

        return board;
    }

    // ─── SAVE TO CSV (Topic 11: File I/O) ───
    private void saveToCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {

            // Write header
            writer.println("username,score,solved_ids");

            // Write each user row
            for (String user : scores.keySet()) {
                String solvedIds = String.join("|",
                    solved.getOrDefault(user, new HashSet<>())
                          .stream()
                          .map(String::valueOf)
                          .toList()
                );
                // Format: username,score,1|2|3
                writer.println(user + "," + scores.get(user) + "," + solvedIds);
            }

            System.out.println("[HackArena] Scores saved to " + CSV_FILE);

        } catch (IOException e) {
            System.err.println("[HackArena] Failed to save CSV: " + e.getMessage());
        }
    }

    // ─── LOAD FROM CSV (Topic 11: File I/O) ───
    private void loadFromCSV() {
        File file = new File(CSV_FILE);

        // If no CSV yet, start fresh
        if (!file.exists()) {
            System.out.println("[HackArena] No scores.csv found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                // Skip header row
                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                // Topic 5: String split operations
                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String user  = parts[0].trim();
                int score    = Integer.parseInt(parts[1].trim());
                scores.put(user, score);

                // Load solved challenge IDs
                Set<Integer> solvedIds = new HashSet<>();
                if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                    for (String idStr : parts[2].split("\\|")) {
                        solvedIds.add(Integer.parseInt(idStr.trim()));
                    }
                }
                solved.put(user, solvedIds);
            }

            System.out.println("[HackArena] Loaded " + scores.size() + " users from CSV.");

        } catch (IOException e) {
            System.err.println("[HackArena] Failed to load CSV: " + e.getMessage());
        }
    }
}