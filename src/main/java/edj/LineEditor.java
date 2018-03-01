package edj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * LineEditor implements a very small subset of the Unix line editor ed(1).
 * It is NOT intended to be a real-world editor; the market for line editors is
 * rather limited, and is already served by ed. It is rather meant just as
 * a rather involved example of the Command pattern, used to implement Undo.
 * 
 * @author Ian Darwin
 */
public class LineEditor {
	
	protected static AbstractBufferPrims buffHandler = new BufferPrimsWithUndo();
	
	protected static BufferedReader in = null;	// command input

	protected static String currentFileName;
	
	/** Should remove throws, use try-catch inside loop */
	public static void main(String[] args) throws IOException {
		String line;
		in = new BufferedReader(new InputStreamReader(System.in));

		if (args.length == 1) {
			currentFileName = args[0];
			buffHandler.readBuffer(currentFileName);
			// Since readBuffer can be used from here or interactively, here we drop its Undoable.
			if (buffHandler instanceof BufferPrimsWithUndo) {
				((BufferPrimsWithUndo)buffHandler).popUndo();
			}
		}

		// The main loop of the editor is right here:
		while ((line = in.readLine())  != null) {
			// System.out.println("Command Line is: " + line);

			ParsedLine c = LineParser.parse(line);
			// Try to keep in alphabetical order within each section
			
			// ------------
			// FILE RELATED
			// ------------

			// Edit a new file
			if (line.startsWith("e")) {
				buffHandler.clearBuffer();
				if (line.length() >= 3) 
					currentFileName = line.substring(1).trim();
				if (currentFileName == null) {
					System.out.println("?no filename");
				} else {
					buffHandler.readBuffer(currentFileName);
				}
				continue;
			}
			if (line.startsWith("f")) {
				System.out.println(currentFileName == null ? "(no file)" : currentFileName);
				continue;
			}
			// Like e but reads into current buffer w/o setting filename
			if (line.startsWith("r")) {
				buffHandler.readBuffer(line.substring(1).trim());
				continue;
			}
			// Quit
			if (line.equals("q")) {
				System.exit(0);
			}
			// Write - maybe someday
			if (line.startsWith("w")) {
				System.err.println("?file is read-only");
				continue;
			}
			
			// --------------
			// BUFFER-RELATED
			// --------------

			if (line.equals("=")) {
				System.err.println(buffHandler.getCurrentLineNumber() + " of " + buffHandler.size());
				continue;
			}
			if (line.equals(".")) {
				int i = buffHandler.getCurrentLineNumber();
				buffHandler.printLines(i, i);
				continue;
			}
			if (line.equals("a")) {	// XXX accept line number
				List<String> lines = gatherLines();
				buffHandler.addLines(lines);
				continue;
			}
			if (line.endsWith("d")) {
				int[] range = buffHandler.getLineRange(line);
				buffHandler.deleteLines(range[0], range[1]);
				continue;
			}
			if (line.endsWith("p")) {
				int[] range = buffHandler.getLineRange(line);
				buffHandler.printLines(range[0], range[1]);
				continue;
			}
			if (line.equals("u")) {
				buffHandler.undo();
				continue;
			}
			if (line.matches("\\d+")) {
				buffHandler.goToLine(Integer.parseInt(line));
				continue;
			}
			// default: standard 'ed' error handling
			System.out.println("?");
		}
	}

	/**
	 * Read lines from the user until they type a "." line
	 * @return The List of lines.
	 * @throws IOException 
	 */
	private static List<String> gatherLines() throws IOException {
		List<String> ret = new ArrayList<>();
		String line;
		while ((line = in.readLine()) != null && !line.equals(".")) {
			ret.add(line);
		}
		return ret;
	}
}
