package battleship;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Frigate.
 * Author: fabio
 * Date: 2026-11-17 10:43
 * Cyclomatic Complexity for each method:
 * - Frigate(Compass, IPosition): 5
 */
public class FrigateTest {

	private Frigate frigate;

	@BeforeEach
	void setUp() {
		frigate = new Frigate(Compass.NORTH, new Position(5, 5));
	}

	@AfterEach
	void tearDown() {
		frigate = null;
	}

	/**
	 * Test for the constructor with NORTH bearing.
	 */
	@Test
	@DisplayName("Constructs a frigate facing north")
	void testConstructorNorth() {
		assertNotNull(frigate, "Error: Frigate instance should not be null.");
		assertEquals("Fragata", frigate.getCategory(), "Error: Frigate category should be 'Fragata'.");
		assertEquals(Compass.NORTH, frigate.getBearing(), "Error: Frigate bearing is incorrect.");
		assertEquals(4, frigate.getSize(), "Error: Frigate size should be 4.");

		List<IPosition> positions = frigate.getPositions();
		assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions.");
		assertEquals(new Position(5, 5), positions.get(0), "Error: First position is incorrect for NORTH.");
		assertEquals(new Position(6, 5), positions.get(1), "Error: Second position is incorrect for NORTH.");
		assertEquals(new Position(7, 5), positions.get(2), "Error: Third position is incorrect for NORTH.");
		assertEquals(new Position(8, 5), positions.get(3), "Error: Fourth position is incorrect for NORTH.");
	}

	/**
	 * Test for the constructor with EAST bearing.
	 */
	@Test
	@DisplayName("Constructs a frigate facing east")
	void testConstructorEast() {
		frigate = new Frigate(Compass.EAST, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();

		assertNotNull(frigate, "Error: Frigate instance should not be null.");
		assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions.");
		assertEquals(new Position(5, 5), positions.get(0), "Error: First position is incorrect for EAST.");
		assertEquals(new Position(5, 6), positions.get(1), "Error: Second position is incorrect for EAST.");
		assertEquals(new Position(5, 7), positions.get(2), "Error: Third position is incorrect for EAST.");
		assertEquals(new Position(5, 8), positions.get(3), "Error: Fourth position is incorrect for EAST.");
	}

	/**
	 * Test for the constructor with WEST bearing.
	 */
	@Test
	@DisplayName("Constructs a frigate facing west")
	void testConstructorWest() {
		frigate = new Frigate(Compass.WEST, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();

		assertNotNull(frigate, "Error: Frigate instance should not be null.");
		assertEquals(4, positions.size(), "Error: Frigate should have exactly 4 positions.");
		assertEquals(new Position(5, 5), positions.get(0), "Error: First position is incorrect for WEST.");
		assertEquals(new Position(5, 6), positions.get(1), "Error: Second position is incorrect for WEST.");
		assertEquals(new Position(5, 7), positions.get(2), "Error: Third position is incorrect for WEST.");
		assertEquals(new Position(5, 8), positions.get(3), "Error: Fourth position is incorrect for WEST.");
	}

	/**
	 * Test for the constructor with SOUTH bearing.
	 */
	@Test
	@DisplayName("Constructs a frigate facing south")
	void testConstructorSouth() {
		frigate = new Frigate(Compass.SOUTH, new Position(5, 5));
		List<IPosition> positions = frigate.getPositions();

		assertAll(
				() -> assertNotNull(frigate, "Error: expected a south-facing frigate but got null"),
				() -> assertEquals(4, positions.size(), "Error: expected a frigate to occupy four positions"),
				() -> assertEquals(new Position(5, 5), positions.get(0),
						"Error: expected the first SOUTH position to match the initial position"),
				() -> assertEquals(new Position(6, 5), positions.get(1),
						"Error: expected the second SOUTH position to be one row down"),
				() -> assertEquals(new Position(7, 5), positions.get(2),
						"Error: expected the third SOUTH position to be two rows down"),
				() -> assertEquals(new Position(8, 5), positions.get(3),
						"Error: expected the fourth SOUTH position to be three rows down")
		);
	}

	/**
	 * Test for the getSize method.
	 */
	@Test
	@DisplayName("Reports the frigate size")
	void testGetSize() {
		assertEquals(4, frigate.getSize(), "Error: Frigate size should be 4.");
	}

	/**
	 * Test for the stillFloating method (all positions intact).
	 */
	@Test
	@DisplayName("Reports an undamaged frigate as floating")
	void testStillFloating1() {
		assertTrue(frigate.stillFloating(), "Error: Frigate should still be floating.");
	}

	/**
	 * Test for the stillFloating method (one position hit).
	 */
	@Test
	@DisplayName("Reports a frigate with one hit position as floating")
	void testStillFloating2() {
		frigate.getPositions().get(0).shoot();
		assertTrue(frigate.stillFloating(), "Error: Frigate should still be floating with one hit position.");
	}

	/**
	 * Test for the stillFloating method (all positions hit).
	 */
	@Test
	@DisplayName("Reports a fully hit frigate as sunk")
	void testStillFloating3() {
		frigate.getPositions().forEach(IPosition::shoot);
		assertFalse(frigate.stillFloating(), "Error: Frigate should not be floating if all positions are hit.");
	}

	/**
	 * Test for the getTopMostPos method.
	 */
	@Test
	@DisplayName("Gets the topmost frigate row")
	void testGetTopMostPos() {
		assertEquals(5, frigate.getTopMostPos(), "Error: The topmost position should be 5.");
	}

	/**
	 * Test for the getBottomMostPos method.
	 */
	@Test
	@DisplayName("Gets the bottommost frigate row")
	void testGetBottomMostPos() {
		assertEquals(8, frigate.getBottomMostPos(), "Error: The bottommost position should be 8.");
	}

	/**
	 * Test for the getLeftMostPos method.
	 */
	@Test
	@DisplayName("Gets the leftmost frigate column")
	void testGetLeftMostPos() {
		assertEquals(5, frigate.getLeftMostPos(), "Error: The leftmost position should be 5.");
	}

	/**
	 * Test for the getRightMostPos method.
	 */
	@Test
	@DisplayName("Gets the rightmost frigate column")
	void testGetRightMostPos() {
		assertEquals(5, frigate.getRightMostPos(), "Error: The rightmost position should be 5.");
	}

	/**
	 * Test for the constructor with invalid input (null).
	 */
	@Test
	@DisplayName("Rejects null constructor arguments")
	void testConstructorInvalidInput() {
		assertThrows(NullPointerException.class, () -> new Frigate(null, null),
				"Error: expected a null bearing to throw NullPointerException");
		assertThrows(NullPointerException.class, () -> new Frigate(Compass.NORTH, null),
				"Error: expected a null position to throw NullPointerException");
	}
}