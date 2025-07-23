// Level UUID constants - must match backend Const.java
export const LEVEL_UUIDS = {
  LEVEL_1_UUID: "tetris-level-1",
  LEVEL_2_UUID: "correlation-level-2",
  LEVEL_3_UUID: "minesweeper-level-3",
  LEVEL_4_UUID: "wordle-level-4",
  LEVEL_5_UUID: "color-level-5",
  LEVEL_6_UUID: "number-sequences-level-6",
  LEVEL_7_UUID: "friends-level-7",
  LEVEL_8_UUID: "minecraft-level-8",
  LEVEL_9_UUID: "cat-level-9",
  LEVEL_10_UUID: "geography-level-10",
} as const;

// Puzzle UUID constants - must match backend Const.java
export const PUZZLE_UUIDS = {
  PUZZLE_1_UUID: "puzzle-1",
  PUZZLE_2_UUID: "puzzle-2",
  PUZZLE_3_UUID: "puzzle-3",
  PUZZLE_4_UUID: "puzzle-4",
} as const;

// Congratulations UUID constant
export const CONGRATULATIONS_UUID = "congratulations-final";

// Export individual constants for convenience
export const {
  LEVEL_1_UUID,
  LEVEL_2_UUID,
  LEVEL_3_UUID,
  LEVEL_4_UUID,
  LEVEL_5_UUID,
  LEVEL_6_UUID,
  LEVEL_7_UUID,
  LEVEL_8_UUID,
  LEVEL_9_UUID,
  LEVEL_10_UUID,
} = LEVEL_UUIDS;

export const {
  PUZZLE_1_UUID,
  PUZZLE_2_UUID,
  PUZZLE_3_UUID,
  PUZZLE_4_UUID,
} = PUZZLE_UUIDS;

export const EASTER_EGG_UUIDS = {
  COLOR_EASTER_EGG_UUID: "color-easter-egg",
  WORDLE_EASTER_EGG_UUID: "wordle-easter-egg",
  MINESWEEPER_EASTER_EGG_UUID: "minesweeper-easter-egg",
  TETRIS_EASTER_EGG_UUID: "tetris-easter-egg",
  NUMBER_SEQUENCES_EASTER_EGG_UUID: "number-sequences-easter-egg",
  CORRELATION_EASTER_EGG_UUID: "correlation-easter-egg",
  FRIENDS_EASTER_EGG_UUID: "friends-easter-egg",
  MINECRAFT_EASTER_EGG_UUID: "minecraft-easter-egg",
  CAT_EASTER_EGG_UUID: "cat-easter-egg",
  GEOGRAPHY_EASTER_EGG_UUID: "geography-easter-egg",
} as const;

export const {
  COLOR_EASTER_EGG_UUID,
  WORDLE_EASTER_EGG_UUID,
  MINESWEEPER_EASTER_EGG_UUID,
  TETRIS_EASTER_EGG_UUID,
  NUMBER_SEQUENCES_EASTER_EGG_UUID,
  CORRELATION_EASTER_EGG_UUID,
  FRIENDS_EASTER_EGG_UUID,
  MINECRAFT_EASTER_EGG_UUID,
  CAT_EASTER_EGG_UUID,
  GEOGRAPHY_EASTER_EGG_UUID,
} = EASTER_EGG_UUIDS;
