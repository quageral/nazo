package com.nazo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import com.nazo.model.Const;
import com.nazo.model.LevelInfo;

import lombok.Data;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
public class GameController {

  private static final String PASSWORD = "U1RBUlRUSEVHQU1F";

  // 模拟用户数据（实际项目中应使用数据库）
  private static Map<String, String> users = new HashMap<>();
  static {
    users.putAll(Map.of(
        "gino", PASSWORD,
        "Gino", PASSWORD,
        "WJH", PASSWORD,
        "wjh", PASSWORD,
        "Gino Wang", PASSWORD,
        "gino Wang", PASSWORD,
        "gino wang", PASSWORD,
        "Gino W", PASSWORD,
        "ginowang", PASSWORD));
    users.putAll(Map.of(
        "gino w", PASSWORD,
        "Gino wang", PASSWORD,
        "wangjinghong", PASSWORD,
        "Wang Jinghong", PASSWORD,
        "Jinghong Wang", PASSWORD,
        "你的名字", PASSWORD,
        "王景鸿", PASSWORD));
  }

  // 游戏会话数据（实际项目中应使用数据库或Redis）
  private final Map<String, GameSession> gameSessions = new HashMap<>();

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
    if (users.containsKey(request.getUsername()) &&
        users.get(request.getUsername()).equals(request.getPassword())) {

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("message", "登录成功");
      response.put("token", "nazo_token_" + request.getUsername() + "_" + System.currentTimeMillis()); // 生成简单的token
      response.put("nextLevel", Const.LEVEL_1_UUID);

      return ResponseEntity.ok(response);
    } else {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "用户名或密码错误");

      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/level/{uuid}")
  public ResponseEntity<Map<String, Object>> getLevel(@PathVariable String uuid) {
    LevelInfo level = Const.levels.get(uuid);

    if (level == null) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "关卡不存在");
      return ResponseEntity.notFound().build();
    }

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("level", level);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/game/start")
  public ResponseEntity<Map<String, Object>> startGame(@RequestBody Map<String, String> request) {
    String levelUuid = request.get("levelUuid");
    String username = request.get("username");
    String sessionId = UUID.randomUUID().toString();

    // 创建游戏会话
    GameSession session = new GameSession(username, sessionId, levelUuid, System.currentTimeMillis());
    gameSessions.put(username + "_" + sessionId, session);

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("sessionId", sessionId);
    response.put("message", "游戏会话已创建");

    return ResponseEntity.ok(response);
  }

  @PostMapping("/game/complete")
  public ResponseEntity<Map<String, Object>> completeGame(@RequestBody GameCompleteRequest request) {
    System.out.println("Received game complete request:");
    System.out.println("Username: " + request.getUsername());
    System.out.println("LevelUuid: " + request.getLevelUuid());
    System.out.println("SessionId: " + request.getSessionId());
    System.out.println("Data: " + request.getData());
    GameSession session = gameSessions.get(request.getUsername() + "_" + request.getSessionId());

    Map<String, Object> response = new HashMap<>();

    if (session == null) {
      response.put("success", false);
      response.put("message", "无效的游戏会话");
      return ResponseEntity.badRequest().body(response);
    }

    // 验证游戏时间（防止作弊）
    // long playTime = System.currentTimeMillis() - session.getStartTime();
    // if (playTime < 30000) { // 至少需要30秒
    // response.put("success", false);
    // response.put("message", "游戏时间过短，请认真完成游戏");
    // return ResponseEntity.badRequest().body(response);
    // }

    // 根据不同关卡验证通关条件
    boolean passCondition = validateGameCompletion(request.getLevelUuid(), request);

    if (passCondition) {
      // 移除已使用的会话
      gameSessions.remove(request.getSessionId());

      response.put("success", true);
      response.put("message", "恭喜通关！");
      response.put("nextLevel", getNextLevel(request.getLevelUuid()));
    } else {
      response.put("success", false);
      response.put("message", "未达到通关条件");
    }

    return ResponseEntity.ok(response);
  }

  // 验证游戏完成条件
  private boolean validateGameCompletion(String levelUuid, GameCompleteRequest request) {
    // 检查data是否为空
    if (request.getData() == null) {
      System.out.println("Error: request.getData() is null");
      return false;
    }

    switch (levelUuid) {
      case Const.LEVEL_1_UUID:
        // 获取得分，如果不存在或不是数字则返回0
        Object scoreObj = request.getData().get("score");
        int score = 0;
        if (scoreObj instanceof Integer) {
          score = (Integer) scoreObj;
        } else if (scoreObj instanceof String) {
          try {
            score = Integer.parseInt((String) scoreObj);
          } catch (NumberFormatException e) {
            score = 0;
          }
        }
        return score >= 600; // 俄罗斯方块要求得分超过600
      case Const.LEVEL_2_UUID:
        // 看图猜相关率游戏通关条件
        Object gameScoreObj = request.getData().get("score");
        int gameScore = 0;
        if (gameScoreObj instanceof Integer) {
          gameScore = (Integer) gameScoreObj;
        } else if (gameScoreObj instanceof String) {
          try {
            gameScore = Integer.parseInt((String) gameScoreObj);
          } catch (NumberFormatException e) {
            gameScore = 0;
          }
        }
        return gameScore >= 80; // 看图猜相关率要求得分超过80分

      case Const.LEVEL_3_UUID:// 扫雷游戏通关条件
      case Const.LEVEL_4_UUID:// Wordle游戏通关条件
      case Const.LEVEL_5_UUID:// Color游戏通关条件
      case Const.LEVEL_6_UUID: // Number Sequences游戏通关条件
      case Const.LEVEL_7_UUID:// Friends游戏通关条件
      case Const.LEVEL_8_UUID:// Minecraft游戏通关条件
      case Const.LEVEL_9_UUID:// Cat游戏通关条件
      case Const.LEVEL_10_UUID:// 地理知识问答游戏通关条件
        Object resultObj = request.getData().get("gameWon");
        boolean gameWon = false;
        if (resultObj instanceof Boolean) {
          gameWon = (Boolean) resultObj;
        } else if (resultObj instanceof String) {
          gameWon = "true".equals(resultObj);
        }
        return gameWon;

      case Const.CONGRATULATIONS_UUID:// 恭喜通关页面，直接返回true
        return true;

      default:
        return false;
    }
  }

  // 获取下一关
  private String getNextLevel(String currentLevelUuid) {
    return Const.levels.get(currentLevelUuid).getNextLevelUuid();
  }

  // 内部类
  @Data
  @AllArgsConstructor
  public static class LoginRequest {
    private String username;
    private String password;
  }

  @Data
  @AllArgsConstructor
  public static class GameSession {
    private String username;
    private String sessionId;
    private String levelUuid;
    private long startTime;

  }

  @Data
  public static class GameCompleteRequest {
    private String username;
    private String levelUuid;
    private String sessionId;
    private Map<String, Object> data;
  }
}