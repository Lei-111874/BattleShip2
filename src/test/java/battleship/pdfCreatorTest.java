package battleship;

import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for pdfCreator.
 * Author: ${user.name}
 * Date: 2026-11-20 13:57
 * Cyclomatic Complexity:
 * - constructor: 1
 * - export: 10
 */
class pdfCreatorTest {

    private pdfCreator pdfCreator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfCreator = new pdfCreator();
    }

    @AfterEach
    void tearDown() {
        pdfCreator = null;
    }

    @Test
    @DisplayName("Constructor creates a pdfCreator instance")
    void pdfCreator() {
        assertNotNull(pdfCreator, "Error: expected the constructor to create a non-null pdfCreator instance.");
    }

    @Test
    @DisplayName("export1: rejects a null moves list")
    void export1() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pdfCreator.export(null, outputPath("null-moves.pdf")),
                "Error: expected null moves to cause IllegalArgumentException."
        );

        assertEquals(
                "Não existem jogadas para exportar.",
                exception.getMessage(),
                "Error: expected the null-moves exception message to match."
        );
    }

    @Test
    @DisplayName("export2: rejects an empty moves list")
    void export2() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pdfCreator.export(List.of(), outputPath("empty-moves.pdf")),
                "Error: expected an empty moves list to cause IllegalArgumentException."
        );

        assertEquals(
                "Não existem jogadas para exportar.",
                exception.getMessage(),
                "Error: expected the empty-moves exception message to match."
        );
    }

    @Test
    @DisplayName("export3: exports a move with no shots")
    void export3() throws IOException, DocumentException {
        String filePath = outputPath("no-shots.pdf");
        pdfCreator.export(List.of(new Move(1, List.of(), List.of())), filePath);

        assertPdfCreated(filePath);
    }

    @Test
    @DisplayName("export4: exports a shot when its move has no results")
    void export4() throws IOException, DocumentException {
        String filePath = outputPath("no-results.pdf");
        pdfCreator.export(
                List.of(new Move(1, List.of(new Position(0, 0)), List.of())),
                filePath
        );

        assertPdfCreated(filePath);
    }

    @Test
    @DisplayName("export5: labels an invalid shot")
    void export5() throws IOException, DocumentException {
        String filePath = outputPath("invalid-shot.pdf");
        pdfCreator.export(
                List.of(new Move(
                        1,
                        List.of(new Position(0, 0)),
                        List.of(new IGame.ShotResult(false, false, null, false))
                )),
                filePath
        );

        assertPdfCreated(filePath);
    }

    @Test
    @DisplayName("export6: labels a repeated shot")
    void export6() throws IOException, DocumentException {
        String filePath = outputPath("repeated-shot.pdf");
        pdfCreator.export(
                List.of(new Move(
                        1,
                        List.of(new Position(0, 0)),
                        List.of(new IGame.ShotResult(true, true, null, false))
                )),
                filePath
        );

        assertPdfCreated(filePath);
    }

    @Test
    @DisplayName("export7: labels a shot that sinks a ship")
    void export7() throws IOException, DocumentException {
        String filePath = outputPath("sunk-ship.pdf");
        IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
        pdfCreator.export(
                List.of(new Move(
                        1,
                        List.of(new Position(0, 0)),
                        List.of(new IGame.ShotResult(true, false, ship, true))
                )),
                filePath
        );

        assertPdfCreated(filePath);
    }

    @Test
    @DisplayName("export8: labels a shot that hits a ship without sinking it")
    void export8() throws IOException, DocumentException {
        String filePath = outputPath("ship-hit.pdf");
        IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
        pdfCreator.export(
                List.of(new Move(
                        1,
                        List.of(new Position(0, 0)),
                        List.of(new IGame.ShotResult(true, false, ship, false))
                )),
                filePath
        );

        assertPdfCreated(filePath);
    }

    @Test
    @DisplayName("export9: labels a valid shot that misses")
    void export9() throws IOException, DocumentException {
        String filePath = outputPath("water-shot.pdf");
        pdfCreator.export(
                List.of(new Move(
                        1,
                        List.of(new Position(0, 0)),
                        List.of(new IGame.ShotResult(true, false, null, false))
                )),
                filePath
        );

        assertPdfCreated(filePath);
    }

    @Test
    @DisplayName("export10: processes multiple moves and loops through the move list")
    void export10() throws IOException, DocumentException {
        String filePath = outputPath("multiple-moves.pdf");
        List<IMove> moves = List.of(
                new Move(1, List.of(), List.of()),
                new Move(2, List.of(new Position(0, 0)), List.of())
        );
        pdfCreator.export(moves, filePath);

        assertPdfCreated(filePath);
    }

    private String outputPath(String fileName) {
        return tempDir.resolve(fileName).toString();
    }

    private void assertPdfCreated(String filePath) {
        File pdfFile = new File(filePath);
        assertAll(
                "PDF output",
                () -> assertTrue(
                        pdfFile.exists(),
                        "Error: expected export to create the PDF file."
                ),
                () -> assertTrue(
                        pdfFile.length() > 0,
                        "Error: expected the created PDF file to contain data."
                )
        );
    }
}