
package battleship;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class pdfCreator {

    public void export(List<IMove> moves, String filePath)
            throws IOException, DocumentException {

        // Verificar se existem jogadas
        if (moves == null || moves.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não existem jogadas para exportar.");
        }

        // Criar o documento PDF
        Document document = new Document();

        try (FileOutputStream out = new FileOutputStream(filePath)) {

            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Jogadas da partida - Battleship"));
            document.add(new Paragraph(" "));

            // Percorrer todas as jogadas
            for (IMove move : moves) {

                document.add(new Paragraph(
                        "Jogada nº " + move.getNumber()));

                List<IPosition> shots = move.getShots();
                List<IGame.ShotResult> results = move.getShotResults();

                // Escrever as posições e os resultados
                for (int i = 0; i < shots.size(); i++) {

                    String linha = "Tiro " + (i + 1)
                            + ": " + shots.get(i);

                    if (i < results.size()) {
                        IGame.ShotResult result = results.get(i);

                        if (!result.valid()) {
                            linha += " - Tiro inválido";
                        } else if (result.repeated()) {
                            linha += " - Tiro repetido";
                        } else if (result.sunk()) {
                            linha += " - Navio afundado";
                        } else if (result.ship() != null) {
                            linha += " - Acertou num navio";
                        } else {
                            linha += " - Água";
                        }
                    }

                    document.add(new Paragraph(linha));
                }

                document.add(new Paragraph(" "));
            }

            document.close();
        }
    }
}
