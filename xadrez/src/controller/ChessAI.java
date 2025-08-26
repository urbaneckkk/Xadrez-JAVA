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
        System.out.println("IA está pensando...");

        // Abordagem simplificada: encontrar qualquer movimento válido
        boolean isWhiteTurn = game.isWhiteTurn();
        Board board = game.getBoard();

        // Lista para armazenar todos os movimentos possíveis
        List<Move> allValidMoves = new ArrayList<>();

        // Percorre o tabuleiro procurando peças da cor atual
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.getPieceAt(from);

                // Se encontrou uma peça da cor atual
                if (piece != null && piece.isWhite() == isWhiteTurn) {
                    // Obtém todos os movimentos possíveis para esta peça
                    List<Position> possibleMoves = piece.getPossibleMoves();
                    if (possibleMoves != null) {
                        for (Position to : possibleMoves) {
                            // Verifica se o movimento não causa xeque no próprio rei
                            if (!game.moveCausesCheck(piece, to)) {
                                allValidMoves.add(new Move(from, to, piece, board.getPieceAt(to)));
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Encontrados " + allValidMoves.size() + " movimentos válidos");

        // Se encontrou algum movimento válido, executa um deles aleatoriamente
        if (!allValidMoves.isEmpty()) {
            // Seleciona um movimento aleatório da lista de movimentos válidos
            Move selectedMove = allValidMoves.get(random.nextInt(allValidMoves.size()));
            System.out.println("Movendo " +
                    selectedMove.getPiece().getSymbol() +
                    " de " + selectedMove.getFrom() +
                    " para " + selectedMove.getTo());

            // Usa movePieceDirect para mover a peça diretamente
            boolean success = game.movePieceDirect(selectedMove.getFrom(), selectedMove.getTo());

            if (success) {
                System.out.println("Movimento executado com sucesso!");
                return;
            } else {
                System.out.println("Falha ao executar movimento!");
                // Se falhar, tenta outro movimento aleatório
                for (int attempt = 1; attempt < 10; attempt++) {
                    // Remove o movimento que falhou da lista
                    allValidMoves.remove(selectedMove);
                    if (allValidMoves.isEmpty())
                        break;

                    // Seleciona outro movimento aleatório
                    selectedMove = allValidMoves.get(random.nextInt(allValidMoves.size()));
                    System.out.println("Tentativa " + (attempt + 1) + ": Movendo " +
                            selectedMove.getPiece().getSymbol() +
                            " de " + selectedMove.getFrom() +
                            " para " + selectedMove.getTo());

                    success = game.movePieceDirect(selectedMove.getFrom(), selectedMove.getTo());

                    if (success) {
                        System.out.println("Movimento executado com sucesso!");
                        return;
                    }
                }
                System.out.println("Todas as tentativas de movimento falharam!");
            }
        } else {
            System.out.println("Nenhum movimento válido encontrado!");
        }
    }

    public Move findBestMove(int depth) {
        Board boardClone = game.getBoard().clone();
        List<Move> possibleMoves = getAllPossibleMoves(boardClone, game.isWhiteTurn());
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        if (possibleMoves.isEmpty()) {
            return null; // Retorna null se não houver movimentos possíveis
        }

        // Se houver apenas um movimento possível, retorna ele imediatamente
        if (possibleMoves.size() == 1) {
            return possibleMoves.get(0);
        }

        for (Move move : possibleMoves) {
            Board testBoard = boardClone.clone();
            makeTestMove(testBoard, move);

            // Avalia o movimento usando o algoritmo minimax
            int moveValue = -minimax(testBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }

        // Se não encontrou um bom movimento, escolhe um aleatório
        if (bestMove == null && !possibleMoves.isEmpty()) {
            bestMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
        }

        return bestMove;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
        // Condição de parada: profundidade zero ou jogo terminado
        if (depth == 0) {
            return evaluateBoard(board, isMaximizing ? game.isWhiteTurn() : !game.isWhiteTurn());
        }

        // Obtém todos os movimentos possíveis para o jogador atual
        List<Move> possibleMoves = getAllPossibleMoves(board, isMaximizing ? game.isWhiteTurn() : !game.isWhiteTurn());

        // Se não há movimentos possíveis, é xeque-mate ou empate
        if (possibleMoves.isEmpty()) {
            // Se for xeque-mate, retorna um valor extremo
            if (isKingInCheck(board, isMaximizing ? game.isWhiteTurn() : !game.isWhiteTurn())) {
                return isMaximizing ? -100000 : 100000; // Valor muito negativo para xeque-mate quando maximizando
            } else {
                return 0; // Empate (stalemate)
            }
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : possibleMoves) {
                Board testBoard = board.clone();
                makeTestMove(testBoard, move);

                int eval = minimax(testBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Poda alfa-beta
                }
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
                if (beta <= alpha) {
                    break; // Poda alfa-beta
                }
            }
            return minEval;
        }
    }

    private void makeTestMove(Board board, Move move) {
        Piece pieceToMove = board.getPieceAt(move.getFrom());
        if (pieceToMove == null) {
            System.out.println("ERRO: Tentando mover uma peça nula de " + move.getFrom());
            return;
        }

        // Verifica se há uma peça para capturar
        Piece capturedPiece = board.getPieceAt(move.getTo());
        if (capturedPiece != null) {
            board.removePiece(move.getTo()); // Remove a peça capturada
        }

        board.removePiece(move.getFrom());
        board.placePiece(pieceToMove, move.getTo());

        // Verifica se o movimento foi bem-sucedido
        if (board.getPieceAt(move.getTo()) == null) {
            System.out.println("ERRO: Falha ao colocar peça em " + move.getTo());
        }
    }

    private boolean isValidMove(Board board, Piece piece, Position destination) {
        // Cria uma cópia do tabuleiro para testar o movimento
        Board tempBoard = board.clone();
        Position originalPos = piece.getPosition();

        // Obtém a peça no tabuleiro temporário
        Piece tempPiece = tempBoard.getPieceAt(originalPos);
        Piece capturedPiece = tempBoard.getPieceAt(destination);

        // Executa o movimento no tabuleiro temporário
        tempBoard.removePiece(originalPos);
        tempBoard.placePiece(tempPiece, destination);

        // Verifica se o rei da mesma cor está em xeque após o movimento
        boolean kingInCheck = isKingInCheck(tempBoard, piece.isWhite());

        return !kingInCheck;
    }

    private boolean isKingInCheck(Board board, boolean isWhiteKing) {
        // Encontra a posição do rei
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.getPieceAt(pos);
                if (p instanceof King && p.isWhite() == isWhiteKing) {
                    kingPos = pos;
                    break;
                }
            }
            if (kingPos != null)
                break;
        }

        // Se não encontrou o rei, algo está errado
        if (kingPos == null)
            return false;

        // Verifica se o rei está sob ataque
        return board.isUnderAttack(kingPos, !isWhiteKing);
    }

    private int evaluateBoard(Board board, boolean isWhiteTurn) {
        int value = 0;

        // Avalia o valor material das peças
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece != null) {
                    int pieceValue = getPieceValue(piece);

                    // Adiciona bônus para peças no centro do tabuleiro
                    if ((row >= 2 && row <= 5) && (col >= 2 && col <= 5)) {
                        pieceValue += 10;
                    }

                    // Adiciona bônus para cavalos e bispos desenvolvidos
                    if ((piece instanceof Knight || piece instanceof Bishop) &&
                            ((piece.isWhite() && row < 6) || (!piece.isWhite() && row > 1))) {
                        pieceValue += 15;
                    }

                    // Adiciona o valor à avaliação total
                    value += piece.isWhite() == isWhiteTurn ? pieceValue : -pieceValue;
                }
            }
        }

        // Verifica se o rei está em xeque
        if (isKingInCheck(board, isWhiteTurn)) {
            value -= 50; // Penalidade por estar em xeque
        }

        // Verifica se o oponente está em xeque
        if (isKingInCheck(board, !isWhiteTurn)) {
            value += 30; // Bônus por colocar o oponente em xeque
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
                    List<Position> possibleMoves = piece.getPossibleMoves();
                    if (possibleMoves != null) {
                        for (Position dest : possibleMoves) {
                            // Verificar se o movimento não causa xeque no próprio rei
                            if (isValidMove(board, piece, dest)) {
                                moves.add(new Move(pos, dest, piece, board.getPieceAt(dest)));
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }
}