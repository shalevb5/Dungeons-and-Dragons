package Tiles;

import Tiles.Units.Unit;

/**
 * Represents an empty tile on the game board.
 * Allows movement through its position and interacts with units via the visitor pattern.
 */
public class Empty extends Tile {

    public Empty() {
        super();
        this.character = '.';
    }

    @Override
    public void accept(Unit unit) {
        unit.visit(this);
    }
}

