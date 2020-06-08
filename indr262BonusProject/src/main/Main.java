/*
 * author:Metehan Gelgi 64178
 */


package main;

import javax.swing.JFrame;

import userInterface.FirstPanel;

/*
 * This main creates solution Frame, then pass it to the first panel to get objetive and coeeficients
 */

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame UserFrame = new JFrame();
		FirstPanel frame = new FirstPanel(UserFrame);
		frame.setVisible(true);
	}

}
