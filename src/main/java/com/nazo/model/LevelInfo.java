package com.nazo.model;

/**
 * 关卡信息
 * 
 */
public class LevelInfo {
  private final String id;
  private final String name;
  // 在游戏关中，description是描述和彩蛋提示
  // 在谜题关中，description是谜题描述
  private final String description;
  private final String nextLevelUuid;

  public LevelInfo(String id, String name, String description, String nextLevelUuid) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.nextLevelUuid = nextLevelUuid;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getNextLevelUuid() {
    return nextLevelUuid;
  }
}
