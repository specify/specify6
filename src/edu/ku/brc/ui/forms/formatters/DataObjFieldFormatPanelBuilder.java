package edu.ku.brc.ui.forms.formatters;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableInfo;

public abstract class DataObjFieldFormatPanelBuilder {

	protected DBTableInfo tableInfo;
	protected PanelBuilder mainPanelBuilder;

	public abstract void fillWithObjFormatter(DataObjSwitchFormatter fmt);


	public DataObjFieldFormatPanelBuilder(DBTableInfo tableInfo) {
		super();

		this.tableInfo = tableInfo;
        init();
        buildUI();
	}

	protected void buildUI()
	{
	}

	protected void init() 
	{
	}

	public void enableUIControls() 
	{
	}

	public JPanel getPanel() 
	{
		return mainPanelBuilder.getPanel();
	}

	public PanelBuilder getMainPanelBuilder() 
	{
		return mainPanelBuilder;
	}

}