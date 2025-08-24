package controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import model.board.Board;
import model.board.Move;
import model.board.Position;
import model.pieces.*;

public class Game {
    private Board board;
    private boolean isWhiteTurn;
    private boolean isGameOver;
    private Piece selectedPiece;
    private Position lastPawnDoubleMove;
    private List moveHistory;

    public Game() {
        board = new Board();
        isWhiteTurn = true;
        isGameOver = false;
        setupPieces();
        moveHistory = new ArrayList<>();
    }

    private void setupPieces() {
        // Colocar peças na posição inicial
        // Peças brancas
        board.placePiece(new Rook(board, true), new Position(7, 0));
        board.placePiece(new Knight(board, true), new Position(7, 1));
        board.placePiece(new Bishop(board, true), new Position(7, 2));
        board.placePiece(new Queen(board, true), new Position(7, 3));
        board.placePiece(new King(board, true), new Position(7, 4));
        board.placePiece(new Bishop(board, true), new Position(7, 5));
        board.placePiece(new Knight(board, true), new Position(7, 6));
        board.placePiece(new Rook(board, true), new Position(7, 7));

        for (int col = 0; col < 8; col++) {
            board.placePiece(new Pawn(board, true), new Position(6, col));
        }

        // Peças pretas (mesma lógica)
        // ...
    }

    public Board getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public void selectPiece(Position position) {
        Piece piece = board.getPieceAt(position);

        // Só pode selecionar peça da cor do jogador atual
        if (piece != null && piece.isWhite() == isWhiteTurn) {
            selectedPiece = piece;
        }
    }

    public boolean movePiece(Position destination) {
        if (selectedPiece == null || isGameOver) {
            return false;
        }

        // Verificar se o movimento é válido
        if (!selectedPiece.canMoveTo(destination)) {
            return false;
        }

        // Verificar se o movimento deixa o rei em xeque
        if (moveCausesCheck(selectedPiece, destination)) {
            return false;
        }

        // Capturar peça, se necessário
        Piece capturedPiece = board.getPieceAt(destination);

        // Guardar posição original para desfazer o movimento, se necessário
        Position originalPosition = selectedPiece.getPosition();

        // Fazer o movimento
        board.removePiece(originalPosition);
        board.placePiece(selectedPiece, destination);

         Move move = new Move(originalPosition, destination, 
        selectedPiece, capturedPiece);
    
    if (selectedPiece instanceof King &&
        Math.abs(destination.getColumn() - originalPosition.getColumn()) == 2) {
        move.setCastling(true);
    } else if (selectedPiece instanceof Pawn &&
               Math.abs(destination.getRow() - originalPosition.getRow()) == 2) {
        move.setEnPassant(true);
    } else if (selectedPiece instanceof Pawn &&
               (destination.getRow() == 0 || destination.getRow() == 7)) {
        move.setPromotion(true);
    }
    
    // Adicionar ao histórico
    moveHistory.add(move);
        // Verificar condições especiais (promoção de peão, etc.)
        checkSpecialConditions(selectedPiece, destination);

        // Verificar se o oponente está em xeque ou xeque-mate
        checkGameStatus();

        // Passar o turno
        isWhiteTurn = !isWhiteTurn;
        selectedPiece = null;
        if (selectedPiece instanceof Pawn) {
            // Verificar movimento duplo
            if (Math.abs(destination.getRow() -
                    originalPosition.getRow()) == 2) {
                lastPawnDoubleMove = destination;
            } else {
                // Verificar captura en passant
                if (Math.abs(destination.getColumn() -
                        originalPosition.getColumn()) == 1 &&
                        board.getPieceAt(destination) == null) {

                    // É uma captura en passant
                    Position capturedPawnPos = new Position(
                            originalPosition.getRow(),
                            destination.getColumn());

                    board.removePiece(capturedPawnPos);
                }
            }
            
        } else {
            lastPawnDoubleMove = null;
        }
        
        return true;
    }

    private boolean moveCausesCheck(Piece piece, Position destination) {
        // Implementação para verificar se um movimento deixa o próprio rei em xeque
        // ...
        return false;
    }

    private void checkSpecialConditions(Piece piece, Position destination) {
        // Verificar promoção de peão
        if (piece instanceof Pawn) {
            if ((piece.isWhite() && destination.getRow() == 0) ||
                    (!piece.isWhite() && destination.getRow() == 7)) {

                // Perguntar ao jogador para qual peça deseja promover
                String[] options = { "Rainha", "Torre", "Bispo", "Cavalo" };
                int choice = JOptionPane.showOptionDialog(null,
                        "Escolha uma peça para promoção:",
                        "Promoção de Peão",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                // Criar a nova peça
                Piece newPiece;
                switch (choice) {
                    case 0:
                        newPiece = new Queen(board, piece.isWhite());
                        break;
                    case 1:
                        newPiece = new Rook(board, piece.isWhite());
                        break;
                    case 2:
                        newPiece = new Bishop(board, piece.isWhite());
                        break;
                    case 3:
                        newPiece = new Knight(board, piece.isWhite());
                        break;
                    default:
                        newPiece = new Queen(board, piece.isWhite());
                }

                // Substituir o peão pela nova peça
                board.removePiece(destination);
                board.placePiece(newPiece, destination);
            }
        }

        // Verificar outras condições especiais (en passant, etc.)
        // ..
    }

 private void checkGameStatus() {
    boolean whiteKingInCheck = isInCheck(true);
    boolean blackKingInCheck = isInCheck(false);
    
    if (whiteKingInCheck && isCheckmate(true)) {
        isGameOver = true;
        // Pretas vencem
    } else if (blackKingInCheck && isCheckmate(false)) {
        isGameOver = true;
        // Brancas vencem
    }
    
    // Verificar empate (stalemate, repetição, etc.)
    // ...
 }

    if(selectedPiece instanceof King&&Math.abs(destination.getColumn()-originalPosition.getColumn())==2)

    {

        Position destination;
        Position originalPosition;
        // É um movimento de roque
        int rookColumn = destination.getColumn() > originalPosition.getColumn() ? 7 : 0;
        int newRookColumn = destination.getColumn() > originalPosition.getColumn() ? destination.getColumn() - 1
                : destination.getColumn() + 1;

        Position rookPosition = new Position(
                originalPosition.getRow(), rookColumn);
        Position newRookPosition = new Position(
                originalPosition.getRow(), newRookColumn);

        Piece rook = board.getPieceAt(rookPosition);
        board.removePiece(rookPosition);
        board.placePiece(rook, newRookPosition);
    }

  private boolean isInCheck(boolean whiteKing) {
    // Encontrar a posição do rei
    Position kingPosition = null;
    
    for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {
            Position pos = new Position(row, col);
            Piece piece = board.getPieceAt(pos);
            
            if (piece instanceof King && 
                piece.isWhite() == whiteKing) {
                kingPosition = pos;
                break;
            }
        }
        if (kingPosition != null) break;
    }
    
    // Verificar se alguma peça adversária pode capturar o rei
    return board.isUnderAttack(kingPosition, !whiteKing);
 }

 public boolean undoLastMove() {
    if (moveHistory.isEmpty()) {
        return false;
    }
    
    Move lastMove = moveHistory.remove(moveHistory.size() - 1);
    
    // Mover a peça de volta
    board.removePiece(lastMove.getTo());
    board.placePiece(lastMove.getPiece(), lastMove.getFrom());
    
    // Restaurar peça capturada, se houver
    if (lastMove.getCapturedPiece() != null) {
        board.placePiece(lastMove.getCapturedPiece(), 
            lastMove.getTo());
    }
    
    // Lidar com casos especiais (roque, en passant, promoção)
    // ...
    
    // Restaurar o turno
    isWhiteTurn = !isWhiteTurn;
    
    return true;
 }

 private boolean isCheckmate(boolean whiteKing) {
    if (!isInCheck(whiteKing)) {
        return false;
    }
    
    // Verificar se há algum movimento legal para sair do xeque
    for (int row = 0; row < 8; row++) {
        for (int col = 0; col < 8; col++) {
            Position pos = new Position(row, col);
            Piece piece = board.getPieceAt(pos);
            
            if (piece != null && piece.isWhite() == whiteKing) {
                for (Object obj : piece.getPossibleMoves()) {
                    Position movePos = (Position) obj;
                    // Testar se o movimento tira o rei do xeque
                    if (!moveCausesCheck(piece, movePos)) {
                        return false;
                    }
                }
                }
            }
        }
        return true;
    }
    
        public void saveGame(String filePath) {
    try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(filePath))) {
        
        // Salvar o estado do jogo
        oos.writeObject(board);
        oos.writeBoolean(isWhiteTurn);
        oos.writeObject(moveHistory);
        oos.writeInt(movesSinceLastCaptureOrPawnMove);
        // Salvar outras informações relevantes
        
        System.out.println("Jogo salvo com sucesso em: " + filePath);
    } catch (IOException e) {
        System.err.println("Erro ao salvar o jogo: " + e.getMessage());
    }
 }

 public static Game loadGame(String filePath) {
    try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(filePath))) {
        
        Game game = new Game(false); // Construtor especial sem setup inicial
        
        // Carregar o estado do jogo
        game.board = (Board) ois.readObject();
        game.isWhiteTurn = ois.readBoolean();
        game.moveHistory = (List) ois.readObject();
        game.movesSinceLastCaptureOrPawnMove = ois.readInt();
        // Carregar outras informações relevantes
        
        System.out.println("Jogo carregado com sucesso de: " + filePath);
        return game;
    } catch (IOException | ClassNotFoundException e) {
        System.err.println("Erro ao carregar o jogo: " + e.getMessage());
        return null;
    }
 }return true;
}}