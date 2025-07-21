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
@CrossOrigin(origins = "*")
public class PuzzleController {

    private static final Map<String, String> puzzles = new HashMap<>();
    static {
        puzzles.put(Const.PUZZLE_1_UUID, "谜题1谜面");
        puzzles.put(Const.PUZZLE_2_UUID, "谜题2谜面");
        puzzles.put(Const.PUZZLE_3_UUID, "谜题3谜面");
        puzzles.put(Const.PUZZLE_4_UUID, "谜题4谜面");
        puzzles.put(Const.PUZZLE_5_UUID, "谜题5谜面");
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
        switch (puzzleUuid) {
            default:
                return false;
        }
    }
}
