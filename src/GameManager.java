import Callbacks.*;
import Tiles.Empty;
import Tiles.TileFactory;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Players.Player;
import Utils.Direction;
import Utils.Position;

import java.util.ArrayList;
import java.util.List;


public class GameManager {

    private boolean gameRunning;
    private Board board;
    private Player player;
    private List<Enemy> enemies;
    private CLI cli;
    private MapLoader mapLoader;
    private int currentLevel;

    private MessageCallback mcb;
    private EnemyCreatedCallback eccb;
    private PlayerDeathCallback pdcb;
    private EnemyDeathCallback edcb;
    private ChangePositionCallback pcb;

    /**
     * Initializes the game manager, CLI, and callback handlers.
     */
    public GameManager() {
        gameRunning = false;
        currentLevel = 0;
        enemies = new ArrayList<>();
        cli = new CLI();

        mcb = cli.getMcb();
        eccb = this::enemyCreated;
        pdcb = this::playerDied;
        edcb = this::enemyDied;
    }

    /**
     * Initializes the game by loading maps, selecting a player, and loading the first level.
     * @param mapsPath the path to the directory containing map files
     */
    public void initializeGame(String mapsPath) {
        mapLoader = new MapLoader(mapsPath);
        selectPlayer();
        loadLevel();
    }

    /**
     * Starts the main game loop.
     */
    public void start() {
        gameRunning = true;
        gameLoop();
    }

    /**
     * The main game loop that processes game ticks and evaluates level status.
     */
    private void gameLoop() {
        while(gameRunning) {
            gameTick();
            evaluateLevelStatus();
        }
        handleGameOver();
    }

    /**
     * Loads the current level's board and sets up player callbacks.
     */
    private void loadLevel() {
        board = new Board( mcb, eccb);
        pcb = board.getPcb();
        player.setCallbacks(mcb,pcb,pdcb);
        board.setBoardMap(mapLoader.loadMap(currentLevel));
    }

    /**
     * Prompts the user to select a player character.
     */
    private void selectPlayer() {
        player = TileFactory.GetPlayer(cli.getCharacterSelection());
        mcb.call("You have selected: " + player.getName());

    }

    /**
     * Executes a single game tick, including player and enemy turns.
     */
    private void gameTick() {
        roundStats();
        playerTurn();

        if(!player.isDead())
            enemiesTurn();
    }

    /**
     * Displays the current round's board and player stats.
     */
    private void roundStats() {
        mcb.call(board.toString());
        mcb.call(player.description());
    }

    /**
     * Handles the player's turn, including input and actions.
     */
    private void playerTurn() {
        char c = cli.getUserAction();
        player.gameTick();
        playerAction(c);
    }

    /**
     * Executes the player's action based on the input character.
     * @param action the character representing the player's action
     */
    private void playerAction(char action) {
        switch (action) {
            case 'w':
            case 'a':
            case 's':
            case 'd':
            case 'q':
                Direction direction = Direction.fromChar(action);
                Position nextPos = Position.GetPosition(player.getPosition(),direction);
                player.visit(board.getTile(nextPos));
                break;
            case 'e':
                player.castAbility(enemies);
                break;
            default:
                playerTurn(); // Ask again
                break;
        }
    }

    /**
     * Executes all enemies' turns.
     */
    private void enemiesTurn() {
        for(Enemy e : enemies) {
            e.setPlayerPosition(player.getPosition());
            e.gameTick();
            e.visit(board.getTile(e.getDesiredPosition()));
        }
    }

    /**
     * Evaluates the current level status and advances or ends the game as needed.
     */
    private void evaluateLevelStatus() {
        if (player.isDead() || (allEnemiesDead() && hasEnded()))
        {
            gameRunning = false;
            return;
        }

        if (allEnemiesDead())
        {
            currentLevel++;
            loadLevel();
        }
    }

    /**
     * Checks if all enemies are dead.
     * @return true if all enemies are dead, false otherwise
     */
    private boolean allEnemiesDead() {
        return enemies.isEmpty();
    }

    /**
     * Callback method invoked when the player dies.
     * Stops the game loop by setting gameRunning to false.
     */
    private void playerDied() { gameRunning = false; }

    /**
     * Handles the end of the game, displaying win or loss messages.
     */
    private void handleGameOver() {
        if(!player.isDead())
            mcb.call("You Won!");
        else {
            mcb.call("You lost\n");
            roundStats();
            mcb.call("Game Over.");
        }
    }

    /**
     * Checks if all levels have been completed.
     * @return true if the game has ended, false otherwise
     */
    private boolean hasEnded() {
        return currentLevel ==  mapLoader.NUM_OF_LEVELS - 1;
    }

    /**
     * Callback for when an enemy is created; adds the enemy to the list and sets callbacks.
     * @param e the created enemy
     */
    private void enemyCreated(Enemy e) {
        this.enemies.add(e);
        e.setCallbacks(mcb,pcb,edcb);
    }

    /**
     * Callback for when an enemy dies; updates player XP, board, and enemy list.
     * @param e the dead enemy
     */
    private void enemyDied(Enemy e) {
        player.gainXP(e.getXP());
        Position pos = e.getPosition();
        Empty empty = new Empty();
        empty.setPosition(pos);
        board.setTile(empty,pos);
        enemies.remove(e);
    }

}
