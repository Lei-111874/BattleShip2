package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Move.
 * Author: martimcordeiro
 * Date: 2026-11-17 22:15
 *
 * Cyclomatic Complexity:
 * constructor: 1
 * toString(): 1
 * getNumber(): 1
 * getShots(): 1
 * getShotResults(): 1
 * processEnemyFire(): 26
 */
public class MoveTest {

    private Move move;
    private List<IPosition> defaultShots;
    private List<IGame.ShotResult> defaultResults;

    @BeforeEach
    void setUp() {
        defaultShots = new ArrayList<>();
        defaultShots.add(new Position(1, 1));
        defaultResults = new ArrayList<>();
        move = new Move(1, defaultShots, defaultResults);
    }

    @AfterEach
    void tearDown() {
        move = null;
        defaultShots = null;
        defaultResults = null;
    }

    @Test
    @DisplayName("Constructor initializes move number, shots, and results properly")
    void constructor() {
        Move customMove = new Move(5, defaultShots, defaultResults);
        assertAll(
                () -> assertEquals(5, customMove.getNumber(), "Error: expected move number 5 but got " + customMove.getNumber()),
                () -> assertSame(defaultShots, customMove.getShots(), "Error: expected same shots list reference"),
                () -> assertSame(defaultResults, customMove.getShotResults(), "Error: expected same shotResults list reference")
        );
    }

    @Test
    @DisplayName("toString returns formatted string with move number, shots size, and results size")
    void toStringTest() {
        String expected = "Move{number=1, shots=1, results=0}";
        String actual = move.toString();
        assertEquals(expected, actual, "Error: expected string '" + expected + "' but got '" + actual + "'");
    }

    @Test
    @DisplayName("getNumber returns the assigned move number")
    void getNumber() {
        assertEquals(1, move.getNumber(), "Error: expected move number 1 but got " + move.getNumber());
    }

    @Test
    @DisplayName("getShots returns the list of positions shot in the move")
    void getShots() {
        assertEquals(1, move.getShots().size(), "Error: expected shots list of size 1 but got " + move.getShots().size());
    }

    @Test
    @DisplayName("getShotResults returns the list of shot results")
    void getShotResults() {
        assertTrue(move.getShotResults().isEmpty(), "Error: expected empty shotResults list initially");
    }

    // --- processEnemyFire(boolean verbose) independent paths (CC = 26) ---

    @Test
    @DisplayName("processEnemyFire1: non-verbose mode with empty shot results (all outside shots)")
    void processEnemyFire1() {
        String json = move.processEnemyFire(false);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 0"), "Error: expected validShots to be 0 in JSON"),
                () -> assertTrue(json.contains("\"outsideShots\" : " + Game.NUMBER_SHOTS), "Error: expected outsideShots equal to Game.NUMBER_SHOTS"),
                () -> assertTrue(json.contains("\"repeatedShots\" : 0"), "Error: expected repeatedShots to be 0"),
                () -> assertTrue(json.contains("\"missedShots\" : 0"), "Error: expected missedShots to be 0")
        );
    }

    @Test
    @DisplayName("processEnemyFire2: invalid shot is ignored")
    void processEnemyFire2() {
        defaultResults.add(new IGame.ShotResult(false, false, null, false));
        String json = move.processEnemyFire(false);
        assertTrue(json.contains("\"validShots\" : 0"), "Error: expected invalid shot to be ignored leaving validShots at 0");
    }

    @Test
    @DisplayName("processEnemyFire3: single valid repeated shot in non-verbose mode")
    void processEnemyFire3() {
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        String json = move.processEnemyFire(false);
        assertTrue(json.contains("\"repeatedShots\" : 1"), "Error: expected repeatedShots to be 1");
    }

    @Test
    @DisplayName("processEnemyFire4: single valid missed shot (water) in non-verbose mode")
    void processEnemyFire4() {
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        String json = move.processEnemyFire(false);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 1"), "Error: expected validShots to be 1"),
                () -> assertTrue(json.contains("\"missedShots\" : 1"), "Error: expected missedShots to be 1")
        );
    }

    @Test
    @DisplayName("processEnemyFire5: valid hit on a ship without sinking it")
    void processEnemyFire5() {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        defaultResults.add(new IGame.ShotResult(true, false, barge, false));
        String json = move.processEnemyFire(false);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 1"), "Error: expected validShots to be 1"),
                () -> assertTrue(json.contains("\"hitsOnBoats\""), "Error: expected hitsOnBoats section in JSON"),
                () -> assertTrue(json.contains("\"hits\" : 1"), "Error: expected 1 hit on boat")
        );
    }

    @Test
    @DisplayName("processEnemyFire6: valid hit that sinks a ship")
    void processEnemyFire6() {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        defaultResults.add(new IGame.ShotResult(true, false, barge, true));
        String json = move.processEnemyFire(false);
        assertAll(
                () -> assertTrue(json.contains("\"sunkBoats\""), "Error: expected sunkBoats section in JSON"),
                () -> assertTrue(json.contains("\"count\" : 1"), "Error: expected sunk count to be 1")
        );
    }

    @Test
    @DisplayName("processEnemyFire7: verbose mode with zero valid shots and single repeated shot")
    void processEnemyFire7() {
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"repeatedShots\" : 1"), "Error: expected repeatedShots to be 1 in verbose mode");
    }

    @Test
    @DisplayName("processEnemyFire8: verbose mode with zero valid shots and multiple repeated shots")
    void processEnemyFire8() {
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"repeatedShots\" : 2"), "Error: expected repeatedShots to be 2 in verbose mode");
    }

    @Test
    @DisplayName("processEnemyFire9: verbose mode with zero valid shots and zero repeated shots (only outside shots)")
    void processEnemyFire9() {
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"outsideShots\" : " + Game.NUMBER_SHOTS), "Error: expected all shots to be outside shots");
    }

    @Test
    @DisplayName("processEnemyFire10: verbose mode with single valid missed shot")
    void processEnemyFire10() {
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 1"), "Error: expected validShots to be 1"),
                () -> assertTrue(json.contains("\"missedShots\" : 1"), "Error: expected missedShots to be 1")
        );
    }

    @Test
    @DisplayName("processEnemyFire11: verbose mode with multiple valid missed shots")
    void processEnemyFire11() {
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 2"), "Error: expected validShots to be 2"),
                () -> assertTrue(json.contains("\"missedShots\" : 2"), "Error: expected missedShots to be 2")
        );
    }

    @Test
    @DisplayName("processEnemyFire12: verbose mode with single sunk ship and no missed shots (trailing '+' removal)")
    void processEnemyFire12() {
        IShip barge = new Barge(Compass.NORTH, new Position(2, 2));
        defaultResults.add(new IGame.ShotResult(true, false, barge, true));
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"count\" : 1"), "Error: expected 1 sunk boat");
    }

    @Test
    @DisplayName("processEnemyFire13: verbose mode with multiple sunk ships of same category")
    void processEnemyFire13() {
        IShip barge1 = new Barge(Compass.NORTH, new Position(1, 1));
        IShip barge2 = new Barge(Compass.NORTH, new Position(3, 3));
        defaultResults.add(new IGame.ShotResult(true, false, barge1, true));
        defaultResults.add(new IGame.ShotResult(true, false, barge2, true));
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"count\" : 2"), "Error: expected 2 sunk boats of same category");
    }

    @Test
    @DisplayName("processEnemyFire14: verbose mode with single hit on non-sunk ship and no missed shots")
    void processEnemyFire14() {
        IShip frigate = new Frigate(Compass.NORTH, new Position(2, 2));
        defaultResults.add(new IGame.ShotResult(true, false, frigate, false));
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"hits\" : 1"), "Error: expected 1 hit on Frigate");
    }

    @Test
    @DisplayName("processEnemyFire15: verbose mode with multiple hits on same non-sunk ship")
    void processEnemyFire15() {
        IShip frigate = new Frigate(Compass.NORTH, new Position(2, 2));
        defaultResults.add(new IGame.ShotResult(true, false, frigate, false));
        defaultResults.add(new IGame.ShotResult(true, false, frigate, false));
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"hits\" : 2"), "Error: expected 2 hits on Frigate");
    }

    @Test
    @DisplayName("processEnemyFire16: verbose mode with one ship sunk and another ship of same category hit")
    void processEnemyFire16() {
        IShip barge1 = new Barge(Compass.NORTH, new Position(1, 1));
        IShip barge2 = new Barge(Compass.NORTH, new Position(4, 4));
        defaultResults.add(new IGame.ShotResult(true, false, barge1, true));
        defaultResults.add(new IGame.ShotResult(true, false, barge2, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"sunkBoats\""), "Error: expected sunkBoats in JSON"),
                () -> assertTrue(json.contains("\"count\" : 1"), "Error: expected sunk count of 1 for category")
        );
    }

    @Test
    @DisplayName("processEnemyFire17: verbose mode with sunk ship and missed shot combined")
    void processEnemyFire17() {
        IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
        defaultResults.add(new IGame.ShotResult(true, false, barge, true));
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"count\" : 1"), "Error: expected 1 sunk boat"),
                () -> assertTrue(json.contains("\"missedShots\" : 1"), "Error: expected 1 missed shot")
        );
    }

    @Test
    @DisplayName("processEnemyFire18: verbose mode with hit on ship and missed shot combined")
    void processEnemyFire18() {
        IShip frigate = new Frigate(Compass.NORTH, new Position(1, 1));
        defaultResults.add(new IGame.ShotResult(true, false, frigate, false));
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"hits\" : 1"), "Error: expected 1 hit on ship"),
                () -> assertTrue(json.contains("\"missedShots\" : 1"), "Error: expected 1 missed shot")
        );
    }

    @Test
    @DisplayName("processEnemyFire19: verbose mode with valid shot and single repeated shot")
    void processEnemyFire19() {
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 1"), "Error: expected validShots to be 1"),
                () -> assertTrue(json.contains("\"repeatedShots\" : 1"), "Error: expected repeatedShots to be 1")
        );
    }

    @Test
    @DisplayName("processEnemyFire20: verbose mode with valid shot and multiple repeated shots")
    void processEnemyFire20() {
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 1"), "Error: expected validShots to be 1"),
                () -> assertTrue(json.contains("\"repeatedShots\" : 2"), "Error: expected repeatedShots to be 2")
        );
    }

    @Test
    @DisplayName("processEnemyFire21: verbose mode with exact Game.NUMBER_SHOTS valid shots (outsideShots == 0)")
    void processEnemyFire21() {
        for (int i = 0; i < Game.NUMBER_SHOTS; i++) {
            defaultResults.add(new IGame.ShotResult(true, false, null, false));
        }
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"outsideShots\" : 0"), "Error: expected outsideShots to be 0 when all shots are valid");
    }

    @Test
    @DisplayName("processEnemyFire22: verbose mode with exact Game.NUMBER_SHOTS - 1 valid shots (outsideShots == 1)")
    void processEnemyFire22() {
        for (int i = 0; i < Game.NUMBER_SHOTS - 1; i++) {
            defaultResults.add(new IGame.ShotResult(true, false, null, false));
        }
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"outsideShots\" : 1"), "Error: expected outsideShots to be 1");
    }

    @Test
    @DisplayName("processEnemyFire23: verbose mode with invalid shots ignored leaving all shots as outside shots")
    void processEnemyFire23() {
        for (int i = 0; i < Game.NUMBER_SHOTS - 1; i++) {
            defaultResults.add(new IGame.ShotResult(false, false, null, false));
        }
        String json = move.processEnemyFire(true);
        assertTrue(json.contains("\"outsideShots\" : " + Game.NUMBER_SHOTS),
                "Error: expected outsideShots to equal Game.NUMBER_SHOTS when all shots are invalid");
    }

    @Test
    @DisplayName("processEnemyFire24: verbose mode combining sunk boat, hit boat, missed shot, repeated shot, and outside shot")
    void processEnemyFire24() {
        IShip barge = new Barge(Compass.NORTH, new Position(0, 0));
        IShip frigate = new Frigate(Compass.EAST, new Position(3, 3));
        defaultResults.add(new IGame.ShotResult(true, false, barge, true));
        defaultResults.add(new IGame.ShotResult(true, false, frigate, false));
        defaultResults.add(new IGame.ShotResult(true, false, null, false));
        defaultResults.add(new IGame.ShotResult(true, true, null, false));
        String json = move.processEnemyFire(true);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 3"), "Error: expected validShots to be 3"),
                () -> assertTrue(json.contains("\"repeatedShots\" : 1"), "Error: expected repeatedShots to be 1"),
                () -> assertTrue(json.contains("\"missedShots\" : 1"), "Error: expected missedShots to be 1")
        );
    }

    @Test
    @DisplayName("processEnemyFire25: non-verbose mode with multiple sunk ships of different categories")
    void processEnemyFire25() {
        IShip barge = new Barge(Compass.NORTH, new Position(0, 0));
        IShip frigate = new Frigate(Compass.EAST, new Position(3, 3));
        defaultResults.add(new IGame.ShotResult(true, false, barge, true));
        defaultResults.add(new IGame.ShotResult(true, false, frigate, true));
        String json = move.processEnemyFire(false);
        assertAll(
                () -> assertTrue(json.contains("\"validShots\" : 2"), "Error: expected validShots to be 2"),
                () -> assertTrue(json.contains("\"sunkBoats\""), "Error: expected sunkBoats list in JSON")
        );
    }

    @Test
    @DisplayName("processEnemyFire26: throws NullPointerException when shotResults list is null")
    void processEnemyFire26() {
        Move nullResultsMove = new Move(2, defaultShots, null);
        assertThrows(NullPointerException.class, () -> nullResultsMove.processEnemyFire(false),
                "Error: expected NullPointerException when shotResults is null");
    }
}