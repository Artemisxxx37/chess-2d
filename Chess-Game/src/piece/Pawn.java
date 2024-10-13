package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece{
    public Pawn(int color, int col, int row) {
        super(color, col, row);
        type = Type.PAWN;
        if (color == GamePanel.WHITE){
            image = getImage("/piece/white-pawn");
        }
        else {
            image = getImage("/piece/black-pawn");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinTheBoard(targetCol,targetRow) && !isSameSquare(targetCol, targetRow)){
            //move is conditional by color
            int moveValue;
            if(color == GamePanel.WHITE){
                moveValue = -1;
            }
            else {
                moveValue = 1;
            }
            //checking hitting piece
            hittingP = getHittingP(targetCol,targetRow);
            //one square move
            if(targetCol==preCol && targetRow==preRow+moveValue && hittingP==null ){
                return  true;
            }
            //2 square movement
            if(targetCol==preCol && targetRow==preRow+moveValue*2 && hittingP==null && !moved && !pieceIsOnStraightLine(targetCol, targetRow)){
                return true;
            }
            //passant and diagonal capture
            if(Math.abs(targetCol-preCol)==1 && targetRow==preRow + moveValue && hittingP !=null && hittingP.color !=color){
                return  true;
            }
            if(Math.abs(targetCol-preCol)==1 && targetRow==preRow + moveValue){
                for(Piece piece: GamePanel.simPieces){
                    if(piece.col == targetCol && piece.row == preRow && piece.twoStepped){
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
