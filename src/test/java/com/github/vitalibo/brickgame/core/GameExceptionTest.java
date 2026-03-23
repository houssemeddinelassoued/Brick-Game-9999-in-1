package com.github.vitalibo.brickgame.core;

import com.github.vitalibo.brickgame.game.Point;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GameExceptionTest {

    // ── constructor 1: (Point) ─────────────────────────────────────────────

    @Test
    public void testConstructorPoint_returnsPoint() {
        Point point = Point.of(5, 3);

        GameException ex = new GameException(point);

        Assert.assertEquals(ex.getPoint(), point);
    }

    @Test
    public void testConstructorPoint_messageIsNull() {
        GameException ex = new GameException(Point.of(0, 0));

        Assert.assertNull(ex.getMessage());
    }

    // ── constructor 2: (Point, String) ────────────────────────────────────

    @Test
    public void testConstructorPointMessage_returnsPoint() {
        Point point = Point.of(1, 2);

        GameException ex = new GameException(point, "collision");

        Assert.assertEquals(ex.getPoint(), point);
    }

    @Test
    public void testConstructorPointMessage_returnsMessage() {
        GameException ex = new GameException(Point.of(1, 2), "collision");

        Assert.assertEquals(ex.getMessage(), "collision");
    }

    // ── constructor 3: (Point, String, Throwable) ─────────────────────────

    @Test
    public void testConstructorPointMessageCause_returnsPoint() {
        Point point = Point.of(3, 4);

        GameException ex = new GameException(point, "msg", new RuntimeException("root"));

        Assert.assertEquals(ex.getPoint(), point);
    }

    @Test
    public void testConstructorPointMessageCause_returnsMessage() {
        GameException ex = new GameException(Point.of(3, 4), "msg", new RuntimeException());

        Assert.assertEquals(ex.getMessage(), "msg");
    }

    @Test
    public void testConstructorPointMessageCause_returnsCause() {
        Throwable cause = new RuntimeException("root");

        GameException ex = new GameException(Point.of(3, 4), "msg", cause);

        Assert.assertEquals(ex.getCause(), cause);
    }

    // ── constructor 4: (Point, Throwable) ─────────────────────────────────

    @Test
    public void testConstructorPointCause_returnsPoint() {
        Point point = Point.of(0, 0);

        GameException ex = new GameException(point, new IllegalStateException("bad"));

        Assert.assertEquals(ex.getPoint(), point);
    }

    @Test
    public void testConstructorPointCause_returnsCause() {
        Throwable cause = new IllegalStateException("bad state");

        GameException ex = new GameException(Point.of(0, 0), cause);

        Assert.assertEquals(ex.getCause(), cause);
    }

}
