package controller;

import model.board.Position;
import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChessAI {
    private Game game;
    private Random random;

    public ChessAI(Game game) {
        this.game = game;
        this.random = new Random();
    }

    public void makeMove() {
        // Coletar todos os movimentos possíveis
        List possibleMoves = getAllPossibleMoves();
        if (possibleMoves.isEmpty()) {
            return; // Não há movimentos possíveis
        }
        // Escolher um movimento aleatório (IA básica)
        Move selectedMove = possibleMoves.get(
                random.nextInt(possibleMoves.size()));
        // Executar o movimento
        game.selectPiece(selectedMove.getFrom());
        game.movePiece(selectedMove.getTo());
    }

    private List getAllPossibleMoves() {
        List moves = new ArrayList<>();
        boolean isWhite = game.isWhiteTurn();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = game.getBoard().getPieceAt(pos);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (Position movePos : piece.getPossibleMoves()) {
                        // Verificar se o movimento é legal
                        if (!game.moveCausesCheck(piece, movePos)) {
                            moves.add(new Move(pos, movePos,
                                    piece, game.getBoard().getPieceAt(movePos)));
                        }
                    }
                }
            }
        }
        return moves;
    }
}