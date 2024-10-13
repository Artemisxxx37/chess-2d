package main;

import javax.swing.*;

public class Main {
    public  static  void  main(String [] args){
        JFrame window = new JFrame("Chess-Game v1");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //add gaming panel to window
        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack();

        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gp.launchGame();
    }
}