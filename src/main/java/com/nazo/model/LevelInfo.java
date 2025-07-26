package com.nazo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 关卡信息
 * 
 */
@Data
@AllArgsConstructor
public class LevelInfo {
  // 关卡编号
  private final String id;
  // 关卡名称
  private final String name;
  // 在游戏关中，hint是描述和彩蛋提示
  // 在谜题关中，hint是谜题提示
  private final String hint;
  // 下一关卡的uuid
  private final String nextLevelUuid;
  // 上一关卡的uuid
  private final String lastLevelUuid;

}
