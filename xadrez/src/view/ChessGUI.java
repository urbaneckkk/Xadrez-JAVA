package view;

import controller.ChessAI;
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
import view.SoundPlayer;

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
    private ChessAI ai;
    private boolean playAgainstAI;
    private boolean aiPlaysWhite;
    
    // Temporizador
    private int whiteTimeSeconds = 600; // 10 minutos
    private int blackTimeSeconds = 600; // 10 minutos
    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;
    private Timer gameTimer;
    private boolean timerActive = false;

    public ChessGUI() {
        game = new Game();
        ai = new ChessAI(game);
        playAgainstAI = true; // Ativa o modo contra IA por padrão
        aiPlaysWhite = false; // IA joga com as peças pretas por padrão
        piecesTheme = "classic"; // tema inicial
        initializeGUI();
        applyTheme("Clássico");
        loadPieceIcons();
        updateBoardDisplay();
        initializeTimer();
    }
    
    private void initializeTimer() {
        gameTimer = new Timer(1000, e -> {
            if (timerActive) {
                if (game.isWhiteTurn()) {
                    whiteTimeSeconds--;
                    updateTimerDisplay();
                    
                    if (whiteTimeSeconds <= 0) {
                        timerActive = false;
                        gameTimer.stop();
                        handleTimeOut(true);
                    }
                } else {
                    blackTimeSeconds--;
                    updateTimerDisplay();
                    
                    if (blackTimeSeconds <= 0) {
                        timerActive = false;
                        gameTimer.stop();
                        handleTimeOut(false);
                    }
                }
            }
        });
        gameTimer.setRepeats(true);
    }
    
    private void updateTimerDisplay() {
        whiteTimerLabel.setText(formatTime(whiteTimeSeconds));
        blackTimerLabel.setText(formatTime(blackTimeSeconds));
    }
    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    private void handleTimeOut(boolean isWhite) {
        // Conta peças para determinar o vencedor
        int whitePieces = countPieces(true);
        int blackPieces = countPieces(false);
        
        String winner;
        if (isWhite) {
            // Tempo das brancas acabou
            winner = whitePieces < blackPieces ? "Pretas" : "Brancas";
        } else {
            // Tempo das pretas acabou
            winner = blackPieces < whitePieces ? "Brancas" : "Pretas";
        }
        
        JOptionPane.showMessageDialog(this, 
            "Tempo esgotado! " + (isWhite ? "Brancas" : "Pretas") + 
            " ficaram sem tempo.\n" + winner + " vencem por ter mais peças!");
    }
    
    /**
     * Reproduz o som de movimento ou captura
     * @param isCapture true se for uma captura, false se for um movimento normal
     */
    private void playMoveSound(boolean isCapture) {
        SoundPlayer.playSound(isCapture);
    }
    
    private int countPieces(boolean isWhite) {
        int count = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = game.getBoard().getPieceAt(new Position(row, col));
                if (piece != null && piece.isWhite() == isWhite) {
                    count++;
                }
            }
        }
        return count;
    }

    private void initializeGUI() {
        setTitle("Jogo de Xadrez em Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 630); // ajuste para caber painel direito
        setLayout(new BorderLayout());

        // Painel superior com informações do jogo e botões
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        infoPanel.setPreferredSize(new Dimension(600, 30));
        
        // Timer das brancas
        whiteTimerLabel = new JLabel("10:00");
        whiteTimerLabel.setForeground(Color.BLUE);
        whiteTimerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel whiteTimerPanel = new JPanel();
        whiteTimerPanel.add(new JLabel("Brancas: "));
        whiteTimerPanel.add(whiteTimerLabel);
        infoPanel.add(whiteTimerPanel);
        
        // Label do turno
        turnLabel = new JLabel("Turno: Brancas");
        turnLabel.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(turnLabel);
        
        // Timer das pretas
        blackTimerLabel = new JLabel("10:00");
        blackTimerLabel.setForeground(Color.RED);
        blackTimerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel blackTimerPanel = new JPanel();
        blackTimerPanel.add(new JLabel("Pretas: "));
        blackTimerPanel.add(blackTimerLabel);
        infoPanel.add(blackTimerPanel);
        
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
            boolean undone = game.undoLastMove();
            if (undone) {
                updateBoardDisplay();
                updateMoveHistory();
                turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
            } else {
                JOptionPane.showMessageDialog(this, "Não há jogadas para desfazer!");
            }
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

        // ======================
        // MENU AJUSTADO PARA SWING
        // ======================
        JMenuBar menuBar = new JMenuBar(); // criar JMenuBar local

        JMenu gameMenu = new JMenu("Jogo");

        JMenuItem humanVsHuman = new JMenuItem("Humano vs Humano");
        humanVsHuman.addActionListener(e -> {
            playAgainstAI = false;
            game = new Game();
            ai = new ChessAI(game); // Atualiza a instância da IA com o novo jogo
            updateBoardDisplay();
            updateMoveHistory();
            turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
        });
        gameMenu.add(humanVsHuman);

        JMenuItem humanVsAI = new JMenuItem("Humano vs Computador (Brancas)");
        humanVsAI.addActionListener(e -> {
            playAgainstAI = true;
            aiPlaysWhite = false;
            game = new Game();
            ai = new ChessAI(game); // Atualiza a instância da IA com o novo jogo
            updateBoardDisplay();
            updateMoveHistory();
            turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
        });
        gameMenu.add(humanVsAI);

        JMenuItem aiVsHuman = new JMenuItem("Computador vs Humano (Pretas)");
        aiVsHuman.addActionListener(e -> {
            playAgainstAI = true;
            aiPlaysWhite = true;
            game = new Game();
            ai = new ChessAI(game); // Atualiza a instância da IA com o novo jogo
            updateBoardDisplay();
            updateMoveHistory();
            turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
            // IA faz o primeiro movimento
            playAIMoveIfNeeded();
        });
        gameMenu.add(aiVsHuman);
        
        // Menu de temas
        JMenu themeMenu = new JMenu("Temas");
        
        JMenuItem classicTheme = new JMenuItem("Clássico");
        classicTheme.addActionListener(e -> applyTheme("Clássico"));
        themeMenu.add(classicTheme);
        
        JMenuItem blueTheme = new JMenuItem("Azul");
        blueTheme.addActionListener(e -> applyTheme("Azul"));
        themeMenu.add(blueTheme);
        
        JMenuItem greenTheme = new JMenuItem("Verde");
        greenTheme.addActionListener(e -> applyTheme("Verde"));
        themeMenu.add(greenTheme);
        
        JMenuItem purpleTheme = new JMenuItem("Roxo e Branco");
        purpleTheme.addActionListener(e -> applyTheme("Roxo e Branco"));
        themeMenu.add(purpleTheme);
        
        JMenuItem redTheme = new JMenuItem("Vermelho e Preto");
        redTheme.addActionListener(e -> applyTheme("Vermelho e Preto"));
        themeMenu.add(redTheme);
        
        JMenuItem greenBlackTheme = new JMenuItem("Verde e Preto");
        greenBlackTheme.addActionListener(e -> applyTheme("Verde e Preto"));
        themeMenu.add(greenBlackTheme);
        
        JMenuItem pinkTheme = new JMenuItem("Rosa e Branco");
        pinkTheme.addActionListener(e -> applyTheme("Rosa e Branco"));
        themeMenu.add(pinkTheme);
        
        JMenuItem newGame = new JMenuItem("Novo Jogo");
        newGame.addActionListener(e -> {
            game = new Game();
            ai = new ChessAI(game); // Atualiza a instância da IA com o novo jogo
            updateBoardDisplay();
            updateMoveHistory();
            turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));
            
            // Reinicia o temporizador
            whiteTimeSeconds = 600;
            blackTimeSeconds = 600;
            updateTimerDisplay();
            timerActive = true;
            gameTimer.start();
            
            // Se for modo contra IA e for a vez da IA, faz o movimento
            playAIMoveIfNeeded();
        });
        gameMenu.add(newGame);

        menuBar.add(gameMenu);
        menuBar.add(themeMenu);
        setJMenuBar(menuBar); // definir JMenuBar no JFrame
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

    private void playAIMoveIfNeeded() {
        if (!playAgainstAI)
            return;

        // Checa se é vez da IA
        if (game.isWhiteTurn() == aiPlaysWhite && !game.isGameOver()) {
            Timer timer = new Timer(500, e -> {
                ai.makeMove();
                // Atualiza a interface gráfica imediatamente após o movimento da IA
                SwingUtilities.invokeLater(() -> {
                    updateBoardDisplay();
                    updateMoveHistory();
                    turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));

                    if (game.isInCheck(game.isWhiteTurn())) {
                        JOptionPane.showMessageDialog(this, "Xeque!");
                    }

                    if (game.isGameOver()) {
                        String winner = game.isWhiteTurn() ? "Pretas" : "Brancas";
                        JOptionPane.showMessageDialog(this, winner + " vencem! Xeque-mate.");
                    }

                    // Chamar novamente caso seja turno contínuo da IA (ex: IA joga as duas brancas)
                    playAIMoveIfNeeded();
                });
            });
            timer.setRepeats(false);
            timer.start();
        }
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
        Piece selectedPiece = game.getSelectedPiece();

        clearHighlights();

        if (selectedPiece == null) {
            game.selectPiece(position);
            selectedPiece = game.getSelectedPiece();
            if (selectedPiece != null)
                highlightSelection(selectedPiece);
        } else {
            boolean moveSuccessful = game.movePiece(selectedPiece.getPosition(), position);
            if (moveSuccessful) {
                // Reproduz som de movimento
                playMoveSound(game.getBoard().getPieceAt(position) != null);
                
                // Inicia o temporizador se ainda não estiver ativo
                if (!timerActive) {
                    timerActive = true;
                    gameTimer.start();
                }
                
                updateBoardDisplay();
                updateMoveHistory();
                turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));

                if (game.isInCheck(game.isWhiteTurn())) {
                    JOptionPane.showMessageDialog(this, "Xeque!");
                }

                if (game.isGameOver()) {
                    timerActive = false;
                    gameTimer.stop();
                    String winner = game.isWhiteTurn() ? "Pretas" : "Brancas";
                    JOptionPane.showMessageDialog(this, winner + " vencem! Xeque-mate.");
                }

                if (playAgainstAI && game.isWhiteTurn() == aiPlaysWhite) {
                    Timer timer = new Timer(500, e -> {
                        ai.makeMove();
                        // Atualiza a interface gráfica imediatamente após o movimento da IA
                        SwingUtilities.invokeLater(() -> {
                            // Verifica se houve captura para tocar o som apropriado
                            boolean wasCapture = false;
                            List<Move> history = game.getMoveHistory();
                            if (!history.isEmpty()) {
                                Move lastMove = history.get(history.size() - 1);
                                wasCapture = lastMove.getCapturedPiece() != null;
                            }
                            playMoveSound(wasCapture);
                            
                            updateBoardDisplay();
                            updateMoveHistory();
                            turnLabel.setText("Turno: " + (game.isWhiteTurn() ? "Brancas" : "Pretas"));

                            if (game.isInCheck(game.isWhiteTurn())) {
                                JOptionPane.showMessageDialog(this, "Xeque!");
                            }
                            
                            if (game.isGameOver()) {
                                timerActive = false;
                                gameTimer.stop();
                                String winner = game.isWhiteTurn() ? "Pretas" : "Brancas";
                                JOptionPane.showMessageDialog(this, winner + " vencem! Xeque-mate.");
                            }
                        });
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            } else {
                // Se o movimento for inválido, tenta selecionar outra peça
                Piece newSelectedPiece = game.getBoard().getPieceAt(position);
                if (newSelectedPiece != null && newSelectedPiece.isWhite() == game.isWhiteTurn()) {
                    game.selectPiece(position);
                    highlightSelection(game.getSelectedPiece());
                } else {
                    game.selectPiece(null); // Desseleciona se o clique for inválido
                }
            }
        }
    }

    private void highlightSelection(Piece piece) {
        Position from = piece.getPosition();
        squares[from.getRow()][from.getColumn()]
                .setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));

        List<Position> moves = piece.getPossibleMoves();
        if (moves != null) {
            for (Position pos : moves) {
                Piece targetPiece = game.getBoard().getPieceAt(pos);
                if (targetPiece == null) {
                    // Movimento normal
                    squares[pos.getRow()][pos.getColumn()]
                            .setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                } else if (targetPiece.isWhite() != piece.isWhite()) {
                    // Captura
                    squares[pos.getRow()][pos.getColumn()]
                            .setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                }
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
            case "Roxo e Branco" -> {
                lightSquareColor = new Color(255, 255, 255);
                darkSquareColor = new Color(128, 0, 128);
                piecesTheme = "classic";
            }
            case "Vermelho e Preto" -> {
                lightSquareColor = new Color(0, 0, 0);
                darkSquareColor = new Color(178, 34, 34);
                piecesTheme = "classic";
            }
            case "Verde e Preto" -> {
                lightSquareColor = new Color(0, 0, 0);
                darkSquareColor = new Color(0, 128, 0);
                piecesTheme = "classic";
            }
            case "Rosa e Branco" -> {
                lightSquareColor = new Color(255, 255, 255);
                darkSquareColor = new Color(255, 105, 180);
                piecesTheme = "classic";
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
                String key = switch (piece) {
                    case "king" -> (color.equals("white") ? "wK" : "bK");
                    case "queen" -> (color.equals("white") ? "wQ" : "bQ");
                    case "rook" -> (color.equals("white") ? "wR" : "bR");
                    case "bishop" -> (color.equals("white") ? "wB" : "bB");
                    case "knight" -> (color.equals("white") ? "wN" : "bN");
                    case "pawn" -> (color.equals("white") ? "wP" : "bP");
                    default -> "";
                };

                String path = "/resources/pieces/" + piecesTheme + "/" + color + "_" + piece + ".png";

                URL imageURL = getClass().getResource(path);
                if (imageURL != null) {
                    ImageIcon icon = new ImageIcon(imageURL);
                    Image scaled = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    pieceIcons.put(key, new ImageIcon(scaled));
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