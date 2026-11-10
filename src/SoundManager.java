import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;
import java.io.File;

// ENCAPSULATION: clips + selected typing sound hidden here.
public class SoundManager {
    public static final String THOCK = "Thock";
    public static final String CLACK = "Clack";

    private static final int POOL_SIZE = 8;

    private final Clip music;
    private final Clip[] thockPool;
    private final Clip[] clackPool;
    private final Clip hitClip;                 // synthesized damage sfx
    private int thockIndex = 0;
    private int clackIndex = 0;
    private String typingSound = THOCK;

    public SoundManager() {
        music = load("main-menu-bgm.wav");
        thockPool = loadPool("thock.wav", POOL_SIZE);
        clackPool = loadPool("clack.wav", POOL_SIZE);
        hitClip = createHitSound();             // always available
    }

    public String getTypingSound() { return typingSound; }

    public void setTypingSound(String choice) {
        if (THOCK.equals(choice) || CLACK.equals(choice)) typingSound = choice;
    }

    public void playTyping() {
        Clip clip;
        if (THOCK.equals(typingSound)) {
            clip = thockPool[thockIndex];
            thockIndex = (thockIndex + 1) % POOL_SIZE;
        } else {
            clip = clackPool[clackIndex];
            clackIndex = (clackIndex + 1) % POOL_SIZE;
        }
        restart(clip);
    }

    public void playHit() { restart(hitClip); }

    private void restart(Clip clip) {
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void playMusic() {
        if (music == null || music.isRunning()) return;
        music.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopMusic() {
        if (music != null && music.isRunning()) music.stop();
    }

    private Clip[] loadPool(String fileName, int size) {
        Clip[] pool = new Clip[size];
        for (int i = 0; i < size; i++) pool[i] = load(fileName, i == 0);
        return pool;
    }

    private Clip load(String fileName) { return load(fileName, true); }

    private Clip load(String fileName, boolean verbose) {
        File file = new File("sound", fileName);
        if (!file.exists()) {
            if (verbose) System.out.println("Sound file not found: " + file.getAbsolutePath());
            return null;
        }
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            if (verbose) System.out.println("Loaded sound: " + file.getAbsolutePath());
            return clip;
        } catch (Exception e) {
            if (verbose) System.out.println("Could not load " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    // Programmatic "slash/hit" sfx: noise burst + descending tone, fast decay.
    private Clip createHitSound() {
        try {
            float sampleRate = 44100f;
            int durationMs = 170;
            int samples = (int) (sampleRate * durationMs / 1000.0);
            byte[] buf = new byte[samples * 2];
            java.util.Random rng = new java.util.Random(42);
            double dur = durationMs / 1000.0;
            double phase = 0;
            for (int i = 0; i < samples; i++) {
                double t = i / (double) sampleRate;
                double env = Math.exp(-t * 24);
                double noise = rng.nextDouble() * 2 - 1;
                double freq = 320 - 220 * (t / dur);
                if (freq < 60) freq = 60;
                phase += 2 * Math.PI * freq / sampleRate;
                double tone = Math.sin(phase) * 0.35;
                double s = (noise * 0.6 + tone) * env;
                s = Math.max(-1, Math.min(1, s));
                short val = (short) (s * Short.MAX_VALUE * 0.85);
                buf[i * 2] = (byte) (val & 0xff);
                buf[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
            }
            AudioFormat fmt = new AudioFormat(sampleRate, 16, 1, true, false);
            AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(buf), fmt, samples);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            ais.close();
            return clip;
        } catch (Exception e) {
            System.out.println("Could not create hit sound: " + e.getMessage());
            return null;
        }
    }
}