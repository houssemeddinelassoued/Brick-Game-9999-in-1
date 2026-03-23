package com.github.vitalibo.brickgame.core;

import javax.sound.sampled.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Retro sound engine — no audio files needed.
 * <p>
 * All audio is synthesised on-the-fly as 16-bit PCM square waves, which
 * closely mimic the sound chip of the classic 9999-in-1 handheld.
 * <ul>
 *   <li>Background music: Korobeiniki (Tetris Type-A) loops on a daemon thread.</li>
 *   <li>SFX: one-shot note sequences played on a separate daemon thread so they
 *       overlay the music without interrupting it.</li>
 * </ul>
 */
public final class SoundManager {

    // ── Public API ────────────────────────────────────────────────────────────

    /** One-shot sound effects. */
    public enum Sound {
        TOGGLE, MOVE, ROTATE, DROP, CLEAR, GAME_OVER, GAME_START
    }

    /** Mirrors the sound icon initial state (sound_on = true). */
    private static volatile boolean enabled = true;

    private SoundManager() {}

    /** Called by Controller on S-key press. Starts or stops music accordingly. */
    public static void setEnabled(boolean on) {
        enabled = on;
        if (on) startMusic(); else stopMusic();
    }

    public static boolean isEnabled() { return enabled; }

    /** Enqueue a one-shot SFX for async playback. No-op when sound is disabled. */
    public static void play(Sound sfx) {
        if (!enabled) return;
        SFX_EXECUTOR.submit(() -> playSfx(sfx));
    }

    /** Start (or restart) the looping background music. */
    public static void startMusic() {
        stopMusic();
        if (!enabled) return;
        MUSIC_RUNNING.set(true);
        MUSIC_FUTURE.set(MUSIC_EXECUTOR.submit(SoundManager::musicLoop));
    }

    /** Stop the background music as soon as the current note finishes (≤ ~50 ms). */
    public static void stopMusic() {
        MUSIC_RUNNING.set(false);
        Future<?> f = MUSIC_FUTURE.getAndSet(null);
        if (f != null) f.cancel(true);
    }

    // ── Executors ─────────────────────────────────────────────────────────────

    private static final ExecutorService SFX_EXECUTOR   = daemonPool("brick-sfx");
    private static final ExecutorService MUSIC_EXECUTOR = daemonPool("brick-music");
    private static final AtomicBoolean   MUSIC_RUNNING  = new AtomicBoolean(false);
    private static final AtomicReference<Future<?>> MUSIC_FUTURE = new AtomicReference<>();

    private static ExecutorService daemonPool(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }

    // ── Korobeiniki (Tetris Type-A, 144 BPM) ─────────────────────────────────
    // Each row: { frequency_Hz, duration_ms }.  A frequency of 0 is a rest.

    private static final double Q  = 60_000.0 / 144;   // quarter  note ≈ 417 ms
    private static final double E  = Q / 2;             // eighth   note ≈ 208 ms
    private static final double DQ = Q * 1.5;           // dotted quarter ≈ 625 ms
    private static final double H  = Q * 2;             // half     note ≈ 833 ms

    // Frequencies (Hz)
    private static final double A4 = 440.00, B4 = 493.88;
    private static final double C5 = 523.25, D5 = 587.33, E5 = 659.25;
    private static final double F5 = 698.46, G5 = 783.99, A5 = 880.00;

    /**
     * Korobeiniki main theme — two eight-bar sections looped forever.
     *
     * Section A: bars 1-4 (classic ascending/descending motif)
     * Section B: bars 5-8 (bridge with high A5 peak)
     */
    private static final double[][] KOROBEINIKI = {
        // — Section A —
        {E5, Q},  {B4, E},  {C5, E},            // bar 1
        {D5, Q},  {C5, E},  {B4, E},
        {A4, Q},  {A4, E},  {C5, E},            // bar 2
        {E5, Q},  {D5, E},  {C5, E},
        {B4, DQ}, {C5, E},  {D5, Q},  {E5, Q},  // bar 3
        {C5, Q},  {A4, Q},  {A4, H},            // bar 4
        // — Section B —
        {D5, DQ}, {F5, E},  {A5, Q},            // bar 5
        {G5, E},  {F5, E},
        {E5, DQ}, {C5, E},  {E5, Q},            // bar 6
        {D5, E},  {C5, E},
        {B4, Q},  {B4, E},  {C5, E},  {D5, Q},  // bar 7
        {E5, Q},
        {C5, Q},  {A4, Q},  {A4, H},            // bar 8
    };

    private static void musicLoop() {
        while (MUSIC_RUNNING.get() && enabled && !Thread.currentThread().isInterrupted()) {
            for (double[] note : KOROBEINIKI) {
                if (!MUSIC_RUNNING.get() || !enabled || Thread.currentThread().isInterrupted()) return;
                squareWave(note[0], (int) note[1], 2200, true);
            }
        }
    }

    // ── Sound Effects ─────────────────────────────────────────────────────────

    private static void playSfx(Sound sfx) {
        switch (sfx) {
            case TOGGLE:
                squareWave(880, 60, 4500, false);
                break;
            case MOVE:
                squareWave(1047, 35, 3800, false);
                break;
            case ROTATE:
                squareWave(784, 38, 4200, false);
                squareWave(1047, 38, 4200, false);
                break;
            case DROP:
                squareWave(196, 120, 5500, false);
                break;
            case CLEAR:
                // Ascending chime — iconic line-clear sound
                squareWave(523,  65, 5000, false);
                squareWave(659,  65, 5000, false);
                squareWave(784,  65, 5000, false);
                squareWave(1047, 110, 5500, false);
                break;
            case GAME_OVER:
                // Descending "death" fanfare
                squareWave(494, 140, 5200, false);
                squareWave(440, 140, 5200, false);
                squareWave(370, 140, 5200, false);
                squareWave(294, 140, 5200, false);
                squareWave(262, 180, 5200, false);
                squareWave(196, 380, 5000, false);
                break;
            case GAME_START:
                // Ascending fanfare
                squareWave(523,  80, 5000, false);
                squareWave(659,  80, 5000, false);
                squareWave(784,  80, 5000, false);
                squareWave(1047, 80, 5000, false);
                squareWave(1319, 200, 5500, false);
                break;
        }
    }

    // ── PCM Square-Wave Synthesis ─────────────────────────────────────────────

    private static final float RATE       = 44100f;
    /** Internal line buffer (100 ms). Chunks are 50 ms so we can interrupt quickly. */
    private static final int   LINE_BUF   = (int) (RATE * 0.10) * 2;
    private static final int   CHUNK_SIZE = LINE_BUF / 2;

    /**
     * Synthesise and play a square-wave tone.
     *
     * @param hz           Fundamental frequency in Hz; 0 = rest (silence).
     * @param ms           Duration in milliseconds.
     * @param amplitude    Peak amplitude 0–32 767.
     * @param interruptible When true the method exits early if the thread is
     *                     interrupted or {@link #MUSIC_RUNNING} goes false,
     *                     allowing music to stop within one chunk (~50 ms).
     */
    private static void squareWave(double hz, int ms, int amplitude, boolean interruptible) {
        if (ms <= 0) return;
        if (hz <= 0) {
            // Rest: just sleep so the timeline stays accurate
            try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return;
        }
        try {
            int    samples = (int) (RATE * ms / 1000.0);
            byte[] buf     = new byte[samples * 2];

            double period  = RATE / hz;
            double fadeIn  = RATE * 0.008;   //  8 ms attack
            double fadeOut = RATE * 0.012;   // 12 ms release

            for (int i = 0; i < samples; i++) {
                double env = Math.min(1.0, Math.min(i / fadeIn, (samples - i) / fadeOut));
                int    sq  = (i % (int) period < period / 2) ? 1 : -1;
                short  v   = (short) (env * amplitude * sq);
                buf[2 * i]     = (byte)  (v & 0xFF);
                buf[2 * i + 1] = (byte) ((v >> 8) & 0xFF);
            }

            AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                line.open(fmt, LINE_BUF);
                line.start();

                int offset = 0;
                while (offset < buf.length) {
                    if (interruptible && (!MUSIC_RUNNING.get() || Thread.currentThread().isInterrupted())) {
                        line.stop();
                        return;
                    }
                    int toWrite = Math.min(CHUNK_SIZE, buf.length - offset);
                    line.write(buf, offset, toWrite);
                    offset += toWrite;
                }
                line.drain();
            }
        } catch (LineUnavailableException ignored) {
            // No audio device — skip silently
        } catch (Exception ignored) {}
    }
}
