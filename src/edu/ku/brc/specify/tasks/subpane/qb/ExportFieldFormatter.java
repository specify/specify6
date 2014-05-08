/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Vector;

import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.util.Pair;

/**
 * @author Administrator
 *
 *Formatters for export to MySQL
 */
public abstract class ExportFieldFormatter implements UIFieldFormatterIFace
{
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#byYearApplies()
	 */
	@Override
	public boolean byYearApplies()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#formatFromUI(java.lang.Object)
	 */
	@Override
	public Object formatFromUI(Object data)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#formatToUI(java.lang.Object[])
	 */
	@Override
	public Object formatToUI(Object... data)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getAutoNumber()
	 */
	@Override
	public AutoNumberIFace getAutoNumber()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getByYear()
	 */
	@Override
	public boolean getByYear()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getDataClass()
	 */
	@Override
	public Class<?> getDataClass()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getDateWrapper()
	 */
	@Override
	public DateWrapper getDateWrapper()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getFieldName()
	 */
	@Override
	public String getFieldName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getFields()
	 */
	@Override
	public Vector<UIFieldFormatterField> getFields()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getIncPosition()
	 */
	@Override
	public Pair<Integer, Integer> getIncPosition()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getLength()
	 */
	@Override
	public int getLength()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setLength(int)
	 */
	@Override
	public void setLength(int length) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#resetLength()
     */
    @Override
    public void resetLength()
    {
    }

    /* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getMaxValue()
	 */
	@Override
	public Number getMaxValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getMinValue()
	 */
	@Override
	public Number getMinValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getName()
	 */
	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getNextNumber(java.lang.String)
	 */
	@Override
	public String getNextNumber(String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getNextNumber(java.lang.String, boolean)
	 */
	@Override
	public String getNextNumber(String value, boolean incrementValue) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getPartialDateType()
	 */
	@Override
	public PartialDateEnum getPartialDateType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getSample()
	 */
	@Override
	public String getSample()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getTitle()
	 */
	@Override
	public String getTitle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getUILength()
	 */
	@Override
	public int getUILength()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getYear()
	 */
	@Override
	public UIFieldFormatterField getYear()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#getYearPosition()
	 */
	@Override
	public Pair<Integer, Integer> getYearPosition()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isDate()
	 */
	@Override
	public boolean isDate()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isDefault()
	 */
	@Override
	public boolean isDefault()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isFromUIFormatter()
	 */
	@Override
	public boolean isFromUIFormatter()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isInBoundFormatter()
	 */
	@Override
	public boolean isInBoundFormatter()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isIncrementer()
	 */
	@Override
	public boolean isIncrementer()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isLengthOK(int)
	 */
	@Override
	public boolean isLengthOK(int lengthOfData)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isNumeric()
	 */
	@Override
	public boolean isNumeric()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isSystem()
	 */
	@Override
	public boolean isSystem()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isUserInputNeeded()
	 */
	@Override
	public boolean isUserInputNeeded()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setAutoNumber(edu.ku.brc.af.core.db.AutoNumberIFace)
	 */
	@Override
	public void setAutoNumber(AutoNumberIFace autoNumber)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setByYear(boolean)
	 */
	@Override
	public void setByYear(boolean byYear)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setDataClass(java.lang.Class)
	 */
	@Override
	public void setDataClass(Class<?> dataClass)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setDefault(boolean)
	 */
	@Override
	public void setDefault(boolean isDefault)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setFieldName(java.lang.String)
	 */
	@Override
	public void setFieldName(String fieldName)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setIncrementer(boolean)
	 */
	@Override
	public void setIncrementer(boolean isIncrementer)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setName(java.lang.String)
	 */
	@Override
	public void setName(String name)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setPartialDateType(edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.PartialDateEnum)
	 */
	@Override
	public void setPartialDateType(PartialDateEnum partialDateType)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#setType(edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace.FormatterType)
	 */
	@Override
	public void setType(FormatterType type)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#toPattern()
	 */
	@Override
	public String toPattern()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#toXML(java.lang.StringBuilder)
	 */
	@Override
	public void toXML(StringBuilder sb)
	{
		// TODO Auto-generated method stub
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace#hasDash()
     */
    @Override
    public boolean hasDash()
    {
        return false;
    }
}
