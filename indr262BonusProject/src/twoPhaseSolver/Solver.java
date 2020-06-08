/*
 * author:Metehan Gelgi 64178
 */


package twoPhaseSolver;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Solver extends JPanel {

	// for 2-phase part I used initial Z
	float Z;

	// This constructor use solution frame which comes from main.
	// creates new button in solution Frame to be able to create new LP Problem
	public Solver(float[][] A, float[] b, float[] c, boolean[] artificalneeded, JFrame userFrame, Boolean firstPhase)
			throws Exception {
		// TODO Auto-generated constructor stub
		userFrame.add(this);
		userFrame.setLocationRelativeTo(null);
		userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		userFrame.setSize(450, 475);
		JButton SolveButton = new JButton("Solve new LP");
		ListenSolveAgain SButtonL = new ListenSolveAgain(userFrame);
		SolveButton.addActionListener(SButtonL);

		// if 2-Phase needed go SolveWithPhases method
		// else solve directly than puts solution to the frame
		if (firstPhase) {
			SolveWithPhases(A, b, c, artificalneeded);
		} else {
			RevisedSimplex revisedSol = new RevisedSimplex(A, b, c, 0);
			revisedSol.solve();
			putSolutionOnFrame(revisedSol, A);

		}

		this.add(SolveButton);

	}

	// this method puts solution to the frame
	// It is is for No Need for 2-Phase
	private void putSolutionOnFrame(RevisedSimplex revisedSol, float[][] A) {

		JPanel outputPanel = new JPanel(new GridLayout(2, 1));
		JLabel headLabel = new JLabel("Revised Simplex(No Need for 2-Phase):");
		String[] output1 = new String[A[0].length];
		headLabel.setFont(new Font("Serif", Font.BOLD, 20));// set font
		outputPanel.add(headLabel);

		for (int x = 0; x < A[0].length; x++) {
			boolean check = false;
			for (int y = 0; y < revisedSol.BasicX.length; y++) {
				if (revisedSol.BasicX[y].equals("x" + (x + 1))) {
					output1[x] = revisedSol.BasicX[y] + " = " + revisedSol.xB[y] + " ";
					check = true;
				}
			}
			if (!check) {
				output1[x] = "x" + (x + 1) + " = " + 0 + " ";
			}
		}

		JPanel panel1 = new JPanel(new GridLayout(1, A[0].length + 1));
		for (int x = 0; x < A[0].length; x++) {
			panel1.add(new JLabel(output1[x]));
		}
		panel1.add(new JLabel("Z = " + revisedSol.Z + ""));
		outputPanel.add(panel1);
		this.add(outputPanel);
	}

	private void SolveWithPhases(float[][] A, float[] b, float[] c, boolean[] artificalneeded) throws Exception {

		// Prepare for first phase
		float[] firstPhaseC = new float[A[0].length];
		int NeededForC = A[0].length - c.length;
		Z = 0;

		for (int i = A[0].length - NeededForC; i < A[0].length; i++) {
			firstPhaseC[i] = firstPhaseC[i] - 1;
		}

		for (int i = 0; i < b.length; i++) {
			if (artificalneeded[i]) {
				for (int j = 0; j < A[0].length; j++) {
					firstPhaseC[j] = firstPhaseC[j] + A[i][j];
				}
				Z = Z - b[i];
			}
		}
		// first Phase
		RevisedSimplex phaseOneSol = new RevisedSimplex(A, b, firstPhaseC, Z);
		phaseOneSol.solve();

		// Prepare for second phase
		float[][] newA = new float[b.length][c.length];

		for (int k = 0; k < b.length; k++) {
			for (int i = 0; i < A[0].length; i++) {
				if (i < c.length) {
					float sum = 0;
					for (int j = 0; j < b.length; j++) {

						sum = sum + phaseOneSol.bInverse[k][j] * phaseOneSol.A[j][i];

					}
					newA[k][i] = sum;
				}

			}
		}
		Z = 0;
		float[] newc = updateC(newA, c, phaseOneSol.xB);

		// second Phase
		RevisedSimplex phaseTwoSol = new RevisedSimplex(newA, phaseOneSol.xB, newc, Z);
		phaseTwoSol.solve();

		// puts solutions to the frame
		putSolutionOnFrameForTwoPhases(A, phaseOneSol, newA, phaseTwoSol);

	}

	// for two phase method(phase-2) we have to update C, so I use this one
	private float[] updateC(float[][] newA, float[] c, float[] xB) {
		int BasicIndex = -1;
		for (int j = 0; j < newA[0].length; j++) {
			if (c[j] != 0) {
				for (int i = 0; i < xB.length; i++) {
					if (newA[i][j] == 1) {
						BasicIndex = i;
					} else if (newA[i][j] == 0) {
						continue;
					} else {
						BasicIndex = -1;
						break;
					}
				}
			}
			if (BasicIndex != -1) {
				float coefficient = -c[j];
				for (int i = 0; i < newA[0].length; i++) {
					c[i] = c[i] + coefficient * newA[BasicIndex][i];
				}
				Z -= xB[BasicIndex] * coefficient;
				BasicIndex = -1;
			}
		}
		return c;
	}

	// this method puts solution to the frame
	// It is for Need 2-Phase
	private void putSolutionOnFrameForTwoPhases(float[][] A, RevisedSimplex phaseOneSol, float[][] newA,
			RevisedSimplex phaseTwoSol) {

		// Phase 1
		JPanel outputPanel = new JPanel(new GridLayout(4, 1));
		JLabel headLabel = new JLabel("Revised Simplex Phase 1:");
		String[] output1 = new String[A[0].length];
		headLabel.setFont(new Font("Serif", Font.BOLD, 20));// set font
		outputPanel.add(headLabel);

		for (int x = 0; x < A[0].length; x++) {
			boolean check = false;
			for (int y = 0; y < phaseOneSol.BasicX.length; y++) {
				if (phaseOneSol.BasicX[y].equals("x" + (x + 1))) {
					output1[x] = phaseOneSol.BasicX[y] + " = " + phaseOneSol.xB[y] + " ";
					check = true;
				}
			}
			if (!check) {
				output1[x] = "x" + (x + 1) + " = " + 0 + " ";
			}
		}

		JPanel panel1 = new JPanel(new GridLayout(1, A[0].length + 1));
		for (int x = 0; x < A[0].length; x++) {
			panel1.add(new JLabel(output1[x]));
		}
		panel1.add(new JLabel("Z = " + phaseOneSol.Z + ""));
		outputPanel.add(panel1);

		// Phase 2

		JLabel headLabel2 = new JLabel("Revised Simplex Phase 2:");
		headLabel2.setFont(new Font("Serif", Font.BOLD, 20));// set font
		outputPanel.add(headLabel2);
		String[] output2 = new String[newA[0].length];

		for (int x = 0; x < newA[0].length; x++) {
			boolean check = false;
			for (int y = 0; y < phaseTwoSol.BasicX.length; y++) {
				if (phaseTwoSol.BasicX[y].equals("x" + (x + 1))) {
					output2[x] = phaseTwoSol.BasicX[y] + " = " + phaseTwoSol.xB[y] + " ";
					check = true;
				}
			}
			if (!check) {
				output2[x] = "x" + (x + 1) + " = " + 0 + " ";
			}
		}
		if (Z <= 0) {
			Z = Z * -1;
		}
		JPanel panel2 = new JPanel(new GridLayout(1, newA[0].length + 1));
		for (int x = 0; x < newA[0].length; x++) {
			panel2.add(new JLabel(output2[x]));
		}
		panel2.add(new JLabel("Z = " + phaseTwoSol.Z + ""));
		outputPanel.add(panel2);

		this.add(outputPanel);
	}

	private class ListenSolveAgain implements ActionListener {

		JFrame userFrame = new JFrame();

		public ListenSolveAgain(JFrame userFrame) {
			this.userFrame = userFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			userFrame.getContentPane().removeAll();
			userFrame.repaint();
			userFrame.setVisible(false);
		}

	}

}
