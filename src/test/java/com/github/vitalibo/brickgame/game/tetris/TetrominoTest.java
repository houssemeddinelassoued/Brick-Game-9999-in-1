package com.github.vitalibo.brickgame.game.tetris;

import com.github.vitalibo.brickgame.game.Point;
import com.github.vitalibo.brickgame.game.tetris.tetromino.Factory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TetrominoTest {

    // I-piece state 0 → horizontal bar, points start at y=-1, x=3..6
    private Tetromino tetromino;

    @BeforeMethod
    public void setUp() {
        tetromino = Factory.createI(0);
    }

    // ── initial state ──────────────────────────────────────────────────────

    @Test
    public void testInitialState_stateIsZero() {
        Assert.assertEquals(tetromino.getState(), 0);
    }

    @Test
    public void testInitialState_notEmpty() {
        Assert.assertFalse(tetromino.isEmpty());
    }

    // ── doDown ─────────────────────────────────────────────────────────────

    @Test
    public void testDoDown_movesPointsDown() {
        int initialY = tetromino.get(0).getY();
        tetromino.doDown();
        Assert.assertEquals(tetromino.get(0).getY(), initialY + 1);
    }

    @Test
    public void testDoDown_dotMovesDown() {
        int initialY = tetromino.getDot().getY();
        tetromino.doDown();
        Assert.assertEquals(tetromino.getDot().getY(), initialY + 1);
    }

    // ── doLeft ─────────────────────────────────────────────────────────────

    @Test
    public void testDoLeft_movesPointsLeft() {
        int initialX = tetromino.get(0).getX();
        tetromino.doLeft();
        Assert.assertEquals(tetromino.get(0).getX(), initialX - 1);
    }

    // ── doRight ────────────────────────────────────────────────────────────

    @Test
    public void testDoRight_movesPointsRight() {
        int initialX = tetromino.get(0).getX();
        tetromino.doRight();
        Assert.assertEquals(tetromino.get(0).getX(), initialX + 1);
    }

    // ── doUp ───────────────────────────────────────────────────────────────

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testDoUp_throwsUnsupported() {
        tetromino.doUp();
    }

    // ── doRotate ───────────────────────────────────────────────────────────

    @Test
    public void testDoRotate_incrementsState() {
        tetromino.doRotate();
        Assert.assertEquals(tetromino.getState(), 1);
    }

    @Test
    public void testDoRotate_fourRotationsReachState4() {
        // state increments without modulo wrap — shape wraps via state%4 internally
        for (int i = 0; i < 4; i++) {
            tetromino.doRotate();
        }
        Assert.assertEquals(tetromino.getState(), 4);
    }

    // ── inBottom ───────────────────────────────────────────────────────────

    @Test
    public void testInBottom_falseAtStart() {
        Assert.assertFalse(tetromino.inBottom());
    }

    @Test
    public void testInBottom_trueAfterReachingBottom() {
        // Move down until at the bottom (22 moves more than covers y=-1 → y=19)
        for (int i = 0; i < 22; i++) {
            tetromino.doDown();
        }
        Assert.assertTrue(tetromino.inBottom());
    }

    // ── boundary: doLeft ───────────────────────────────────────────────────

    @Test
    public void testDoLeft_stopsAtLeftEdge() {
        for (int i = 0; i < 10; i++) {
            tetromino.doLeft();
        }
        int minX = tetromino.stream().mapToInt(Point::getX).min().orElseThrow();
        Assert.assertEquals(minX, 0);

        tetromino.doLeft();
        int minXAfter = tetromino.stream().mapToInt(Point::getX).min().orElseThrow();
        Assert.assertEquals(minXAfter, 0);
    }

    // ── boundary: doRight ──────────────────────────────────────────────────

    @Test
    public void testDoRight_stopsAtRightEdge() {
        for (int i = 0; i < 10; i++) {
            tetromino.doRight();
        }
        int maxX = tetromino.stream().mapToInt(Point::getX).max().orElseThrow();
        Assert.assertEquals(maxX, 9);

        tetromino.doRight();
        int maxXAfter = tetromino.stream().mapToInt(Point::getX).max().orElseThrow();
        Assert.assertEquals(maxXAfter, 9);
    }

    // ── boundary: doDown ───────────────────────────────────────────────────

    @Test
    public void testDoDown_stopsAtBottomEdge() {
        for (int i = 0; i < 25; i++) {
            tetromino.doDown();
        }
        int maxY = tetromino.stream().mapToInt(Point::getY).max().orElseThrow();
        Assert.assertEquals(maxY, 19);

        tetromino.doDown();
        int maxYAfter = tetromino.stream().mapToInt(Point::getY).max().orElseThrow();
        Assert.assertEquals(maxYAfter, 19);
    }

    // ── Tetromino.from ─────────────────────────────────────────────────────

    @Test
    public void testFrom_differentInstance() {
        Tetromino clone = Tetromino.from(tetromino);
        Assert.assertNotSame(clone, tetromino);
    }

    @Test
    public void testFrom_sameState() {
        tetromino.doRotate();
        Tetromino clone = Tetromino.from(tetromino);
        Assert.assertEquals(clone.getState(), tetromino.getState());
    }

    @Test
    public void testFrom_sameDotCoordinates() {
        tetromino.doDown();
        tetromino.doRight();
        Tetromino clone = Tetromino.from(tetromino);
        Assert.assertEquals(clone.getDot().getY(), tetromino.getDot().getY());
        Assert.assertEquals(clone.getDot().getX(), tetromino.getDot().getX());
    }

    @Test
    public void testFrom_sameSize() {
        Tetromino clone = Tetromino.from(tetromino);
        Assert.assertEquals(clone.size(), tetromino.size());
    }

    @Test
    public void testFrom_pointsAreIndependent() {
        Tetromino clone = Tetromino.from(tetromino);
        clone.doDown();
        Assert.assertNotEquals(clone.get(0).getY(), tetromino.get(0).getY());
    }

}
