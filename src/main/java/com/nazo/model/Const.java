package com.nazo.model;

import java.util.Map;
import java.util.LinkedHashMap;

public class Const {
  public static final String LEVEL_1_UUID = "tetris-level-1";
  public static final String LEVEL_2_UUID = "correlation-level-2";
  public static final String LEVEL_3_UUID = "minesweeper-level-3";
  public static final String LEVEL_4_UUID = "wordle-level-4";
  public static final String LEVEL_5_UUID = "color-level-5";
  public static final String LEVEL_6_UUID = "number-sequences-level-6";
  public static final String LEVEL_7_UUID = "friends-level-7";
  public static final String LEVEL_8_UUID = "minecraft-level-8";
  public static final String LEVEL_9_UUID = "cat-level-9";
  public static final String LEVEL_10_UUID = "geography-level-10";

  public static final String PUZZLE_1_UUID = "puzzle-1";
  public static final String PUZZLE_2_UUID = "puzzle-2";
  public static final String PUZZLE_3_UUID = "puzzle-3";
  public static final String PUZZLE_4_UUID = "puzzle-4";
  public static final String PUZZLE_5_UUID = "puzzle-5";

  // 模拟关卡数据
  public static final Map<String, LevelInfo> levels = new LinkedHashMap<>();
  static {
    levels.put(LEVEL_1_UUID, new LevelInfo("1", "Tetris", "达到600分即可通关！PS：还好可以暂停", LEVEL_2_UUID));
    levels.put(LEVEL_2_UUID, new LevelInfo("2", "Correlation", "观察散点图，猜测数据的相关系数，通关需要超过80分。PS: 怎么这么难！我要报警了", LEVEL_3_UUID));
    levels.put(LEVEL_3_UUID, new LevelInfo("3", "MineSweeper", "这不是简简单单！PS：我测试的时候被雷晕了", LEVEL_4_UUID));
    levels.put(LEVEL_4_UUID, new LevelInfo("4", "Wordle", "猜对单词即可通关！PS：这个是真不会，靠你了！", LEVEL_5_UUID));
    levels.put(LEVEL_5_UUID, new LevelInfo("5", "Color", "最终关为第10关。PS: 眼睛都看花了", LEVEL_6_UUID));
    levels.put(LEVEL_6_UUID, new LevelInfo("6", "Number Sequences", "80分通关。PS：0分也是一种水平", LEVEL_7_UUID));
    levels.put(LEVEL_7_UUID, new LevelInfo("7", "Friends", "25/35！", LEVEL_8_UUID));
    levels.put(LEVEL_8_UUID, new LevelInfo("8", "Minecraft", "18/30！", LEVEL_9_UUID));
    levels.put(LEVEL_9_UUID, new LevelInfo("9", "Cat", "10/16！PS：布偶好摸", LEVEL_10_UUID));
    levels.put(LEVEL_10_UUID, new LevelInfo("10", "Geography", "16/20！PS：文科生不得满分？", null));

    levels.put(PUZZLE_1_UUID, new LevelInfo("1", "谜题1", "谜题1描述", PUZZLE_2_UUID));
    levels.put(PUZZLE_2_UUID, new LevelInfo("2", "谜题2", "谜题2描述", PUZZLE_3_UUID));
    levels.put(PUZZLE_3_UUID, new LevelInfo("3", "谜题3", "谜题3描述", PUZZLE_4_UUID));
    levels.put(PUZZLE_4_UUID, new LevelInfo("4", "谜题4", "谜题4描述", PUZZLE_5_UUID));
    levels.put(PUZZLE_5_UUID, new LevelInfo("5", "谜题5", "谜题5描述", null));
  }

}
