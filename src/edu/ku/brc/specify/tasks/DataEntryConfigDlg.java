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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 17, 2008
 *
 */
public class DataEntryConfigDlg extends TaskConfigureDlg
{

    /**
     * @param stdList
     * @param miscList
     * @param includeAddPanels
     * @param helpContext
     * @param titleKey
     * @param stdTitle
     * @param miscTitle
     * @param mvRightTTKey
     * @param mvLeftTTKey
     */
    public DataEntryConfigDlg(Vector<TaskConfigItemIFace> stdList,
                              Vector<TaskConfigItemIFace> miscList, 
                              boolean includeAddPanels, 
                              String helpContext,
                              String titleKey, 
                              String stdTitle, 
                              String miscTitle, 
                              String mvRightTTKey,
                              String mvLeftTTKey)
    {
        super(stdList, miscList, includeAddPanels, helpContext, titleKey, stdTitle, miscTitle,
              mvRightTTKey, mvLeftTTKey);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.TaskConfigureDlg#addItem(javax.swing.JList)
     */
    @Override
    protected void addItem(final JList list, final Vector<TaskConfigItemIFace> itemList)
    {
        // Hash all the names so we can figure out which forms are not used
        Hashtable<String, Object> hash = new Hashtable<String, Object>();
        ListModel model = stdPanel.getOrderModel();
        for (int i=0;i<model.getSize();i++)
        {
            DataEntryView dev = (DataEntryView)model.getElementAt(i);
            hash.put(dev.getView(), dev);
        }
        
        model = miscPanel.getOrderModel();
        for (int i=0;i<model.getSize();i++)
        {
            DataEntryView dev = (DataEntryView)model.getElementAt(i);
            hash.put(dev.getView(), dev);
        }
        
        // Add only the unused forms (does NOT return internal views).
        List<String>                 uniqueList    = new Vector<String>();
        List<ViewIFace>              views         = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getAllViews();
        Hashtable<String, ViewIFace> newAvailViews = new Hashtable<String, ViewIFace>();
        for (ViewIFace view : views)
        {
            //System.out.println("["+view.getName()+"]["+view.getTitle()+"]");
            
            if (hash.get(view.getName()) == null)
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                if (ti != null)
                {
                    if (!ti.isHidden() && !InteractionsTask.isInteractionTable(ti.getTableId()))
                    {
                        hash.put(view.getName(), view);
                        String title = StringUtils.isNotEmpty(view.getObjTitle()) ? view.getObjTitle() : ti != null ? ti.getTitle() : view.getName();
                        if (newAvailViews.get(title) != null)
                        {
                            title = view.getName();
                        }
                        uniqueList.add(title);
                        newAvailViews.put(title, view);
                    }
                } else
                {
                    System.err.println("DBTableInfo was null for class["+view.getClassName()+"]");
                }
            }
        }
        
        if (uniqueList.size() == 0)
        {
            JOptionPane.showMessageDialog(this, 
                                          getResourceString("DET_DEV_NONE_AVAIL"), 
                                          getResourceString("DET_DEV_NONE_AVAIL_TITLE"), 
                                          JOptionPane.INFORMATION_MESSAGE); 
            return;
        }
        
        Collections.sort(uniqueList);
        
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>((Frame)UIRegistry.getTopWindow(),
                "DET_AVAIL_VIEWS", uniqueList);
        
        dlg.setUseScrollPane(true);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            model = list.getModel();
            
            for (String title : dlg.getSelectedObjects())
            {
                ViewIFace     view = newAvailViews.get(title);
                DBTableInfo   ti   = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                
                String frmTitle = StringUtils.isNotEmpty(view.getObjTitle()) ? view.getObjTitle() : ti != null ? ti.getTitle() : view.getName();
                DataEntryView dev  = new DataEntryView(frmTitle,                         // Title 
                                                       view.getName(),                   // Name
                                                       ti != null ? ti.getName() : null, // Icon Name
                                                       view.getObjTitle(),               // ToolTip
                                                       model.getSize(),                  // Order
                                                       true);
                dev.setTableInfo(ti);
                ((DefaultListModel)model).addElement(dev);
                itemList.add(dev);
            }
            //pack();
        }
        setHasChanged(true);
    }

}
