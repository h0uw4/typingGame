// INHERITANCE: Boss IS-A Entity
public class Boss extends Entity {
    private final String imageFile;
    private final String difficulty;
    private final int timeLimitSeconds;

    public Boss(String name, int maxHp, String imageFile, String difficulty, int timeLimitSeconds) {
        super(name, maxHp);
        this.imageFile = imageFile;
        this.difficulty = difficulty;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    @Override
    public String getImageFile() { return imageFile; }

    public String getDifficulty() { return difficulty; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
}