// ENCAPSULATION: private final fields, read-only access through getters
public class Result {
    private final double wpm;
    private final double accuracy;
    private final double seconds;
    private final double score;

    public Result(double wpm, double accuracy, double seconds, double score) {
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.seconds = seconds;
        this.score = score;
    }

    public double getWpm()      { return wpm; }
    public double getAccuracy() { return accuracy; }
    public double getSeconds()  { return seconds; }
    public double getScore()    { return score; }
}