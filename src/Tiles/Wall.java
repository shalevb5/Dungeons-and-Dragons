package Tiles;

import Tiles.Units.Unit;


/**
 * Represents an impassable wall tile on the game board.
 * Prevents movement through its position and interacts with units via the visitor pattern.
 */
public class Wall extends Tile {

    public Wall() {
        super();
        this.character = '#';
    }

    @Override
    public void accept(Unit unit) {
        unit.visit(this);
    }
}