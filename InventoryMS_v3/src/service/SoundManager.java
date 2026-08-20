package service;

import javax.sound.sampled.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SoundManager {
    // MUST be volatile: this flag is written on the EDT (when the user
    // clicks the Sound ON/OFF button) but read continuously by the
    // dedicated player thread. Without volatile, the player thread can
    // keep seeing a stale cached "true" and never notice it was turned
    // off, so toggling Sound OFF wouldn't reliably stop playback.
    private static volatile boolean enabled = true;

    private static volatile boolean everWorked = false;

    private static final AudioFormat FORMAT = new AudioFormat(44100, 16, 1, true, false);

    // A single persistent line, opened once and reused for every tone.
    private static SourceDataLine line;

    // ── Playback queue ──────────────────────────────────────────────────────
    // Every sound effect used to spawn its OWN Thread. Under heavy/rapid UI
    // interaction (fast clicking, rapid tab switching) that meant dozens of
    // threads could all be queued up waiting to write to the shared line at
    // once. Since each thread's tones still had to play out one after another,
    // the backlog kept draining audibly for a while AFTER the user had
    // already stopped interacting - heard as a "stuck"/non-stop beep.
    //
    // Now there is exactly one player thread, fed by a small bounded queue.
    // If a burst of clicks fills the queue, further requests are simply
    // dropped (not queued) so playback can never lag more than a couple of
    // sounds behind real time.
    private static final int QUEUE_CAPACITY = 3;
    private static final BlockingQueue<int[][]> QUEUE = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    static {
        Thread player = new Thread(SoundManager::playerLoop, "SoundManager-Player");
        player.setDaemon(true);
        player.start();
    }

    private static void playerLoop() {
        while (true) {
            try {
                int[][] job = QUEUE.take(); // job = { {hz0,ms0,vol0*1000,gap0}, ... }
                if (!enabled) continue;
                for (int[] step : job) {
                    tone(step[0], step[1], step[2] / 1000f);
                    if (step[3] > 0) sleep(step[3]);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Non-blocking submit - if the queue is already full, the sound is dropped
     *  instead of being allowed to pile up and play late. */
    private static void submit(int[]... steps) {
        if (!enabled) return;
        QUEUE.offer(steps); // offer() never blocks; returns false (and drops) if full
    }

    public static void setEnabled(boolean on) {
        enabled = on;
        if (!on) QUEUE.clear(); // also stop anything already backlogged
    }
    public static boolean isEnabled() { return enabled; }

    private static int[] step(int hz, int ms, float vol, int gapMs) {
        return new int[]{hz, ms, Math.round(vol * 1000), gapMs};
    }

    public static void playStockAdded() {
        submit(step(880, 80, 0.4f, 40), step(1320, 120, 0.35f, 0));
    }
    public static void playSaleSuccess() {
        submit(step(523, 60, 0.35f, 30), step(659, 60, 0.35f, 30), step(784, 100, 0.4f, 0));
    }
    public static void playLowStockAlert() {
        submit(step(520, 180, 0.45f, 0));
    }
    public static void playOrderCreated() {
        submit(step(660, 70, 0.3f, 35), step(880, 90, 0.3f, 0));
    }
    public static void playOrderDelivered() {
        submit(step(880, 80, 0.35f, 30), step(660, 80, 0.35f, 30), step(523, 120, 0.3f, 0));
    }
    public static void playOrderCancelled() {
        submit(step(587, 70, 0.3f, 25), step(392, 90, 0.3f, 0));
    }
    public static void playError() {
        submit(step(200, 80, 0.45f, 40), step(180, 120, 0.45f, 0));
    }
    public static void playLoginSuccess() {
        submit(step(523, 70, 0.3f, 30), step(784, 70, 0.3f, 30), step(1047, 120, 0.35f, 0));
    }
    public static void playLoginFail() {
        submit(step(220, 150, 0.5f, 60), step(196, 180, 0.45f, 0));
    }
    public static void playDelete() {
        submit(step(300, 80, 0.3f, 0));
    }
    public static void playSave() {
        submit(step(660, 60, 0.25f, 20), step(880, 80, 0.25f, 0));
    }

    private static volatile long lastClickTime = 0;
    private static final long CLICK_COOLDOWN_MS = 200;

    public static void playClick() {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < CLICK_COOLDOWN_MS) return; // swallow rapid repeats
        lastClickTime = now;
        submit(step(740, 35, 0.22f, 0));
    }

    /** Lazily opens the shared line once. Safe to call repeatedly - if the
     *  line is already open this just returns true immediately. */
    private static boolean ensureLine() {
        if (line != null && line.isOpen()) return true;
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                if (!everWorked) {
                    System.err.println("[SoundManager] No audio output line is available on this system " +
                            "(AudioSystem reports the format is unsupported). Sound effects will be silent.");
                }
                return false;
            }
            line = (SourceDataLine) AudioSystem.getLine(info);
            // Small buffer - just enough for one tone. The queue (not the
            // line buffer) is what absorbs bursts now, so this no longer
            // needs to be large; keeping it small also means nothing can be
            // "in flight" and still audible long after it should have stopped.
            line.open(FORMAT, 8192);
            line.start();
            everWorked = true;
            return true;
        } catch (Exception ex) {
            if (!everWorked) {
                System.err.println("[SoundManager] Couldn't open an audio line (" +
                        ex.getClass().getSimpleName() + ": " + ex.getMessage() +
                        "). This usually means no audio device/driver is available in this environment.");
            }
            line = null;
            return false;
        }
    }

    private static void tone(int hz, int ms, float vol) {
        if (!enabled) return;
        if (!ensureLine()) return;
        try {
            int rate = 44100, samples = rate * ms / 1000;
            byte[] buf = new byte[samples * 2];
            for (int i = 0; i < samples; i++) {
                double env = 1.0 - (double) i / samples;
                short v = (short) (Math.sin(2 * Math.PI * i / (rate / (double) hz)) * env * vol * Short.MAX_VALUE);
                buf[2 * i] = (byte) (v & 0xFF);
                buf[2 * i + 1] = (byte) ((v >> 8) & 0xFF);
            }
            line.write(buf, 0, buf.length);
        } catch (Exception ex) {
            try { if (line != null) line.close(); } catch (Exception ignored) {}
            line = null;
            if (!everWorked) {
                System.err.println("[SoundManager] Couldn't play a sound effect (" +
                        ex.getClass().getSimpleName() + ": " + ex.getMessage() + ").");
            }
        }
    }

    private static void sleep(int ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
