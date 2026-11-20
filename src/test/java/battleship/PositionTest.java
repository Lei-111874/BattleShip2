package battleship;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Position.
 * Author: fabio
 * Date: 2026-11-17 10:47
 * Cyclomatic Complexity for each method:
 * - randomPosition(): 1
 * - Position(char, int): 1
 * - Position(int, int): 1
 * - getRow(): 1
 * - getColumn(): 1
 * - getClassicRow(): 1
 * - getClassicColumn(): 1
 * - isInside(): 4
 * - isAdjacentTo(): 2
 * - adjacentPositions(): 3
 * - isOccupied(): 1
 * - isHit(): 1
 * - occupy(): 1
 * - shoot(): 1
 * - equals(Object): 4
 * - hashCode(): 1
 * - toString(): 1
 */
public class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	//	position = new Position('C', 4);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@Test
	@DisplayName("Constructs a position from numeric coordinates")
	void constructor() {
		Position pos = new Position(1, 1);
		assertNotNull(pos, "Failed to create Position: object is null");
		assertEquals(1, pos.getRow(), "Failed to set row: expected 1 but got " + pos.getRow());
		assertEquals(1, pos.getColumn(), "Failed to set column: expected 1 but got " + pos.getColumn());
		assertFalse(pos.isOccupied(), "New position should not be occupied");
		assertFalse(pos.isHit(), "New position should not be hit");
	}

	@Test
	@DisplayName("Returns the row coordinate")
	void getRow() {
		assertEquals(2, position.getRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("Returns the column coordinate")
	void getColumn() {
		assertEquals(3, position.getColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@Test
	@DisplayName("Returns the classic row")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("Returns the classic column")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(),
				"Error: expected classic column 4 but got " + position.getClassicColumn());
	}

	@Test
	@DisplayName("Checks that the board's upper-left position is valid")
	void isValid1() {
		position = new Position(0, 0);
		assertTrue(position.isInside(), "Position (0,0) should be valid");
	}

	@Test
	@DisplayName("Rejects a negative row")
	void isValid2() {
		position = new Position(-1, 5);
		assertFalse(position.isInside(), "Position with negative row should be invalid");
	}

	@Test
	@DisplayName("Rejects a negative column")
	void isValid3() {
		position = new Position(5, -1);
		assertFalse(position.isInside(), "Position with negative column should be invalid");
	}

	@Test
	@DisplayName("Rejects a row beyond the board")
	void isValid4() {
		position = new Position(Game.BOARD_SIZE, 5);
		assertFalse(position.isInside(), "Position with row >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("Rejects a column beyond the board")
	void isValid5() {
		position = new Position(5, Game.BOARD_SIZE);
		assertFalse(position.isInside(), "Position with column >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("Detects a horizontally adjacent position")
	void isAdjacentTo1() {
		Position other = new Position(2, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect horizontally adjacent position");
	}

	@Test
	@DisplayName("Detects a vertically adjacent position")
	void isAdjacentTo2() {
		Position other = new Position(3, 3);
		assertTrue(position.isAdjacentTo(other), "Failed to detect vertically adjacent position");
	}

	@Test
	@DisplayName("Detects a diagonally adjacent position")
	void isAdjacentTo3() {
		Position other = new Position(3, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect diagonally adjacent position");
	}

	@Test
	@DisplayName("Rejects a position too far away in both coordinates")
	void isAdjacentTo4() {
		Position other = new Position(4, 5);
		assertFalse(position.isAdjacentTo(other), "Non-adjacent position incorrectly identified as adjacent");
	}

	@Test
	@DisplayName("Rejects a position too far away in the column only")
	void isAdjacentTo5() {
		Position other = new Position(2, 5);
		assertFalse(position.isAdjacentTo(other),
				"Error: expected a column difference greater than one to be non-adjacent");
	}

	@Test
	@DisplayName("Throws when adjacency is checked against null")
	void isAdjacentToWithNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"Error: expected a null adjacency argument to throw NullPointerException");
	}

	@Test
	@DisplayName("Tracks whether the position is occupied")
	void isOccupied() {
		assertFalse(position.isOccupied(), "New position should not be occupied");
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@Test
	@DisplayName("Tracks whether the position has been hit")
	void isHit() {
		assertFalse(position.isHit(), "New position should not be hit");
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	@Test
	@DisplayName("Recognizes equal positions")
	void equals1() {
		Position same = new Position(2, 3);
		assertTrue(position.equals(same), "Equal positions not identified as equal");
	}

	@Test
	@DisplayName("Does not consider a position equal to null")
	void equals2() {
		assertFalse(position.equals(null), "Position should not equal null");
	}

	@Test
	@DisplayName("Does not consider a position equal to an unrelated object")
	void equals3() {
		Object other = new Object();
		assertFalse(position.equals(other), "Position should not equal non-Position object");
	}

	@Test
	@DisplayName("Compares both position coordinates")
	void equals4() {
		Position other = new Position(2, 4);
		assertFalse(position.equals(other), "Positions with the same row but different column should not be equal");
	}

	@Test
	@DisplayName("Recognizes a position as equal to itself")
	void equals5() {
		assertTrue(position.equals(position), "A position should be equal to itself");
	}

	@Test
	@DisplayName("Keeps equal positions' hash codes consistent")
	void hashCodeConsistency() {
		Position same = new Position(2, 3);
		assertEquals(position.hashCode(), same.hashCode(),
				"Hash codes not consistent for equal positions");
	}

	@Test
	@DisplayName("Formats a position using its classic row and column")
	void toStringFormat() {
//		String expected = "Row = C, Column = 4";
		String expected = "C4";
		assertEquals(expected, position.toString(),
				"Incorrect string representation: expected '" + expected +
						"' but got '" + position.toString() + "'");
	}

	@Test
	@DisplayName("Constructs a position from an uppercase classic row")
	void classicConstructor1() {
		Position classic = new Position('C', 4);

		assertAll(
				() -> assertEquals(2, classic.getRow(),
						"Error: expected classic row C to map to zero-based row 2"),
				() -> assertEquals(3, classic.getColumn(),
						"Error: expected classic column 4 to map to zero-based column 3"),
				() -> assertEquals('C', classic.getClassicRow(),
						"Error: expected the classic row to remain C"),
				() -> assertEquals(4, classic.getClassicColumn(),
						"Error: expected the classic column to remain 4")
		);
	}

	@Test
	@DisplayName("Constructs a position from a lowercase classic row")
	void classicConstructor2() {
		Position classic = new Position('c', 4);

		assertEquals('C', classic.getClassicRow(),
				"Error: expected a lowercase classic row to be normalized to uppercase C");
	}

	@Test
	@DisplayName("Generates a random position within board bounds")
	void randomPosition() {
		Position random = Position.randomPosition();

		assertTrue(random.isInside(),
				"Error: expected a randomly generated position to be inside the board");
	}

	@Test
	@DisplayName("Returns all eight adjacent positions from the board center")
	void adjacentPositions1() {
		position = new Position(5, 5);

		assertEquals(8, position.adjacentPositions().size(),
				"Error: expected an interior position to have eight adjacent positions");
	}

	@Test
	@DisplayName("Excludes out-of-board neighbors at a corner")
	void adjacentPositions2() {
		position = new Position(0, 0);

		assertEquals(List.of(new Position(0, 1), new Position(1, 0), new Position(1, 1)),
				position.adjacentPositions(),
				"Error: expected a corner position to return only its three in-board neighbors");
	}

	@Test
	@DisplayName("Returns no adjacent positions for a position outside board edges")
	void adjacentPositions3() {
		position = new Position(-2, -2);

		assertTrue(position.adjacentPositions().isEmpty(),
				"Error: expected all neighbors beyond the board corner to be excluded");
	}
}