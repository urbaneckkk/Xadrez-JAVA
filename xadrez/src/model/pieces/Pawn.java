package model.pieces;

import java.util.ArrayList;
import java.util.List;
import model.board.Board;
import model.board.Position;

public class Pawn extends Piece {

    public Pawn(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public List getPossibleMoves() {
        List moves = new ArrayList<>();
        int direction = isWhite ? -1 : 1;

        // Movimento para frente
        Position front = new Position(
                position.getRow() + direction,
                position.getColumn());

        if (front.isValid() && board.isPositionEmpty(front)) {
            moves.add(front);

            // Movimento duplo na primeira jogada
            if ((isWhite && position.getRow() == 6) ||
                    (!isWhite && position.getRow() == 1)) {

                Position doubleFront = new Position(
                        position.getRow() + 2 * direction,
                        position.getColumn());

                if (board.isPositionEmpty(doubleFront)) {
                    moves.add(doubleFront);
                }
            }
        }

        Object lastPawnDoubleMove = false;
        // Verificar capturas en passant
        if (lastPawnDoubleMove != null &&
                ((Position) lastPawnDoubleMove).getRow() == position.getRow()) {

            if (Math.abs(((Position) lastPawnDoubleMove).getColumn() -
                    position.getColumn()) == 1) {

                Position enPassantPos = new Position(
                        position.getRow() + direction,
                        ((Position) lastPawnDoubleMove).getColumn());

                moves.add(enPassantPos);
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return "P";
    }
}