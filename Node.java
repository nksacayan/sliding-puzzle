import java.awt.Point;
import java.util.HashMap;

public class Node {
	Node parent = null;
	int[][] state = new int[3][3];
	int pathCost = 0;
	int hamming; // (h1) Number misplaced tiles
	int manhattan; // (h2) Sum of distances from tiles to goal
	int hammingTotal; // h1 + g
	int manhattanTotal; // h2 + g
	Point free = new Point();
	final static int[][] goalState = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
	static HashMap<Integer, Point> coords = new HashMap<Integer, Point>();

	public Node() {
		int k = 0;
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state.length; j++) {
				coords.put(k++, new Point(j, i));
			}
		}
		setFree();
	}

	public Node(Node parent, int[][] state) {
		this.parent = parent;
		this.state = state;
		if (parent != null)
			this.pathCost = parent.pathCost + 1;
		this.hamming = findHamming();
		this.manhattan = findManhattan();
		setFree();
		setHammingTotal();
		setManhattanTotal();
	}

	// Note to self don't count blank tiles
	int findHamming() {
		int count = 0;
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[0].length; j++) {
				if (state[i][j] != 0 && state[i][j] != goalState[i][j])
					count++;
			}
		}
		return count;
	}

	int findManhattan() {
		int sum = 0;
		for (int i = 0; i < goalState.length; i++) {
			for (int j = 0; j < goalState.length; j++) {
				if (state[i][j] != 0 && state[i][j] != goalState[i][j]) {
					sum += Math.abs(i - coords.get(state[i][j]).y);
					sum += Math.abs(j - coords.get(state[i][j]).x);
				}
			}
		}
		return sum;
	}

	public int getCol(int value) {
		return coords.get(value).x;
	}

	public int getRow(int value) {
		return coords.get(value).y;
	}

	public boolean isGoal() {
		for (int i = 0; i < goalState.length; i++)
			for (int j = 0; j < goalState.length; j++)
				if (state[i][j] != goalState[i][j])
					return false;
		return true;
	}

	void setFree() {
		for (int row = 0; row < goalState.length; row++)
			for (int col = 0; col < goalState.length; col++)
				if (this.state[row][col] == 0)
				{
					this.free.setLocation(col, row);
					return;
				}
				else if (row == 2 && col == 2)
					throw new RuntimeException("Couldn't find free point");
	}

	void setHammingTotal() {
		hammingTotal = hamming + pathCost;
	}

	void setManhattanTotal() {
		manhattanTotal = manhattan + pathCost;
	}

	void printNode() {
		for (int i = 0; i < goalState.length; i++) {
			for (int j = 0; j < goalState.length; j++) {
				System.out.print(state[i][j] + " ");
			}
			System.out.println();
		}
	}
}
