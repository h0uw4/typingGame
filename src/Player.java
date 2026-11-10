public class Player {
    private String name;
    private int gamesPlayed;
    private double bestScore;
    private double bestWpm;

    public Player(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            this.name = "Player";
        } else {
            this.name = name.trim();
        }
    }

    public int getGamesPlayed() { return gamesPlayed; }
    public double getBestScore() { return bestScore; }
    public double getBestWpm()   { return bestWpm; }

    public void recordResult(Result r) {
        gamesPlayed++;
        if (r.getScore() > bestScore) bestScore = r.getScore();
        if (r.getWpm() > bestWpm) bestWpm = r.getWpm();
    }
}