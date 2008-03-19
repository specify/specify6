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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;

/*
 * XXX
 */
public class DataObjFieldFormatSingleDlg extends CustomDialog 
{
	protected DBTableInfo tableInfo;
	protected DataObjDataFieldFormatIFace formatter;
	protected DataObjFieldFormatSinglePanelBuilder fmtSingleEditingPB;

	/**
     * @throws HeadlessException
     */
    public DataObjFieldFormatSingleDlg(Frame frame, 
    								   DBTableInfo tableInfo, 
    								   DataObjDataFieldFormatIFace formatter)
    	throws HeadlessException
    {
        super(frame, getResourceString("FFE_DLG_TITLE"), true, OKCANCELHELP, null); //I18N 
        this.tableInfo = tableInfo;
        this.formatter = formatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        //CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p", "p")/*, new FormDebugPanel()*/);
        
        // format editing panel (single format only)
        fmtSingleEditingPB = new DataObjFieldFormatSinglePanelBuilder(tableInfo, null, null, formatter);
        pb.add(fmtSingleEditingPB.getPanel());
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
    }
    
	public DataObjSwitchFormatter getSwitchFormatter()
	{
		return fmtSingleEditingPB.getSwitchFormatter();
	}
}
