package controller;

import model.board.Board;
import model.board.Move;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChessAI {
    private final Game game;
    private final Random random = new Random();

    public ChessAI(Game game) {
        this.game = game;
    }

    public void makeMove() {
        Move bestMove = findBestMove(3);
        if (bestMove != null) {
            game.movePieceDirect(bestMove.getFrom(), bestMove.getTo());
        }
    }

    public Move findBestMove(int depth) {
        Board boardClone = game.getBoard().clone();
        List<Move> possibleMoves = getAllPossibleMoves(boardClone, game.isWhiteTurn());
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        for (Move move : possibleMoves) {
            Board testBoard = boardClone.clone();
            makeTestMove(testBoard, move);

            int moveValue = -minimax(testBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !game.isWhiteTurn());

            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0) {
            return evaluateBoard(board, game.isWhiteTurn());
        }

        List<Move> possibleMoves = getAllPossibleMoves(board, isMaximizing ? game.isWhiteTurn() : !game.isWhiteTurn());

        if (possibleMoves.isEmpty()) {
            // xeque-mate ou empate
            return isMaximizing ? -10000 : 10000;
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : possibleMoves) {
                Board testBoard = board.clone();
                makeTestMove(testBoard, move);

                int eval = minimax(testBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : possibleMoves) {
                Board testBoard = board.clone();
                makeTestMove(testBoard, move);

                int eval = minimax(testBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break;
            }
            return minEval;
        }
    }

    private void makeTestMove(Board board, Move move) {
        Piece piece = board.getPieceAt(move.getFrom());
        board.removePiece(move.getFrom());
        board.placePiece(piece, move.getTo());
        if (move.getCapturedPiece() != null) {
            // garante que a peça capturada seja "removida"
            board.removePiece(move.getTo());
        }
    }

    private int evaluateBoard(Board board, boolean isWhiteTurn) {
        int value = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(new Position(row, col));
                if (piece != null) {
                    int pieceValue = getPieceValue(piece);
                    value += piece.isWhite() == isWhiteTurn ? pieceValue : -pieceValue;
                }
            }
        }
        return value;
    }

    private int getPieceValue(Piece piece) {
        if (piece instanceof Pawn)
            return 100;
        if (piece instanceof Knight)
            return 300;
        if (piece instanceof Bishop)
            return 300;
        if (piece instanceof Rook)
            return 500;
        if (piece instanceof Queen)
            return 900;
        if (piece instanceof King)
            return 10000;
        return 0;
    }

    private List<Move> getAllPossibleMoves(Board board, boolean forWhite) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPieceAt(pos);
                if (piece != null && piece.isWhite() == forWhite) {
                    for (Position dest : piece.getPossibleMoves()) {
                        moves.add(new Move(pos, dest, piece, board.getPieceAt(dest)));
                    }
                }
            }
        }
        return moves;
    }
}
