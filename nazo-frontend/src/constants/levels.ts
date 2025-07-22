// Level UUID constants - must match backend Const.java
export const LEVEL_UUIDS = {
  LEVEL_1_UUID: "tetris-level-1",
  LEVEL_2_UUID: "correlation-level-2",
  LEVEL_3_UUID: "minesweeper-level-3",
  LEVEL_4_UUID: "wordle-level-4",
  LEVEL_5_UUID: "color-level-5",
  LEVEL_6_UUID: "number-sequences-level-6",
} as const;

// Export individual constants for convenience
export const { LEVEL_1_UUID, LEVEL_2_UUID, LEVEL_3_UUID, LEVEL_4_UUID, LEVEL_5_UUID, LEVEL_6_UUID } =
  LEVEL_UUIDS;

export const EASTER_EGG_UUIDS = {
  COLOR_EASTER_EGG_UUID: "color-easter-egg",
  WORDLE_EASTER_EGG_UUID: "wordle-easter-egg",
  MINESWEEPER_EASTER_EGG_UUID: "minesweeper-easter-egg",
  TETRIS_EASTER_EGG_UUID: "tetris-easter-egg",
  NUMBER_SEQUENCES_EASTER_EGG_UUID: "number-sequences-easter-egg",
  CORRELATION_EASTER_EGG_UUID: "correlation-easter-egg",
} as const;

export const { COLOR_EASTER_EGG_UUID, WORDLE_EASTER_EGG_UUID, MINESWEEPER_EASTER_EGG_UUID, TETRIS_EASTER_EGG_UUID,
  NUMBER_SEQUENCES_EASTER_EGG_UUID, CORRELATION_EASTER_EGG_UUID } =
  EASTER_EGG_UUIDS;