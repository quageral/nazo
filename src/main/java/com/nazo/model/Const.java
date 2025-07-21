package com.nazo.model;

import java.util.Map;

public class Const {
  public static final String LEVEL_1_UUID = "tetris-level-1";
  public static final String LEVEL_2_UUID = "correlation-level-2";
  public static final String LEVEL_3_UUID = "minesweeper-level-3";
  public static final String LEVEL_4_UUID = "wordle-level-4";
  public static final String LEVEL_5_UUID = "color-level-5";

  // 模拟关卡数据
  public static final Map<String, LevelInfo> levels = Map.of(
      LEVEL_1_UUID, new LevelInfo("1", "俄罗斯方块", "达到600分即可通关！PS：游戏可以暂停", LEVEL_2_UUID),
      LEVEL_2_UUID, new LevelInfo("2", "数据相关系数", "观察散点图，猜测数据的相关系数，通关需要超过80分。\nPS: 怎么这么难！我要报警了", LEVEL_3_UUID),
      LEVEL_3_UUID, new LevelInfo("3", "扫雷游戏", "完全遵循经典扫雷规则，成功完成游戏即可通关！", LEVEL_4_UUID),
      LEVEL_4_UUID, new LevelInfo("4", "Wordle", "猜对单词即可通关！\nPS：这个我是真不会，靠 你 了！", LEVEL_5_UUID),
      LEVEL_5_UUID, new LevelInfo("5", "Color", "最终关为第10关。", null));
}
