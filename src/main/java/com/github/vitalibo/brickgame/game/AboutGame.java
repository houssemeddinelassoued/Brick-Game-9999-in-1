package com.github.vitalibo.brickgame.game;

import com.github.vitalibo.brickgame.core.Context;
import com.github.vitalibo.brickgame.util.CanvasTranslator;

import java.util.ArrayList;
import java.util.List;

/**
 * About / Credits screen rendered entirely in the brick-pixel style.
 *
 * Navigation:
 *   DOWN  — scroll forward through credits
 *   UP    — scroll back through credits
 *   SPACE / ROTATE — return to Menu
 */
public class AboutGame extends Game {

    // ── 5-row × 3-col pixel font (0=off, 1=on) for A-Z and space ──────
    private static final boolean[][][] FONT = buildFont();

    // Board dimensions
    private static final int COLS = 10;
    private static final int ROWS = 20;

    /** Each "line" of credits is rendered as one row of bricks. */
    private final List<boolean[]> lines = new ArrayList<>();
    private int scrollOffset = 0;

    public AboutGame(Context context) {
        super(context);
        buildLines();
    }

    // ── credit text ──────────────────────────────────────────────────────

    private static final String[] CREDITS = {
        "  ABOUT  ",
        "",
        " BRICK   ",
        " GAME    ",
        " 9999    ",
        " IN  1   ",
        "",
        " BY      ",
        " VITALIBO",
        " VITALY  ",
        " BONDAR  ",
        "",
        " CONTRIB ",
        " HOUSSEY ",
        " MED     ",
        " DINELAS ",
        " SOUED   ",
        "",
        " PRESS   ",
        " SPACE   ",
        " TO EXIT ",
    };

    // ── Game lifecycle ────────────────────────────────────────────────────

    @Override
    public void init() {
        scrollOffset = 0;
        repaint();
    }

    @Override
    public void doDown() {
        if (scrollOffset < lines.size() - ROWS) {
            scrollOffset++;
        }
        repaint();
    }

    @Override
    public void doUp() {
        if (scrollOffset > 0) {
            scrollOffset--;
        }
        repaint();
    }

    @Override
    public void doLeft() {
        // no-op in About screen
    }

    @Override
    public void doRight() {
        // no-op in About screen
    }

    @Override
    public void doRotate() {
        controller.init(Menu.class);
    }

    // ── rendering ─────────────────────────────────────────────────────────

    private void repaint() {
        boolean[][] canvas = new boolean[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            int lineIdx = scrollOffset + row;
            if (lineIdx < lines.size()) {
                canvas[row] = lines.get(lineIdx);
            } else {
                canvas[row] = new boolean[COLS];
            }
        }
        board.draw(canvas);
    }

    // ── line builder ─────────────────────────────────────────────────────

    /**
     * Converts each credit string into up to 5 boolean[] rows of width COLS,
     * using the 5×3 pixel font. Rows are separated by a blank spacer line.
     */
    private void buildLines() {
        for (String credit : CREDITS) {
            if (credit.isEmpty()) {
                lines.add(new boolean[COLS]);
            } else {
                // Render the 5 pixel-rows for this string
                boolean[][] rendered = renderText(credit);
                for (boolean[] row : rendered) {
                    lines.add(row);
                }
                lines.add(new boolean[COLS]); // line gap
            }
        }
        // Ensure enough padding at the bottom so the last line can scroll to top
        for (int i = 0; i < ROWS; i++) {
            lines.add(new boolean[COLS]);
        }
    }

    /**
     * Renders a string into a 5-row × COLS-col boolean grid,
     * centering the characters horizontally.
     */
    private static boolean[][] renderText(String text) {
        // Each character is 3 cols wide + 1 col gap, except last
        String upper = text.toUpperCase();
        int charCount = upper.length();
        // Total pixel width = charCount*3 + (charCount-1)*1 = charCount*4 - 1
        int textWidth = charCount > 0 ? charCount * 4 - 1 : 0;
        int startCol = Math.max(0, (COLS - textWidth) / 2);

        boolean[][] grid = new boolean[5][COLS];
        int col = startCol;
        for (int ci = 0; ci < charCount && col < COLS; ci++) {
            char c = upper.charAt(ci);
            boolean[][] glyph = getGlyph(c);
            for (int row = 0; row < 5; row++) {
                for (int gc = 0; gc < 3; gc++) {
                    int targetCol = col + gc;
                    if (targetCol < COLS) {
                        grid[row][targetCol] = glyph[row][gc];
                    }
                }
            }
            col += 4; // 3 wide + 1 space
        }
        return grid;
    }

    // ── pixel font ────────────────────────────────────────────────────────

    private static boolean[][] getGlyph(char c) {
        if (c >= 'A' && c <= 'Z') {
            return FONT[c - 'A'];
        }
        // space or unknown → blank 5×3
        return new boolean[5][3];
    }

    /**
     * Builds the 5-row × 3-col pixel font for A–Z.
     * Each character is encoded as 5 ints, one per row, with bits 2-0 = cols left-to-right.
     */
    private static boolean[][][] buildFont() {
        // Rows encoded as binary: bit2=left col, bit1=mid col, bit0=right col
        int[][] raw = {
            // A
            {0b010, 0b101, 0b111, 0b101, 0b101},
            // B
            {0b110, 0b101, 0b110, 0b101, 0b110},
            // C
            {0b011, 0b100, 0b100, 0b100, 0b011},
            // D
            {0b110, 0b101, 0b101, 0b101, 0b110},
            // E
            {0b111, 0b100, 0b110, 0b100, 0b111},
            // F
            {0b111, 0b100, 0b110, 0b100, 0b100},
            // G
            {0b011, 0b100, 0b101, 0b101, 0b011},
            // H
            {0b101, 0b101, 0b111, 0b101, 0b101},
            // I
            {0b111, 0b010, 0b010, 0b010, 0b111},
            // J
            {0b001, 0b001, 0b001, 0b101, 0b010},
            // K
            {0b101, 0b101, 0b110, 0b101, 0b101},
            // L
            {0b100, 0b100, 0b100, 0b100, 0b111},
            // M
            {0b101, 0b111, 0b101, 0b101, 0b101},
            // N
            {0b101, 0b111, 0b111, 0b111, 0b101},
            // O
            {0b010, 0b101, 0b101, 0b101, 0b010},
            // P
            {0b110, 0b101, 0b110, 0b100, 0b100},
            // Q
            {0b010, 0b101, 0b101, 0b011, 0b001},
            // R
            {0b110, 0b101, 0b110, 0b101, 0b101},
            // S
            {0b011, 0b100, 0b010, 0b001, 0b110},
            // T
            {0b111, 0b010, 0b010, 0b010, 0b010},
            // U
            {0b101, 0b101, 0b101, 0b101, 0b010},
            // V
            {0b101, 0b101, 0b101, 0b101, 0b010},
            // W
            {0b101, 0b101, 0b101, 0b111, 0b101},
            // X
            {0b101, 0b101, 0b010, 0b101, 0b101},
            // Y
            {0b101, 0b101, 0b010, 0b010, 0b010},
            // Z
            {0b111, 0b001, 0b010, 0b100, 0b111},
        };

        boolean[][][] font = new boolean[26][5][3];
        for (int ci = 0; ci < 26; ci++) {
            for (int row = 0; row < 5; row++) {
                int bits = raw[ci][row];
                font[ci][row][0] = (bits & 0b100) != 0;
                font[ci][row][1] = (bits & 0b010) != 0;
                font[ci][row][2] = (bits & 0b001) != 0;
            }
        }
        return font;
    }

}
