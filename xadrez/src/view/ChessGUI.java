package view;

import controller.Game;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import model.board.Move;
import model.board.Position;
import model.pieces.Piece;

public class ChessGUI extends JFrame {
    private Game game;
    private JPanel boardPanel;
    private JButton[][] squares;
    private Map<String, ImageIcon> pieceIcons;
    private JTextArea moveHistoryTextArea;
    private JLabel turnLabel;
    private Color lightSquareColor;
    private Color darkSquareColor;
    private String piecesTheme;

    public ChessGUI() {
        game = new Game();
        piecesTheme = "classic"; // tema inicial
        initializeGUI();
        applyTheme("Clássico");
        loadPieceIcons();
        updateBoardDisplay();
    }

    private void initializeGUI() {
        setTitle("Jogo de Xadrez em Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 630); // ajuste para caber painel direito
        setLayout(new BorderLayout());

        // Painel superior com informações do jogo e botões
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(600, 30));
        turnLabel = new JLabel("Turno: Brancas");
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(turnLabel);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton newGameButton = new JButton("Novo Jogo");
        newGameButton.addActionListener(e -> {
            game = new Game();
            updateBoardDisplay();
            updateMoveHistory();
            turnLabel.setText("Turno: Brancas");
        });
        controlPanel.add(newGameButton);

        JButton undoButton = new JButton("Desfazer");
        undoButton.addActionListener(e -> {
            // Implementar funcionalidade de desfazer
        });
        controlPanel.add(undoButton);

        JButton saveButton = new JButton("Salvar");
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Jogo");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                game.saveGame(fileToSave.getAbsolutePath());
            }
        });
        controlPanel.add(saveButton);

        JButton loadButton = new JButton("Carregar");
        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Carregar Jogo");
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToLoad = fileChooser.getSelectedFile();
                Game loadedGame = Game.loadGame(fileToLoad.getAbsolutePath());
                if (loadedGame != null) {
                    game = loadedGame;
                    updateBoardDisplay();
                    updateMoveHistory();
                    turnLabel.setText("Turno: " +
                            (game.isWhiteTurn() ? "Brancas" : "Pretas"));
                }
            }
        });
        controlPanel.add(loadButton);

        topPanel.add(controlPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Painel do tabuleiro
        boardPanel = new JPanel(new GridLayout(8, 8));
        squares = new JButton[8][8];
        boolean isWhite = true;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = new JButton();
                squares[row][col].setPreferredSize(new Dimension(70, 70));
                squares[row][col].setBackground(isWhite ? Color.WHITE : Color.GRAY);

                final int r = row;
                final int c = col;
                squares[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSquareClick(r, c);
                    }
                });

                boardPanel.add(squares[row][col]);
                isWhite = !isWhite;
            }
            isWhite = !isWhite;
        }
        add(boardPanel, BorderLayout.CENTER);

        // Painel direito com histórico de movimentos
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(150, 600));
        JLabel historyLabel = new JLabel("Histórico de Movimentos");
        historyLabel.setHorizontalAlignment(JLabel.CENTER);
        rightPanel.add(historyLabel, BorderLayout.NORTH);
        moveHistoryTextArea = new JTextArea();
        moveHistoryTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(moveHistoryTextArea);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateMoveHistory() {
        List<Move> history = game.getMoveHistory();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0)
                sb.append((i / 2) + 1).append(". ");
            sb.append(history.get(i).toString()).append(" ");
            if (i % 2 == 1)
                sb.append("\n");
        }
        moveHistoryTextArea.setText(sb.toString());
    }

    private void updateBoardColors() {
        boolean isWhite = true;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setBackground(
                        isWhite ? lightSquareColor : darkSquareColor);
                isWhite = !isWhite;
            }
            isWhite = !isWhite;
        }
    }

    private void updateBoardDisplay() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = game.getBoard().getPieceAt(new Position(row, col));
                if (piece == null) {
                    squares[row][col].setIcon(null);
                } else {
                    squares[row][col].setIcon(pieceIcons.get(getPieceKey(piece)));
                }
                squares[row][col].setBorder(null); // limpa bordas após mover
            }
        }
    }

    private String getPieceKey(Piece piece) {
        String color = piece.isWhite() ? "w" : "b";
        String symbol = switch (piece.getSymbol()) {
            case "K" -> "K";
            case "Q" -> "Q";
            case "R" -> "R";
            case "B" -> "B";
            case "N" -> "N";
            case "P" -> "P";
            default -> "";
        };
        return color + symbol;
    }

    private void handleSquareClick(int row, int col) {
        Position position = new Position(row, col);
        Piece targetPiece = game.getBoard().getPieceAt(position);
        Piece selectedPiece = game.getSelectedPiece();

        clearHighlights();

        if (selectedPiece == null) {
            // Selecionar peça
            game.selectPiece(position);
            selectedPiece = game.getSelectedPiece();
            if (selectedPiece != null)
                highlightSelection(selectedPiece);
        } else {
            if (targetPiece != null && targetPiece.isWhite() == selectedPiece.isWhite()) {
                // Selecionar outra peça da mesma cor
                game.selectPiece(position);
                selectedPiece = game.getSelectedPiece();
                if (selectedPiece != null)
                    highlightSelection(selectedPiece);
            } else {
                // Tentar mover peça
                boolean moveSuccessful = game.movePiece(position);
                if (moveSuccessful) {
                    updateBoardDisplay();
                    updateMoveHistory();
                    turnLabel.setText("Turno: " +
                            (game.isWhiteTurn() ? "Brancas" : "Pretas"));

                    if (game.isInCheck(game.isWhiteTurn())) {
                        JOptionPane.showMessageDialog(this, "Xeque!");
                    }

                    if (game.isGameOver()) {
                        String winner = game.isWhiteTurn() ? "Pretas" : "Brancas";
                        JOptionPane.showMessageDialog(this, winner + " vencem! Xeque-mate.");
                    }
                }
            }
        }
    }

    private void highlightSelection(Piece piece) {
        squares[piece.getPosition().getRow()][piece.getPosition().getColumn()]
                .setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
        List<Position> moves = piece.getPossibleMoves();
        if (moves != null) {
            for (Position pos : moves) {
                squares[pos.getRow()][pos.getColumn()]
                        .setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            }
        }

    }

    private void clearHighlights() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                squares[r][c].setBorder(null);
    }

    private void applyTheme(String theme) {
        switch (theme) {
            case "Clássico" -> {
                lightSquareColor = new Color(240, 217, 181);
                darkSquareColor = new Color(181, 136, 99);
                piecesTheme = "classic";
            }
            case "Azul" -> {
                lightSquareColor = new Color(222, 227, 230);
                darkSquareColor = new Color(140, 184, 219);
                piecesTheme = "blue";
            }
            case "Verde" -> {
                lightSquareColor = new Color(235, 236, 208);
                darkSquareColor = new Color(119, 149, 86);
                piecesTheme = "green";
            }
            default -> {
                lightSquareColor = Color.WHITE;
                darkSquareColor = Color.GRAY;
                piecesTheme = "default";
            }
        }

        loadPieceIcons();
        updateBoardColors();
        updateBoardDisplay();
    }

    private void loadPieceIcons() {
        pieceIcons = new HashMap<>();
        String[] pieces = { "king", "queen", "rook", "bishop", "knight", "pawn" };
        String[] colors = { "white", "black" };

        for (String color : colors) {
            for (String piece : pieces) {
                String key = (color.equals("white") ? "w" : "b") + piece.substring(0, 1).toUpperCase();
                String path = "/resources/pieces/" + piecesTheme + "/" +
                        color + "_" + piece + ".png";

                URL imageURL = getClass().getResource(path);
                if (imageURL != null) {
                    pieceIcons.put(key, new ImageIcon(imageURL));
                } else {
                    System.err.println("Não foi possível encontrar: " + path);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
