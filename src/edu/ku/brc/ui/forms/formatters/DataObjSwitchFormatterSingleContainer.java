package edu.ku.brc.ui.forms.formatters;

public class DataObjSwitchFormatterSingleContainer implements DataObjSwitchFormatterContainerIface
{
	protected DataObjSwitchFormatter formatter;

	public DataObjSwitchFormatterSingleContainer(DataObjSwitchFormatter formatter)
	{
		this.formatter = formatter;
	}
	
	public DataObjSwitchFormatter getSelectedFormatter()
	{
		return formatter;
	}

}
