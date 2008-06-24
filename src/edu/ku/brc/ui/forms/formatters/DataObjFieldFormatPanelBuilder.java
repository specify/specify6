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
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;

import edu.ku.brc.dbsupport.DBTableInfo;

public abstract class DataObjFieldFormatPanelBuilder {

	protected AvailableFieldsComponent 				availableFieldsComp;
	protected DataObjSwitchFormatterContainerIface 	formatContainer;
	protected PanelBuilder 							mainPanelBuilder;
	protected DBTableInfo  							tableInfo;
	protected JButton								okButton;
	protected boolean 								newFormat;

	protected UIFieldFormatterMgr 					uiFieldFormatterMgrCache;

	public abstract void fillWithObjFormatter(DataObjSwitchFormatter fmt);
	
	public DataObjFieldFormatPanelBuilder(DBTableInfo 							tableInfo,
										  AvailableFieldsComponent 				availableFieldsComp,
										  DataObjSwitchFormatterContainerIface 	formatContainer,
										  JButton 								okButton,
										  UIFieldFormatterMgr 					uiFieldFormatterMgrCache) 
	{
		super();

		if (formatContainer == null)
			throw new RuntimeException("Cannot instantiate data obj format panel builder with null format container.");
		
		this.uiFieldFormatterMgrCache 	= uiFieldFormatterMgrCache;
		this.tableInfo           		= tableInfo;
		this.availableFieldsComp 		= availableFieldsComp;
		this.formatContainer     		= formatContainer;
		this.okButton            		= okButton;
		this.newFormat           		= false;
		
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
		okButton.setEnabled(true);
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