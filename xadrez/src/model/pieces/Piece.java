package model.pieces;

import java.util.List;
import model.board.Board;
import model.board.Position;

public abstract class Piece {
    protected Position position;
    protected boolean isWhite;
    protected Board board;

    public Piece(Board board, boolean isWhite) {
        this.board = board;
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    // Método abstrato que será implementado por cada tipo de peça
    public abstract List getPossibleMoves();

    // Verifica se a peça pode se mover para a posição especificada
    public boolean canMoveTo(Position position) {
        List possibleMoves = getPossibleMoves();
        return possibleMoves.contains(position);
    }

    // Retorna o nome abreviado da peça (K para rei, Q para rainha, etc.)
    public abstract String getSymbol();
}