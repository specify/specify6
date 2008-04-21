/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.persist.ViewIFace;

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
        
        // Add only the unused forms
        List<String>                 uniqueList    = new Vector<String>();
        List<ViewIFace>              views         = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getAllViews();
        Hashtable<String, ViewIFace> newAvailViews = new Hashtable<String, ViewIFace>();
        for (ViewIFace view : views)
        {
            if (hash.get(view.getName()) == null)
            {
                hash.put(view.getName(), view);
                uniqueList.add(view.getTitle());
                newAvailViews.put(view.getTitle(), view);
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
        
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>((Frame)UIRegistry.getTopWindow(),
                getResourceString("DET_AVAIL_VIEWS"), uniqueList);
        
        dlg.setUseScrollPane(true);
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            model = list.getModel();
            
            for (String name : dlg.getSelectedObjects())
            {
                ViewIFace     view = newAvailViews.get(name);
                DBTableInfo   ti   = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                DataEntryView dev  = new DataEntryView(view.getObjTitle(), 
                                                       view.getName(), 
                                                       ti != null ? ti.getName() : null, 
                                                       view.getObjTitle(), 
                                                       model.getSize(),
                                                       true);
                
                ((DefaultListModel)model).addElement(dev);
                itemList.add(dev);
            }
            //pack();
        }
        setHasChanged(true);
    }

}
