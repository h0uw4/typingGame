import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SoundManager {
    public static final String THOCK = "Thock";

    private static final String[] TYPING_SOUNDS = {"Thock", "Clack", "Cream", "Switch"};

    private static final int POOL_SIZE = 8;

    private final Clip music;
    private final Map<String, Clip[]> pools = new LinkedHashMap<>();
    private final Map<String, Integer> nextIndex = new HashMap<>();
    private String typingSound = THOCK;

    public SoundManager() {
        music = load("main-menu-bgm.wav", true);
        for (String name : TYPING_SOUNDS) {
            pools.put(name, loadPool(name.toLowerCase() + ".wav", POOL_SIZE));
            nextIndex.put(name, 0);
        }
    }

    public String[] getTypingSoundNames() {
        return TYPING_SOUNDS.clone();
    }

    public String getTypingSound() {
        return typingSound;
    }

    public void setTypingSound(String choice) {
        if (pools.containsKey(choice)) {
            typingSound = choice;
        }
    }

    public void playTyping() {
        Clip[] pool = pools.get(typingSound);
        int i = nextIndex.get(typingSound);
        nextIndex.put(typingSound, (i + 1) % POOL_SIZE);

        Clip clip = pool[i];
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
        if (music != null && music.isRunning()) {
            music.stop();
        }
    }

    private Clip[] loadPool(String fileName, int size) {
        Clip[] pool = new Clip[size];
        for (int i = 0; i < size; i++) {
            pool[i] = load(fileName, i == 0); 
        }
        return pool;
    }

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
}