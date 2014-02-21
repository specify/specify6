/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author timo
 *
 *Combines rows with consecutive values in the specified smush col and identical data for all other columns.  
 */
public class Smusher {

	protected int smushCol;
	int recordIdCol = 0;
	protected Vector<Vector<Object>> toSmush;
	
	/**
	 * @param toSmush
	 * @param smushCol
	 * @param recordIdCol
	 */
	public Smusher(Vector<Vector<Object>> toSmush, int smushCol, int recordIdCol) {
		this.recordIdCol = recordIdCol;
		this.smushCol = smushCol;
		this.toSmush = toSmush;
	}
	
	
	/**
	 * @param startVal
	 * @param endVal
	 * @return
	 */
	protected String getText(Integer startVal, Integer endVal) {
		String result = startVal.toString();
		if (!startVal.equals(endVal)) {
			result += "-" + endVal.toString();
		} 
		return result;
	}
	
	/**
	 * @param startVal
	 * @param endVal
	 * @param row
	 * @param ids
	 * @return
	 */
	protected Vector<Object> newRow(Integer startVal, Integer endVal, Vector<Object> row, List<Integer> ids) {
		Vector<Object> result = new Vector<Object>();
		int first = 0;
		for (int i = first; i < row.size(); i++) {
			if (i != recordIdCol) {
				if (i == smushCol) {
					result.add(getText(startVal, endVal));
				} else {
					result.add(row.get(i));
				}
			} else {
				result.add(ids);
			}
		}
		return result;
	}
	
	/**
	 * @param startRow
	 * @param row
	 * @param currentVal
	 * @return
	 */
	protected boolean isSmushable(Vector<Object> startRow, Vector<Object> row, Integer currentVal) {
		int first = 0;
		for (int i = first; i < startRow.size(); i++) {
			if (i != recordIdCol) {
				if (i != smushCol) {
					Object o1 = startRow.get(i);
					Object o2 = row.get(i);
					if (o1 == null ^ o2 == null) {
						return false;
					} else if (o1 != null && o2 != null && !o1.equals(o2)) {
						return false;
					}
				} else {
					if (currentVal + 1 != Integer.valueOf(row.get(i).toString())) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * @return
	 */
	public Vector<Vector<Object>> smush() {
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		Integer startVal = null;
		Integer currentVal = null;
		Vector<Object> startRow = null;
		int r = 0;
		Vector<Object> row = null;
		List<Integer> ids = null;
		while (r < toSmush.size()+1) {
			if (r < toSmush.size()) {
				row = toSmush.get(r);
				if (currentVal == null) {
					startVal = Integer.valueOf(row.get(smushCol).toString());
					startRow = row;
					if (recordIdCol != -1) {
						ids = new ArrayList<Integer>();
						ids.add((Integer )row.get(recordIdCol));
					}
					currentVal = Integer.valueOf(row.get(smushCol).toString());
				} else {
					if (isSmushable(startRow, row, currentVal)) {
						currentVal =  Integer.valueOf(row.get(smushCol).toString());
						if (recordIdCol != -1) {
							ids.add((Integer )row.get(recordIdCol));
						}
					} else {
						result.add(newRow(startVal, currentVal, startRow, ids));
						currentVal = null;
						r--;
					}
					
				}
			} else if (row != null) {
				result.add(newRow(startVal, currentVal, startRow, ids));				
			}
			r++;
		}
		return result;
	}
	
	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		Vector<Vector<Object>> tst = new Vector<Vector<Object>>();
//		Vector<Object> r = new Vector<Object>();
//		r.add("0001");
//		r.add("BOB");
//		tst.add(r);
//		
//		r = new Vector<Object>();
//		r.add("0002");
//		r.add("BOB");
//		tst.add(r);
//		
//		r = new Vector<Object>();
//		r.add("0004");
//		r.add("BOB");
//		tst.add(r);
//		
//		r = new Vector<Object>();
//		r.add("0005");
//		r.add("JOE");
//		tst.add(r);
//
//		r = new Vector<Object>();
//		r.add("0006");
//		r.add("JOE");
//		tst.add(r);
//
//		r = new Vector<Object>();
//		r.add("0007");
//		r.add("MIKE");
//		tst.add(r);
//
//		Smusher s = new Smusher(tst, 0, -1);
//		Vector<Vector<Object>> smushed = s.smush();
//		
//		for (Vector<Object> row : smushed) {
//			for (Object o : row) {
//				System.out.print(o + " | ");
//			}
//			System.out.println();
//		}
//	}

}
