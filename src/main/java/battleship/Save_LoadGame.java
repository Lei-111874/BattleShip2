package battleship;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Save_LoadGame {
    public static boolean perguntar_guardar(IGame game, Scanner scanner) {
        System.out.print("Deseja guardar o jogo? (sim/nao): ");
        String resposta = scanner.next().trim();

        if (resposta.equalsIgnoreCase("sim")) {
            System.out.print("Introduza o nome do ficheiro para guardar (ex: jogo.json): ");
            String nomeFicheiro = scanner.next().trim();

            if (!nomeFicheiro.toLowerCase().endsWith(".json")) {
                nomeFicheiro += ".json";
            }


            return guardar(game, nomeFicheiro);
        }

        return false; // O jogo não foi guardado; prossegue normalmente
    }

    public static boolean guardar(IGame game, String nomeFicheiro) {
        File pasta = new File("SaveGames");
        if (!pasta.exists()) {
            pasta.mkdirs();
        }
        File ficheiroSave = new File(pasta, nomeFicheiro);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("tirosJogador", game.getMyMoves());
        dados.put("tirosInimigo", game.getAlienMoves());
        dados.put("naviosJogador", game.getMyFleet().getShips());
        dados.put("naviosInimigo", game.getAlienFleet().getShips());
        dados.put("tirosInvalidos", game.getInvalidShots());
        dados.put("tirosRepetidos", game.getRepeatedShots());
        dados.put("acertos", game.getHits());
        dados.put("naviosAfundados", game.getSunkShips());
        dados.put("naviosRestantes", game.getRemainingShips());

        try {
            mapper.writeValue(ficheiroSave, dados);
            System.out.println("Jogo guardado com sucesso em: " + ficheiroSave.getPath());

            // Retorna TRUE SE O FICHEIRO ESTÁ OK
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao guardar o jogo: " + e.getMessage());

            return false;
        }
    }

    public static File escolher_ficheiro(Scanner scanner) {
        File pasta = new File("SaveGames");

        if (!pasta.exists() || !pasta.isDirectory()) {
            System.out.println("Ainda não existem jogos guardados.");
            return null;
        }

        File[] ficheiros = pasta.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (ficheiros == null || ficheiros.length == 0) {
            System.out.println("Nenhum jogo guardado encontrado na pasta " + "SaveGames" + ".");
            return null;
        }

        System.out.println("\n--- JOGOS GUARDADOS ---");
        for (int i = 0; i < ficheiros.length; i++) {
            System.out.println((i + 1) + ". " + ficheiros[i].getName());
        }

        System.out.print("Escolha o número do jogo a carregar (ou 0 para cancelar): ");
        if (!scanner.hasNextInt()) {
            System.out.println("Opção inválida.");
            scanner.next(); // Limpa token inválido
            return null;
        }

        int escolha = scanner.nextInt();

        if (escolha <= 0 || escolha > ficheiros.length) {
            System.out.println("Operação cancelada ou opção inválida.");
            return null;
        }

        return ficheiros[escolha - 1];
    }

    public static IGame fromMap(Scanner scanner) {
        if (scanner == null) return null;

        // 1. Escolhe o ficheiro JSON a partir da pasta SaveGames
        File ficheiroEscolhido = escolher_ficheiro(scanner);
        if (ficheiroEscolhido == null) {
            return null; // Operação cancelada ou pasta vazia
        }

        // 2. Lê o ficheiro e converte o JSON num Map<String, Object>
        Map<String, Object> dados;
        try {
            ObjectMapper mapper = new ObjectMapper();
            dados = mapper.readValue(ficheiroEscolhido, Map.class);
        } catch (Exception e) {
            System.out.println("Erro ao ler o ficheiro JSON: " + e.getMessage());
            return null;
        }
        System.out.println("Passei 1");
        // 3. Reconstrói o jogo a partir do mapa lido
        return fromMapData(dados);
    }

    @SuppressWarnings("unchecked")

    private static Game fromMapData(Map<String, Object> dados) {
        if (dados == null) return null;

        IFleet fleet = new Fleet();
        List<Map<String, Object>> naviosJogador = (List<Map<String, Object>>) dados.get("naviosJogador");

        if (naviosJogador != null) {
            for (Map<String, Object> shipMap : naviosJogador) {
                // 1. Converter categoria para minúsculas para coincidir com o switch do Ship.buildShip
                String rawCategory = (String) shipMap.get("category");
                String category = (rawCategory != null) ? rawCategory.toLowerCase() : "";

                // 2. Ler a orientação diretamente do JSON (ex: "SOUTH" -> Compass.SOUTH)
                Compass bearing = Compass.NORTH;
                if (shipMap.containsKey("bearing")) {
                    try {
                        bearing = Compass.valueOf(((String) shipMap.get("bearing")).toUpperCase());
                    } catch (Exception e) {
                        bearing = Compass.NORTH;
                    }
                }

                List<Map<String, Object>> posList = (List<Map<String, Object>>) shipMap.get("positions");

                if (posList != null && !posList.isEmpty()) {
                    try {
                        // 3. Extrair a posição inicial (primeiro elemento da lista)
                        Position firstPos = parsePositionFromMap(posList.get(0));

                        if (firstPos == null) {
                            System.out.println("Não foi possível determinar a posição inicial do navio: " + category);
                            continue;
                        }

                        // 4. Construir e adicionar o navio à frota
                        IShip ship = Ship.buildShip(category, bearing, firstPos);

                        if (ship != null) {
                            fleet.addShip(ship);
                        } else {
                            System.out.println("Navio não reconhecido para a categoria: " + category);
                        }
                    } catch (Exception e) {
                        System.out.println("Erro ao recriar navio (" + category + "): " + e.getMessage());
                    }
                }
            }
        }

        // Instanciar o jogo com a frota restaurada
        Game restoredGame = new Game(fleet);

        // Restaurar contadores
        if (dados.containsKey("tirosInvalidos"))
            restoredGame.countInvalidShots = parseCoordinate(dados.get("tirosInvalidos"));
        if (dados.containsKey("tirosRepetidos"))
            restoredGame.countRepeatedShots = parseCoordinate(dados.get("tirosRepetidos"));
        if (dados.containsKey("acertos"))
            restoredGame.countHits = parseCoordinate(dados.get("acertos"));
        if (dados.containsKey("naviosAfundados"))
            restoredGame.countSinks = parseCoordinate(dados.get("naviosAfundados"));

        // Restaurar histórico de jogadas do inimigo
        List<Map<String, Object>> tirosInimigo = (List<Map<String, Object>>) dados.get("tirosInimigo");
        if (tirosInimigo != null) {
            for (Map<String, Object> moveMap : tirosInimigo) {
                List<Map<String, Object>> shotsList = (List<Map<String, Object>>) moveMap.get("shots");
                if (shotsList != null) {
                    List<IPosition> shots = new ArrayList<>();
                    for (Map<String, Object> shotMap : shotsList) {
                        Position shotPos = parsePositionFromMap(shotMap);
                        if (shotPos != null) shots.add(shotPos);
                    }
                    if (!shots.isEmpty()) restoredGame.fireShots(shots);
                }
            }
        }

        return restoredGame;
    }

    /**
     * Auxiliar para converter um Map com informações de posição para um objeto Position.
     * Suporta tanto representações numéricas (row, column) como alfanuméricas ("A", 3).
     */
    private static Position parsePositionFromMap(Map<String, Object> map) {
        if (map == null) return null;

        Object rawRow = map.get("row");
        Object rawCol = map.get("column");

        if (rawRow == null || rawCol == null) return null;

        // Caso a coluna seja dada por uma letra (ex: "A", "B", 'A')
        if (rawCol instanceof String && ((String) rawCol).matches("[A-Za-z]")) {
            char colChar = ((String) rawCol).toUpperCase().charAt(0);
            int rowNum = parseCoordinate(rawRow);
            return new Position(colChar, rowNum);
        }

        // Caso a linha seja dada por uma letra
        if (rawRow instanceof String && ((String) rawRow).matches("[A-Za-z]")) {
            char colChar = ((String) rawRow).toUpperCase().charAt(0);
            int rowNum = parseCoordinate(rawCol);
            return new Position(colChar, rowNum);
        }

        // Caso ambas sejam representações numéricas
        int r = parseCoordinate(rawRow);
        int c = parseCoordinate(rawCol);
        return new Position(r, c);
    }

    /**
     * Auxiliar para converter com segurança qualquer valor (Number ou String) num int sem exceções de cast.
     */
    private static int parseCoordinate(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.length() == 1 && Character.isLetter(str.charAt(0))) {
                return Character.toUpperCase(str.charAt(0)) - 'A';
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}