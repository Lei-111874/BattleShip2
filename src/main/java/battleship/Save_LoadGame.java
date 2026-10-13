package battleship;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Save_LoadGame {
    //Perguntar se o jogador quer guardar o jogo ou não
    public static void perguntar_guardar (Game game, Scanner scanner){
        System.out.print("Deseja guardar o jogo? (sim/nao): ");
        String resposta = scanner.nextLine().trim();
        if (resposta.equalsIgnoreCase("s")) {
            guardar(game); }
         }
    public static void guardar(Game game) {
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
            mapper.writeValue(new File("savegame.json"), dados);
            System.out.println("Jogo guardado com sucesso!"); }
        catch (IOException e) {
            System.out.println("Erro ao guardar o jogo: " + e.getMessage());
        }
    }
    }


