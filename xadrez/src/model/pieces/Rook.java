package model.pieces;

import model.board.Board;
import model.board.Position;
import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public List getPossibleMoves() {
        List moves = new ArrayList<>();

        // Direções: cima, direita, baixo, esquerda
        int[][] directions = { { -1, 0 }, { 0, 1 }, { 1, 0 }, { 0, -1 } };

        for (int[] direction : directions) {
            int row = position.getRow();
            int col = position.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];
                Position newPos = new Position(row, col);

                if (!newPos.isValid())
                    break;

                Piece pieceAtPosition = board.getPieceAt(newPos);
                if (pieceAtPosition == null) {
                    // Casa vazia, movimento válido
                    moves.add(newPos);
                } else if (pieceAtPosition.isWhite() != isWhite) {
                    // Casa com peça adversária, movimento válido (captura)
                    moves.add(newPos);
                    break;
                } else {
                    // Casa com peça da mesma cor, movimento inválido
                    break;
                }
            }
        }

        return moves;
    }

    @Override
    public String getSymbol() {
        return "R";
    }

    public boolean hasMoved() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasMoved'");
    }
}