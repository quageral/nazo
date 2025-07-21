package com.nazo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EasterController {

    private static final Map<String, String> easterEggs = new HashMap<>();
    static {
        easterEggs.put("color", "这一关是2001年，因为0-1岁是婴儿的视觉发育发育期。（强行解释）");
        easterEggs.put("wordle", "这一关是2020年？记得是纽约时报上的游戏，在扫雷之前，貌似这是摸鱼首选。笑死");
        easterEggs.put("minesweeper", "这一关是2021年？大学期间摸鱼总是玩扫雷，无可置疑的摸鱼首选。神中神！（虽然我现在还没有通关过一次高级。。。）");
        easterEggs.put("tetris", "这一关是2004年。3岁就开始玩俄罗斯方块？？？（只是因为刚好这是俄罗斯方块20周年。。。）");
    }

    @GetMapping("/easter/{easterEggId}")
    public ResponseEntity<Map<String, Object>> getEaster(@PathVariable String easterEggId) {
        Map<String, Object> response = new HashMap<>();
        if (easterEggs.containsKey(easterEggId)) {
            response.put("success", true);
            response.put("message", easterEggs.get(easterEggId));
        } else {
            response.put("success", false);
            response.put("message", "这里什么也没有");
        }
        return ResponseEntity.ok(response);
    }
}