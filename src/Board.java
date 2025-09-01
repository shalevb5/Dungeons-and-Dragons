import Callbacks.ChangePositionCallback;
import Callbacks.EnemyCreatedCallback;
import Callbacks.MessageCallback;
import Tiles.Tile;
import Tiles.TileFactory;
import Utils.Position;

import java.util.List;

public class Board {
    private Tile[][] tiles;
    private int width, height;
    private ChangePositionCallback pcb;
    private MessageCallback mcb;
    private EnemyCreatedCallback eccb;

    /**
     * Constructs a new Board with the given message and enemy created callbacks.
     * @param mcb the message callback for board messages
     * @param eccb the callback for when an enemy is created
     */
    public Board(MessageCallback mcb, EnemyCreatedCallback eccb) {
        this.pcb = this::swapTiles;
        this.eccb = eccb;
        this.mcb = mcb;
    }

    /**
     * Sets up the board tiles from the given level map.
     * @param levelMap the list of strings representing the level map
     */
    public void setBoardMap(List<String> levelMap) {
        height = levelMap.size();
        width = levelMap.getFirst().length();
        tiles = new Tile[width][height];
        initializeBoard(levelMap);
    }

    /**
     * Returns the change position callback for swapping tiles.
     * @return the change position callback
     */
    public ChangePositionCallback getPcb() { return this.pcb; }

    /**
     * Sets the tile at the specified position.
     * @param t the tile to set
     * @param p the position to set the tile at
     */
    public void setTile(Tile t, Position p) {
        tiles[p.getX()][p.getY()] = t;
    }

    /**
     * Returns the tile at the specified position.
     * @param pos the position of the tile
     * @return the tile at the given position
     */
    public Tile getTile(Position pos) {
        return tiles[pos.getX()][pos.getY()];
    }

    /**
     * Returns a string representation of the board.
     * @return the board as a string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(tiles[x][y].getCharacter());
            }
            sb.append('\n');
        }
        return sb.toString();
    }


    /**
     * Initializes the board tiles from the level map.
     * @param levelMap the list of strings representing the level map
     */
    private void initializeBoard(List<String> levelMap) {
        for(int y = 0; y < height; y++) {
            String row = levelMap.get(y);
            for(int x = 0; x < width; x++) {
                Position pos = new Position(x,y);
                tiles[x][y] = TileFactory.createTile(row.charAt(x),pos,eccb);

            }
        }
    }

    /**
     * Swaps the positions of two tiles on the board.
     * @param t1 the first tile
     * @param t2 the second tile
     */
    private void swapTiles(Tile t1, Tile t2) {
        Position pos1 = t1.getPosition();
        Position pos2 = t2.getPosition();

        setTile(t1,pos2);
        setTile(t2,pos1);

        t1.setPosition(pos2);
        t2.setPosition(pos1);
    }
}
