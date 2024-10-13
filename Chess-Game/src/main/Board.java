package main;

import java.awt.*;

public class Board {
    final int MAX_COL = 8;
    final  int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 80;
    public  static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;

    public  void draw(Graphics2D g2){
        int d= 0;
        for(int row=0; row<MAX_ROW; row++){
            for(int col=0; col<MAX_COL; col++){
                if(d==0){
                    g2.setColor(new Color(118,150,86));
                    d = 1;
                }
                else {
                    g2.setColor(new Color(238,238,210));
                    d = 0;
                }
                g2.fillRect(col*SQUARE_SIZE , row*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
            if(d==0){
                d=1;
            }
            else {
                d=0;
            }

        }

    }
}
