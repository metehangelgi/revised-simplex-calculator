/*
 * author:Metehan Gelgi 64178
 */


package twoPhaseSolver;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class RevisedSimplex {

	float[] w;
	float[][] bInverse;
	float[] cB;
	float[] cN;
	float[] xB;
	float Z;
	float[][] N;
	float[][] BV;
	float[] y; // bInverse ai
	float[][] B;
	float[] zNcN;
	int NumNB;
	int NumBasic;
	float[] b;
	float[][] A;
	int enteringBasic;
	int LeavingBasic;
	float initialZ;
	int[] indexBV;
	int[] indexNB;
	String[] BasicX;
	String[] NonBasicX;

	/*
	 * it is constructorr for revised simplex method initialize problem.
	 */
	public RevisedSimplex(float[][] A, float[] b, float[] c, float Z) throws Exception {
		NumNB = A[0].length - b.length;
		NumBasic = b.length;

		N = new float[NumBasic][NumNB];
		BV = new float[NumBasic][NumBasic];
		bInverse = new float[NumBasic][NumBasic];
		cB = new float[NumBasic];
		cN = new float[NumNB];
		xB = new float[NumBasic];
		BasicX = new String[NumBasic];
		NonBasicX = new String[NumNB];
		this.Z = Z;
		this.initialZ = Z;
		w = new float[NumBasic];
		y = new float[NumBasic];
		// mu = new int[NumBasic];
		B = new float[NumBasic][NumBasic];
		zNcN = new float[NumNB];
		this.b = b;
		this.A = A;
		enteringBasic = -1;
		LeavingBasic = -1;
		initialize(c);

		try {
			findXBandZ();
		} catch (Exception excp) {
			JOptionPane.showMessageDialog(new JFrame(), "Unbounded LP Problem!", "Unbounded LP Error",
					JOptionPane.ERROR_MESSAGE);
		}

		findW();
		findZNCN();
	}

	/*
	 * when solve function called from solver class we are going to solve LP problem
	 * which initialized in Constructor.
	 */
	public void solve() {
		// TODO Auto-generated method stub

		try {
			while (optimalityCheck() != -1) {
				findy();
				MinRatio();
				findbInverse();
				houseKeepig();
				findXBandZ();
				findW();
				findZNCN();
			}

		} catch (Exception excp) {
			JOptionPane.showMessageDialog(new JFrame(), "Unbounded LP Problem!", "Unbounded LP Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void initialize(float[] c) {
		int BasicIndex = -1;
		int addedCN = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 0.0) {
				for (int j = 0; j < NumBasic; j++) {
					if (A[j][i] == 1.0) {
						BasicIndex = j;
					} else if (A[j][i] == 0.0) {
						continue;
					} else {
						BasicIndex = -1;
						break;
					}
				}
			}
			if (BasicIndex != -1) {
				cB[BasicIndex] = c[i];
				BasicX[BasicIndex] = "x" + (i + 1);
				for (int j = 0; j < NumBasic; j++) {
					B[j][BasicIndex] = A[j][i];
				}
			} else {
				cN[addedCN] = c[i];
				NonBasicX[addedCN] = "x" + (i + 1);
				for (int j = 0; j < NumBasic; j++) {
					N[j][addedCN] = A[j][i];
				}

				addedCN++;
			}

			BasicIndex = -1;
		}

		for (int j = 0; j < NumBasic; j++) {
			xB[j] = b[j];
		}

		BV = B;
		bInverse = FindFirstBInverse(B);
	}

	private void houseKeepig() {
		float temp = cB[LeavingBasic];
		cB[LeavingBasic] = cN[enteringBasic];
		cN[enteringBasic] = temp;

		String temp2 = BasicX[LeavingBasic];
		BasicX[LeavingBasic] = NonBasicX[enteringBasic];
		NonBasicX[enteringBasic] = temp2;

		float[] TempN = new float[NumBasic]; // tekrar yap
		float[] TempB = new float[NumBasic]; // tekrar yap

		for (int i = 0; i < NumBasic; i++) {
			TempN[i] = N[i][enteringBasic];
			TempB[i] = BV[i][LeavingBasic];
		}

		for (int i = 0; i < NumBasic; i++) {
			N[i][enteringBasic] = TempB[i];
			BV[i][LeavingBasic] = TempN[i];
		}

	}

	private void findXBandZ() throws ExceptionInInitializerError {
		float sum = 0;
		for (int j = 0; j < NumBasic; j++) {
			for (int i = 0; i < NumBasic; i++) {
				sum = sum + bInverse[j][i] * b[i];
			}

			xB[j] = sum;
			sum = 0;
			if (sum < 0) {
				throw new ExceptionInInitializerError();
			}
		}

		for (int j = 0; j < NumBasic; j++) {
			sum = sum + cB[j] * xB[j];
		}

		Z = sum + initialZ;

	}

	private void findW() {
		for (int j = 0; j < NumBasic; j++) {
			float sum = 0;
			for (int i = 0; i < NumBasic; i++) {
				sum = sum + cB[i] * bInverse[i][j];
			}
			w[j] = sum;
		}
	}

	private void findZNCN() {
		for (int j = 0; j < NumNB; j++) {
			float sum = 0;
			for (int i = 0; i < NumBasic; i++) {
				sum = sum + w[i] * N[i][j];
			}
			zNcN[j] = sum - cN[j];
		}

	}

	private int optimalityCheck() {

		int index = -1;
		for (int i = 0; i < NumNB; i++) {
			if (zNcN[i] < 0) {
				if (index == -1) {
					index = i;
				} else {
					if (zNcN[i] < zNcN[index]) {
						index = i;
					}
				}
			}
		}

		if (index != -1)
			enteringBasic = index;

		return index;

	}

	private void findy() {
		for (int j = 0; j < NumBasic; j++) {
			float sum = 0;
			for (int i = 0; i < NumBasic; i++) {
				sum = sum + bInverse[j][i] * N[i][enteringBasic];
			}
			y[j] = sum;
		}

	}

	private void MinRatio() {
		int index = -1;
		float[] ratio = new float[NumBasic];

		for (int j = 0; j < NumBasic; j++) {

			if (y[j] != 0 || (y[j] == 0 && xB[j] == 0)) {
				ratio[j] = xB[j] / y[j];
				if (ratio[j] > 0) {
					if (index == -1) {
						index = j;
					} else {
						if (ratio[j] < ratio[index]) {
							index = j;
						}
					}
				}
			} else {
				ratio[j] = 0;
			}

		}
		LeavingBasic = index;

	}

	// for initial Binverse I use INverseMatrix class
	private float[][] FindFirstBInverse(float[][] B) {

		InverseMatrix Matrix = new InverseMatrix(B);
		return Matrix.inverse();

	}

	private void findbInverse() {

		float[][] bInverseNew = new float[NumBasic][NumBasic];
		for (int i = 0; i < NumBasic; i++) {
			for (int j = 0; j < NumBasic; j++) {
				if (i == LeavingBasic) {
					bInverseNew[i][j] = bInverse[LeavingBasic][j] / y[LeavingBasic];
				} else {
					bInverseNew[i][j] = bInverse[i][j] - (y[i] / y[LeavingBasic] * bInverse[LeavingBasic][j]);
				}

			}
		}
		bInverse = bInverseNew;
	}
}
