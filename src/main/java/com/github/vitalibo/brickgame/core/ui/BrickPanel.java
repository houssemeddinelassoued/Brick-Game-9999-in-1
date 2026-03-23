package com.github.vitalibo.brickgame.core.ui;

import com.github.vitalibo.brickgame.core.Canvas;
import com.github.vitalibo.brickgame.core.State;
import com.github.vitalibo.brickgame.util.BooleanCollector;
import com.github.vitalibo.brickgame.util.Builder;

import javax.swing.*;
import java.awt.*;
import java.awt.RenderingHints;
import java.util.stream.IntStream;

public class BrickPanel extends JPanel implements Canvas {

    private static final Color ON = new Color(0x000000);
    private static final Color OFF = new Color(0x61705B);

    private final Brick[][] bricks;

    private final int width;
    private final int height;

    BrickPanel(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.bricks = IntStream.range(0, height)
            .mapToObj(h -> IntStream.range(0, width)
                .mapToObj(w -> new Brick())
                .peek(this::add)
                .toArray(Brick[]::new))
            .toArray(v -> new Brick[height][width]);
        this.setLayout(Builder.of(new GridLayout(0, width))
            .with(l -> l.setHgap(1))
            .with(l -> l.setVgap(1))
            .get());
        this.setOpaque(false);
    }

    @Override
    public synchronized void draw(boolean[][] src) {
        IntStream.range(0, height)
            .forEach(h -> IntStream.range(0, width)
                .forEach(w -> bricks[h][w].set(src[h][w])));
    }

    @Override
    public synchronized boolean[][] get() {
        return IntStream.range(0, height)
            .mapToObj(h -> IntStream.range(0, width)
                .mapToObj(w -> bricks[h][w].get())
                .collect(BooleanCollector.toArray()))
            .collect(BooleanCollector.toTwoDimensionalArray());
    }

    static class Brick extends JPanel implements State {

        private boolean state;

        Brick() {
            this.setOpaque(false);
        }

        @Override
        public boolean get() {
            return state;
        }

        @Override
        public void set(boolean state) {
            if (this.state == state) {
                return;
            }

            this.state = state;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h);
            int ox = (w - size) / 2;
            int oy = (h - size) / 2;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = state ? ON : OFF;
            g2.setColor(color);
            g2.drawRect(ox, oy, size - 1, size - 1);
            int pad = Math.max(1, size / 3);
            g2.fillRect(ox + pad, oy + pad, size - 2 * pad, size - 2 * pad);
        }

    }

}