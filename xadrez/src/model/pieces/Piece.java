package model.pieces;

import java.util.List;
import model.board.Position;

public abstract class Piece {
    protected Position position;
    protected boolean isWhite;

    // Dê acesso ao tabuleiro somente pelas subclasses
    protected model.board.Board board;

    public Piece(model.board.Board board, boolean isWhite) {
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

    // Agora tipado corretamente
    public abstract List<Position> getPossibleMoves();

    public boolean canMoveTo(Position position) {
        List<Position> possibleMoves = getPossibleMoves();
        return possibleMoves != null && possibleMoves.contains(position);
    }

    public abstract String getSymbol();
}
