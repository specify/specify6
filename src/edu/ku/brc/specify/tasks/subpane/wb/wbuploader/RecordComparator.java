/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.util.Orderable;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class RecordComparator implements Comparator<DataModelObjBase> 
{
	//a list of no-argument methods that return comparable values - i.e. getters for fields of record
	final List<Pair<Method, Boolean>> comparisons;
	
	
	/**
	 * @param comparisons
	 */
	public RecordComparator(List<Pair<Method, Boolean>> comparisons) throws Exception
	{
		super();
		for (Pair<Method, Boolean> m : comparisons)
		{
			Class<?> rt = m.getFirst().getReturnType();
			if (!Comparable.class.isAssignableFrom(rt) && !"int".equals(rt.getName()))
			{
				throw new Exception("comparison returns non-comparable result for " + m.getFirst().getName());
			}
		}
		this.comparisons = comparisons;
	}


	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(DataModelObjBase arg0, DataModelObjBase arg1) 
	{
		try
		{
			for (Pair<Method, Boolean> mp : comparisons)
			{
				Method m = mp.getFirst();
				Comparable<Object> v0 = (Comparable<Object> )m.invoke(arg0, (Object [])null);
				Object v1 = m.invoke(arg1, (Object [])null);
				int r = v0.compareTo(v1);
				if (r != 0)
				{
					return mp.getSecond() ? r*-1 : r;
				}
			}
		} catch (Exception ex)
		{
			//yes??
		} 		
		return 0;
	}
	
	/**
	 * @param cls
	 * @return
	 * @throws NoSuchMethodException
	 */
	public static RecordComparator createRecordComparator(final Class<?> cls) throws Exception
	{
		Vector<Pair<Method, Boolean>> ms = new Vector<Pair<Method, Boolean>>();

		if (cls.equals(Determination.class))
		{
			ms.add(new Pair<Method, Boolean>(cls.getMethod("getIsCurrent"), true));
		}

		if (Orderable.class.isAssignableFrom(cls))
		{
			ms.add(new Pair<Method, Boolean>(cls.getMethod("getOrderIndex"), false));
		}

		if (ms.size() == 0)
		{
			//XXX actually, given the reverse-order method for one-to-manys
			//currently used in UploadTable.WriteRowOrNot, this causes
			//records added via wb update, to be displayed in reverse order if they get
			//exported to a dataset, which perhaps nothing to worry about.
			//Otherwise this seems to produce the order displayed in forms.
			ms.add(new Pair<Method, Boolean>(cls.getMethod("getId"), false));
		}
		
		return new RecordComparator(ms);
	}

}
