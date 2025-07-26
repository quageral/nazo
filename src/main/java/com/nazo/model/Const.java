package com.nazo.model;

import java.util.Map;
import java.util.LinkedHashMap;

public class Const {
  // 关卡名，同步替换前端
  public static final String LEVEL_1_UUID = "dGV0cmlzLWxldmVsLTF2Y3h2ZnI";
  public static final String LEVEL_2_UUID = "bmp1LW5qdS1jb3JyZWxhdGlvbi1sZXZlbC0y";
  public static final String LEVEL_3_UUID = "bWluZXN3ZWVwZXItbGV2ZWwtM2RzYWRzYQ";
  public static final String LEVEL_4_UUID = "d29yZGxlLWxldmVsLTRkYWRhcw";
  public static final String LEVEL_5_UUID = "Y29sb3ItbGV2ZWwtNWNkd3ZjZXY";
  public static final String LEVEL_6_UUID = "bnVtYmVyLXNlcXVlbmNlcy1sZXZlbC02";
  public static final String LEVEL_7_UUID = "ZnJpZW5kcy1sZXZlbC03";
  public static final String LEVEL_8_UUID = "bWluZWNyYWZ0LWxldmVsLTg";
  public static final String LEVEL_9_UUID = "Y2F0LWxldmVsLTl2ZmFj";
  public static final String LEVEL_10_UUID = "Z2VvZ3JhcGh5LWxldmVsLTEw";

  public static final String PUZZLE_1_UUID = "cHV6emxlLTFkYWNkdmU";
  public static final String PUZZLE_2_UUID = "cHV6emxlLTJiZ3J2cg";
  public static final String PUZZLE_3_UUID = "ZGVjZWNlcHV6emxlLTM";
  public static final String PUZZLE_4_UUID = "dmZydmZycHV6emxlLTQ";
  public static final String PUZZLE_5_UUID = "cHV6emxlLTViZ3RidGdidA";

  public static final String CONGRATULATIONS_UUID = "Y29uZ3JhdHVsYXRpb25zLWZpbmFs";

  // 模拟关卡数据
  public static final Map<String, LevelInfo> levels = new LinkedHashMap<>();
  static {
    // game
    levels.put(LEVEL_1_UUID, new LevelInfo("11", "Tetris", "达到600分即可通关！PS：还好可以暂停", LEVEL_8_UUID, PUZZLE_5_UUID));
    levels.put(LEVEL_2_UUID,
        new LevelInfo("15", "Correlation", "观察散点图，猜测数据的相关系数，通关需要超过80分。PS: 怎么这么难！我要报警了", CONGRATULATIONS_UUID,
            LEVEL_7_UUID));
    levels.put(LEVEL_3_UUID, new LevelInfo("7", "MineSweeper", "这不是简简单单！PS：我测试的时候被雷晕了", PUZZLE_4_UUID, PUZZLE_3_UUID));
    levels.put(LEVEL_4_UUID, new LevelInfo("3", "Wordle", "Wordle！PS：这个是真不会，靠你了！", PUZZLE_1_UUID, PUZZLE_2_UUID));
    levels.put(LEVEL_5_UUID, new LevelInfo("13", "Color", "10/10。PS: 眼睛都看花了", LEVEL_7_UUID, LEVEL_8_UUID));
    // 关卡
    levels.put(LEVEL_6_UUID, new LevelInfo("9", "Number Sequences", "80分通关。PS：0分也是一种水平", PUZZLE_5_UUID, PUZZLE_4_UUID));
    levels.put(LEVEL_7_UUID, new LevelInfo("14", "Friends", "25/35通关！PS：Friends也是Family", LEVEL_2_UUID, LEVEL_5_UUID));
    levels.put(LEVEL_8_UUID,
        new LevelInfo("12", "Minecraft", "18/30通关！PS：我的世界玩了好多遍还是没通过关。", LEVEL_5_UUID, LEVEL_1_UUID));
    levels.put(LEVEL_9_UUID, new LevelInfo("1", "Cat", "10/16通关！PS：看过很多布偶的视频，感觉还是它最好摸", PUZZLE_2_UUID, null));
    levels.put(LEVEL_10_UUID,
        new LevelInfo("5", "Geography", "16/20通关！(问题可能有时效性，十分抱歉) PS：文科生不得满分？", PUZZLE_3_UUID, PUZZLE_1_UUID));
    // 谜题
    levels.put(PUZZLE_1_UUID, new LevelInfo("4", "棍斤拷烫烫烫？", "unicode", LEVEL_10_UUID, LEVEL_4_UUID));
    levels.put(PUZZLE_2_UUID, new LevelInfo("2", "shock", "😲", LEVEL_4_UUID, LEVEL_9_UUID));
    levels.put(PUZZLE_3_UUID, new LevelInfo("6", "quark", "poem", LEVEL_3_UUID, LEVEL_10_UUID));
    levels.put(PUZZLE_4_UUID, new LevelInfo("8", "life", "life", LEVEL_6_UUID, LEVEL_3_UUID));
    levels.put(PUZZLE_5_UUID, new LevelInfo("10", "brainfuck", "visualizer", LEVEL_1_UUID, LEVEL_6_UUID));
    levels.put(CONGRATULATIONS_UUID, new LevelInfo("16", "恭喜通关！", "恭喜通关！生日快乐！", LEVEL_2_UUID, null));
  }

}
