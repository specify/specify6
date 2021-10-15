/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.ArrayList;
import java.util.List;

/**
 * @author timo
 *
 */
public class DatePiecer {

	protected final static int NOT_NEW = 0;
	protected final static int NEW = 1;
	protected final static int NEW_LEFT = 2;
	protected final static int NEW_RIGHT = 3;
	
	protected final static int NUMB = 4;
	protected final static int STRI = 5;
	
	protected String EOL = "$";
	
	protected String[] separators = {
			" ", ".", "/",  "-", ","
	};
	
	/**
	 * @param p
	 * @return
	 */
	protected int getPieceType(String p) {
		try {
			new Integer(p);
			return NUMB;
		} catch (NumberFormatException nfe) {
			return STRI;
		}
	}
	
	/**
	 * @param oldpiece
	 * @param next
	 * @return
	 */
	protected int newPiece(String oldpiece, String next) {
		if (EOL.equals(next)) {
			return NEW;
		}
		for (String sep : separators) {
			if (sep.equals(next)) {
				return NEW;
			}
		}
		if (!"".equals(oldpiece)) {
			if (getPieceType(oldpiece) != getPieceType(next)) {
				return NEW_RIGHT;
			}
		}
		return NOT_NEW;
	}
	
	/**
	 * @param piecee
	 * @return
	 */
	public List<Piece> getPieces(String piecee) {
		String input = piecee + EOL;
		List<Piece> result = new ArrayList<Piece>();
		String piece = "";
		for (int c = 0; c < input.length(); c++) {
			String s = input.substring(c, c+1);
			int r = newPiece(piece, s); 
			if (r == NOT_NEW) {
				piece += s;
			} else {
				if (r == NEW_LEFT) {
					piece += s; 
				}
				if (!"".equals(piece)) {
					result.add(new Piece(piece, false));
					if (r == NEW) {
						result.add(new Piece(s, true));
					}
					piece = "";
				}
				if (r == NEW_RIGHT) {
					piece = s;
				}
			}
		}
		return result;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DatePiecer piecer = new DatePiecer();
		String[] toPiece = {
				"12November1986", "12.X.1994", "Feb 25, 2004", "Summer2004-05", "12 Sep 2004"
		};
		for (String piecee : toPiece) {
			List<Piece> pieces = piecer.getPieces(piecee);
			System.out.print(piecee + " => ");
			for (Piece piece : pieces) {
				System.out.print((piece.isDelimiter() ? "[" : "") + piece.getText() + 
						(piece.isDelimiter() ? "]" : "") + " : ");
			}
			System.out.println();
		}
	}

}
