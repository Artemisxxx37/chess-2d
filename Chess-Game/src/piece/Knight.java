package piece;

import main.GamePanel;
import main.Main;
import main.Type;

public class Knight extends Piece{
    public Knight(int color, int col, int row) {
        super(color, col, row);
        type = Type.KNIGHT;
        if (color== GamePanel.WHITE){
            image = getImage("/piece/white-knight");
        }
        else {
            image = getImage("/piece/black-knight");
        }
    }
    public  boolean canMove(int targetCol, int targetRow){
        if(isWithinTheBoard(targetCol, targetRow)){
            //le fou 3e carré , coord : (1;2)or(2;1)
            if (Math.abs(targetCol -preCol) * Math.abs(targetRow -preRow) == 2){
                if (isValidSquare(targetCol, targetRow)){
                    return  true;
                }
            }
        }
        return  false;
    }
}
