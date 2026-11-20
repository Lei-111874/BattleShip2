package battleship;

import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class pdfCreatorTest {

    private pdfCreator pdfCreator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfCreator = new pdfCreator();
    }

    @Test
    void export_LancaExcecao_QuandoMovesNulo() {
        String filePath = tempDir.resolve("teste_nulo.pdf").toString();

        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pdfCreator.export(null, filePath);
        });

        Assertions.assertEquals("Não existem jogadas para exportar.", exception.getMessage());
    }

    @Test
    void export_LancaExcecao_QuandoMovesVazio() {
        String filePath = tempDir.resolve("teste_vazio.pdf").toString();

        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            pdfCreator.export(Collections.emptyList(), filePath);
        });

        Assertions.assertEquals("Não existem jogadas para exportar.", exception.getMessage());
    }

    @Test
    void export_CriaPdfComSucesso_TestandoTodosOsCenariosDeTiro() throws IOException, DocumentException {
        String filePath = tempDir.resolve("teste_sucesso.pdf").toString();

        // 1. Criar um IShip "falso" implementando todos os métodos da interface
        IShip dummyShip = new IShip() {
            @Override public String getCategory() { return "DummyShip"; }
            @Override public Integer getSize() { return 1; }
            @Override public List<IPosition> getPositions() { return Collections.emptyList(); }
            @Override public List<IPosition> getAdjacentPositions() { return Collections.emptyList(); }
            @Override public IPosition getPosition() { return null; }
            @Override public Compass getBearing() { return null; } // Retorna null pois não é usado aqui
            @Override public boolean stillFloating() { return false; }
            @Override public int getTopMostPos() { return 0; }
            @Override public int getBottomMostPos() { return 0; }
            @Override public int getLeftMostPos() { return 0; }
            @Override public int getRightMostPos() { return 0; }
            @Override public boolean occupies(IPosition pos) { return false; }
            @Override public boolean tooCloseTo(IShip other) { return false; }
            @Override public boolean tooCloseTo(IPosition pos) { return false; }
            @Override public boolean isInsideBoard() { return true; }
            @Override public void shoot(IPosition pos) { }
            @Override public void sink() { }
        };

        // 2. Criar um IMove "falso"
        IMove fakeMove = new IMove() {
            @Override
            public int getNumber() {
                return 1;
            }

            @Override
            public List<IPosition> getShots() {
                return Arrays.asList(
                        criarFakePosition("A1"),
                        criarFakePosition("B2"),
                        criarFakePosition("C3"),
                        criarFakePosition("D4"),
                        criarFakePosition("E5")
                );
            }

            @Override
            public List<IGame.ShotResult> getShotResults() {
                return Arrays.asList(
                        // false, _, _, _ -> Inválido
                        new IGame.ShotResult(false, false, null, false),
                        // true, true, _, _ -> Repetido
                        new IGame.ShotResult(true, true, null, false),
                        // true, false, navio, true -> Afundado
                        new IGame.ShotResult(true, false, dummyShip, true),
                        // true, false, navio, false -> Acertou num navio
                        new IGame.ShotResult(true, false, dummyShip, false),
                        // true, false, null, false -> Água
                        new IGame.ShotResult(true, false, null, false)
                );
            }

            @Override
            public String processEnemyFire(boolean verbose) {
                return "";
            }
        };

        List<IMove> moves = Collections.singletonList(fakeMove);

        // 3. Executa o método
        pdfCreator.export(moves, filePath);

        // 4. Verificações (Asserts)
        File pdfFile = new File(filePath);

        Assertions.assertTrue(pdfFile.exists(), "O ficheiro PDF devia ter sido criado.");
        Assertions.assertTrue(pdfFile.length() > 0, "O ficheiro PDF não deve estar vazio.");
    }

    // --- Método Auxiliar para criar o Fake Position ---

    private IPosition criarFakePosition(String nomePosicao) {
        return new IPosition() {
            @Override public int getRow() { return 0; }
            @Override public int getColumn() { return 0; }
            @Override public char getClassicRow() { return 'A'; }
            @Override public int getClassicColumn() { return 1; }
            @Override public boolean isInside() { return true; }
            @Override public boolean isAdjacentTo(IPosition other) { return false; }
            @Override public void occupy() { }
            @Override public void shoot() { }
            @Override public boolean isOccupied() { return false; }
            @Override public boolean isHit() { return false; }

            @Override
            public List<IPosition> adjacentPositions() {
                return Collections.emptyList();
            }

            // O único método da posição que realmente importa para a concatenação de texto no PDF
            @Override
            public String toString() {
                return nomePosicao;
            }
        };
    }
}