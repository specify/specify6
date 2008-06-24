package edu.ku.brc.ui.forms.formatters;

import javax.swing.JList;

public class DataObjSwitchFormatterListContainer implements DataObjSwitchFormatterContainerIface
{
	protected JList formatList;
	
	public DataObjSwitchFormatterListContainer(JList formatList)
	{
		this.formatList = formatList;
	}
	
	public DataObjSwitchFormatter getSelectedFormatter()
	{
		if (formatList.getSelectedIndex() == -1)
			return null;
		
		Object value = formatList.getSelectedValue(); 
		if (!(value instanceof DataObjSwitchFormatter))
			return null;
		
		return (DataObjSwitchFormatter) value;
	}
}
