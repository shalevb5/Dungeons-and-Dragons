package Tiles.Units.Enemies;

import Utils.Direction;
import Utils.Position;

public class Monster extends Enemy {
    private static final String DIRECTIONS_KEYS = "wasd";
    protected int visionRange;


    public Monster(String name, char tile, int health, int attack, int defense, int vision, int exp) {
        super(name, tile, health, attack, defense, exp);
        this.visionRange = vision;
    }

    /**
     * Returns a string describing the monster's stats and vision range.
     * @return the formatted description string
     */
    public String description() {
        return String.format(
                super.description()  + "Vision Range: %d\t",visionRange);
    }

    /**
     * Updates the monster's desired position each game tick, moving toward the player if in vision range.
     */
    @Override
    public void gameTick() {
        desiredPosition = nextPosition(playerPosition);

    }

    /**
     * Determines the next position for the monster to move toward, either chasing the player or moving randomly.
     * @param playerPosition the current position of the player
     * @return the next position for the monster
     */
    private Position nextPosition(Position playerPosition) {
        Direction toMove;
        if(this.position.calculateRange(playerPosition) < visionRange) {
            int dx = position.getX() - playerPosition.getX();
            int dy = position.getY() - playerPosition.getY();

            if (Math.abs(dx) > Math.abs(dy)) {
                toMove = dx > 0 ? Direction.LEFT : Direction.RIGHT;
            } else {
                toMove =  dy > 0 ? Direction.UP : Direction.DOWN;
            }
        } else {
            char randomChar = DIRECTIONS_KEYS.charAt(new java.util.Random().nextInt(DIRECTIONS_KEYS.length()));
            toMove = Direction.fromChar(randomChar);
        }
        return Position.GetPosition(position,toMove);
    }


    ///For tests:
    public int getVisionRange() { return this.visionRange; }

}
