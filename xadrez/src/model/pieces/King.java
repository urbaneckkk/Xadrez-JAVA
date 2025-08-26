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

        // Lógica do Roque
        if (!this.hasMoved) {
            // Roque do lado do rei (king side)
            Position rookKingSidePos = new Position(position.getRow(), 7);
            Piece rookKingSide = board.getPieceAt(rookKingSidePos);
            if (rookKingSide instanceof Rook && !rookKingSide.hasMoved) {
                if (board.getPieceAt(new Position(position.getRow(), 5)) == null &&
                        board.getPieceAt(new Position(position.getRow(), 6)) == null) {
                    moves.add(new Position(position.getRow(), 6));
                }
            }

            // Roque do lado da rainha (queen side)
            Position rookQueenSidePos = new Position(position.getRow(), 0);
            Piece rookQueenSide = board.getPieceAt(rookQueenSidePos);
            if (rookQueenSide instanceof Rook && !rookQueenSide.hasMoved) {
                if (board.getPieceAt(new Position(position.getRow(), 1)) == null &&
                        board.getPieceAt(new Position(position.getRow(), 2)) == null &&
                        board.getPieceAt(new Position(position.getRow(), 3)) == null) {
                    moves.add(new Position(position.getRow(), 2));
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return "K";
    }
}