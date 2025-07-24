package com.nazo.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import com.nazo.model.Const;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:5173",
        "http://127.0.0.1:5173" }, allowCredentials = "true", allowedHeaders = "*")
public class EasterController {

    private static final Map<String, EasterEgg> easterEggs = new HashMap<>();
    // 用户已收集的彩蛋存储
    private static final Map<String, List<CollectedEasterEgg>> userCollectedEggs = new HashMap<>();

    static {
        easterEggs.put("color-easter-egg", new EasterEgg(2001, "color", "这一关是2001年，因为0-1岁是婴儿的视觉发育期。（强行解释）"));
        easterEggs.put("wordle-easter-egg", new EasterEgg(2020, "wordle", "这一关是2020年？记得是纽约时报上的游戏，在扫雷之前，貌似这是摸鱼首选。笑死"));
        easterEggs.put("minesweeper-easter-egg",
                new EasterEgg(2021, "minesweeper", "这一关是2021年？大学期间摸鱼总是玩扫雷，无可置疑的摸鱼首选。神中神！（虽然我现在还没有通关过一次高级。。。）"));
        easterEggs.put("tetris-easter-egg",
                new EasterEgg(2024, "tetris", "这一关是2024年。这一年发生的事可太多了。（并且这一年是俄罗斯方块40周年。😂）"));
        easterEggs.put("number-sequences-easter-egg",
                new EasterEgg(2016, "number-sequences", "这一关是2016年。要好好学数学，这样以后才可以和袁长林一起waaan数学"));
        easterEggs.put("correlation-easter-egg",
                new EasterEgg(2019, "correlation", "这一关是2019年。学经济的同学，需要培养较高的数据敏感性（???）"));

        easterEggs.put("friends-easter-egg", new EasterEgg(2023, "friends", "这一关是2023年。十季到现在我还是只看了七季。。。"));
        easterEggs.put("minecraft-easter-egg", new EasterEgg(2014, "minecraft", "这一关是2014年。梦回MineCraft时间！"));
        easterEggs.put("cat-easter-egg", new EasterEgg(2022, "cat", "这一关是2022年，学校里好多猫(>^ω^<)(>^ω^<)(>^ω^<)"));
        easterEggs.put("geography-easter-egg",
                new EasterEgg(2017, "geography", "这一关是2017年。文理分班！如果现在新高考，我估计会选地理"));

        easterEggs.put("chicken-easter-egg",
                new EasterEgg(2015, "chicken", "这一关是2015年。winner winner, chicken dinner! 梦回荒野行动"));
        easterEggs.put("trollface-easter-egg",
                new EasterEgg(2025, "trollface", "这一关是2025年。距离第一期暴走大事件已经过去12年了。荆轲刺秦王，两条毛腿肩上扛！"));
        easterEggs.put("duck-easter-egg",
                new EasterEgg(2013, "duck", "这一关是2013年。门前大桥下，游过一群鸭。快来快来数一数，24678。所以为什么我会记得这首诗？"));
        easterEggs.put("life-easter-egg", new EasterEgg(2012, "life", "这一关是2012年。开始思考人生的意义！思考宇宙的目的和世界的答案！"));
        easterEggs.put("brainfuck-easter-egg", new EasterEgg(2022, "brainfuck",
                "这一年是2022年。AI元年，实在是记忆犹新，我的毕设要是没有gpt估计完成不了hhh。那一年从copilot开始，3年不到，AI真是日新月异。（本次网站前端代码也得感谢cursor和Claude-4-sonnet）"));
        easterEggs.put("kfcrazythursdayv50", new EasterEgg(2018, "v50", "这一年是2018年。是kfc疯狂星期四元年。恭喜找到最终彩蛋！"));

    }

    // 辅助函数：从cookie或请求头获取用户名
    private String getUsernameFromRequest(HttpServletRequest request) {
        // 首先尝试从cookie获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("nazo_user".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 然后尝试从请求参数获取（作为备选方案）
        String username = request.getParameter("username");
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }

        return "anonymous"; // 默认用户名
    }

    @GetMapping("/easter/{easterEggId}")
    public ResponseEntity<Map<String, Object>> getEaster(
            @PathVariable String easterEggId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        if (easterEggs.containsKey(easterEggId)) {
            EasterEgg easterEgg = easterEggs.get(easterEggId);
            String username = getUsernameFromRequest(request);

            // 获取用户的收集列表
            List<CollectedEasterEgg> userEggs = userCollectedEggs.computeIfAbsent(username, k -> new ArrayList<>());

            // 检查彩蛋是否已经被收集过
            boolean alreadyCollected = userEggs.stream()
                    .anyMatch(egg -> easterEggId.equals(egg.getEasterEggId()));

            if (!alreadyCollected) {
                // 创建收集记录
                CollectedEasterEgg collectedEgg = new CollectedEasterEgg(
                        easterEggId,
                        easterEgg.getTime(),
                        easterEgg.getLevelName(),
                        easterEgg.getMessage(),
                        System.currentTimeMillis());

                // 保存到用户的收集列表中
                userEggs.add(collectedEgg);

                response.put("success", true);
                response.put("time", easterEgg.getTime());
                response.put("message", easterEgg.getMessage());
                response.put("levelName", easterEgg.getLevelName());
                response.put("collectedAt", System.currentTimeMillis());
            } else {
                response.put("success", false);
                response.put("message", "彩蛋已收集过");
            }
        } else {
            response.put("success", false);
            response.put("message", "这里什么也没有");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/easter/collected")
    public ResponseEntity<Map<String, Object>> getCollectedEasterEggs(HttpServletRequest request) {
        String username = getUsernameFromRequest(request);
        System.out.println("获取已收集彩蛋 - 用户名: " + username);

        List<CollectedEasterEgg> collected = userCollectedEggs.getOrDefault(username, new ArrayList<>());
        System.out.println("用户 " + username + " 已收集彩蛋数量: " + collected.size());

        // 按时间排序（最新的在前）
        collected.sort((a, b) -> Long.compare(b.getTime(), a.getTime()));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("eggs", collected);

        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    private static class EasterEgg {
        private long time;
        //
        private String levelName;
        private String message;
    }

    @Data
    @AllArgsConstructor
    private static class CollectedEasterEgg {
        private String easterEggId;
        private long time;
        private String levelName;
        private String message;
        private long collectedAt;
    }
}