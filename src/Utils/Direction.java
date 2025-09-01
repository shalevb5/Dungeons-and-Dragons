package Utils;

public enum Direction {
    UP(0, -1), //Closer to the 0,0 point
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    STAY(0, 0);

    private final int deltaX, deltaY;

    Direction(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public int getX() { return deltaX; }
    public int getY() { return deltaY; }

    public static Direction fromChar(char c) {
        switch (c) {
            case 'w': return UP;
            case 's': return DOWN;
            case 'a': return LEFT;
            case 'd': return RIGHT;
            case 'q': return STAY;
            default: return STAY;
        }
    }
}

