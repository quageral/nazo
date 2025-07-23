package com.nazo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import com.nazo.model.Const;
import com.nazo.model.LevelInfo;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
public class PuzzleController {

    private static final Map<String, String> puzzles = new HashMap<>();
    static {
        puzzles.put(Const.PUZZLE_1_UUID, "&#33618;&#37326;&#34892;&#21160;");
        puzzles.put(Const.PUZZLE_2_UUID,
                "/src/assets/Troll-Face-Meme-PNG.webp");
        puzzles.put(Const.PUZZLE_3_UUID,
                "<h3>Duck</h3>");
        puzzles.put(Const.PUZZLE_4_UUID,
                "<h3>The Answer to the Ultimate Question of Life, The Universe, and Everything =</h3>");

    }

    @GetMapping("/puzzle/{uuid}")
    public ResponseEntity<Map<String, Object>> getPuzzle(@PathVariable String uuid) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "谜题信息");
        response.put("puzzle", puzzles.get(uuid));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/puzzle/{uuid}/complete")
    public ResponseEntity<Map<String, Object>> completePuzzle(
            @PathVariable String uuid,
            @RequestBody Map<String, String> answer) {

        LevelInfo puzzle = Const.levels.get(uuid);
        if (puzzle == null) {
            return ResponseEntity.notFound().build();
        }

        // 简单的答案验证逻辑（实际项目中需要更复杂的验证）
        boolean isCorrect = validateAnswer(uuid, answer.get("answer"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", isCorrect);

        if (isCorrect) {
            response.put("message", "恭喜通关！");
            response.put("nextLevel", puzzle.getNextLevelUuid());
        } else {
            response.put("message", "答案错误，请重试");
        }

        return ResponseEntity.ok(response);
    }

    private boolean validateAnswer(String puzzleUuid, String answer) {
        // 简单的答案验证逻辑
        if (answer == null)
            return false;
        String trimmedAnswer = answer.trim().toLowerCase();

        switch (puzzleUuid) {
            case Const.PUZZLE_1_UUID:
                return "荒野行动".equals(trimmedAnswer);
            case Const.PUZZLE_2_UUID:
                return "trollface".equals(trimmedAnswer) || "暴走漫画".equals(trimmedAnswer)
                        || "暴漫".equals(trimmedAnswer);
            case Const.PUZZLE_3_UUID:
                return trimmedAnswer.contains("门前大桥下");
            case Const.PUZZLE_4_UUID:
                return "42".equals(trimmedAnswer);
            default:
                return false;
        }
    }
}
