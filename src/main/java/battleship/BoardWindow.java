package battleship;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Janela gráfica (Swing + FlatLaf) que representa um tabuleiro da Batalha Naval.
 * Cada chamada a {@link #show} atualiza (ou cria) a janela com o estado atual do jogo.
 */
public class BoardWindow extends JFrame {

    /** Estados possíveis de uma quadrícula do tabuleiro. */
    enum CellState { WATER, SHIP, SHIP_HIT, SHIP_SUNK, SHOT_WATER, SUNK_HALO }

    private static final int N = Game.BOARD_SIZE;
    private static final int CELL = 44;
    private static final int MARGIN = 36;

    private static final Color C_WATER = new Color(0xCFE8F7);
    private static final Color C_HALO = new Color(0xE8F1F7);
    private static final Color C_SHIP = new Color(0x4A5568);
    private static final Color C_HIT = new Color(0xE53E3E);
    private static final Color C_SUNK = new Color(0x822727);
    private static final Color C_SHOT_WATER = new Color(0x2B6CB0);
    private static final Color C_LABEL = new Color(0x2D3748);

    /** Uma janela por tabuleiro (só é acedido na thread de eventos do Swing). */
    private static final Map<String, BoardWindow> WINDOWS = new HashMap<>();
    private static boolean lookAndFeelInstalled = false;

    private final BoardPanel boardPanel = new BoardPanel();

    private BoardWindow(String title) {
        super(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(boardPanel, BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);
        setResizable(false);
        setLocationByPlatform(true);
    }

    // ------------------------------------------------------------------
    // API pública
    // ------------------------------------------------------------------

    /**
     * Mostra (ou atualiza) a janela de um tabuleiro.
     * O estado do tabuleiro é calculado na thread do jogo e só o desenho
     * é feito na thread de eventos do Swing.
     *
     * @param title     título da janela (identifica o tabuleiro)
     * @param fleet     frota a desenhar
     * @param moves     jogadas (rajadas) já efetuadas sobre essa frota
     * @param showShots se true, desenha também o resultado dos tiros
     */
    public static void show(String title, IFleet fleet, List<IMove> moves, boolean showShots) {
        if (GraphicsEnvironment.isHeadless())
            return; // ambientes sem ecrã (ex.: contentor Docker) continuam só com a consola

        CellState[][] map = buildMap(fleet, moves, showShots);

        SwingUtilities.invokeLater(() -> {
            if (!lookAndFeelInstalled) {
                FlatLightLaf.setup();
                lookAndFeelInstalled = true;
            }
            BoardWindow window = WINDOWS.computeIfAbsent(title, BoardWindow::new);
            window.boardPanel.setMap(map);
            window.pack();
            window.setVisible(true);
        });
    }

    // ------------------------------------------------------------------
    // Construção do estado do tabuleiro (mesma lógica de Game.printBoard)
    // ------------------------------------------------------------------

    private static CellState[][] newMap() {
        CellState[][] map = new CellState[N][N];
        for (CellState[] row : map)
            Arrays.fill(row, CellState.WATER);
        return map;
    }

    private static CellState[][] buildMap(IFleet fleet, List<IMove> moves, boolean showShots) {
        CellState[][] map = newMap();

        // 1. Navios e "halo" de água à volta dos navios afundados
        for (IShip ship : fleet.getShips()) {
            for (IPosition p : ship.getPositions())
                map[p.getRow()][p.getColumn()] = CellState.SHIP;
            if (!ship.stillFloating())
                for (IPosition adj : ship.getAdjacentPositions())
                    map[adj.getRow()][adj.getColumn()] = CellState.SUNK_HALO;
        }

        if (showShots) {
            // 2. Tiros: acertos em navios ou na água
            for (IMove move : moves)
                for (IPosition shot : move.getShots())
                    if (shot.isInside()) {
                        CellState current = map[shot.getRow()][shot.getColumn()];
                        if (current == CellState.SHIP)
                            map[shot.getRow()][shot.getColumn()] = CellState.SHIP_HIT;
                        else if (current == CellState.WATER || current == CellState.SUNK_HALO)
                            map[shot.getRow()][shot.getColumn()] = CellState.SHOT_WATER;
                    }

            // 3. Navios afundados destacados dos apenas atingidos
            for (IShip ship : fleet.getSunkShips())
                for (IPosition p : ship.getPositions())
                    map[p.getRow()][p.getColumn()] = CellState.SHIP_SUNK;
        }
        return map;
    }

    // ------------------------------------------------------------------
    // Componentes visuais
    // ------------------------------------------------------------------

    private static Color fillColor(CellState state) {
        return switch (state) {
            case SHIP -> C_SHIP;
            case SHIP_HIT -> C_HIT;
            case SHIP_SUNK -> C_SUNK;
            case SUNK_HALO -> C_HALO;
            default -> C_WATER;
        };
    }

    private static JPanel buildLegend() {
        JPanel legend = new JPanel(new GridLayout(2, 3, 12, 4));
        legend.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));
        legend.add(swatch(C_WATER, "Água"));
        legend.add(swatch(C_SHIP, "Navio"));
        legend.add(swatch(C_HIT, "Navio atingido"));
        legend.add(swatch(C_SUNK, "Navio afundado"));
        legend.add(swatch(C_SHOT_WATER, "Tiro na água"));
        legend.add(swatch(C_HALO, "Adjacente a afundado"));
        return legend;
    }

    private static JLabel swatch(Color color, String text) {
        Icon icon = new Icon() {
            @Override public int getIconWidth() { return 14; }
            @Override public int getIconHeight() { return 14; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRoundRect(x, y, 14, 14, 4, 4);
            }
        };
        return new JLabel(text, icon, SwingConstants.LEFT);
    }

    /** Painel que desenha a grelha 10x10 com as coordenadas A-J e 1-10. */
    private static class BoardPanel extends JPanel {

        private CellState[][] map = newMap();

        BoardPanel() {
            int side = MARGIN + CELL * N + 8;
            setPreferredSize(new Dimension(side, side));
        }

        void setMap(CellState[][] map) {
            this.map = map;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();

            // Coordenadas: colunas 1-10 em cima, linhas A-J à esquerda
            g2.setColor(C_LABEL);
            for (int i = 0; i < N; i++) {
                String col = String.valueOf(i + 1);
                g2.drawString(col, MARGIN + i * CELL + (CELL - fm.stringWidth(col)) / 2, MARGIN - 12);

                String row = String.valueOf((char) ('A' + i));
                int baseline = MARGIN + i * CELL + (CELL - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(row, (MARGIN - fm.stringWidth(row)) / 2, baseline);
            }

            // Quadrículas
            for (int r = 0; r < N; r++)
                for (int c = 0; c < N; c++)
                    paintCell(g2, map[r][c], MARGIN + c * CELL, MARGIN + r * CELL);

            g2.dispose();
        }

        private void paintCell(Graphics2D g2, CellState state, int x, int y) {
            g2.setColor(fillColor(state));
            g2.fillRoundRect(x + 1, y + 1, CELL - 2, CELL - 2, 8, 8);

            switch (state) {
                case SHOT_WATER -> {
                    g2.setColor(C_SHOT_WATER);
                    g2.fillOval(x + CELL / 2 - 5, y + CELL / 2 - 5, 10, 10);
                }
                case SHIP_HIT, SHIP_SUNK -> {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 12, y + 12, x + CELL - 12, y + CELL - 12);
                    g2.drawLine(x + CELL - 12, y + 12, x + 12, y + CELL - 12);
                }
                default -> { }
            }
        }
    }
}