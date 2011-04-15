/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author timo
 *
 *Status: alpha
 *
 *Not ready for use.
 *
 */
public class UniquenessChecker
{

	protected TreeMap<Integer, String> map = new TreeMap<Integer, String>();
	
	/**
	 * 
	 */
	public UniquenessChecker()
	{
		//nothing to do yet
	}
	
	/**
	 * clear all stored values
	 */
	public void clear()
	{
		map.clear();
	}
	
	/**
	 * @param row
	 * @param value
	 * @param check
	 * @return if check is thre then list of rows with equal value else null
	 * 
	 * sets value for row and checks for equal rows if check is true
	 */
	public Vector<Integer> setValue(int row, String value, boolean check)
	{
		map.put(row, value);
		
		if (check)
		{
			return checkValues(row, row);
		}
		
		return null;
	}

	/**
	 * @param row
	 * @param sortedValues
	 * @return list 
	 */
	public boolean checkValue(int row, Vector<String> sortedValues) {
		String value = map.get(row);
		int idx = Collections.binarySearch(sortedValues, value);
		int currIdx = idx;
		//can actually quit once a second occurrence is found??
		while (--currIdx >= 0 && sortedValues.get(currIdx).equals(value))
			return true;
		currIdx = idx + 1;
		while (currIdx < sortedValues.size() && sortedValues.get(currIdx++).equals(value))
			return true;
		return false;
	}
	
	/**
	 * @param rows
	 * @return
	 */
	public Vector<Integer> checkValues(int[] rows)
	{
		return checkValues(-1, -1, rows);
	}
	
	/**
	 * @param startRow
	 * @param endRow
	 * @return
	 */
	public Vector<Integer> checkValues(int startRow, int endRow)
	{
		return checkValues(startRow, endRow, null);
	}
	
	/**
	 * @param startRow
	 * @param endRow
	 * @param rows
	 * @return
	 */
	public Vector<Integer> checkValues(int startRow, int endRow, int[] rows)
	{
		Vector<Integer> result = new Vector<Integer>();
		Collection<String> valuesColl = map.values();
		Vector<String> values = new Vector<String>(valuesColl);
		Collections.sort(values);
		if (rows == null)
		{
			for (int row = startRow; row <= endRow; row++)
			{
				if (checkValue(row, values))
				{
					result.add(row);
				}
			}
		} else 
		{
			for (Integer row : rows)
			{
				if (checkValue(row, values))
				{
					result.add(row);
				}
			}
			
		}
		return result;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		UniquenessChecker uc = new UniquenessChecker();
		for (int i = 0; i < 2000000; i++)
		{
			uc.setValue(i, String.valueOf(i), false);
		}
		System.out.println(uc.setValue(50, "4", true));
		System.out.println(uc.setValue(500, "4", true));
		System.out.println(uc.setValue(5000, "4", true));
		System.out.println(uc.setValue(50000, "4", true));
		System.out.println(uc.setValue(500000, "4", true));
		System.out.println(uc.setValue(123, "bash", true));
		Vector<Integer> duped = uc.checkValues(0, 1999999);
		for (Integer dupe : duped)
		{
			System.out.println(dupe);
		}
		int[] checkees = {4, 50, 500, 5000, 50000, 500000, 666, 52, 37, 99};
		duped = uc.checkValues(checkees);
		System.out.println();
		for (Integer dupe : duped)
		{
			System.out.println(dupe);
		}		
	}

}
