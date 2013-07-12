/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;

/**
 * 
 * @author Ricardo
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 5, 2008
 *
 */
public class DataObjFieldFormatSingleDlg extends CustomDialog implements ChangeListener
{
    protected DBTableInfo                          tableInfo;
    protected DataObjSwitchFormatter               formatter;
    protected DataObjFieldFormatSinglePanel        fmtSingleEditingPanel;
    
    protected DataObjFieldFormatMgr                dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr                  uiFieldFormatterMgrCache;
    
    /**
     * @throws HeadlessException
     */
    public DataObjFieldFormatSingleDlg(final Frame                       frame, 
                                       final DBTableInfo                 tableInfo,
                                       final DataObjDataFieldFormatIFace singleFormatter,
                                       final DataObjFieldFormatMgr       dataObjFieldFormatMgrCache,
                                       final UIFieldFormatterMgr         uiFieldFormatterMgrCache)
        throws HeadlessException
    {
        super(frame, getResourceString("DOF_DLG_TITLE"), true, OKCANCELHELP, null);
        
        this.tableInfo                  = tableInfo;
        this.formatter                  = new DataObjSwitchFormatter("", "", true, false, tableInfo.getClassObj(), "");
        this.uiFieldFormatterMgrCache   = uiFieldFormatterMgrCache;
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.helpContext                = "DOF_SINGLE_FF";
        this.formatter.setSingle(singleFormatter);
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
        DataObjSwitchFormatterContainerIface fmtContainer = new DataObjSwitchFormatterSingleContainer(formatter);
        fmtSingleEditingPanel = new DataObjFieldFormatSinglePanel(tableInfo, fmtContainer, 
                dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, this, getOkBtn());
        pb.add(fmtSingleEditingPanel);
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        okBtn.setEnabled(false);
        
        pack();
    }
    
    /**
     * @return
     */
    public DataObjDataFieldFormatIFace getSingleFormatter()
    {
        return formatter.getSingle();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        okBtn.setEnabled(!fmtSingleEditingPanel.isInError());
    }
}
