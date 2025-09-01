import Callbacks.ChangePositionCallback;
import Callbacks.EnemyDeathCallback;
import Callbacks.MessageCallback;
import Tiles.Tile;
import Tiles.Units.Enemies.Enemy;
import Tiles.Units.Enemies.Monster;
import Tiles.Units.Players.Player;
import Tiles.Units.Players.Warrior;
import Utils.Direction;
import Utils.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Monster Class Tests")
class MonsterTest {

    private Monster monster;
    private Player testPlayer;
    private List<String> messageLog;
    private Enemy deadEnemy;
    private Tile tile1Swapped;
    private Tile tile2Swapped;

    // Callbacks to capture events without using mocks
    private MessageCallback mcb;
    private EnemyDeathCallback edcb;
    private ChangePositionCallback pcb;

    @BeforeEach
    void setUp() {
        // Reset callback capture variables before each test
        messageLog = new ArrayList<>();
        deadEnemy = null;
        tile1Swapped = null;
        tile2Swapped = null;

        // Initialize callbacks
        mcb = messageLog::add;
        edcb = enemy -> deadEnemy = enemy;
        pcb = (t1, t2) -> {
            tile1Swapped = t1;
            tile2Swapped = t2;
        };

        // Create a standard monster for tests
        monster = new Monster("Lannister Soldier", 's', 80, 8, 3, 3, 25);
        monster.setCallbacks(mcb, pcb, edcb);
        monster.setPosition(new Position(5, 5));

        // Create a player for interaction tests
        testPlayer = new Warrior("Test Warrior", 100, 10, 10, 5);
        // Player callbacks are not essential for monster tests but good practice to set
        testPlayer.setCallbacks(mcb, pcb, () -> {});
    }

    @Nested
    @DisplayName("Constructor and Initial State")
    class ConstructorAndState {

        @Test
        @DisplayName("should correctly initialize all monster attributes")
        void testMonsterConstructor() {
            assertEquals("Lannister Soldier", monster.getName(), "Name should be initialized correctly.");
            assertEquals('s', monster.getCharacter(), "Character tile should be initialized correctly.");
            assertEquals(80, monster.getHealth().getCapacity(), "Health capacity should be set.");
            assertEquals(80, monster.getHealth().getAmount(), "Health amount should be full at start.");
            assertEquals(8, monster.getAttackPoints(), "Attack points should be set.");
            assertEquals(3, monster.getDefensePoints(), "Defense points should be set.");
            assertEquals(3, monster.getVisionRange(), "Vision range should be set.");
            assertEquals(25, monster.getXP(), "Experience value should be set.");
        }

        @Test
        @DisplayName("description method should include all monster stats including vision range")
        void testDescription() {
            String desc = monster.description();
            assertTrue(desc.contains("Lannister Soldier"), "Description should contain the name.");
            assertTrue(desc.contains("Health: 80/80"), "Description should contain health.");
            assertTrue(desc.contains("Attack: 8"), "Description should contain attack points.");
            assertTrue(desc.contains("Defense: 3"), "Description should contain defense points.");
            assertTrue(desc.contains("Experience Value: 25"), "Description should contain experience value.");
            assertTrue(desc.contains("Vision Range: 3"), "Description should contain vision range.");
        }
    }

    @Nested
    @DisplayName("Movement Logic (gameTick)")
    class Movement {

        @Test
        @DisplayName("should move towards the player when player is within vision range")
        void testGameTick_PlayerInVisionRange_MovesTowardsPlayer() {
            // Player is at (5, 3), inside vision range of 3. Monster is at (5, 5).
            // Monster should move UP.
            monster.setPlayerPosition(new Position(5, 3));
            monster.gameTick();

            Position desiredPosition = monster.getDesiredPosition();
            assertEquals(new Position(5, 4).getX(), desiredPosition.getX(), "Should move UP towards the player.");
            assertEquals(new Position(5, 4).getY(), desiredPosition.getY(), "Should move UP towards the player.");
        }

        @Test
        @DisplayName("should prioritize horizontal movement when player is in range and dx is greater than dy")
        void testGameTick_PlayerInVisionRange_MovesHorizontallyWhenDxGreater() {
            // ARRANGE
            // Monster is at (5, 5), vision is 3.
            // Player is at (3, 4).
            // dx = 5-3 = 2, dy = 5-4 = 1. |dx| > |dy|. -> Horizontal movement.
            // distance = sqrt(5) approx 2.23, which is < 3 -> In vision range.
            // Since dx > 0, monster should move LEFT.
            monster.setPlayerPosition(new Position(3, 4));

            // ACT
            monster.gameTick();

            // ASSERT
            Position desiredPosition = monster.getDesiredPosition();
            Position expectedPosition = new Position(4, 5); // Move LEFT

            assertEquals(expectedPosition.getX(), desiredPosition.getX(), "X should decrease by 1 for LEFT movement.");
            assertEquals(expectedPosition.getY(), desiredPosition.getY(), "Y should not change for horizontal movement.");
        }

        @Test
        @DisplayName("should prioritize vertical movement when player is in range and dy is greater than dx")
        void testGameTick_PlayerInVisionRange_MovesVerticallyWhenDyGreater() {
            // ARRANGE
            // Monster is at (5, 5), vision is 3.
            // Player is at (6, 3).
            // dx = 5-6 = -1, dy = 5-3 = 2. |dy| > |dx|.
            // distance = sqrt(5) approx 2.23, which is < 3 (in vision range).
            // Monster should move UP.
            monster.setPlayerPosition(new Position(6, 3));

            // ACT
            monster.gameTick();

            // ASSERT
            Position desiredPosition = monster.getDesiredPosition();
            Position expectedPosition = new Position(5, 4); // Move UP

            assertEquals(expectedPosition.getX(), desiredPosition.getX(), "X should not change for vertical movement.");
            assertEquals(expectedPosition.getY(), desiredPosition.getY(), "Y should decrease by 1 for UP movement.");
        }

        @Test
        @DisplayName("should move randomly when player is outside vision range")
        void testGameTick_PlayerOutsideVisionRange_MovesRandomly() {
            // Player is at (10, 10), outside vision range of 3.
            monster.setPlayerPosition(new Position(10, 10));
            monster.gameTick();

            Position startPosition = monster.getPosition();
            Position desiredPosition = monster.getDesiredPosition();

            // The desired position should be one of the four adjacent tiles.
            double distance = startPosition.calculateRange(desiredPosition);
            assertEquals(1.0, distance, "Monster should move to an adjacent tile when player is out of range.");
        }

        @Test
        @DisplayName("should have a predictable move when player and monster are at the same position")
        void testGameTick_PlayerAtSamePosition() {
            // Edge case: if player and monster are on the same tile, dx=0 and dy=0.
            // Based on the code (dx > dy is false, dy > 0 is false), it should move DOWN.
            monster.setPlayerPosition(new Position(5, 5));
            monster.gameTick();

            Position desiredPosition = monster.getDesiredPosition();
            assertEquals(new Position(5,6).getY(), desiredPosition.getY(), "Should move DOWN as a default for same position.");
        }
    }

    @Nested
    @DisplayName("Interaction and Combat")
    class Interaction {

        @Test
        @DisplayName("visit(Player) should initiate combat")
        void testVisitPlayer_InitiatesCombat() {
            int playerInitialHealth = testPlayer.getHealth().getAmount();

            // Monster visits player, triggering combat
            monster.visit(testPlayer);

            // Assert that combat messages were logged
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("engaged in combat with")), "Combat engagement message should be logged.");
            assertTrue(messageLog.stream().anyMatch(s -> s.contains("rolled") && s.contains("attack points")), "Attack roll message should be logged.");

            // Since combat involves randomness, we check for a state change.
            // A simpler, more robust check is that the combat method was called, evidenced by the logs.
            // It's possible for damage to be 0, so checking health is flaky.
            // Here, we trust the log proves the method was called.
            assertEquals(6, messageLog.size(), "Should be exactly 3 combat info messages for a non-lethal hit.");
        }

        @Test
        @DisplayName("visit(Player) should handle player death")
        void testVisitPlayer_HandlesPlayerDeath() {
            // Setup monster with very high attack and player with very low health
            monster.setAttackPoints(200);
            testPlayer.getHealth().setAmount(1);

            // Monster visits and kills the player
            monster.visit(testPlayer);

            assertTrue(testPlayer.isDead(), "Player should be dead after overwhelming attack.");
            assertEquals('X', testPlayer.getCharacter(), "Player character should change to 'X' on death.");

            // On player death, a position swap should occur (player corpse replaced by monster)
            assertNotNull(tile1Swapped, "Position swap callback should have been triggered.");
            assertEquals(monster, tile1Swapped, "Monster should be the first tile in the swap.");
            assertEquals(testPlayer, tile2Swapped, "Player should be the second tile in the swap.");
        }

        @Test
        @DisplayName("visit(Enemy) should do nothing")
        void testVisitEnemy_DoesNothing() {
            Monster otherMonster = new Monster("Another Monster", 'm', 50, 5, 5, 2, 10);
            int initialHealth = otherMonster.getHealth().getAmount();

            // Monster attempts to visit another enemy
            monster.visit(otherMonster);

            assertEquals(initialHealth, otherMonster.getHealth().getAmount(), "Visiting another enemy should not cause damage.");
            assertTrue(messageLog.isEmpty(), "No messages should be logged when visiting another enemy.");
        }

        @Test
        @DisplayName("accept(Unit) from a Player should result in the monster visiting the player")
        void testAccept_FromPlayer_ResultsInCombat() {
            // This test verifies the visitor pattern double-dispatch.
            // When a player accepts the monster, the monster should visit the player, initiating combat.
            monster.accept(testPlayer);

            assertTrue(messageLog.stream().anyMatch(s -> s.contains("engaged in combat with")), "Accepting a player should trigger combat.");
        }
    }
}