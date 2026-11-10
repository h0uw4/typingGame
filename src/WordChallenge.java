// INHERITANCE: WordChallenge IS-A Challenge
public class WordChallenge extends Challenge {
    private static final String[] WORDS = {
        "apple", "river", "keyboard", "planet", "orange", "window", "coffee",
        "school", "bridge", "garden", "rocket", "silver", "thunder", "pencil",
        "monitor", "castle", "button", "forest", "guitar", "island"
    };

    public WordChallenge() { super("Random Words"); }

    @Override
    public String generateText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i > 0) sb.append(' ');
            sb.append(pick(WORDS));
        }
        return sb.toString();
    }

    @Override public double getMultiplier() { return 1.0; }

    @Override
    public Boss createBoss() {
        return new Boss("Word Golem", 120, "word-golem.png", "Easy", 30);
    }
}