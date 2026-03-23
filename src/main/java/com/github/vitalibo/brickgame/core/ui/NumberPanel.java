package com.github.vitalibo.brickgame.core.ui;

import com.github.vitalibo.brickgame.core.Number;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.stream.IntStream;

public class NumberPanel extends JPanel implements Number {

    private static final Image[] NUMERICAL_DIGIT = IntStream.range(0, 10)
        .mapToObj(i -> String.format("ui/digit/%d.bmp", i))
        .map(NumberPanel::resourceAsImage)
        .toArray(Image[]::new);

    private final int maxValue;

    private Digit[] sequence;
    private int value;

    NumberPanel(int capacity) {
        this(capacity, Integer.MAX_VALUE);
    }

    NumberPanel(int capacity, final int maxValue) {
        this.maxValue = maxValue;
        this.value = 0;
        this.sequence = IntStream.range(0, capacity)
            .mapToObj(i -> new Digit())
            .peek(this::add)
            .toArray(Digit[]::new);
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
        this.setOpaque(false);
        this.repaint();
    }

    @Override
    public int get() {
        return value;
    }

    @Override
    public synchronized void set(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("The value must be mere 0.");
        }

        if (value > maxValue) {
            this.value = 0;
        } else {
            this.value = value;
        }

        IntStream.range(0, sequence.length)
            .mapToObj(i -> new AbstractMap.SimpleEntry<>(i, 0))
            .peek(entry -> entry.setValue((this.value / (int) Math.pow(10, sequence.length - entry.getKey() - 1)) % 10))
            .forEach(entry -> sequence[entry.getKey()].set(entry.getValue()));
    }

    @SneakyThrows(IOException.class)
    private static Image resourceAsImage(String resource) {
        return ImageIO.read(ClassLoader.getSystemResourceAsStream(resource));
    }

    static class Digit extends JPanel implements Number {

        private int value;

        Digit() {
            this.setPreferredSize(new Dimension(8, 13));
        }

        @Override
        public Dimension getPreferredSize() {
            Container parent = getParent();
            if (parent != null && parent.getWidth() > 8) {
                int n = Math.max(1, parent.getComponentCount());
                // 1px gap between digits + 2px FlowLayout hgap on each side
                int gaps = (n - 1) + 4;
                int digitW = Math.max(8, (parent.getWidth() - gaps) / n);
                int availH = parent.getHeight() > 13 ? parent.getHeight() - 2 : Integer.MAX_VALUE;
                int digitH = Math.min(availH, Math.max(13, digitW * 13 / 8));
                // If height was clamped, also constrain width to preserve aspect ratio
                digitW = Math.min(digitW, digitH * 8 / 13);
                return new Dimension(digitW, digitH);
            }
            return new Dimension(8, 13);
        }

        public int get() {
            return value;
        }

        public void set(int value) {
            if (value < 0 || value > 9) {
                throw new IllegalArgumentException("The value must be in the range 0 - 9.");
            }

            if (this.value == value) {
                return;
            }

            this.value = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(NUMERICAL_DIGIT[value], 0, 0, getWidth(), getHeight(), null);
        }

    }

}