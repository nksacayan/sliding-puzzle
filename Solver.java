import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.management.RuntimeErrorException;

public class Solver {

	public static void main(String[] args) throws PuzzleTooLong, IOException {

		// Menu
		System.out.println("Enter 1 to enter your own puzzle");
		System.out.println("Enter 2 to randomly generate a puzzle");
		System.out.println("Enter 3 for data collection");
		Scanner user = new Scanner(System.in);
		byte selection = user.nextByte();
		while (selection < 1 || selection > 3) {
			System.out.println("Please try again");
			selection = user.nextByte();
		}
		Node initialState = new Node();
		boolean solveable = true;
		if (selection == 1) {
			do {
				// User input puzzle
				solveable = true;
				System.out.println("\nCopy paste from the sample files and hit enter or input in the following format");
				System.out.println("Program takes three rows of three numbers delimited by spaces");
				System.out.println("Example: ");
				System.out.println("0 1 2");
				System.out.println("3 4 5");
				System.out.println("6 7 8\n");

				user.nextLine();
				String in1 = user.nextLine();
				String in2 = user.nextLine();
				String in3 = user.nextLine();
				String in = in1 + " " + in2 + " " + in3;
				String[] inArray = in.split(" ");

				int k = 0;
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 3; j++)
						initialState.state[i][j] = Integer.parseInt(inArray[k++]);

				if (testSolvable(initialState.state) == false) {
					System.out.println("Puzzle not solveable, please try again\n");
					solveable = false;
				}
			} while (solveable == false);
			printResults(solveManhattan(initialState));

		} else if (selection == 2) {
			do {
				// System.out.println("Enter the desired length of the puzzle: ");
				// int length = user.nextInt();
				// randomly generate
				solveable = true;
				initialState = generate();
				if (testSolvable(initialState.state) == false) {
					System.out.println("Puzzle not solveable, generating another");
					solveable = false;
				}
			} while (solveable == false);

			printResults(solveManhattan(initialState));

		} else { // Data collection
			System.out.println("Starting data collection...");
			int count = 0;
			Node hammingNode = new Node();
			Node manhattanNode = null;
			Node hammingSol;
			Node manhattanSol;
			// Only enter provided samples
			System.out.println("\nCopy paste from the sample files and hit enter or input in the following format");
			System.out.println("Program takes three rows of three numbers delimited by spaces");
			System.out.println("Example: ");
			System.out.println("0 1 2");
			System.out.println("3 4 5");
			System.out.println("6 7 8\n");

			user.nextLine();
			String in1 = user.nextLine();
			String in2 = user.nextLine();
			String in3 = user.nextLine();
			String in = in1 + " " + in2 + " " + in3;
			String[] inArray = in.split(" ");

			int k = 0;
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					hammingNode.state[i][j] = Integer.parseInt(inArray[k++]);

			manhattanNode = cloneNode(hammingNode);
			hammingNode.setFree();
			manhattanNode.setFree();

			FileWriter hammingWriter = new FileWriter("hammingTime.txt", true); // Set true for append mode
			PrintWriter hammingPrintWriter = new PrintWriter(hammingWriter);
			// printWriter.println(textToAppend); //New line

			FileWriter manhattanWriter = new FileWriter("manhattanWriter.txt", true); // Set true for append mode
			PrintWriter manhattanPrintWriter = new PrintWriter(manhattanWriter);
			// printWriter.println(textToAppend); //New line

			while (count < 5) {

				long startHammingTime;
				long endHammingTime;
				long durationHamming;

				long startManhattanTime;
				long endManhattanTime;
				long durationManhattan;

				try {
					startHammingTime = System.nanoTime();
					hammingSol = solveHamming(hammingNode);
					endHammingTime = System.nanoTime();
					durationHamming = (endHammingTime - startHammingTime); // divide by 1000000 to get milliseconds.
				} catch (PuzzleTooLong e) {
					System.out.println("Puzzle too long, starting over without incrementing");
					continue;
				}

				try {
					startManhattanTime = System.nanoTime();
					manhattanSol = solveManhattan(manhattanNode);
					endManhattanTime = System.nanoTime();
					durationManhattan = (endManhattanTime - startManhattanTime); // divide by 1000000 to get
																					// milliseconds.
				} catch (PuzzleTooLong e) {
					System.out.println("Puzzle too long, starting over without incrementing");
					continue;
				}
				count++;
				System.out.println("Trial " + count);
				System.out.println("Hamming:");
				System.out.println("Length: " + hammingSol.pathCost);
				System.out.println("Runtime: " + (durationHamming) + " nanoseconds");
				hammingPrintWriter.println(durationHamming);
				System.out.println();
				System.out.println("Manhattan:");
				System.out.println("Length: " + manhattanSol.pathCost);
				System.out.println("Runtime: " + (durationManhattan) + " nanoseconds");
				manhattanPrintWriter.println(durationManhattan);
				System.out.println();
			}
			hammingPrintWriter.close();
			manhattanPrintWriter.close();
		}
		user.close();

	}

	static boolean testSolvable(int[][] puzzle) {
		int inversions = 0;
		for (int row = 0; row < puzzle.length; row++) {
			for (int col = 0; col < puzzle.length; col++) {
				for (int row2 = 0; row2 < puzzle.length; row2++) {
					for (int col2 = 0; col2 < puzzle.length; col2++) {
						if (puzzle[row][col] != 0 && puzzle[row2][col2] != 0)
							if (puzzle[row][col] > puzzle[row2][col2])
								inversions++;
					}
				}
			}
		}

		return (inversions % 2 == 0);
	}

	static int getChildren(Node currentNode, PriorityQueue<Node> output) {
		Point currentFree = currentNode.free.getLocation();
		int nodesGenerated = 0;
		// Move up if possible
		if (currentFree.y > 0) {
			int[][] newState = new int[currentNode.state.length][];
			for (int i = 0; i < currentNode.state.length; i++)
				newState[i] = currentNode.state[i].clone();
			int switchVal = currentNode.state[currentFree.y - 1][currentFree.x];
			newState[currentFree.y - 1][currentFree.x] = 0;
			newState[currentFree.y][currentFree.x] = switchVal;
			nodesGenerated++;
			output.add(new Node(currentNode, newState));
		}
		// Move down if possible
		if (currentFree.y < 2) {
			int[][] newState = new int[currentNode.state.length][];
			for (int i = 0; i < currentNode.state.length; i++)
				newState[i] = currentNode.state[i].clone();
			int switchVal = currentNode.state[currentFree.y + 1][currentFree.x];
			newState[currentFree.y + 1][currentFree.x] = 0;
			newState[currentFree.y][currentFree.x] = switchVal;
			nodesGenerated++;
			output.add(new Node(currentNode, newState));
		}
		// Move left if possible
		if (currentFree.x > 0) {
			int[][] newState = new int[currentNode.state.length][];
			for (int i = 0; i < currentNode.state.length; i++)
				newState[i] = currentNode.state[i].clone();
			int switchVal = currentNode.state[currentFree.y][currentFree.x - 1];
			newState[currentFree.y][currentFree.x - 1] = 0;
			newState[currentFree.y][currentFree.x] = switchVal;
			nodesGenerated++;
			output.add(new Node(currentNode, newState));
		}
		// Move right if possible
		if (currentFree.x < 2) {
			int[][] newState = new int[currentNode.state.length][];
			for (int i = 0; i < currentNode.state.length; i++)
				newState[i] = currentNode.state[i].clone();
			int switchVal = currentNode.state[currentFree.y][currentFree.x + 1];
			newState[currentFree.y][currentFree.x + 1] = 0;
			newState[currentFree.y][currentFree.x] = switchVal;
			nodesGenerated++;
			output.add(new Node(currentNode, newState));
		}
		return nodesGenerated;
	}

	static Node generate() {

		List<Integer> array = new ArrayList<>();
		for (int i = 0; i < 9; i++)
			array.add(i);
		Collections.shuffle(array);

		int[][] puzzle = new int[3][3];
		int k = 0;
		for (int i = 0; i < puzzle.length; i++) {
			for (int j = 0; j < puzzle.length; j++) {
				puzzle[i][j] = array.get(k++);
			}
		}
		Node newNode = new Node(null, puzzle);

		return newNode;
	}

	static Node solveHamming(Node initial) throws PuzzleTooLong, IOException { // returns solution Node
		Comparator<Node> hammingComparator = new HammingSort();
		// Initialize open list (frontier)
		PriorityQueue<Node> frontier = new PriorityQueue<Node>(hammingComparator);
		// Initialize closed list (explored)
		Node currentNode;
		int searchCost = 0;
		initial.setFree();
		// Set<Node> explored = new HashSet<Node>();
		// Stack<Node> explored = new Stack<Node>();
		// Put starting node on open list
		frontier.add(initial);
		Node possibleSol = null;
		// While list is not empty
		while (!frontier.isEmpty()) {
			if (frontier.peek().pathCost > 20)
				throw new PuzzleTooLong("Path cost greater than 20");
			if (possibleSol != null && possibleSol.manhattanTotal < frontier.peek().manhattanTotal)
				break;
			// Pop lowest f off frontier but keep pointer
			currentNode = frontier.poll();
			// Check if front of pq is goal
			if (currentNode.isGoal()) {
				if (possibleSol == null || possibleSol.manhattanTotal > currentNode.manhattanTotal)
					possibleSol = currentNode;
				continue;
			}
			// Add successors to frontier
			searchCost += getChildren(currentNode, frontier);
			// Fail if loop never breaks
		}
		if (possibleSol == null)
			throw new RuntimeErrorException(null, "No solution found");
		System.out.println("Search cost:" + searchCost);

		return possibleSol;
	}

	static Node solveManhattan(Node initial) throws PuzzleTooLong, IOException { // returns solution Node
		Comparator<Node> manhattanComparator = new ManhattanSort();
		int searchCost = 0;
		// Initialize open list (frontier)
		PriorityQueue<Node> frontier = new PriorityQueue<Node>(manhattanComparator);
		// Initialize closed list (explored)
		Node currentNode;
		initial.setFree();
		// Set<Node> explored = new HashSet<Node>();
		// Stack<Node> explored = new Stack<Node>();
		// Put starting node on open list
		frontier.add(initial);
		Node possibleSol = null;
		// While list is not empty
		while (!frontier.isEmpty()) {
			if (frontier.peek().pathCost > 20)
				throw new PuzzleTooLong("Path cost greater than 20");
			if (possibleSol != null && possibleSol.manhattanTotal < frontier.peek().manhattanTotal)
				break;
			// Pop lowest f off frontier but keep pointer
			currentNode = frontier.poll();
			// Check if front of pq is goal
			if (currentNode.isGoal()) {
				if (possibleSol == null || possibleSol.manhattanTotal > currentNode.manhattanTotal)
					possibleSol = currentNode;
				continue;
			}
			// Add successors to frontier
			searchCost += getChildren(currentNode, frontier);
			// Fail if loop never breaks
		}
		if (possibleSol == null)
			throw new RuntimeErrorException(null, "No solution found");
		System.out.println("Search cost: " + searchCost);
		return possibleSol;
	}

	static void printResults(Node solution) {
		Stack<Node> reverse = new Stack<Node>();
		reverse.add(solution);
		while (solution.parent != null) {
			solution = solution.parent;
			reverse.add(solution);
		}
		int step = 0;
		while (!reverse.isEmpty()) {
			System.out.println("\nStep " + step++ + ":");
			reverse.pop().printNode();
		}
	}

	static Node cloneNode(Node n) {
		Node newNode = new Node();
		for (int i = 0; i < n.state.length; i++)
			newNode.state[i] = n.state[i].clone();
		return newNode;
	}
}

class PuzzleTooLong extends Exception {
	public PuzzleTooLong(String message) {
		super(message);
	}
}

class HammingSort implements Comparator<Node> { // Not sure if the sorts work
	@Override
	public int compare(Node x, Node y) {
		return x.hammingTotal - y.hammingTotal;
	}
}

class ManhattanSort implements Comparator<Node> {
	@Override
	public int compare(Node x, Node y) {
		return x.manhattanTotal - y.manhattanTotal;
	}
}