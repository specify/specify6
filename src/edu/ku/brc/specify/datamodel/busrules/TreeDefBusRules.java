/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.BaseBusRules;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.validation.ValComboBox;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 1, 2008
 *
 */
public class TreeDefBusRules extends BaseBusRules
{
    protected int                   origDirection = -1;
    protected TreeDefIface<?, ?, ?> cachedTreeDef = null;
    
    /**
     * 
     */
    public TreeDefBusRules()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        ValComboBox fnDirCBX = (ValComboBox)formViewObj.getControlByName("fnDirCBX");
        if (fnDirCBX != null)
        {
            DefaultComboBoxModel model = (DefaultComboBoxModel)fnDirCBX.getModel();
            model.addElement(UIRegistry.getResourceString("TTV_FORWARD"));
            model.addElement(UIRegistry.getResourceString("TTV_REVERSE"));
        
            fnDirCBX.getComboBox().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0)
                {
                    formViewObj.getValidator().setHasChanged(true);
                    formViewObj.getValidator().validateForm();
                    
                    checkForNumOfChanges();
                    
                    /*
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            checkForNumOfChanges();
                        }
                    });*/
                }
            });
        }
    }
    
    /**
     * 
     */
    protected void checkForNumOfChanges()
    {
        ValComboBox fnDirCBX = (ValComboBox)formViewObj.getControlByName("fnDirCBX");
        if (fnDirCBX != null)
        {
            int newDir = fnDirCBX.getComboBox().getSelectedIndex() == 0 ? TreeDefIface.FORWARD : TreeDefIface.REVERSE;
            if (newDir == origDirection)
            {
                return;
            }
        }
        
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                DataProviderSessionIFace session = null;
                try
                {
                    Class<?> dataClass = cachedTreeDef.getNodeClass();
                    System.out.println(dataClass.getName());
                    String      sqlStr = "SELECT COUNT(id) FROM " + dataClass.getName();
                    DBTableInfo ti     = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
                    sqlStr = sqlStr + " WHERE " + QueryAdjusterForDomain.getInstance().getSpecialColumns(ti, true);
                    session = DataProviderFactory.getInstance().createSession();
                    Integer count = (Integer)session.getData(sqlStr);
                    
                    return count;
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                return null;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public void finished()
            {
                Object retVal = getValue();
                
                
                ///TreeHelper.f
                System.out.println(retVal);
            }
        };
        
        // start the background task
        workerThread.start();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (dataObj != null)
        {
            ValComboBox fnDirCBX = (ValComboBox)formViewObj.getControlByName("fnDirCBX");
            if (fnDirCBX != null)
            {
                TreeDefIface<?, ?, ?> treeDef = (TreeDefIface<?, ?, ?>)dataObj;
                origDirection = treeDef.getFullNameDirection();
                fnDirCBX.getComboBox().setSelectedIndex(origDirection == TreeDefIface.FORWARD ? 0 : 1);
                cachedTreeDef = treeDef;
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj)
    {
        ValComboBox fnDirCBX = (ValComboBox)formViewObj.getControlByName("fnDirCBX");
        if (fnDirCBX != null)
        {
            int newDir = fnDirCBX.getComboBox().getSelectedIndex() == 0 ? TreeDefIface.FORWARD : TreeDefIface.REVERSE;
            if (newDir != origDirection)
            {
                TreeDefIface<?, ?, ?> treeDef = (TreeDefIface<?, ?, ?>)dataObj;
                treeDef.setFullNameDirection(newDir);
            }
        }
        return super.afterSaveCommit(dataObj);
    }
    
}
