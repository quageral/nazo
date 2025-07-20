package com.nazo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class WordleController {

  private final Map<String, WordleSession> wordleSessions = new HashMap<>();

  private static final List<String> wordleWords = new ArrayList<>();

  static {
    try {
      File file = ResourceUtils.getFile("classpath:words.txt");
      List<String> words = Files.readAllLines(file.toPath());
      wordleWords.addAll(words);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to load wordle words", e);
    }
  }

  @PostMapping("/wordle/start")
  public ResponseEntity<Map<String, Object>> startWordle(@RequestBody Map<String, String> request) {
    String username = request.get("username");
    String sessionId = UUID.randomUUID().toString();
    String answer = wordleWords.get(new Random().nextInt(wordleWords.size())).toUpperCase();
    WordleSession session = new WordleSession(username, sessionId, System.currentTimeMillis(), answer, 0);
    wordleSessions.put(sessionId, session);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("sessionId", sessionId);
    response.put("message", "游戏会话已创建");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/wordle/guess")
  public ResponseEntity<Map<String, Object>> guessWordle(@RequestBody Map<String, String> request) {
    String sessionId = request.get("sessionId");
    String guess = request.get("guess");
    WordleSession session = wordleSessions.get(sessionId);
    if (session == null) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "会话不存在"));
    }
    if (guess.length() != 5) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "猜测的单词长度必须为5"));
    }
    if (!guess.matches("[A-Za-z]{5}")) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "猜测的单词必须为5个字母"));
    }
    if (session.getGuessCount() >= 6) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "猜测次数已用完"));
    }
    if (!wordleWords.contains(guess.toLowerCase())) {
      return ResponseEntity.badRequest().body(Map.of("success", false, "message", "猜测的单词不在词库中"));
    }
    session.setGuessCount(session.getGuessCount() + 1);
    String result = checkGuess(guess, session.getAnswer());
    return ResponseEntity.ok(Map.of("success", true, "result", result, "guessCount", session.getGuessCount(), "answer",
        session.getAnswer()));
  }

  private String checkGuess(String guess, String answer) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < 5; i++) {
      if (guess.charAt(i) == answer.charAt(i)) {
        result.append("+");
      } else if (answer.contains(String.valueOf(guess.charAt(i)))) {
        result.append("x");
      } else {
        result.append("-");
      }
    }
    return result.toString();
  }

  @Data
  @AllArgsConstructor
  private static class WordleSession {
    private String username;
    private String sessionId;
    private long startTime;
    private String answer;
    private int guessCount;
  }
}
