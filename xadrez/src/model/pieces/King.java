package model.pieces;

import model.board.Board;
import model.board.Position;
import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        int[][] dirs = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };
        for (int[] d : dirs) {
            Position np = new Position(position.getRow() + d[0], position.getColumn() + d[1]);
            if (np.isValid()) {
                Piece at = board.getPieceAt(np);
                if (at == null || at.isWhite() != isWhite)
                    moves.add(np);
            }
        }
        return moves;
    }

    @Override
    public String getSymbol() {
        return "K";
    }
}