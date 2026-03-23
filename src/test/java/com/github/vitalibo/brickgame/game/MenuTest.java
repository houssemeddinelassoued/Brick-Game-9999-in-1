package com.github.vitalibo.brickgame.game;

import com.github.vitalibo.brickgame.core.Canvas;
import com.github.vitalibo.brickgame.core.Context;
import com.github.vitalibo.brickgame.core.Controller;
import com.github.vitalibo.brickgame.core.Kernel;
import com.github.vitalibo.brickgame.core.Number;
import com.github.vitalibo.brickgame.core.State;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class MenuTest {

    @Mock
    private Canvas mockBoard;
    @Mock
    private Canvas mockPreview;
    @Mock
    private Number mockScore;
    @Mock
    private Number mockSpeed;
    @Mock
    private Number mockLevel;
    @Mock
    private Number mockLife;
    @Mock
    private State mockSound;
    @Mock
    private State mockPause;
    @Mock
    private Kernel mockKernel;
    @Mock
    private Controller mockController;

    private Menu menu;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Context context = new Context(
            mockBoard, mockPreview, mockScore, mockSpeed,
            mockLevel, mockLife, mockSound, mockPause, mockKernel, mockController);
        Menu.setGames(new Class[]{Menu.class});
        menu = new Menu(context);
        menu.init();
        // Reset call counts so individual tests start clean
        Mockito.reset(mockBoard, mockLevel, mockSpeed, mockController);
    }

    // ── navigation ─────────────────────────────────────────────────────────

    @Test
    public void testDoDown_incrementsLevel() {
        menu.doDown();
        Mockito.verify(mockLevel).inc();
    }

    @Test
    public void testDoUp_incrementsSpeed() {
        menu.doUp();
        Mockito.verify(mockSpeed).inc();
    }

    @Test
    public void testDoRight_repaints() {
        menu.doRight();
        Mockito.verify(mockBoard, Mockito.atLeastOnce()).draw(Mockito.any());
    }

    @Test
    public void testDoLeft_repaints() {
        menu.doLeft();
        Mockito.verify(mockBoard, Mockito.atLeastOnce()).draw(Mockito.any());
    }

    // ── game selection ─────────────────────────────────────────────────────

    @Test
    public void testDoRotate_initializesSelectedGame() {
        menu.doRotate();
        Mockito.verify(mockController).init(Menu.class);
    }

    // ── init / repaint ─────────────────────────────────────────────────────

    @Test
    public void testInit_drawsBoard() {
        menu.init();   // fresh call after setUp reset
        Mockito.verify(mockBoard, Mockito.atLeastOnce()).draw(Mockito.any());
    }

}
