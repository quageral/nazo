package com.nazo.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:5173",
    "http://127.0.0.1:5173" }, allowCredentials = "true", allowedHeaders = "*")
public class EasterController {

  private static final Map<String, EasterEgg> easterEggs = new HashMap<>();
  // 用户已收集的彩蛋存储 (实际项目中应使用数据库)
  private static final Map<String, List<CollectedEasterEgg>> userCollectedEggs = new HashMap<>();

  static {
    easterEggs.put("color-easter-egg", new EasterEgg("2001", "这一关是2001年，因为0-1岁是婴儿的视觉发育期。（强行解释）"));
    easterEggs.put("wordle-easter-egg", new EasterEgg("2020", "这一关是2020年？记得是纽约时报上的游戏，在扫雷之前，貌似这是摸鱼首选。笑死"));
    easterEggs.put("minesweeper-easter-egg",
        new EasterEgg("2021", "这一关是2021年？大学期间摸鱼总是玩扫雷，无可置疑的摸鱼首选。神中神！（虽然我现在还没有通关过一次高级。。。）"));
    easterEggs.put("tetris-easter-egg", new EasterEgg("2004", "这一关是2004年。3岁就开始玩俄罗斯方块？？？（只是因为刚好这是俄罗斯方块20周年。。。）"));
    easterEggs.put("number-sequences-easter-egg",
        new EasterEgg("2007", "这一关是2007年。上小学了，要好好学数学。这样以后在高中，才可以和袁老师一起waaan数学"));
    easterEggs.put("correlation-easter-egg", new EasterEgg("2019", "这一关是2019年。学经济的同学，需要培养较高的数据敏感性（???）"));

    easterEggs.put("friends-easter-egg", new EasterEgg("2023", "这一关是2023年。十季到现在我还是只看了七季。。。"));
    easterEggs.put("minecraft-easter-egg", new EasterEgg("2014", "这一关是2014年。梦回MineCraft时间！"));
    easterEggs.put("cat-easter-egg", new EasterEgg("9999", "其实这里没有彩蛋，但是可以看看猫咪的图片，毕竟猫咪是人类的好朋友。"));
    easterEggs.put("geography-easter-egg", new EasterEgg("2017", "这一关是2017年。文理分班！这不得拿满分？如果现在新高考，我估计会选地理"));
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

      // 创建收集记录
      CollectedEasterEgg collectedEgg = new CollectedEasterEgg(
          easterEggId,
          easterEgg.getTime(),
          easterEgg.getMessage(),
          System.currentTimeMillis());

      // 保存到用户的收集列表中
      userCollectedEggs.computeIfAbsent(username, k -> new ArrayList<>()).add(collectedEgg);

      response.put("success", true);
      response.put("time", easterEgg.getTime());
      response.put("message", easterEgg.getMessage());
      response.put("collectedAt", collectedEgg.getCollectedAt());
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
    collected.sort((a, b) -> Long.compare(b.getCollectedAt(), a.getCollectedAt()));

    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("eggs", collected);

    return ResponseEntity.ok(response);
  }

  @Data
  @AllArgsConstructor
  private static class EasterEgg {
    private String time;
    private String message;
  }

  @Data
  @AllArgsConstructor
  private static class CollectedEasterEgg {
    private String easterEggId;
    private String time;
    private String message;
    private long collectedAt;
  }
}