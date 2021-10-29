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
public class PieceCategorizer {

	protected List<Class<? extends UnDateComponent>> compClasses;
	
	protected List<Class<? extends UnDateComponent>> buildCompClasses() {
		List<Class<? extends UnDateComponent>> result = new ArrayList<Class<? extends UnDateComponent>>();
		result.add(UnDay.class);
		result.add(UnMonth.class);
		result.add(UnYear.class);
		result.add(UnSeparator.class);
		return result;
	}
	
	public PieceCategorizer() {
		compClasses = buildCompClasses();
	}
	
	public List<UnDateComponent> categorize(Piece piece) {
		List<UnDateComponent> result = new ArrayList<UnDateComponent>();
		
		return result;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
