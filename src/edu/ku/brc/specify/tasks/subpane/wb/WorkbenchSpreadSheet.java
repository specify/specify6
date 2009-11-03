/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Vector;

import org.jdesktop.swingx.table.TableColumnExt;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.TableSearcher;
import edu.ku.brc.ui.TableSearcherCell;
import edu.ku.brc.ui.tmanfe.SearchReplacePanel;
import edu.ku.brc.ui.tmanfe.SpreadSheet;
import edu.ku.brc.ui.tmanfe.SpreadSheetModel;
import edu.ku.brc.util.DateConverter;
import edu.ku.brc.util.GeoRefConverter;

/**
 * @author timbo
 *
 *Spreadsheet with Specify workbench-specific functionality.
 */
@SuppressWarnings("serial")
public class WorkbenchSpreadSheet extends SpreadSheet
{
	protected WorkbenchPaneSS       workbenchPaneSS;
	//for sorting
	protected DateConverter         dateConverter       = new DateConverter(); 
	protected GeoRefConverter       geoRefConverter     = new GeoRefConverter();

	protected Vector<Comparator<String>> comparators;
	
	
	/**
     * Constructor for Spreadsheet from model
     * @param model
     */
    public WorkbenchSpreadSheet(final SpreadSheetModel model, final WorkbenchPaneSS workbenchPaneSS)
    {
        super(model);
        this.workbenchPaneSS = workbenchPaneSS;
        buildComparators();
    }
    
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.ui.tmanfe.SpreadSheet#createSearchReplacePanel()
	 */
	@Override
	protected SearchReplacePanel createSearchReplacePanel()
	{
		return new SearchReplacePanel(this) {

			final Vector<Integer> replacedRows = new Vector<Integer>();
			/* (non-Javadoc)
			 * @see edu.ku.brc.ui.tmanfe.SearchReplacePanel#createTableSearcher()
			 */
			@Override
			protected TableSearcher createTableSearcher()
			{
				return new TableSearcher(table, this) {

					/* (non-Javadoc)
					 * @see edu.ku.brc.ui.TableSearcher#replace(edu.ku.brc.ui.TableSearcherCell, java.lang.String, java.lang.String, boolean, boolean)
					 */
					@Override
					public boolean replace(TableSearcherCell cell,
							String findValue, String replaceValue,
							boolean isMtchCaseOn, boolean isSearchSelection)
					{
						
						boolean result = super.replace(cell, findValue, replaceValue, isMtchCaseOn,
								isSearchSelection);
						if (result)
						{
							replacedRows.add(cell.getRow());
						}
						return result;
					}

					/* (non-Javadoc)
					 * @see edu.ku.brc.ui.TableSearcher#replacementCleanup()
					 */
					@Override
					public void replacementCleanup()
					{
						//This is not good.
						//I have avoided making WorkbenchPaneSS a table model listener
						//because lots of unnecessary validation would have been performed.
						//But this is not so efficient either...
						int[] rows = new int[replacedRows.size()];
						for (int r = 0; r < rows.length; r++)
						{
							rows[r] = convertRowIndexToModel(replacedRows.get(r));
						}
						if (!workbenchPaneSS.validateRows(rows))
						{
							model.fireTableDataChanged();
						}
					}

					/* (non-Javadoc)
					 * @see edu.ku.brc.ui.TableSearcher#reset()
					 */
					@Override
					protected void reset()
					{
						super.reset();
						replacedRows.clear();
					}
					
					
				};
			}
			
		};
	}


	/**
     * Builds custom comparators for columns that requre them.
     */
    protected void buildComparators()
    {
    	comparators = new Vector<Comparator<String>>(model.getColumnCount());
    	for (int c = 0; c < model.getColumnCount(); c++)
    	{
    		comparators.add(getComparatorForCol(c));
    	}
    }
    
    /**
     * @param colIdx
     * @return a comparator suitable for the data type of the field the column at colIdx maps to.
     */
    protected Comparator<String> getComparatorForCol(final int colIdx)
    {
    	WorkbenchTemplateMappingItem mapping = ((GridTableModel )model).getColMapping(colIdx);
    	Class<?> dataClass = WorkbenchTask.getDataType(mapping);
    	if (dataClass.equals(Calendar.class))
    	{
    		return new DateColumnComparator();
    	}
    	if (dataClass.equals(Boolean.class))
    	{
    		return new BooleanColumnComparator();
    	}
    	if (isGeoRefMapping(mapping))
    	{
    		return new GeoRefColumnComparator();
    	}
    	if (Number.class.isAssignableFrom(dataClass))
    	{
    		return new NumericColumnComparator();
    	}
    	if (mapping.getFieldName().equalsIgnoreCase("catalognumber")  && mapping.getTableName().equalsIgnoreCase("collectionobject"))
    	{
    		DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(1);
    		DBFieldInfo fi = ti.getFieldByName("catalogNumber");
    		UIFieldFormatterIFace format = fi.getFormatter();
    		if (format != null && format.isNumeric())
    		{
    			return new NumericColumnComparator();
    		}
    	}
    	return null;
    }
    
    /**
     * @param mapping
     * @return true if mapping refers to a Geo-reference
     */
    protected boolean isGeoRefMapping(final WorkbenchTemplateMappingItem mapping)
    {
    	return mapping.getFieldName().equalsIgnoreCase("latitude1")
    	  || mapping.getFieldName().equalsIgnoreCase("latitude2")
    	  || mapping.getFieldName().equalsIgnoreCase("longitude1")
    	  || mapping.getFieldName().equalsIgnoreCase("longitude2");
    }
    
	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.JXTable#toggleSortOrder(int)
	 */
	@Override
	public void toggleSortOrder(int arg0)
	{
		TableColumnExt col = getColumnExt(arg0);
		Comparator<String> cmp = comparators.get(arg0);
		if (col.getComparator() == null && cmp != null)
		{
			getColumnExt(arg0).setComparator(cmp);
		}

		super.toggleSortOrder(arg0);
	}
    
	//------------------------------------------------------------------------------
    //-- Inner Classes
    //------------------------------------------------------------------------------

    /**
     * @author timbo
     *
     *Compares values in Calendar columns.
     *
     *Non dates are less than dates.
     *Non dates are compared by their string values.
     */
    public class DateColumnComparator implements Comparator<String>
    {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(String arg0, String arg1)
		{
			Calendar cal0 = null;
			Calendar cal1 = null;
			try
			{
				cal0 = dateConverter.convert(arg0);
			}
			catch (ParseException ex)
			{
				//ignore
			}
			try
			{
				cal1 = dateConverter.convert(arg1);
			}
			catch (ParseException ex)
			{
				//ignore
			}
			
			//if both are invalid dates just sort by string value
			if (cal0 == null && cal1 == null)
			{
				return arg0.compareTo(arg1);
			}
			
			//non-dates are less than dates
			if (cal0 == null && cal1 != null)
			{
				return -1;
			}
			if (cal0 != null && cal1 == null)
			{
				return 1;
			}
			
			return cal0.compareTo(cal1);
		}
    	
    }

    
    /**
     * @author timbo
     *
     *Compares values in georef columns.
     *
     *Non georefs are less than georefs.
     *Non georefs are compared by their string values.
     */
     public class GeoRefColumnComparator implements Comparator<String>
    {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(String arg0, String arg1)
		{
			String llStr0 = null;
			String llStr1 = null;
			BigDecimal ll0 = null;
			BigDecimal ll1 = null;
			try
			{
				llStr0 = geoRefConverter.convert(arg0, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name());
				ll0 = new BigDecimal(llStr0);
			}
			catch (Exception ex)
			{
				//ignore
			}
			try
			{
				llStr1 = geoRefConverter.convert(arg1, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name());
				ll1 = new BigDecimal(llStr1);
			}
			catch (Exception ex)
			{
				//ignore
			}
			
			//if both are invalid just sort by string value
			if (ll0 == null && ll1 == null)
			{
				return arg0.compareTo(arg1);
			}
			
			//non-georefs are less than georefs
			if (ll0 == null && ll1 != null)
			{
				return -1;
			}
			if (ll0 != null && ll1 == null)
			{
				return 1;
			}
			
			return ll0.compareTo(ll1);
		}
    	
    }
 
     /**
      * @author timbo
      *
      *Compares values in numeric columns.
      *
      *Non numbers are less than numbers.
      *Non numbers are compared by their string values.
      */
    public class NumericColumnComparator implements Comparator<String>
    {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(String arg0, String arg1)
		{
			Number n0 = null;
			Number n1 = null;
			NumberFormat nf = NumberFormat.getInstance();
			try
			{
				n0 = nf.parse(arg0);
			}
			catch (ParseException ex)
			{
				//ignore
			}
			try
			{
				n1 = nf.parse(arg1);
			}
			catch (ParseException ex)
			{
				//ignore
			}
			
			//if both are invalid just sort by string value
			if (n0 == null && n1 == null)
			{
				return arg0.compareTo(arg1);
			}
			
			//non-nums are less than nums
			if (n0 == null && n1 != null)
			{
				return -1;
			}
			if (n0 != null && n1 == null)
			{
				return 1;
			}
			
			//this seems a little dubious...
			return new Double(n0.doubleValue()).compareTo(n1.doubleValue());
		}
    }
    
	     /**
	      * @author timbo
	      *
	      *Compares values in boolean columns.
	      *
	      *Non booleans are less than booleans.
	      *false is less than true.
	      */
	    public class BooleanColumnComparator implements Comparator<String>
	    {
	    	
	    	/**
	    	 * @param fldStr
	    	 * @return a Boolean value for fldStr, null if Bool val cannot be determined.
	    	 */
	    	protected Boolean getBool(final String fldStr)
	    	{
                
	    		if (fldStr == null || fldStr.equals(""))
                {
                    return null;
                }
                else
                {
                    int i;
                    for (i = 0; i < WorkbenchTask.boolStrings.length; i++)
                    {
                        if (fldStr.equalsIgnoreCase(WorkbenchTask.boolStrings[i]))
                            break;
                    }
                    if (i == WorkbenchTask.boolStrings.length) 
                    { 
                    	return null;
                    }
                   return i % 2 == 0 ? true : false;
                }
	    		
	    	}
	    	
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(String arg0, String arg1)
			{
				Boolean b0 = getBool(arg0);
				Boolean b1 = getBool(arg1);

				
				//if both are invalid just sort by string value
				if (b0 == null && b1 == null)
				{
					return arg0.compareTo(arg1);
				}
				
				//non-bools are less than bools
				if (b0 == null && b1 != null)
				{
					return -1;
				}
				if (b0 != null && b1 == null)
				{
					return 1;
				}
				
				return (b0.compareTo(b1));
			}

    }

}
