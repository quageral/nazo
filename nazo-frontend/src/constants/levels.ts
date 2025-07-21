// Level UUID constants - must match backend Const.java
export const LEVEL_UUIDS = {
  LEVEL_1_UUID: "tetris-level-1",
  LEVEL_2_UUID: "correlation-level-2",
  LEVEL_3_UUID: "minesweeper-level-3",
  LEVEL_4_UUID: "wordle-level-4",
  LEVEL_5_UUID: "color-level-5",
} as const;

// Export individual constants for convenience
export const { LEVEL_1_UUID, LEVEL_2_UUID, LEVEL_3_UUID, LEVEL_4_UUID, LEVEL_5_UUID } =
  LEVEL_UUIDS;
