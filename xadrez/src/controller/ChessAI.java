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
        
        // IA melhorada com avaliação básica
        makeSmartMove();
    }
    
    private void makeSmartMove() {
        boolean isWhiteTurn = game.isWhiteTurn();
        Board board = game.getBoard();
        List<Move> allValidMoves = getAllValidMoves(board, isWhiteTurn);

        if (allValidMoves.isEmpty()) {
            System.out.println("Nenhum movimento disponível para a IA!");
            return;
        }

        Move bestMove = evaluateBestMove(allValidMoves, board, isWhiteTurn);
        
        System.out.println("IA escolheu: " + bestMove.getPiece().getSymbol() + 
                         " de " + bestMove.getFrom() + " para " + bestMove.getTo());
        game.movePieceDirect(bestMove.getFrom(), bestMove.getTo());
    }
    
    private List<Move> getAllValidMoves(Board board, boolean isWhiteTurn) {
        List<Move> allValidMoves = new ArrayList<>();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.getPieceAt(from);

                if (piece != null && piece.isWhite() == isWhiteTurn) {
                    List<Position> possibleMoves = piece.getPossibleMoves();
                    if (possibleMoves != null) {
                        for (Position to : possibleMoves) {
                            Move move = new Move(from, to, piece, board.getPieceAt(to));
                            // Validação básica sem recursão complexa
                            if (isBasicValidMove(board, move)) {
                                allValidMoves.add(move);
                            }
                        }
                    }
                }
            }
        }
        return allValidMoves;
    }
    
    private boolean isBasicValidMove(Board board, Move move) {
        Position to = move.getTo();
        
        // Verificação básica de limites do tabuleiro
        if (to.getRow() < 0 || to.getRow() >= 8 || 
            to.getColumn() < 0 || to.getColumn() >= 8) {
            return false;
        }
        
        // Verificação se o movimento é válido para a peça
        Piece piece = move.getPiece();
        if (piece == null || !piece.canMoveTo(to)) {
            return false;
        }
        
        // Verificação simples se não deixa o próprio rei em xeque
        return !game.moveCausesCheck(piece, to);
    }
    
    private Move evaluateBestMove(List<Move> moves, Board board, boolean isWhiteTurn) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Move move : moves) {
            int score = evaluateMoveScore(move, board, isWhiteTurn);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        // Se não encontrou um bom movimento, escolhe aleatório
        return bestMove != null ? bestMove : moves.get(random.nextInt(moves.size()));
    }
    
    private int evaluateMoveScore(Move move, Board board, boolean isWhiteTurn) {
        int score = 0;
        
        // 1. Prioriza capturas valiosas
        if (move.getCapturedPiece() != null) {
            score += getPieceValue(move.getCapturedPiece()) * 10;
        }
        
        // 2. Bonifica controle do centro
        Position to = move.getTo();
        if (isCenterPosition(to)) {
            score += 30;
        }
        
        // 3. Bonifica desenvolvimento de peças
        if (isDevelopmentMove(move, board)) {
            score += 20;
        }
        
        // 4. Penaliza movimentos que expõem peças valiosas
        if (isExposingValuablePiece(move, board)) {
            score -= getPieceValue(move.getPiece()) * 5;
        }
        
        return score;
    }
    
    private boolean isCenterPosition(Position pos) {
        int row = pos.getRow();
        int col = pos.getColumn();
        return (row >= 3 && row <= 4) && (col >= 3 && col <= 4);
    }
    
    private boolean isDevelopmentMove(Move move, Board board) {
        Piece piece = move.getPiece();
        Position from = move.getFrom();
        
        // Cavalos e bispos saindo das posições iniciais
        if (piece instanceof Knight || piece instanceof Bishop) {
            int backRank = piece.isWhite() ? 7 : 0;
            return from.getRow() == backRank;
        }
        
        return false;
    }
    
    private boolean isExposingValuablePiece(Move move, Board board) {
        // Verificação simples se a peça fica em posição vulnerável
        Position to = move.getTo();
        Piece piece = move.getPiece();
        
        // Rainha ou rei em posições perigosas
        if (piece instanceof Queen || piece instanceof King) {
            return isNearEnemyPieces(to, board, !piece.isWhite());
        }
        
        return false;
    }
    
    private boolean isNearEnemyPieces(Position pos, Board board, boolean enemyColor) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                
                int newRow = pos.getRow() + dr;
                int newCol = pos.getColumn() + dc;
                
                if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                    Piece piece = board.getPieceAt(new Position(newRow, newCol));
                    if (piece != null && piece.isWhite() == enemyColor) {
                        return true;
                    }
                }
            }
        }
        return false;
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

        // 1. Avaliação material e posicional das peças
        value += evaluateMaterialAndPosition(board, isWhiteTurn);
        
        // 2. Avaliação de mobilidade (número de movimentos possíveis)
        value += evaluateMobility(board, isWhiteTurn);
        
        // 3. Avaliação de segurança do rei
        value += evaluateKingSafety(board, isWhiteTurn);
        
        // 4. Avaliação de controle do centro
        value += evaluateCenterControl(board, isWhiteTurn);
        
        // 5. Avaliação de ameaças e capturas
        value += evaluateThreats(board, isWhiteTurn);
        
        // 6. Bônus por xeque e xeque-mate
        if (isKingInCheck(board, !isWhiteTurn)) {
            value += 50; // Bônus por dar xeque
            // Verifica se é xeque-mate
            List<Move> opponentMoves = getAllPossibleMoves(board, !isWhiteTurn);
            if (opponentMoves.isEmpty()) {
                value += 10000; // Bônus massivo por xeque-mate
            }
        }
        
        // 7. Penalidade por estar em xeque
        if (isKingInCheck(board, isWhiteTurn)) {
            value -= 60;
        }

        return value;
    }
    
    private int evaluateMaterialAndPosition(Board board, boolean isWhiteTurn) {
        int value = 0;
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece != null) {
                    int pieceValue = getPieceValue(piece);
                    
                    // Bônus posicional específico por tipo de peça
                    pieceValue += getPositionalBonus(piece, row, col, board);
                    
                    // Adiciona ou subtrai baseado na cor da peça
                    value += piece.isWhite() == isWhiteTurn ? pieceValue : -pieceValue;
                }
            }
        }
        
        return value;
    }
    
    private int getPositionalBonus(Piece piece, int row, int col, Board board) {
        int bonus = 0;
        
        // Bônus para peças no centro (mais forte no centro real)
        if (row >= 3 && row <= 4 && col >= 3 && col <= 4) {
            bonus += 20; // Centro forte
        } else if (row >= 2 && row <= 5 && col >= 2 && col <= 5) {
            bonus += 10; // Centro expandido
        }
        
        if (piece instanceof Pawn) {
            // Peões avançados são valiosos
            if (piece.isWhite()) {
                bonus += (7 - row) * 5; // Peões brancos avançando
            } else {
                bonus += row * 5; // Peões pretos avançando
            }
        } else if (piece instanceof Knight) {
            // Cavalos são melhores no centro
            bonus += 15;
            // Penalidade para cavalos na borda
            if (row == 0 || row == 7 || col == 0 || col == 7) {
                bonus -= 20;
            }
        } else if (piece instanceof Bishop) {
            // Bispos desenvolvidos
            if ((piece.isWhite() && row < 6) || (!piece.isWhite() && row > 1)) {
                bonus += 15;
            }
        } else if (piece instanceof Rook) {
            // Torres em colunas abertas ou semi-abertas
            boolean hasOwnPawn = false;
            for (int r = 0; r < 8; r++) {
                Piece p = board.getPieceAt(new Position(r, col));
                if (p instanceof Pawn && p.isWhite() == piece.isWhite()) {
                    hasOwnPawn = true;
                    break;
                }
            }
            if (!hasOwnPawn) {
                bonus += 25; // Torre em coluna aberta/semi-aberta
            }
        }
        
        return bonus;
    }
    
    private int evaluateMobility(Board board, boolean isWhiteTurn) {
        // Simplificado para evitar recursão - apenas conta peças ativas
        int myPieces = 0;
        int opponentPieces = 0;
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPieceAt(new Position(r, c));
                if (piece != null) {
                    if (piece.isWhite() == isWhiteTurn) {
                        myPieces++;
                    } else {
                        opponentPieces++;
                    }
                }
            }
        }
        
        return (myPieces - opponentPieces) * 5;
    }
    
    private int evaluateKingSafety(Board board, boolean isWhiteTurn) {
        int safety = 0;
        
        // Encontra o rei
        Position kingPos = findKing(board, isWhiteTurn);
        if (kingPos == null) return -10000; // Rei não encontrado = problema grave
        
        // Conta peças inimigas próximas ao rei (avaliação simplificada)
        int nearbyEnemies = 0;
        for (int row = Math.max(0, kingPos.getRow() - 2); row <= Math.min(7, kingPos.getRow() + 2); row++) {
            for (int col = Math.max(0, kingPos.getColumn() - 2); col <= Math.min(7, kingPos.getColumn() + 2); col++) {
                Piece piece = board.getPieceAt(new Position(row, col));
                if (piece != null && piece.isWhite() != isWhiteTurn) {
                    nearbyEnemies++;
                }
            }
        }
        
        safety -= nearbyEnemies * 10; // Penalidade por inimigos próximos
        
        return safety;
    }
    
    private int evaluateCenterControl(Board board, boolean isWhiteTurn) {
        int control = 0;
        Position[] centerSquares = {
            new Position(3, 3), new Position(3, 4),
            new Position(4, 3), new Position(4, 4)
        };
        
        // Avaliação simplificada: conta peças no centro e próximas ao centro
        for (Position center : centerSquares) {
            Piece centerPiece = board.getPieceAt(center);
            if (centerPiece != null) {
                if (centerPiece.isWhite() == isWhiteTurn) {
                    control += 20; // Bônus por ocupar o centro
                } else {
                    control -= 20; // Penalidade se oponente ocupa o centro
                }
            }
        }
        
        // Conta peças próximas ao centro
        for (int row = 2; row <= 5; row++) {
            for (int col = 2; col <= 5; col++) {
                Piece piece = board.getPieceAt(new Position(row, col));
                if (piece != null) {
                    if (piece.isWhite() == isWhiteTurn) {
                        control += 5; // Pequeno bônus por estar próximo ao centro
                    } else {
                        control -= 5;
                    }
                }
            }
        }
        
        return control;
    }
    
    private int evaluateThreats(Board board, boolean isWhiteTurn) {
        // Simplificado para evitar recursão - avalia peças em posições vulneráveis
        int threats = 0;
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPieceAt(pos);
                if (piece != null && piece.isWhite() != isWhiteTurn) {
                    // Peça inimiga - verifica se está em posição vulnerável
                    if (isPositionVulnerable(board, pos)) {
                        threats += getPieceValue(piece) / 4;
                    }
                }
            }
        }
        
        return threats;
    }
    
    private boolean isPositionVulnerable(Board board, Position pos) {
        // Verifica se a posição está próxima de peças inimigas
        Piece piece = board.getPieceAt(pos);
        if (piece == null) return false;
        
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                
                int newRow = pos.getRow() + dr;
                int newCol = pos.getColumn() + dc;
                
                if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                    Piece neighbor = board.getPieceAt(new Position(newRow, newCol));
                    if (neighbor != null && neighbor.isWhite() != piece.isWhite()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private Position findKing(Board board, boolean isWhite) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece instanceof King && piece.isWhite() == isWhite) {
                    return pos;
                }
            }
        }
        return null;
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