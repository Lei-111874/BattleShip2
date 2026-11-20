package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Save_LoadGameTest {

    private File testDir;
    private final String testFileName = "test_game.json";

    @BeforeEach
    void setUp() {
        testDir = new File("SaveGames");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        limparPastaSaveGames();
    }

    @AfterEach
    void tearDown() {
        limparPastaSaveGames();
    }

    private void limparPastaSaveGames() {
        if (testDir != null && testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    private void escreverFicheiro(String nome, String conteudo) throws IOException {
        File file = new File(testDir, nome);
        Files.write(file.toPath(), conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void construtor_InstanciaClasseSemErros() {
        Save_LoadGame instance = new Save_LoadGame();
        Assertions.assertNotNull(instance);
    }

    // --- Testes de Gravação (perguntar_guardar / guardar) ---

    @Test
    void perguntar_guardar_RetornaFalso_QuandoUtilizadorDizNao() {
        String simulatedInput = "nao\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        boolean guardou = Save_LoadGame.perguntar_guardar(criarFakeGame(), scanner);

        Assertions.assertFalse(guardou, "Não deve guardar se a resposta for diferente de 'sim'");
    }

    @Test
    void perguntar_guardar_RetornaVerdadeiroESalvaFicheiro_QuandoUtilizadorDizSim() {
        String simulatedInput = "sim\n" + testFileName + "\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        boolean guardou = Save_LoadGame.perguntar_guardar(criarFakeGame(), scanner);

        Assertions.assertTrue(guardou, "Deve guardar com sucesso");
        Assertions.assertTrue(new File(testDir, testFileName).exists());
    }

    @Test
    void perguntar_guardar_AdicionaExtensaoJson_QuandoOmitida() {
        String simulatedInput = "sim\njogo_sem_extensao\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));

        boolean guardou = Save_LoadGame.perguntar_guardar(criarFakeGame(), scanner);

        Assertions.assertTrue(guardou);
        Assertions.assertTrue(new File(testDir, "jogo_sem_extensao.json").exists());
    }

    @Test
    void guardar_CriaPastaSaveGames_QuandoNaoExiste() {
        limparPastaSaveGames();
        testDir.delete();
        Assertions.assertFalse(testDir.exists());

        boolean guardou = Save_LoadGame.guardar(criarFakeGame(), testFileName);

        Assertions.assertTrue(guardou);
        Assertions.assertTrue(testDir.exists());
        Assertions.assertTrue(new File(testDir, testFileName).exists());
    }

    @Test
    void guardar_RetornaFalso_QuandoOcorreExcecaoDeIO() {
        String invalidFileName = "diretorio_inexistente/test.json";

        boolean guardou = Save_LoadGame.guardar(criarFakeGame(), invalidFileName);

        Assertions.assertFalse(guardou, "Deveria falhar ao tentar guardar num caminho inválido");
    }

    // --- Testes de Escolha de Ficheiro (escolher_ficheiro) ---

    @Test
    void escolher_ficheiro_RetornaNull_QuandoPastaNaoExiste() {
        limparPastaSaveGames();
        testDir.delete();

        Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
        File escolhido = Save_LoadGame.escolher_ficheiro(scanner);

        Assertions.assertNull(escolhido);
    }

    @Test
    void escolher_ficheiro_RetornaNull_QuandoNaoExistemFicheirosJson() throws IOException {
        escreverFicheiro("ignorar.txt", "conteudo");

        Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
        File escolhido = Save_LoadGame.escolher_ficheiro(scanner);

        Assertions.assertNull(escolhido);
    }

    @Test
    void escolher_ficheiro_RetornaNull_SeEscolhaForInvalida() {
        Save_LoadGame.guardar(criarFakeGame(), testFileName);

        Scanner scanner = new Scanner(new ByteArrayInputStream("abc\n".getBytes()));
        File escolhido = Save_LoadGame.escolher_ficheiro(scanner);

        Assertions.assertNull(escolhido);
    }

    @Test
    void escolher_ficheiro_RetornaNull_SeEscolhaForZeroOuMaiorQueTotal() {
        Save_LoadGame.guardar(criarFakeGame(), testFileName);

        Scanner scannerZero = new Scanner(new ByteArrayInputStream("0\n".getBytes()));
        Assertions.assertNull(Save_LoadGame.escolher_ficheiro(scannerZero));

        Scanner scannerAcima = new Scanner(new ByteArrayInputStream("99\n".getBytes()));
        Assertions.assertNull(Save_LoadGame.escolher_ficheiro(scannerAcima));
    }

    @Test
    void escolher_ficheiro_RetornaFicheiro_QuandoEscolhaValida() {
        Save_LoadGame.guardar(criarFakeGame(), testFileName);

        Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
        File escolhido = Save_LoadGame.escolher_ficheiro(scanner);

        Assertions.assertNotNull(escolhido);
        Assertions.assertEquals(testFileName, escolhido.getName());
    }

    // --- Testes de Carregamento e Parsing (fromMap / fromMapData / auxiliares) ---

    @Test
    void fromMap_RetornaNull_QuandoScannerNullOuCancelado() {
        Assertions.assertNull(Save_LoadGame.fromMap(null));

        Save_LoadGame.guardar(criarFakeGame(), testFileName);
        Scanner scannerCancel = new Scanner(new ByteArrayInputStream("0\n".getBytes()));
        Assertions.assertNull(Save_LoadGame.fromMap(scannerCancel));
    }

    @Test
    void fromMap_RetornaNull_QuandoJsonInvalidoOuNulo() throws IOException {
        escreverFicheiro("corrompido.json", "{ json invalido");
        Scanner scannerCorrompido = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
        Assertions.assertNull(Save_LoadGame.fromMap(scannerCorrompido));

        limparPastaSaveGames();
        escreverFicheiro("nulo.json", "null");
        Scanner scannerNulo = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
        Assertions.assertNull(Save_LoadGame.fromMap(scannerNulo));
    }

    @Test
    void fromMap_CarregaJogoVazio_QuandoJsonSemCampos() throws IOException {
        escreverFicheiro("vazio.json", "{}");
        Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));

        IGame jogo = Save_LoadGame.fromMap(scanner);

        Assertions.assertNotNull(jogo);
    }

    @Test
    void fromMap_CarregaJogoCompletoECobreTodosOsRamosDeParsing() throws IOException {
        String jsonCompleto = "{\n" +
                "  \"naviosJogador\": [\n" +
                "    {\n" +
                "      \"category\": \"barca\",\n" +
                "      \"bearing\": \"SOUTH\",\n" +
                "      \"positions\": [ { \"row\": 1, \"column\": 1 } ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"category\": \"categoria_inexistente\",\n" +
                "      \"bearing\": \"DIRECAO_INVALIDA\",\n" +
                "      \"positions\": [ { \"row\": 2, \"column\": \"B\" } ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"positions\": [ {} ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"category\": \"barca\",\n" +
                "      \"positions\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"category\": \"barca\",\n" +
                "      \"positions\": [ \"elemento_invalido_para_excecao\" ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tirosInvalidos\": 3,\n" +
                "  \"tirosRepetidos\": \"4\",\n" +
                "  \"acertos\": \"C\",\n" +
                "  \"naviosAfundados\": \"nao_numero\",\n" +
                "  \"tirosInimigo\": [\n" +
                "    {\n" +
                "      \"shots\": [\n" +
                "        { \"row\": \"D\", \"column\": 2 },\n" +
                "        { \"row\": 5, \"column\": 6 },\n" +
                        "        { \"row\": 8, \"column\": 8 },\n" +
                        "        { \"row\": 1 },\n" +
                        "        null\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"shots\": []\n" +
                "    },\n" +
                "    {}\n" +
                "  ]\n" +
                "}";

        escreverFicheiro("completo.json", jsonCompleto);
        Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));

        IGame jogo = Save_LoadGame.fromMap(scanner);

        Assertions.assertNotNull(jogo);
        Assertions.assertEquals(3, jogo.getInvalidShots());
        Assertions.assertEquals(4, jogo.getRepeatedShots());
        Assertions.assertEquals(2, jogo.getHits());
        Assertions.assertEquals(0, jogo.getSunkShips());
    }

    @Test
    void fromMap_ParseCoordinateRetornaZero_QuandoTipoNaoNumeroNemString() throws IOException {
        String jsonTipoBooleano = "{ \"tirosInvalidos\": true }";
        escreverFicheiro("booleano.json", jsonTipoBooleano);

        Scanner scanner = new Scanner(new ByteArrayInputStream("1\n".getBytes()));
        IGame jogo = Save_LoadGame.fromMap(scanner);

        Assertions.assertNotNull(jogo);
        Assertions.assertEquals(0, jogo.getInvalidShots());
    }

    // --- Métodos Auxiliares para Fakes (Stubs) ---

    private IGame criarFakeGame() {
        return new IGame() {
            @Override public IFleet getMyFleet() { return criarFakeFleet(); }
            @Override public IFleet getAlienFleet() { return criarFakeFleet(); }
            @Override public List<IMove> getMyMoves() { return Collections.emptyList(); }
            @Override public List<IMove> getAlienMoves() { return Collections.emptyList(); }
            @Override public int getInvalidShots() { return 1; }
            @Override public int getRepeatedShots() { return 2; }
            @Override public int getHits() { return 3; }
            @Override public int getSunkShips() { return 4; }
            @Override public int getRemainingShips() { return 5; }
            @Override public String randomEnemyFire() { return ""; }
            @Override public String readEnemyFire(Scanner in) { return ""; }
            @Override public void fireShots(List<IPosition> shots) { }
            @Override public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) { return null; }
            @Override public void printMyBoard(boolean show_shots, boolean show_legend) { }
            @Override public void printAlienBoard(boolean show_shots, boolean show_legend) { }
            @Override public void over() { }
        };
    }

    private IFleet criarFakeFleet() {
        return new IFleet() {
            @Override public List<IShip> getShips() { return Collections.emptyList(); }
            @Override public boolean addShip(IShip s) { return false; }
            @Override public List<IShip> getShipsLike(String category) { return Collections.emptyList(); }
            @Override public List<IShip> getFloatingShips() { return Collections.emptyList(); }
            @Override public List<IShip> getSunkShips() { return Collections.emptyList(); }
            @Override public IShip shipAt(IPosition pos) { return null; }
            @Override public void printStatus() { }
        };
    }
}
