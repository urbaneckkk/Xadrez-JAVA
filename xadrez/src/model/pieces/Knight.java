package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Knight extends Piece {

    public Knight(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public List getPossibleMoves() {
        List moves = new ArrayList<>();
        int[][] jumps = {
                { -2, -1 }, { -2, 1 },
                { -1, -2 }, { -1, 2 },
                { 1, -2 }, { 1, 2 },
                { 2, -1 }, { 2, 1 }
        };

        for (int[] jump : jumps) {
            Position newPos = new Position(
                    position.getRow() + jump[0],
                    position.getColumn() + jump[1]);

            if (newPos.isValid()) {
                Piece pieceAt = board.getPieceAt(newPos);
                if (pieceAt == null || pieceAt.isWhite() != isWhite) {
                    moves.add(newPos);
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return "N";
    }
}