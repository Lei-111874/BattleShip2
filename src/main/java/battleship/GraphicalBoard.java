package battleship;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GraphicalBoard extends JFrame {

    private final IGame game;
    private final FlatCell[][] defenseCells = new FlatCell[10][10];
    private final FlatCell[][] attackCells = new FlatCell[10][10];
    private final Color MAC_BLUE = new Color(0, 122, 255);
    private final Color MAC_GRAY = new Color(142, 142, 147);
    private final Color MAC_RED = new Color(255, 59, 48);
    private final Color MAC_GREEN = new Color(52, 199, 89);

    public static void show(IGame game) {
        if (game == null) {
            System.out.println("Cria uma partida primeiro usando 'gerafrota' ou 'lefrota'.");
            return;
        }
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
        } catch (Exception ex) {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            GraphicalBoard frame = new GraphicalBoard(game);
            frame.setVisible(true);
        });
    }

    public GraphicalBoard(IGame game) {
        this.game = game;
        setTitle("Batalha Naval");
        setSize(1000, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(30, 20));
        getContentPane().setBackground(new Color(245, 245, 247));

        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel gridsContainer = new JPanel(new GridLayout(1, 2, 50, 0));
        gridsContainer.setOpaque(false);

        gridsContainer.add(createGridPanel("Grelha de Defesa", defenseCells));
        gridsContainer.add(createGridPanel("Grelha de Ataque", attackCells));

        add(gridsContainer, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem("Água", MAC_BLUE, "", Color.BLACK));
        legendPanel.add(createLegendItem("Meu Navio", MAC_GRAY, "", Color.BLACK));
        legendPanel.add(createLegendItem("Tiro na Água", MAC_BLUE, "X", MAC_RED));
        legendPanel.add(createLegendItem("Navio Atingido", MAC_GRAY, "✔", MAC_GREEN));
        add(legendPanel, BorderLayout.SOUTH);

        updateGrids();

        Timer timer = new Timer(1000, e -> updateGrids());
        timer.start();
    }

    private JPanel createGridPanel(String titleText, FlatCell[][] cellsArray) {
        JPanel panel = new JPanel(new BorderLayout(5, 10)); // Mais espaço entre o título e a grelha
        panel.setOpaque(false);

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(29, 29, 31));
        panel.add(title, BorderLayout.NORTH);

        JPanel boardWithAxes = new JPanel(new BorderLayout(5, 5));
        boardWithAxes.setOpaque(false);

        JPanel topAxes = new JPanel(new GridLayout(1, 10));
        topAxes.setOpaque(false);
        for(int c = 0; c < 10; c++) topAxes.add(createHeaderLabel(String.valueOf(c + 1)));

        JPanel leftAxes = new JPanel(new GridLayout(10, 1));

        leftAxes.setPreferredSize(new Dimension(25, 0));
        leftAxes.setOpaque(false);
        for(int r = 0; r < 10; r++) leftAxes.add(createHeaderLabel(String.valueOf((char)('A' + r))));

        JLabel corner = new JLabel();
        corner.setPreferredSize(new Dimension(25, 20));

        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.setOpaque(false);
        northPanel.add(corner, BorderLayout.WEST);
        northPanel.add(topAxes, BorderLayout.CENTER);

        boardWithAxes.add(northPanel, BorderLayout.NORTH);
        boardWithAxes.add(leftAxes, BorderLayout.WEST);

        JPanel grid = new JPanel(new GridLayout(10, 10, 1, 1));
        grid.setOpaque(false);
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                FlatCell cell = new FlatCell(r, c);
                cellsArray[r][c] = cell;
                grid.add(cell);
            }
        }
        boardWithAxes.add(grid, BorderLayout.CENTER);

        panel.add(boardWithAxes, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(134, 134, 139));
        return label;
    }

    private void updateGrids() {
        if (game == null) return;

        boolean[][] alienShots = new boolean[10][10];
        List<IMove> alienMoves = game.getAlienMoves();
        if (alienMoves != null) {
            for (IMove move : alienMoves) {
                for (IPosition p : move.getShots()) {
                    if (p.isInside()) {
                        alienShots[p.getRow()][p.getColumn()] = true;
                    }
                }
            }
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Position pos = new Position(r, c);
                IShip ship = game.getMyFleet().shipAt(pos);
                boolean isHit = alienShots[r][c];
                FlatCell cell = defenseCells[r][c];

                if (ship != null && isHit) {
                    cell.setCell(MAC_GRAY, "✔", MAC_GREEN);
                } else if (ship != null) {
                    cell.setCell(MAC_GRAY, "", Color.BLACK);
                } else if (isHit) {
                    cell.setCell(MAC_BLUE, "X", MAC_RED);
                } else {
                    cell.setCell(MAC_BLUE, "", Color.BLACK);
                }
            }
        }

        int[][] myShotsMap = new int[10][10];
        List<IMove> myMoves = game.getMyMoves();
        if (myMoves != null) {
            for (IMove move : myMoves) {
                List<IPosition> shots = move.getShots();
                List<IGame.ShotResult> results = move.getShotResults();
                for (int i = 0; i < shots.size(); i++) {
                    IPosition p = shots.get(i);
                    if (p.isInside() && i < results.size()) {
                        IGame.ShotResult res = results.get(i);
                        if (res.valid()) {
                            if (res.ship() != null) {
                                myShotsMap[p.getRow()][p.getColumn()] = 2;
                            } else {
                                myShotsMap[p.getRow()][p.getColumn()] = 1;
                            }
                        }
                    }
                }
            }
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int state = myShotsMap[r][c];
                FlatCell cell = attackCells[r][c];

                if (state == 2) {
                    cell.setCell(MAC_GRAY, "✔", MAC_GREEN);
                } else if (state == 1) {
                    cell.setCell(MAC_BLUE, "X", MAC_RED);
                } else {
                    cell.setCell(MAC_BLUE, "", Color.BLACK);
                }
            }
        }
    }

    private JPanel createLegendItem(String text, Color bgColor, String symbol, Color symbolColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        JLabel colorBox = new JLabel(symbol, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        colorBox.setPreferredSize(new Dimension(24, 24));
        colorBox.setForeground(symbolColor);
        colorBox.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(new Color(29, 29, 31));

        panel.add(colorBox);
        panel.add(label);
        return panel;
    }

    class FlatCell extends JLabel {
        private final int row, col;
        private Color bgColor = MAC_BLUE;

        public FlatCell(int row, int col) {
            super("", SwingConstants.CENTER);
            this.row = row;
            this.col = col;
            setOpaque(false);
            setFont(new Font("SansSerif", Font.BOLD, 22));
        }

        public void setCell(Color bg, String text, Color fg) {
            this.bgColor = bg;
            setText(text);
            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 16;

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (!(row == 0 && col == 0)) g2.fillRect(0, 0, arc, arc);
            if (!(row == 0 && col == 9)) g2.fillRect(w - arc, 0, arc, arc);
            if (!(row == 9 && col == 0)) g2.fillRect(0, h - arc, arc, arc);
            if (!(row == 9 && col == 9)) g2.fillRect(w - arc, h - arc, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}