/*
 * author:Metehan Gelgi 64178
 */


package userInterface;

import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import twoPhaseSolver.Solver;

/*
 * This is the first Screen you are going to see. 
 * First create simple IU to get # of variables and # of constraints. 
 * Then generate button creates fields for input. 
 * Solve button get numbers form from fields then pass it to the Revised Simplex class(before gets appropriate form for Algorithm)
 */

@SuppressWarnings("serial")
public class FirstPanel extends JFrame {

	private JFrame userFrame;
	private JPanel panel;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton SolveButton;

	private JTextField numVar;
	private JTextField numCons;

	private JTextField[][] consValues;
	private JTextField[] objValues;
	private JTextField[] RHSValues;
	private Choice[] compares;
	private JComboBox<String> MinMax;

	private static int NumberOfVar;
	private static int NumberOfCons;
	private float[][] A;
	private float[] b;
	private float[] c;
	private boolean Max;
	private boolean[] Artificalneeded;
	private String[] direction;
	Boolean FirstPhase;

	public FirstPanel(JFrame userFrame) {
		super("2-Phase Revised Simplex Solver");

		this.userFrame = userFrame;
		panel = new JPanel(new GridLayout(2, 1));// it is a general panel to group items.

		buttonPanel = new JPanel(); // it is button panel for generate and solve buttons
		buttonPanel.setLayout(new FlowLayout());
		setLayout(new FlowLayout()); // set frame layout
		this.setSize(600, 300); // first create small UI to get numbers
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		NumVarConst();

		JButton startButton = new JButton("Generate");
		ListenForButton ButonL = new ListenForButton();
		startButton.addActionListener(ButonL);
		buttonPanel.add(startButton);

		this.add(panel);
		this.add(buttonPanel);

	}

	// crreates fields for #Var and #Cons
	private void NumVarConst() {
		// TODO Auto-generated method stub

		JLabel var = new JLabel("#Variable");
		JLabel cons = new JLabel("#Constraints");

		numVar = new JTextField("", 5);
		numCons = new JTextField("", 5);

		JPanel panelNumbers = new JPanel(new GridLayout(1, 4));

		panelNumbers.add(var);
		panelNumbers.add(numVar);
		numVar.addActionListener(new ListenForButton());
		panelNumbers.add(cons);
		panelNumbers.add(numCons);
		numCons.addActionListener(new ListenForButton());

		panelNumbers.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Number of Variable and Constraints"));

		panel.add(panelNumbers);

	}

	// when button clicked sets Frame invisible
	// then print out fields for LP problem.
	public void GenerateFunction(int var, int cons) {
		this.setVisible(false);

		if (dataPanel != null) {
			this.getContentPane().remove(dataPanel);
			this.repaint();
			dataPanel.removeAll();
			dataPanel.revalidate();
			dataPanel.repaint();
			buttonPanel.remove(SolveButton);
			buttonPanel.repaint();
		}

		String[] MaxList = { "Max", "Min" };
		MinMax = new JComboBox<String>(MaxList);
		MinMax.addItemListener(new ListenSolver());
		MinMax.setSelectedItem("Max");
		objValues = new JTextField[var];
		consValues = new JTextField[cons][var];
		RHSValues = new JTextField[cons];
		compares = new Choice[cons];

		for (int i = 0; i < cons; i++) {
			compares[i] = new Choice();
			compares[i].add(">=");
			compares[i].add("<=");
			compares[i].add("=");
			compares[i].addItemListener(new ListenSolver());
			RHSValues[i] = new JTextField("", 3);
		}

		dataPanel = new JPanel();
		dataPanel.setLayout(new GridLayout(2, 1));

		JPanel ObjPanel = new JPanel(new GridLayout(1, 2 * var + 1));
		ObjPanel.add(MinMax);

		for (int i = 0; i < var; i++) {
			objValues[i] = new JTextField("", 3);
			ObjPanel.add(objValues[i]);
			ObjPanel.add(new JLabel("x" + (i + 1)));
		}

		JPanel ConsPanel = new JPanel(new GridLayout(cons, 2 * var + 2));

		for (int i = 0; i < cons; i++) {
			for (int j = 0; j < var + 2; j++) {

				if (j == var) {
					ConsPanel.add(compares[i]);
					continue;
				}

				if (j == (var + 1)) {
					ConsPanel.add(RHSValues[i]);
					continue;
				}

				consValues[i][j] = new JTextField("", 3);
				ConsPanel.add(consValues[i][j]);
				ConsPanel.add(new JLabel("x" + (j + 1)));
			}
		}

		dataPanel.add(ObjPanel);
		dataPanel.add(ConsPanel);
		dataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Objective Function and Constraints(Greater and Less than signs are not default selected)"));

		this.setSize(900, 750);
		this.remove(buttonPanel);

		SolveButton = new JButton("Solve");
		ListenSolver SButtonL = new ListenSolver();
		SolveButton.addActionListener(SButtonL);
		buttonPanel.add(SolveButton);

		this.add(buttonPanel);
		this.add(dataPanel);
		this.setVisible(true);

	}

	// This is generate Button Listener
	private class ListenForButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				NumberOfCons = Integer.parseInt(numCons.getText());
				NumberOfVar = Integer.parseInt(numVar.getText());

				// listen for input for less than zero
				if (NumberOfCons <= 0 | NumberOfVar <= 0)
					throw new NumberFormatException();

				GenerateFunction(NumberOfVar, NumberOfCons);

			} catch (NumberFormatException excep) {// if number is less than zero gives error page
				JOptionPane.showMessageDialog(FirstPanel.this, "Please enter a bigger valid number than zero!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}

		}

	}

	/*
	 * This is solver button listener Get numbers from listeners create A,b,c for
	 * Revised Simplex class
	 **/
	private class ListenSolver implements ActionListener, ItemListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {

				int NumBasis = 0;
				int NumBasisForC = 0;

				for (String is : direction) {
					if (is.equals("<=")) {
						NumBasis++;
						NumBasisForC++;
					} else if (is.equals("=")) {
						NumBasis++;
					} else {
						NumBasis = NumBasis + 2;
						NumBasisForC++;
					}
				}

				A = new float[NumberOfCons][NumberOfVar + NumBasis];
				b = new float[NumberOfCons];
				c = new float[NumberOfVar + NumBasisForC];
				Artificalneeded = new boolean[NumberOfCons];
				FirstPhase = false;

				for (int i = 0; i < NumberOfCons; i++) {
					for (int j = 0; j < NumberOfVar; j++) {
						c[j] = Float.parseFloat(objValues[j].getText());
						A[i][j] = Float.parseFloat(consValues[i][j].getText());

					}

					b[i] = Float.parseFloat(RHSValues[i].getText());

				}

				if (!Max) {

					for (int j = 0; j < NumberOfVar; j++) {
						c[j] = -c[j];
					}
				}

				int back = 0;
				for (int i = 0; i < NumberOfCons; i++) {
					if (direction[i].equals("<=")) {
						Artificalneeded[i] = false;
						A[i][NumberOfVar + i - back] = 1;
					} else if (direction[i].equals(">=")) {
						Artificalneeded[i] = true;
						A[i][NumberOfVar + i - back] = -1;
						FirstPhase = true;
					} else if (direction[i].equals("=")) {
						Artificalneeded[i] = true;
						back++;
						FirstPhase = true;
					}
				}

				for (int i = 0; i < NumberOfCons; i++) {
					if (Artificalneeded[i]) {
						A[i][NumberOfVar + NumberOfCons + i - back] = 1;
					} else {
						back++;
					}
				}
				@SuppressWarnings("unused") // it is not unused but kind of class implementation so do not need to
											// recall it.
				Solver solve = new Solver(A, b, c, Artificalneeded, userFrame, FirstPhase);

			} catch (NullPointerException except) {
				JOptionPane.showMessageDialog(FirstPanel.this,
						"Please do not leave any field empty/Please be careful : Greater and Less than signs are not default selected!",
						"Empty Field Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(FirstPanel.this,
						"This LP Problem is unsolvable with this algorithm or I have error that I couldn't catch(see ReadMe Errors-3)",
						"General Error", JOptionPane.ERROR_MESSAGE);
				JButton SolveButton = new JButton("Solve new LP");
				ListenSolveAgain SButtonL = new ListenSolveAgain(userFrame);
				SolveButton.addActionListener(SButtonL);
				JPanel panel = new JPanel();
				panel.add(SolveButton);
				userFrame.add(panel);

			}
			userFrame.setSize(800, 300);
			userFrame.setVisible(true);

		}

		// Max-Min field is default selected so needed itemListener
		@Override
		public void itemStateChanged(ItemEvent e) {

			direction = new String[NumberOfCons];

			if (MinMax.getSelectedItem() == "Max") {
				Max = true;
			} else {
				Max = false;
			}

			for (int i = 0; i < NumberOfCons; i++) {
				direction[i] = compares[i].getSelectedItem();
			}
		}

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
