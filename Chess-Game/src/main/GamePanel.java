package main;

import piece.*;
import java.io.ObjectInputStream;
import  java.io.ObjectOutputStream;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
public class GamePanel extends JPanel implements Runnable {
    public static  final  int WIDTH = 800;
    public  static final  int HEIGHT = 640;
    final int FPS = 60;
    Thread GameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    public static ArrayList<Piece> pieces = new ArrayList<>();
    public  static  ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> PromoPieces = new ArrayList<>();
    Piece activeP, checkingP;
    public static Piece castlingP;

    public static final  int WHITE = 0;
    public  static final  int BLACK = 1;
    int  currentColor = WHITE;
    //booleans values
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameover;
    boolean statlemate;
  

    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        setPieces();
        copyPieces(pieces, simPieces);
    }
    public  void  launchGame(){
        GameThread = new Thread(this);
        GameThread.start();
    }
    public void  setPieces(){
        //white team
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        //black team
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }
    private  void  copyPieces(ArrayList<Piece> source, ArrayList<Piece> target){
        target.clear();
        for(int i=0; i<source.size(); i++){
            target.add(source.get(i));
        }
    }
    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        while (GameThread !=null){
           currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >=1){
                update();
                repaint();
                delta --;
            }
        }
    }
    private void  update(){
        if(promotion){
            promoting();
        }
        else if(!gameover && !statlemate) {
            //checking if player click on board
            if (mouse.pressed){
                //check it out if  some piece is holding
                if (activeP == null){
                    for (Piece piece: simPieces){
                        if (piece.color == currentColor &&
                                piece.col == mouse.x/Board.SQUARE_SIZE &&
                                piece.row == mouse.y/Board.SQUARE_SIZE){
                            activeP = piece;
                        }
                    }
                }
                else {
                    simulate();
                }
            }
            //we released the throwing button here
            if(!mouse.pressed){
                if (activeP !=null){
                    //check th movemen't validity
                    if (validSquare){
                        //then confirm the move
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();
                        if (castlingP !=null) {
                            castlingP.updatePosition();
                        }

                        if(isKingInCheck() && isCheckmate()){
                            gameover = true;
                        } else if (isStatlemate()) {
                            statlemate = true;
                            
                        } else {
                            if (canPromote()) {
                                promotion = true;
                            } else {
                                changePlayer();
                            }
                        }
                    }
                    else {
                        //then movement not accomplished , resetting
                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }

    }



    private void simulate() {
        copyPieces(pieces, simPieces);
        if(castlingP != null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP =null;

        }
        canMove = false;
        validSquare  =false;
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);
        if(activeP.canMove(activeP.col, activeP.row)){
            canMove =true;
            if (activeP.hittingP !=null){
                simPieces.remove(activeP.hittingP.getIndex());
            }
            checkCastling();
            if(!isIllegal(activeP) && !opponentCanCaptureKing()) {
                validSquare = true;
            }
            }
    }
    private boolean isIllegal(Piece King) {
        if (King.type == Type.KING ) {
            for (Piece piece : simPieces) {
                if (piece != King && piece.color != King.color && piece.canMove(King.col, King.row)) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean opponentCanCaptureKing(){
        Piece King = getKing(false);
        for(Piece piece: simPieces){
            if(piece.color != King.color && piece.canMove(King.col, King.row)){
                return  true;
            }
        }
        return  false;
    }
    private boolean isKingInCheck() {
        Piece King = getKing(true);
        if(activeP.canMove(King.col, King.row)){
            checkingP = activeP;
            return true;
        }
        else {
            checkingP = null;
        }
        return false;
    }
    private Piece getKing(boolean opponent){
        Piece King = null;
        for(Piece piece: simPieces){
            if(opponent){
                if(piece.type == Type.KING && piece.color != currentColor){
                    King = piece;
                }
            }
            else {
                if(piece.type == Type.KING && piece.color==currentColor){
                    King = piece;
                }
            }
        }
        return  King;
    }
    private boolean isCheckmate(){
        Piece King = getKing(true);
        if(kingCanMove(King)){
            return false;
        }
        else {
            int colDiff = Math.abs(checkingP.col - King.col);
            int rowDiff = Math.abs(checkingP.row - King.row);
            if(colDiff ==0){
                if(checkingP.row < King.row){
                    for(int row = checkingP.row; row<King.row; row++){
                        for (Piece piece: simPieces){
                            if(piece !=King && piece.color !=currentColor && piece.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.row > King.row){
                    for(int row = checkingP.row; row>King.row; row--){
                        for (Piece piece: simPieces){
                            if(piece !=King && piece.color !=currentColor && piece.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }
            } else if (rowDiff ==0) {
                if(checkingP.col < King.col){
                    for(int col = checkingP.col; col<King.col; col++){
                        for (Piece piece: simPieces){
                            if(piece !=King && piece.color !=currentColor && piece.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.col > King.col){
                    for(int col = checkingP.col; col>King.col; col--){
                        for (Piece piece: simPieces){
                            if(piece !=King && piece.color !=currentColor && piece.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
                
            } else if (colDiff==rowDiff) {
                if(checkingP.row < King.row){
                    if(checkingP.col < King.col){
                        for(int col = checkingP.col, row = checkingP.row;col < King.col; col++,row++){
                            for (Piece piece: simPieces){
                                if(piece !=King && piece.color !=currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > King.col){
                        for(int col = checkingP.col, row = checkingP.row; col > King.col ; col--, row++){
                            for (Piece piece: simPieces){
                                if(piece !=King && piece.color !=currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingP.row > King.row){
                    if(checkingP.col < King.col){
                        for(int col = checkingP.col, row = checkingP.row; col < King.col ; col++, row--){
                            for (Piece piece: simPieces){
                                if(piece !=King && piece.color !=currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > King.col){
                        for(int col = checkingP.col, row = checkingP.row; col > King.col ; col--, row--){
                            for (Piece piece: simPieces){
                                if(piece !=King && piece.color !=currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            else {

            }
        }
        return true;
    }
    private boolean kingCanMove(Piece King){
        if(isValidMove(King, -1, -1)) {return true;}
        if(isValidMove(King, 0, -1)) {return true;}
        if(isValidMove(King, 1, -1)) {return true;}
        if(isValidMove(King, -1, 0)) {return true;}
        if(isValidMove(King, 1, 0)) {return true;}
        if(isValidMove(King, -1, 1)) {return true;}
        if(isValidMove(King, 0, 1)) {return true;}
        if(isValidMove(King, 1, 1)) {return true;}
        return false;
    }
    private boolean isValidMove(Piece King, int colPlus, int rowPlus){
        boolean isValidMove = false;
        King.col += colPlus;
        King.row +=rowPlus;

        if(King.canMove(King.col, King.row)){
            if(King.hittingP != null){
                simPieces.remove(King.hittingP.getIndex());
            }
            if(!isIllegal(King)){
                isValidMove = true;
            }
        }
        King.resetPosition();
        copyPieces(pieces, simPieces);
        return isValidMove;
    }

    private boolean isStatlemate(){
        int count = 0;
        for (Piece piece: simPieces){
            if(piece.color !=currentColor){
                count++;
            }
        }
        if(count==1){
            if(kingCanMove(getKing(true))==false){
                return  true;
            }
        }
        return false;
    }
    private void checkCastling(){
        if(castlingP!=null){
            if (castlingP.col == 0){
                castlingP.col +=3;
            } else if (castlingP.col ==7) {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private void changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK;
            //reset black's twostepped
            for (Piece piece : pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        } else {
            currentColor = WHITE;
            //reset white's twostepped
            for (Piece piece : pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }
        activeP = null;
    }
    private  boolean canPromote(){
        if(activeP.type == Type.PAWN){
            if(currentColor==WHITE && activeP.row ==0 || currentColor==BLACK && activeP.row == 7){
                PromoPieces.clear();
                PromoPieces.add(new Rook(currentColor, 9, 2));
                PromoPieces.add(new Queen(currentColor, 9, 3));
                PromoPieces.add(new Knight(currentColor, 9, 4));
                PromoPieces.add(new Bishop(currentColor, 9, 5));
                return true;
            }
        }
        return false;
    }
    private void promoting(){
        if(mouse.pressed){
            for (Piece piece: PromoPieces){
                if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch (piece.type){
                        case ROOK : simPieces.add(new Rook(currentColor, activeP.col, activeP.row));break;
                        case KNIGHT : simPieces.add(new Knight(currentColor, activeP.col, activeP.row));break;
                        case QUEEN : simPieces.add(new Queen(currentColor, activeP.col, activeP.row));break;
                        case BISHOP : simPieces.add(new Bishop(currentColor, activeP.col, activeP.row));break;
                        default: break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    public void paintComponent(Graphics g){

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        board.draw(g2);
        for(Piece p : simPieces){
            p.draw(g2);
        }
        if (activeP != null){
           if (canMove){
               if(isIllegal(activeP) && opponentCanCaptureKing()){
                   g2.setColor(Color.gray);
                   g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                   g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                   g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
               }
               else {
                   g2.setColor(Color.gray);
                   g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                   g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                   g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
               }
           }
            //here drawing the piece
            activeP.draw(g2);
        }
        //Rendable text to view player's turn
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("hack bold", Font.PLAIN, 18));
        g2.setColor(Color.white);
        if(promotion){
            g2.drawString(" Promotion:", 650, 150);
            for(Piece piece: PromoPieces){
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE , null);
            }
        }
        else {
            if(currentColor==WHITE){
                g2.drawString("TOUR : BLANC", 650, 540);
                if(checkingP !=null && checkingP.color == BLACK){
                    g2.setColor(Color.red);
                    g2.drawString("LE ROI", 650, 200);
                    g2.drawString("est en ÉCHEC", 650,220);
                }
            }
            else {
                g2.drawString("TOUR : NOIR", 650, 150);
                if(checkingP !=null && checkingP.color == WHITE){
                    g2.setColor(Color.red);
                    g2.drawString(" Le ROI", 650, 200);
                    g2.drawString("est en ÉCHEC", 650,250);
                }
            }
        }
        if(gameover){
            String s = "";
            if(currentColor==WHITE){
                s = "VICTOIRE , LES BLANCS ONT MATÉ !";
            }
            else {
                s = "VICTOIRE, LES NOIRS ONT MATÉ !";
            }
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.green);
            g2.drawString(s, 200, 420);
        }
        if(statlemate){
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("Egalité", 200, 420);
        }
    }


}
