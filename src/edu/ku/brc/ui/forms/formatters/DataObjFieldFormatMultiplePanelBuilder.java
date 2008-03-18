package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableInfo;

public class DataObjFieldFormatMultiplePanelBuilder extends DataObjFieldFormatPanelBuilder {

	protected JTable formatSwitch;
	
	DataObjFieldFormatMultiplePanelBuilder(DBTableInfo tableInfo)
	{
		super(tableInfo);
	}
	
	protected void buildUI() 
	{
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:130px:g,10px,p,15px")/*, new FormDebugPanel()*/);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Field Value");
        tableModel.addColumn("Display Format");
        tableModel.addRow(new String[] {"1", "Last Name, First Name"});
        tableModel.addRow(new String[] {"2", "Last Name, First Name"});
        tableModel.addRow(new String[] {"3", "Last Name, First Name"});
        formatSwitch = new JTable(tableModel);
        JScrollPane formatSwitchScroll = new JScrollPane(formatSwitch);
        
        // delete button
        PanelBuilder deletePB = new PanelBuilder(new FormLayout("l:p", "p"));  
        JButton deleteBtn = new JButton(getResourceString("FFE_DELETE")); 
        deleteBtn.setEnabled(false);
        //deleteBtn.addActionListener(deleteListener);
        deletePB.add(deleteBtn);

        // lay out components on main panel
        pb.add(formatSwitchScroll, cc.xy(1, 1));
        pb.add(deletePB.getPanel(), cc.xy(1, 3));
        this.mainPanelBuilder = pb;
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
	
	public void fillWithObjFormatter(DataObjSwitchFormatter switchFormatter)
	{ 
    	// display each formatter as a table row
    	//DefaultTableModel tableModel = (DefaultTableModel) formatSwitch.getModel();
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Field Value");
		model.addColumn("Display Format");

		Vector<DataObjDataFieldFormatIFace> formatters;
		formatters = new Vector<DataObjDataFieldFormatIFace>(switchFormatter.getFormatters());
        Collections.sort(formatters, new Comparator<DataObjDataFieldFormatIFace>() {
            public int compare(DataObjDataFieldFormatIFace o1, DataObjDataFieldFormatIFace o2)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
    	for (DataObjDataFieldFormatIFace formatter : formatters)
    	{
    		model.addRow(new Object[] {formatter.getValue(), formatter} );
    	}
		formatSwitch.setModel(model);
		formatSwitch.invalidate();
	}
}
