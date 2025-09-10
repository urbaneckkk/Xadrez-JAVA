package model.pieces;

import java.util.List;
import model.board.Position;

public abstract class Piece {
    protected Position position;
    protected boolean isWhite;
    protected boolean hasMoved = false; // Adicionado para roque

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
        this.hasMoved = true;
    }
    
    public void setInitialPosition(Position position) {
        this.position = position;
        // Não marca hasMoved = true na inicialização
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public abstract List<Position> getPossibleMoves();

    public boolean canMoveTo(Position position) {
        List<Position> possibleMoves = getPossibleMoves();
        return possibleMoves != null && possibleMoves.contains(position);
    }

    public abstract String getSymbol();
}