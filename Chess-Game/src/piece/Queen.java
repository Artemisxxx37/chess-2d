package piece;

import main.GamePanel;
import main.Type;

public class Queen extends Piece{

    public Queen(int color, int col, int row) {
        super(color, col, row);
        type = Type.QUEEN;
        if (color == GamePanel.WHITE){
            image = getImage("/piece/white-queen");
        }
        else {
            image = getImage("/piece/black-queen");
        }
    }
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinTheBoard(targetCol,targetRow) && !isSameSquare(targetCol,targetRow)){
            //vertical + horizontal movement
            if (targetCol == preCol || targetRow == preRow){
                if(isValidSquare(targetCol,targetRow) && !pieceIsOnStraightLine(targetCol, targetRow)){
                    return true;
                }
            }
            //diagonal moves
            if(Math.abs(targetCol -preCol)== Math.abs(targetRow - preRow)){
                if(isValidSquare(targetCol,targetRow) && pieceIsOnDiagonalLine(targetCol, targetRow)){
                    return  true;
                }
            }

        }
        return false;
    }
}
