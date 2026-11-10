public abstract class Game {

    public final void run() {
        showWelcome();
        start();
        showGoodbye();
    }

    protected abstract void showWelcome();
    protected abstract void start();
    protected abstract void showGoodbye();
}