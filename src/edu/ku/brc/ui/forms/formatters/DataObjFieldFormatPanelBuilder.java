/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.ui.forms.formatters;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;

import edu.ku.brc.dbsupport.DBTableInfo;

public abstract class DataObjFieldFormatPanelBuilder {

	protected PanelBuilder 			mainPanelBuilder;
	protected DBTableInfo  			tableInfo;
	protected JList 	   			formatList;
	protected ListSelectionListener formatListSL;
	protected JButton				okButton;
	protected boolean 				newFormat;

	public abstract void fillWithObjFormatter(DataObjSwitchFormatter fmt);
	public abstract DataObjSwitchFormatter getSwitchFormatter();
	
	public DataObjFieldFormatPanelBuilder(DBTableInfo tableInfo, 
									      JList formatList,
									      ListSelectionListener formatListSL,
									      JButton okButton) 
	{
		super();

		this.tableInfo = tableInfo;
		this.formatList = formatList;
		this.formatListSL = formatListSL;
		this.okButton = okButton;
		
		this.newFormat = false;
		
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

	public boolean isNewFormat()
	{
		return newFormat;
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