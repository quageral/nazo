package com.nazo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GameController {

    // 模拟用户数据（实际项目中应使用数据库）
    private final Map<String, String> users = Map.of(
        "gino", "U1RBUlRUSEVHQU1F",
        "Gino", "U1RBUlRUSEVHQU1F",
        "WJH", "U1RBUlRUSEVHQU1F",
        "Gino Wang", "U1RBUlRUSEVHQU1F",
        "Gino W", "U1RBUlRUSEVHQU1F",
        "你的名字", "U1RBUlRUSEVHQU1F",
        "gino wang", "U1RBUlRUSEVHQU1F",
        "wangjinghong", "U1RBUlRUSEVHQU1F",
        "Wang Jinghong", "U1RBUlRUSEVHQU1F",
        "王景鸿", "U1RBUlRUSEVHQU1F"
        
    );

    // 模拟关卡数据
    private final Map<String, LevelInfo> levels = Map.of(
        "tetris-level-1", new LevelInfo("1", "俄罗斯方块", "消除10行即可通关！移动方块，填满行以消除。", "level-2-uuid-67890"),
        "level-2-uuid-67890", new LevelInfo("2", "逻辑推理", "如果所有的A都是B，所有的B都是C，那么所有的A都是？", "level-3-uuid-abcde")
    );

    // 游戏会话数据（实际项目中应使用数据库或Redis）
    private final Map<String, TetrisSession> tetrisSessions = new HashMap<>();

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        if (users.containsKey(request.getUsername()) && 
            users.get(request.getUsername()).equals(request.getPassword())) {
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", "nazo_token_" + request.getUsername() + "_" + System.currentTimeMillis()); // 生成简单的token
            response.put("nextLevel", "start");
            
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
        LevelInfo level = levels.get(uuid);
        
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

    @PostMapping("/level/{uuid}/complete")
    public ResponseEntity<Map<String, Object>> completeLevel(
            @PathVariable String uuid, 
            @RequestBody Map<String, String> answer) {
        
        LevelInfo level = levels.get(uuid);
        if (level == null) {
            return ResponseEntity.notFound().build();
        }

        // 简单的答案验证逻辑（实际项目中需要更复杂的验证）
        boolean isCorrect = validateAnswer(uuid, answer.get("answer"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", isCorrect);
        
        if (isCorrect) {
            response.put("message", "恭喜通关！");
            response.put("nextLevel", level.getNextLevelUuid());
        } else {
            response.put("message", "答案错误，请重试");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/game/start")
    public ResponseEntity<Map<String, Object>> startGame(@RequestBody Map<String, String> request) {
        String levelUuid = request.get("levelUuid");
        String username = request.get("username");
        String sessionId = UUID.randomUUID().toString();
        
        // 创建游戏会话
        TetrisSession session = new TetrisSession(username, sessionId, levelUuid, System.currentTimeMillis());
        tetrisSessions.put(username+"_"+sessionId, session);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sessionId", sessionId);
        response.put("message", "游戏会话已创建");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/game/complete")
    public ResponseEntity<Map<String, Object>> completeGame(@RequestBody GameCompleteRequest request) {
        TetrisSession session = tetrisSessions.get(request.getUsername()+"_"+request.getSessionId());
        
        Map<String, Object> response = new HashMap<>();
        
        if (session == null) {
            response.put("success", false);
            response.put("message", "无效的游戏会话");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 验证游戏时间（防止作弊）
        long playTime = System.currentTimeMillis() - session.getStartTime();
        if (playTime < 60000) { // 至少需要60秒
            response.put("success", false);
            response.put("message", "游戏时间过短，请认真完成游戏");
            return ResponseEntity.badRequest().body(response);
        }
        
        // 根据不同关卡验证通关条件
        boolean passCondition = validateGameCompletion(request.getLevelUuid(), request);
        
        if (passCondition) {
            // 移除已使用的会话
            tetrisSessions.remove(request.getSessionId());
            
            response.put("success", true);
            response.put("message", "恭喜通关！");
            response.put("nextLevel", getNextLevel(request.getLevelUuid()));
        } else {
            response.put("success", false);
            response.put("message", getFailureMessage(request.getLevelUuid()));
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 验证游戏完成条件
    private boolean validateGameCompletion(String levelUuid, GameCompleteRequest request) {
        switch (levelUuid) {
            case "tetris-level-1":
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
            default:
                return false;
        }
    }
    
    // 获取下一关
    private String getNextLevel(String currentLevelUuid) {
        switch (currentLevelUuid) {
            case "tetris-level-1":
                return "level-2-uuid-67890";
            default:
                return null;
        }
    }
    
    // 获取失败消息
    private String getFailureMessage(String levelUuid) {
        switch (levelUuid) {
            case "tetris-level-1":
                return "未达到通关条件：需要得分超过600分";
            default:
                return "未达到通关条件";
        }
    }

    private boolean validateAnswer(String levelUuid, String answer) {
        // 简单的答案验证逻辑
        switch (levelUuid) {
            case "level-1-uuid-12345":
                return "8".equals(answer); // 斐波那契数列
            case "level-2-uuid-67890":
                return "C".equals(answer) || "c".equals(answer);
            default:
                return false;
        }
    }

    // 内部类
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LevelInfo {
        private String id;
        private String name;
        private String description;
        private String nextLevelUuid;

        public LevelInfo(String id, String name, String description, String nextLevelUuid) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.nextLevelUuid = nextLevelUuid;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getNextLevelUuid() { return nextLevelUuid; }
    }

    public static class TetrisSession {
        private String username;
        private String sessionId;
        private String levelUuid;
        private long startTime;

        public TetrisSession(String username, String sessionId, String levelUuid, long startTime) {
            this.username = username;
            this.sessionId = sessionId;
            this.levelUuid = levelUuid;
            this.startTime = startTime;
        }
        public String getUsername() { return username; }
        public String getSessionId() { return sessionId; }
        public String getLevelUuid() { return levelUuid; }
        public long getStartTime() { return startTime; }
    }

    public static class GameCompleteRequest {
      private String username;
      private String levelUuid;
      private String sessionId;
      private Map<String, Object> data;

      public String getUsername() { return username; }
      public void setUsername(String username) { this.username = username; }
      public String getLevelUuid() { return levelUuid; }
      public void setLevelUuid(String levelUuid) { this.levelUuid = levelUuid; }
      public String getSessionId() { return sessionId; }
      public void setSessionId(String sessionId) { this.sessionId = sessionId; }
      public Map<String, Object> getData() { return data; }
      public void setData(Map<String, Object> data) { this.data = data; }
    }
}