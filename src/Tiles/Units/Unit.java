package Tiles.Units;

import Callbacks.ChangePositionCallback;
import Callbacks.MessageCallback;
import Tiles.Empty;
import Tiles.Tile;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Players.Player;
import Tiles.Wall;
import Utils.Resource;

import java.util.Random;

public abstract class Unit extends Tile {
    protected static Random rand = new Random();
    protected MessageCallback mcb;
    protected ChangePositionCallback pcb;
    protected String name;
    protected Resource health;
    protected int attackPoints;
    protected int defensePoints;

    /**
     * Constructs a new Unit.
     */
    public Unit() {
        super();
    }

    /**
     * Returns a string describing the unit's stats.
     * @return a formatted string with unit details
     */
    public String description() {
        return String.format(
                "%-20s\t\t" +
                        "Health: %s\t\t" +
                        "Attack: %d\t\t" +
                        "Defense: %s\t\t",
                name,health.toString(),attackPoints,defensePoints);

    }

    /**
     * Sets the message and position change callbacks for the unit.
     * @param mcb the message callback
     * @param pcb the position change callback
     */
    public void setCallbacks(MessageCallback mcb, ChangePositionCallback pcb) {
        this.mcb = mcb;
        this.pcb = pcb;
    }

    /**
     * Gets the name of the unit.
     * @return the unit's name
     */
    public String getName() { return this.name; }

    /**
     * Checks if the unit is dead.
     * @return true if health is 0 or less, false otherwise
     */
    public boolean isDead() { return health.getAmount() <= 0; }

    /**
     * Reduces the unit's health by the specified damage amount.
     * @param damage the amount of damage to take
     */
    public void takeDamage(int damage) { health.reduceAmount(damage); }

    /**
     * Rolls a random attack value up to the unit's attack points.
     * @return the attack roll value
     */
    public int rollAttack() {
        return rand.nextInt(attackPoints + 1);
    }

    /**
     * Rolls a random defense value up to the unit's defense points.
     * @return the defense roll value
     */
    public int rollDefense() {
        return rand.nextInt(defensePoints + 1);
    }

    /**
     * Handles the unit visiting an empty tile.
     * @param empty the empty tile being visited
     */
    public void visit(Empty empty) {
        pcb.call(this,empty);
    }

    /**
     * Handles the unit visiting a wall tile (does nothing).
     * @param wall the wall tile being visited
     */
    public void visit(Wall wall) {}

    /**
     * Handles the unit visiting a generic tile.
     * @param tile the tile being visited
     */
    public  void visit(Tile tile) {
        tile.accept(this);
    }

    public abstract void accept(Unit unit);
    public abstract void visit(Player player);
    public abstract void visit(Enemy enemy);
    public abstract void onDeath(Unit e);
    public abstract void gameTick();

    /**
     * Handles combat between this unit and a defender.
     * @param defender the defending unit
     */
    protected void combat(Unit defender) {
        int attackRoll = this.rollAttack();
        int defenseRoll = defender.rollDefense();
        int damage = Math.max(0, attackRoll - defenseRoll);

        defender.takeDamage(damage);

        combatInfo(defender,attackRoll,defenseRoll,damage);

        if (defender.isDead()) {
            pcb.call(this, defender);
            defender.onDeath(this);
        }
    }

    /**
     * Sends combat information messages using the message callback.
     * @param defender the defending unit
     * @param attackRoll the attack roll value
     * @param defenseRoll the defense roll value
     * @param damage the damage dealt
     */
    private void combatInfo(Unit defender, int attackRoll, int defenseRoll, int damage) {
        mcb.call(String.format("%s engaged in combat with %s.", this.getName(), defender.getName()));
        mcb.call(this.description());
        mcb.call(defender.description());
        mcb.call(String.format("%s rolled %d attack points.", this.getName(), attackRoll));
        mcb.call(String.format("%s rolled %d defense points.", defender.getName(), defenseRoll));
        mcb.call(String.format("%s dealt %d damage to %s.", this.getName(), damage, defender.getName()));
    }


    ///For tests:
    public MessageCallback getMessageCallback() {
        return mcb;
    }

    public ChangePositionCallback getChangePositionCallback() {
        return pcb;
    }

    public Resource getHealth() {
        return health;
    }

    public int getAttackPoints() {
        return attackPoints;
    }

    public int getDefensePoints() {
        return defensePoints;
    }

    public void setAttackPoints(int p) { this.attackPoints = p; }

    public void setDefensePoints(int d) { this.defensePoints = d; }
}
