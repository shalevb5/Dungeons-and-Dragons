package Tiles;

import Tiles.Units.Unit;
import Utils.Position;

public abstract class Tile {
    protected Position position;
    protected char character;


    /**
     * Abstract base class for all tiles on the game board.
     * Stores position and character representation, and defines interaction with units.
     */
    public Tile() {}

    public abstract void accept(Unit unit);
    public char getCharacter() { return character; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    @Override
    public String toString() {
        return String.valueOf(character);
    }
}
