package edu.ku.brc.specify.ui.db;
import javax.swing.table.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class WorkbenchTableModel extends AbstractTableModel {
	private static Log log = LogFactory.getLog(WorkbenchTableModel.class);
	public WorkbenchTableModel(){
		super();
		// TODO Auto-generated constructor stub
	}
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 10;
	}
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 1000;
	}
	public Object getValueAt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
