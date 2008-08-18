package edu.ku.brc.af.ui.forms.formatters;

/** 
 * This interface is used to adapt format edited by the DataObjFieldFormatSinglePanelBuilder. 
 * That is needed because the formatter being edited may be a one of the formatters in a switch 
 * formatter or one of the formatters from a JList.  
 * 
 * @author Ricardo
 *
 */
public interface DataObjSwitchFormatterContainerIface
{
	/**
	 * @return
	 */
	public DataObjSwitchFormatter getSelectedFormatter();
}
