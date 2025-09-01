package Tiles.Units.Enemies;

import Callbacks.ChangePositionCallback;
import Callbacks.EnemyDeathCallback;
import Callbacks.MessageCallback;
import Tiles.Units.Players.Player;
import Tiles.Units.Unit;
import Utils.Position;
import Utils.Resource;

public abstract class Enemy extends Unit {
    
    protected int experienceValue;
    protected Position playerPosition;
    protected Position desiredPosition;
    private EnemyDeathCallback edcb;

    public Enemy(String name, char tile, int health, int attack, int defense, int exp) {
        super();
        this.name = name;
        this.character = tile;
        this.health = new Resource(health,health);
        this.attackPoints = attack;
        this.defensePoints = defense;
        this.experienceValue = exp;
    }

    /**
     * Returns a string describing the enemy's stats and experience value.
     * @return the formatted description string
     */
    public String description() {
        return String.format(
                super.description()  + "Experience Value: %d\t",experienceValue);
    }

    /**
     * Sets the callbacks for messages, position changes, and enemy death.
     * @param mcb the message callback
     * @param pcb the position change callback
     * @param edcb the enemy death callback
     */
    public void setCallbacks(MessageCallback mcb, ChangePositionCallback pcb, EnemyDeathCallback edcb) {
        super.setCallbacks(mcb,pcb);
        this.edcb = edcb;
    }

    /**
     * Returns the desired position for the enemy to move to.
     * @return the desired position
     */
    public Position getDesiredPosition() { return desiredPosition; }

    /**
     * Returns the experience value awarded for defeating this enemy.
     * @return the experience value
     */
    public int getXP() { return this.experienceValue; }

    /**
     * Handles logic when the enemy dies, including callbacks and board updates.
     * @param u the unit responsible for the enemy's death
     */
    public void onDeath(Unit u) {
        String output = String.format("%s died.",this.name);
        mcb.call(output);
        edcb.call(this);
    }

    /**
     * Sets the current position of the player for enemy tracking.
     * @param p the player's position
     */
    public void setPlayerPosition(Position p) { playerPosition = p; }

    /**
     * Accepts a visiting unit for interaction.
     * @param unit the visiting unit
     */
    @Override
    public void accept(Unit unit) {
        unit.visit(this);
    }

    /**
     * Handles being visited by a player (initiates combat).
     * @param player the player visiting
     */
    @Override
    public void visit(Player player) {
        combat(player);
    }

    /**
     * Handles being visited by another enemy (does nothing).
     * @param enemy the enemy visiting
     */
    @Override
    public void visit(Enemy enemy) { /* Do nothing. */ }

}
