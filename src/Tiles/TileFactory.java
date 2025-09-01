package Tiles;

import Callbacks.EnemyCreatedCallback;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Enemies.Monster;
import Tiles.Units.Enemies.Trap;
import Tiles.Units.Players.Mage;
import Tiles.Units.Players.Player;
import Tiles.Units.Players.Rogue;
import Tiles.Units.Players.Warrior;
import Tiles.Units.Players.Hunter; // Import the new Hunter class
import Utils.Position;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class TileFactory {
    private static Player selectedPlayer;

    private static final Map<Character, Supplier<Enemy>> enemyMap = Map.ofEntries(
            Map.entry('s', () -> new Monster("Lannister Soldier", 's', 80, 8, 3, 3, 25)),
            Map.entry('k', () -> new Monster("Lannister Knight", 'k', 200, 14, 8, 4, 50)),
            Map.entry('q', () -> new Monster("Queen’s Guard", 'q', 400, 20, 15, 5, 100)),
            Map.entry('z', () -> new Monster("Wright", 'z', 600, 30, 15, 3, 100)),
            Map.entry('b', () -> new Monster("Bear-Wright", 'b', 1000, 75, 30, 4, 250)),
            Map.entry('g', () -> new Monster("Giant-Wright", 'g', 1500, 100, 40, 5, 500)),
            Map.entry('w', () -> new Monster("White Walker", 'w', 2000, 150, 50, 6, 1000)),
            Map.entry('M', () -> new Monster("The Mountain", 'M', 1000, 60, 25, 6, 500)),
            Map.entry('C', () -> new Monster("Queen Cersei", 'C', 100, 10, 10, 1, 1000)),
            Map.entry('K', () -> new Monster("Night’s King", 'K', 5000, 300, 150, 8, 5000)),
            Map.entry('B', () -> new Trap("Bonus Trap", 'B', 1, 1, 1, 250, 1, 5)),
            Map.entry('Q', () -> new Trap("Queen’s Trap", 'Q', 250, 50, 10, 100, 3, 7)),
            Map.entry('D', () -> new Trap("Death Trap", 'D', 500, 100, 20, 250, 1, 10))
    );

    private static final List<Supplier<Player>> playerSuppliers = List.of(
            () -> new Warrior("Jon Snow", 300, 30, 4, 3),
            () -> new Warrior("The Hound", 400, 20, 6, 5),
            () -> new Mage("Melisandre", 100, 5, 1, 300, 30,15 ,5, 6),
            () -> new Mage("Thoros of Myr", 250, 25, 4, 150, 20,20,3, 4),
            () -> new Rogue("Arya Stark", 150, 40, 2,20),
            () -> new Rogue("Bronn", 250, 35, 3,50),
            () -> new Hunter("Ygritte", 220, 30, 2, 6)
    );

    /**
     * Creates a tile based on the given character, position, and enemy creation callback.
     * @param tileChar the character representing the tile type
     * @param position the position of the tile on the board
     * @param eccb the callback to call when an enemy is created
     * @return the created Tile
     * @throws IllegalArgumentException if the tile character is unknown
     */
    public static Tile createTile(char tileChar, Position position, EnemyCreatedCallback eccb) {
        Tile tile;
        switch (tileChar) {
            case '.':
                tile = new Empty();
                break;
            case '#':
                tile = new Wall();
                break;
            case '@':
                tile = selectedPlayer;
                break;
            default:
                if (enemyMap.containsKey(tileChar)) {
                    Enemy e = enemyMap.get(tileChar).get();
                    eccb.call(e);
                    tile = e;
                } else {
                    throw new IllegalArgumentException("Unknown tile character: " + tileChar);
                }

        }
        tile.setPosition(position);
        return tile;
    }

    /**
     * Returns a string describing all available player types.
     * @return a formatted string with player descriptions
     */
    public static String PlayersDescription() {
        int i = 1;
        StringBuilder output = new StringBuilder();
        for (Supplier<Player> playerSupplier : playerSuppliers) {
            Player player = playerSupplier.get();
            output.append(i).append(". ").append(player.description()).append('\n');
            i++;
        }
        return output.deleteCharAt(output.length() - 1).toString();
    }

    /**
     * Selects and returns a player based on the user's choice.
     * @param choice the index of the chosen player (1-based)
     * @return the selected Player
     */
    public static Player GetPlayer(int choice) {
        selectedPlayer = playerSuppliers.get(choice - 1).get();
        return selectedPlayer;
    }

    /**
     * Returns the total number of available player characters.
     * @return the player count
     */
    public static int getPlayerCount() {
        return playerSuppliers.size();
    }
}