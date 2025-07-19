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
        "admin", "password",
        "player1", "123456"
    );

    // 模拟关卡数据
    private final Map<String, LevelInfo> levels = Map.of(
        "start", new LevelInfo("start", "开始关卡", "欢迎来到Nazo解谜游戏！", "level-1-uuid-12345"),
        "level-1-uuid-12345", new LevelInfo("1", "数字谜题", "找出规律：1, 1, 2, 3, 5, ?", "level-2-uuid-67890"),
        "level-2-uuid-67890", new LevelInfo("2", "逻辑推理", "如果所有的A都是B，所有的B都是C，那么所有的A都是？", "level-3-uuid-abcde")
    );

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        if (users.containsKey(request.getUsername()) && 
            users.get(request.getUsername()).equals(request.getPassword())) {
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", "mock-jwt-token"); // 实际项目中应生成真实JWT
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
}