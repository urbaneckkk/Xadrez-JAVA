package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Rook extends Piece {

    public Rook(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
public List<Position> getPossibleMoves() {
    List<Position> moves = new ArrayList<>();
    int[][] dirs = {{-1,0},{0,1},{1,0},{0,-1}};
    for (int[] d : dirs) {
        int r = position.getRow(), c = position.getColumn();
        while (true) {
            r += d[0]; c += d[1];
            Position np = new Position(r, c);
            if (!np.isValid()) break;
            Piece at = board.getPieceAt(np);
            if (at == null) moves.add(np);
            else { if (at.isWhite() != isWhite) moves.add(np); break; }
        }
    }
    return moves;
}

// Remova este método que lança exceção ou implemente depois.
// boolean hasMoved() { return ... }
// ==> por enquanto REMOVA e também REMOVA qualquer uso de hasMoved no King.


    @Override
    public String getSymbol() {
        return "R";
    }

    boolean hasMoved() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}