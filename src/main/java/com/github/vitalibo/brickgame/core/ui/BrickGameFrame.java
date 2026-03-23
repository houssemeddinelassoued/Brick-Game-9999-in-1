package com.github.vitalibo.brickgame.core.ui;

import com.github.vitalibo.brickgame.util.Builder;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class BrickGameFrame extends JFrame {

    @Getter
    private final BrickPanel board;
    @Getter
    private final BrickPanel preview;
    @Getter
    private final NumberPanel score;
    @Getter
    private final NumberPanel speed;
    @Getter
    private final NumberPanel level;
    @Getter
    private final IconPanel sound;
    @Getter
    private final IconPanel pause;

    private JPanel sidebarPanel;

    public BrickGameFrame() {
        super("Brick Game");
        this.board = new BrickPanel(10, 20);
        this.preview = new BrickPanel(4, 4);
        this.score = new NumberPanel(6);
        this.speed = new NumberPanel(2, 15);
        this.level = new NumberPanel(2, 15);
        this.sound = Builder.of(new IconPanel("sound_on", "sound_off", true))
            .with(i -> i.setPreferredSize(new Dimension(16, 16)))
            .get();
        this.pause = Builder.of(new IconPanel("pause_on", "pause_off"))
            .with(i -> i.setPreferredSize(new Dimension(40, 13)))
            .get();
        this.init();
    }

    /** Base dimensions — used to compute the aspect ratio. */
    private static final int BASE_WIDTH  = 190;
    private static final int BASE_HEIGHT = 260;

    private void init() {
        this.setBackground(new Color(0x6D785C));
        this.setContentPane(root());
        this.pack();
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        this.setMinimumSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        this.setResizable(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(
            (int) ((screen.getWidth()  - BASE_WIDTH)  / 2),
            (int) ((screen.getHeight() - BASE_HEIGHT) / 2));
        this.setVisible(true);
    }

    /**
     * Intercept every resize at the lowest level (before the native peer commits it).
     * Width is the leading axis; height is derived from it to maintain the aspect ratio.
     * This eliminates the flicker and off-ratio frames that a post-hoc ComponentListener
     * produces when the user drags any edge or corner.
     */
    @Override
    public void setBounds(int x, int y, int w, int h) {
        if (!isVisible()) {
            // During construction (pack, setSize before setVisible) – apply as-is.
            super.setBounds(x, y, w, h);
            return;
        }
        int cw = Math.max(w, BASE_WIDTH);
        int ch = (int) Math.round(cw * (double) BASE_HEIGHT / BASE_WIDTH);
        // Update sidebar preferred width only when frame width actually changes.
        if (sidebarPanel != null && cw != getWidth()) {
            int sidebarW = (int) Math.round(cw * 60.0 / BASE_WIDTH);
            sidebarPanel.setPreferredSize(new Dimension(sidebarW, 1));
        }
        super.setBounds(x, y, cw, ch);
    }

    private JPanel root() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setOpaque(false);

        board.setBorder(new LineBorder(new Color(0x0), 1));
        root.add(board, BorderLayout.CENTER);

        sidebarPanel = panel(new GridLayout(0, 1))
            .with(sidebar -> panel(new GridLayout(0, 1))
                .with(o -> o.add(label("Score", JLabel.RIGHT)))
                .with(o -> o.add(score))
                .with(sidebar::add))
            .with(sidebar -> Builder.of(new JPanel(new BorderLayout(0, 0)))
                .with(p -> p.setOpaque(false))
                .with(p -> p.add(preview, BorderLayout.CENTER))
                .with(sidebar::add))
            .with(sidebar -> panel(new GridLayout(0, 1))
                .with(o -> o.add(label("Speed", JLabel.CENTER)))
                .with(o -> o.add(speed))
                .with(sidebar::add))
            .with(sidebar -> panel(new GridLayout(0, 1))
                .with(o -> o.add(level))
                .with(o -> o.add(label("Level", JLabel.CENTER)))
                .with(sidebar::add))
            .with(sidebar -> panel(new GridLayout(0, 1))
                .with(o -> o.add(sound))
                .with(o -> o.add(pause))
                .with(sidebar::add))
            .get();

        root.add(sidebarPanel, BorderLayout.EAST);
        return root;
    }

    private static JLabel label(String text, int alignment) {
        return Builder.of(new JLabel(text))
            .with(l -> l.setFont(new Font("Consolas", Font.BOLD, 11)))
            .with(l -> l.setHorizontalAlignment(alignment))
            .get();
    }

    private static Builder<JPanel> panel(LayoutManager layout) {
        return Builder.of(new JPanel(layout))
            .with(p -> p.setOpaque(false));
    }

}