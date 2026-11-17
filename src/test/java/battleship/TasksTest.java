package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Tasks.
 * Author: martimcordeiro
 * Date: 2026-11-18 21:59
 *
 * Cyclomatic Complexity:
 * constructor: 1
 * menu(): 27
 * menuHelp(): 1
 * buildFleet(): 5
 * readShip(): 2
 * readPosition(): 2
 * readClassicPosition(): 7
 */
public class TasksTest {

	private Tasks tasks;
	private InputStream originalSystemIn;

	// 11 non-adjacent valid ships on a 10x10 board (rows 0..9, cols 0..9)
	private static final String VALID_FLEET_INPUT =
			"galeao 0 1 s " +
					"fragata 0 4 s " +
					"caravela 0 6 s " +
					"caravela 0 8 s " +
					"caravela 3 8 s " +
					"nau 5 0 s " +
					"nau 7 3 s " +
					"barca 6 8 n " +
					"barca 8 8 n " +
					"barca 9 0 n " +
					"barca 5 6 n ";

	@BeforeEach
	void setUp() {
		tasks = new Tasks();
		originalSystemIn = System.in;
	}

	@AfterEach
	void tearDown() {
		tasks = null;
		System.setIn(originalSystemIn);
	}

	private void provideInput(String data) {
		ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
		System.setIn(testIn);
	}

	// --- constructor (CC = 1) ---

	@Test
	@DisplayName("Constructor creates a valid Tasks instance")
	void constructor() {
		Tasks instance = new Tasks();
		assertNotNull(instance, "Error: expected non-null Tasks instance");
	}

	// --- menuHelp() (CC = 1) ---

	@Test
	@DisplayName("menuHelp prints menu instructions without throwing exceptions")
	void menuHelp() {
		assertDoesNotThrow(Tasks::menuHelp, "Error: menuHelp should execute without throwing any exception");
	}

	// --- readPosition(Scanner) (CC = 2) ---

	@Test
	@DisplayName("readPosition1: reads valid row and column integers into a Position")
	void readPosition1() {
		Scanner scanner = new Scanner("3 5");
		Position pos = Tasks.readPosition(scanner);
		assertAll(
				() -> assertEquals(3, pos.getRow(), "Error: expected row 3 but got " + pos.getRow()),
				() -> assertEquals(5, pos.getColumn(), "Error: expected column 5 but got " + pos.getColumn())
		);
	}

	@Test
	@DisplayName("readPosition2: throws AssertionError when scanner is null")
	void readPosition2() {
		assertThrows(AssertionError.class, () -> Tasks.readPosition(null),
				"Error: expected AssertionError when passing null Scanner to readPosition");
	}

	// --- readShip(Scanner) (CC = 2) ---

	@Test
	@DisplayName("readShip1: reads ship type, coordinates, and bearing to build a valid Ship")
	void readShip1() {
		Scanner scanner = new Scanner("barca 2 4 n");
		Ship ship = Tasks.readShip(scanner);
		assertAll(
				() -> assertNotNull(ship, "Error: expected a non-null Ship instance for 'barca'"),
				() -> assertEquals("Barca", ship.getCategory(), "Error: expected category 'Barca' but got " + ship.getCategory()),
				() -> assertEquals(Compass.NORTH, ship.getBearing(), "Error: expected NORTH bearing")
		);
	}

	@Test
	@DisplayName("readShip2: throws AssertionError when scanner is null")
	void readShip2() {
		assertThrows(AssertionError.class, () -> Tasks.readShip(null),
				"Error: expected AssertionError when passing null Scanner to readShip");
	}

	// --- buildFleet(Scanner) (CC = 5) ---

	@Test
	@DisplayName("buildFleet1: builds complete valid fleet successfully")
	void buildFleet1() {
		Scanner scanner = new Scanner(VALID_FLEET_INPUT);
		Fleet fleet = Tasks.buildFleet(scanner);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
				"Error: expected fleet size " + Fleet.FLEET_SIZE + " but got " + fleet.getShips().size());
	}

	@Test
	@DisplayName("buildFleet2: handles unknown ship name (s == null) before completing fleet")
	void buildFleet2() {
		Scanner scanner = new Scanner("submarino 0 0 n " + VALID_FLEET_INPUT);
		Fleet fleet = Tasks.buildFleet(scanner);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
				"Error: expected fleet to recover from unknown ship and reach size " + Fleet.FLEET_SIZE);
	}

	@Test
	@DisplayName("buildFleet3: handles ship placement collision (addShip returns false)")
	void buildFleet3() {
		String inputWithCollision = "galeao 0 1 s barca 0 1 n " +
				"fragata 0 4 s caravela 0 6 s caravela 0 8 s caravela 3 8 s " +
				"nau 5 0 s nau 7 3 s barca 6 8 n barca 8 8 n barca 9 0 n barca 5 6 n";
		Scanner scanner = new Scanner(inputWithCollision);
		Fleet fleet = Tasks.buildFleet(scanner);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
				"Error: expected fleet to ignore colliding ship and reach size " + Fleet.FLEET_SIZE);
	}

	@Test
	@DisplayName("buildFleet4: throws AssertionError when scanner is null")
	void buildFleet4() {
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null),
				"Error: expected AssertionError when passing null Scanner to buildFleet");
	}

	@Test
	@DisplayName("buildFleet5: handles out-of-bounds ship coordinates before valid placements")
	void buildFleet5() {
		Scanner scanner = new Scanner("galeao 9 9 s " + VALID_FLEET_INPUT);
		Fleet fleet = Tasks.buildFleet(scanner);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
				"Error: expected fleet to skip out-of-bounds ship and reach size " + Fleet.FLEET_SIZE);
	}

	// --- readClassicPosition(Scanner) (CC = 7) ---

	@Test
	@DisplayName("readClassicPosition1: throws IllegalArgumentException when scanner has no tokens")
	void readClassicPosition1() {
		Scanner emptyScanner = new Scanner("");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(emptyScanner),
				"Error: expected IllegalArgumentException for empty scanner input");
	}

	@Test
	@DisplayName("readClassicPosition2: parses compact uppercase classic position like 'B4'")
	void readClassicPosition2() {
		Scanner scanner = new Scanner("B4");
		IPosition pos = Tasks.readClassicPosition(scanner);
		assertNotNull(pos, "Error: expected non-null IPosition for 'B4'");
	}

	@Test
	@DisplayName("readClassicPosition3: parses compact lowercase classic position like 'c7'")
	void readClassicPosition3() {
		Scanner scanner = new Scanner("c7");
		IPosition pos = Tasks.readClassicPosition(scanner);
		assertNotNull(pos, "Error: expected non-null IPosition for 'c7'");
	}

	@Test
	@DisplayName("readClassicPosition4: parses space-separated classic position like 'A 3'")
	void readClassicPosition4() {
		Scanner scanner = new Scanner("A 3");
		IPosition pos = Tasks.readClassicPosition(scanner);
		assertNotNull(pos, "Error: expected non-null IPosition for 'A 3'");
	}

	@Test
	@DisplayName("readClassicPosition5: throws IllegalArgumentException for invalid non-numeric input like 'AA'")
	void readClassicPosition5() {
		Scanner scanner = new Scanner("AA");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(scanner),
				"Error: expected IllegalArgumentException for invalid format 'AA'");
	}

	@Test
	@DisplayName("readClassicPosition6: throws IllegalArgumentException when two tokens do not form valid coordinate like '12 34'")
	void readClassicPosition6() {
		Scanner scanner = new Scanner("12 34");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(scanner),
				"Error: expected IllegalArgumentException for numeric-only tokens '12 34'");
	}

	@Test
	@DisplayName("readClassicPosition7: throws IllegalArgumentException for single letter without row number like 'D'")
	void readClassicPosition7() {
		Scanner scanner = new Scanner("D");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(scanner),
				"Error: expected IllegalArgumentException for incomplete coordinate 'D'");
	}

	// --- menu() independent command paths ---

	@Test
	@DisplayName("menu1: immediate exit with 'desisto' command")
	void menu1() {
		provideInput("desisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should exit cleanly on 'desisto'");
	}

	@Test
	@DisplayName("menu2: 'ajuda' command prints help and exits with 'desisto'")
	void menu2() {
		provideInput("ajuda\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should handle 'ajuda' and exit cleanly");
	}

	@Test
	@DisplayName("menu3: unknown command triggers default branch and exits with 'desisto'")
	void menu3() {
		provideInput("comandoInvalido\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should handle unknown command gracefully");
	}

	@Test
	@DisplayName("menu4: 'estado', 'mapa', 'tiros', and 'pdf' when game/fleet is null")
	void menu4() {
		provideInput("estado\nmapa\ntiros\npdf\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should safely ignore commands requiring an active fleet/game");
	}

	@Test
	@DisplayName("menu5: 'gerafrota' followed by 'estado', 'mapa', 'tiros', and 'desisto'")
	void menu5() {
		provideInput("gerafrota\nestado\nmapa\ntiros\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should generate random fleet and display status, map, and shots");
	}

	@Test
	@DisplayName("menu6: 'lefrota' builds custom fleet and exits with 'desisto'")
	void menu6() {
		provideInput("lefrota\n" + VALID_FLEET_INPUT + "\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should build fleet via 'lefrota' and exit cleanly");
	}

	@Test
	@DisplayName("menu7: 'gerafrota' followed by 'pdf' with no moves throws IllegalArgumentException")
	void menu7() {
		provideInput("gerafrota\npdf\ndesisto\n");
		assertThrows(IllegalArgumentException.class, Tasks::menu,
				"Error: expected IllegalArgumentException when exporting PDF with no moves recorded");
	}

	@Test
	@DisplayName("menu8: 'Carregar' with invalid/cancelled input handles null loaded game")
	void menu8() {
		provideInput("Carregar\n0\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should handle cancelled game load gracefully");
	}

	@Test
	@DisplayName("menu9: 'gerafrota', valid 'rajada' without saving ('nao'), and 'pdf' export succeeds")
	void menu9() {
		provideInput("gerafrota\nrajada A1 A2 A3\nnao\npdf\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should process a rajada and export moves to PDF");
		File pdf = new File("jogadas.pdf");
		if (pdf.exists()) {
			pdf.delete();
		}
	}

	@Test
	@DisplayName("menu10: 'Carregar' loads an existing saved game and exits cleanly")
	void menu10() {
		Game tempGame = new Game(Fleet.createRandom());
		Save_LoadGame.guardar(tempGame, "test_save_unit.json");
		try {
			provideInput("Carregar\n1\ndesisto\n");
			assertDoesNotThrow(Tasks::menu, "Error: menu should load saved game via 'Carregar' and exit cleanly");
		} finally {
			File saved = new File("SaveGames/test_save_unit.json");
			if (saved.exists()) {
				saved.delete();
			}
		}
	}
	@Test
	@DisplayName("menu11: 'rajada', 'simula', and 'janela' commands when game is null")
	void menu11() {
		provideInput("rajada\nsimula\njanela\ndesisto\n");
		assertDoesNotThrow(Tasks::menu, "Error: menu should handle rajada, simula, and janela safely when game is null");
	}

	@Test
	@DisplayName("menu12: 'gerafrota', valid 'rajada' with saving ('sim') enters recursive menu until EOF")
	void menu12() {
		provideInput("gerafrota\nrajada C1 C2 C3\nsim\ntest_recursive_save\n");
		try {
			assertThrows(java.util.NoSuchElementException.class, Tasks::menu,
					"Error: expected recursive menu() call to throw NoSuchElementException after outer Scanner drains System.in");
		} finally {
			File saved = new File("SaveGames/test_recursive_save.json");
			if (saved.exists()) {
				saved.delete();
			}
		}
	}

}