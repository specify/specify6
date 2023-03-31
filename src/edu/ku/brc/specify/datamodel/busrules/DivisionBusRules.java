/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.SpecifyDBSetupWizard;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.dbsupport.HibernateDataProviderSession;
import edu.ku.brc.specify.dbsupport.SpecifyDeleteHelper;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 1, 2008
 *
 */
public class DivisionBusRules extends BaseBusRules implements CommandListener
{
    private static final String CMD_TYPE = "DivisionBusRules"; 
    private static final String PROGRESS = "progress"; 
    
    private boolean       isOKToCont    = false;

    /**
     * @param dataClasses
     */
    public DivisionBusRules()
    {
        super(Institution.class);
        CommandDispatcher.register(CMD_TYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null && formViewObj.getMVParent().isTopLevel())
        {
            ResultSetController rsc = formViewObj.getRsController();
            if (rsc != null)
            {
                if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
            }
        }
        
        if (formViewObj != null)
        {
            if (formViewObj.getMVParent().isTopLevel())
            {
                ResultSetController rsc = formViewObj.getRsController();
                if (rsc != null)
                {
                    if (rsc.getNewRecBtn() != null) rsc.getNewRecBtn().setVisible(false);
                    if (rsc.getDelRecBtn() != null) rsc.getDelRecBtn().setVisible(false);
                }
            }
            if (formViewObj.getRsController() != null)
            {
                JButton newBtn = formViewObj.getRsController().getNewRecBtn();
                if (newBtn != null)
                {
                    // Remove all ActionListeners, there should only be one
                    for (ActionListener al : newBtn.getActionListeners())
                    {
                        newBtn.removeActionListener(al);
                    }
                    
                    newBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            addNewDivision();
                        }
                    });
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeFormFill()
     */
    @Override
    public void beforeFormFill()
    {
        super.beforeFormFill();

        if (formViewObj != null && formViewObj.getDataObj() != null && formViewObj.getMVParent().isTopLevel())
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
    
                Division div = (Division)formViewObj.getDataObj();
                session.attach(div);
                for (Discipline disp : div.getDisciplines())
                {
                    disp.getCollections().size();
                }
                
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
        }
    }
    
    /**
     * @return
     */
    public static boolean askForExitonChange(final String messageKey)
    {
        int userChoice = JOptionPane.NO_OPTION;
        Object[] options = { getResourceString("Continue"),  //$NON-NLS-1$
                             getResourceString("CANCEL")  //$NON-NLS-1$
              };

        userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                     getResourceString(messageKey),  //$NON-NLS-1$
                                                     getResourceString("EXIT_REQ_TITLE"),  //$NON-NLS-1$
                                                     JOptionPane.YES_NO_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        return userChoice == JOptionPane.YES_OPTION;
    }
    
    /**
     * @param name
     * @return
     */
    private int getNameCount(final String name)
    {
        return BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM division WHERE Name = '%s'", name));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#isOkToSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean isOkToSave(final Object dataObj, final DataProviderSessionIFace session)
    {
        if (formViewObj != null)
        {
            Component comp = formViewObj.getControlByName("name");
            if (comp instanceof ValTextField)
            {
                Division division = (Division)formViewObj.getDataObj();
                Integer  divId    = division.getId();
                
                String name = ((ValTextField)comp).getText();
                int cnt = getNameCount(name);
                if (cnt == 0 || (cnt == 1 && divId != null))
                {
                    return true;
                }
               reasonList.add(UIRegistry.getLocalizedMessage("DIVNAME_DUP", name));
            }
        }
        return false;
    }
    
    /**
     * @param formViewObj
     * @param tblId
     * @return
     */
    public static boolean checkForParentSave(final FormViewObj formViewObj, final int tblId)
    {
        if (formViewObj != null && formViewObj.getMVParent() != null)
        {
            MultiView mv = formViewObj.getMVParent();
            if (mv.getTopLevel() != null)
            {
                if (mv.getTopLevel().getCurrentValidator().hasChanged())
                {
                    UIRegistry.showLocalizedError(JOptionPane.WARNING_MESSAGE, "TOP_MV_SAVE", DBTableIdMgr.getInstance().getTitleForId(Institution.getClassTableId()));
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 
     */
    private void addNewDivision()
    {
        if (!checkForParentSave(formViewObj, Institution.getClassTableId()))
        {
            return;
        }
        
        if (!askForExitonChange("ASK_TO_ADD_DIV"))
        {
            return;
        }
        UIRegistry.writeSimpleGlassPaneMsg(UIRegistry.getResourceString("BUILD_DIV"), 20); // I18N
        isOKToCont = true;
        final AppContextMgr acm = AppContextMgr.getInstance();
        
        UIRegistry.loadAndPushResourceBundle("specifydbsetupwiz");
        
        final SpecifyDBSetupWizard wizardPanel = new SpecifyDBSetupWizard(SpecifyDBSetupWizard.WizardType.Division, null);
        
        String bldTitle = UIRegistry.getResourceString("CREATEDIV");
        final CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "", true, CustomDialog.NONE_BTN, wizardPanel);
        dlg.setCustomTitleBar(bldTitle);
        wizardPanel.setListener(new SpecifyDBSetupWizard.WizardListener() {
            @Override
            public void cancelled()
            {
                isOKToCont = false;
                dlg.setVisible(false);
            }
            @Override
            public void finished()
            {
                dlg.setVisible(false);
            }
            @Override
            public void hide()
            {
                dlg.setVisible(false);
            }
            @Override
            public void panelChanged(String title)
            {
                dlg.setTitle(title);
            }
            @Override
            public void helpContextChanged(String helpTarget)
            {
            	
            }
        });
        dlg.setVisible(true);
        
        UIRegistry.popResourceBundle();
        
        if (!isOKToCont)
        {
            UIRegistry.clearSimpleGlassPaneMsg();
            return;
        }
        
        wizardPanel.processDataForNonBuild();
        
        final BuildSampleDatabase bldSampleDB   = new BuildSampleDatabase();
        final ProgressFrame       progressFrame = bldSampleDB.createProgressFrame(bldTitle);
        progressFrame.turnOffOverAll();
        
        progressFrame.setProcess(0, 17);
        progressFrame.setProcessPercent(true);
        progressFrame.getCloseBtn().setVisible(false);
        progressFrame.setAlwaysOnTop(true);
        progressFrame.adjustProgressFrame();
        
        UIHelper.centerAndShow(progressFrame);
        
        SwingWorker<Integer, Integer> bldWorker = new SwingWorker<Integer, Integer>()
        {
            private int steps = 0;
            private Division newDivision = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @SuppressWarnings("cast")
            @Override
            protected Integer doInBackground() throws Exception
            {
                firePropertyChange(PROGRESS, 0, ++steps);
                
                bldSampleDB.setDataType(acm.getClassObject(DataType.class));
                
                Division   curDivCached  = acm.getClassObject(Division.class);
                Discipline curDispCached = acm.getClassObject(Discipline.class);
                Collection curCollCached = acm.getClassObject(Collection.class);
                
                acm.setClassObject(Division.class, null);
                acm.setClassObject(Discipline.class, null);
                acm.setClassObject(Collection.class, null);
                
                // I don't think this is necessary
                // rods - 08/27/09
                //UIFieldFormatterMgr.getInstance().reset();
                //DataObjFieldFormatMgr.getInstance().reset();
                
                Session session = null;
                try
                {
                    session = HibernateUtil.getNewSession();
                    DataProviderSessionIFace hSession = new HibernateDataProviderSession(session);
                    
                    Institution    inst             = (Institution)formViewObj.getMVParent().getMultiViewParent().getData(); 
                    Institution    institution      = hSession.get(Institution.class, inst.getId());
                    SpecifyUser    specifyAdminUser = (SpecifyUser)acm.getClassObject(SpecifyUser.class);
                    Properties     props            = wizardPanel.getProps();
                    
                    institution      = (Institution)session.merge(institution);
                    specifyAdminUser = (SpecifyUser)hSession.getData("FROM SpecifyUser WHERE id = "+specifyAdminUser.getId());
                    
                    bldSampleDB.setSession(session);
                    
                    DisciplineType dispType = (DisciplineType)props.get("disciplineType");
                    newDivision = bldSampleDB.createEmptyDivision(institution, dispType, specifyAdminUser, props, true, false, true);
                    
                    acm.setClassObject(Division.class, curDivCached);
                    acm.setClassObject(Discipline.class, curDispCached);
                    acm.setClassObject(Collection.class, curCollCached);

                    
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
                
                bldSampleDB.setDataType(null);
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                progressFrame.setVisible(false);
                progressFrame.dispose();
                
               if (newDivision != null)
               {
                   List<?> dataItems = null;
                   
                   FormViewObj instFVO   = formViewObj.getMVParent().getMultiViewParent().getCurrentViewAsFormViewObj();
                   Institution    inst = (Institution)instFVO.getDataObj();
                   DataProviderSessionIFace pSession = null;
                   try
                   {
                       pSession = DataProviderFactory.getInstance().createSession();
                       
                       inst = (Institution)pSession.getData("FROM Institution WHERE id = "+inst.getId());
                       inst.forceLoad();
                       
                       acm.setClassObject(Institution.class, inst);
                       
                       dataItems = pSession.getDataList("FROM Institution");
                       if (dataItems.get(0) instanceof Object[])
                       {
                           Vector<Object>dataList = new Vector<Object>();
                           for (Object row : dataItems)
                           {
                               Object[] cols = (Object[])row;
                               Institution div = (Institution)cols[0];
                               div.forceLoad();
                               dataList.add(div);
                           }
                           dataItems = dataList;
                       }
                       
                   } catch (Exception ex)
                   {
                       System.err.println(ex);
                       ex.printStackTrace();
                       
                   } finally
                   {
                       if (pSession != null)
                       {
                           pSession.close();
                       }
                   }
                   
                   // Not needed now that we are exiting
                   //int curInx = instFVO.getRsController().getCurrentIndex();
                   //instFVO.setDataObj(dataItems);
                   //instFVO.getRsController().setIndex(curInx);
                   
               } else
               {
                   // error creating
               }
               UIRegistry.clearSimpleGlassPaneMsg();
               
               UIRegistry.showLocalizedMsg("Specify.ABT_EXIT");
               CommandDispatcher.dispatch(new CommandAction(BaseTask.APP_CMD_TYPE, BaseTask.APP_REQ_EXIT));

            }
        };
        
        bldWorker.execute();

    }
    

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#okToEnableDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#processBusinessRules(java.lang.Object)
     */
    @Override
    public STATUS processBusinessRules(final Object dataObj)
    {
        reasonList.clear();
        
        if (!(dataObj instanceof Division))
        {
            return STATUS.Error;
        }
        
        STATUS nameStatus = isCheckDuplicateNumberOK("name", 
                                                      (FormDataObjIFace)dataObj, 
                                                      Division.class, 
                                                      "userGroupScopeId");
        
        return nameStatus != STATUS.OK ? STATUS.Error : STATUS.OK;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object                     dataObj,
                           final DataProviderSessionIFace   session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        if (deletable != null)
        {
            Division division = (Division)dataObj;
            
            Integer id = division.getId();
            if (id != null)
            {
                boolean  cont    = false;
                Division currDiv = AppContextMgr.getInstance().getClassObject(Division.class);
                if (currDiv.getId().equals(division.getId()))
                {
                    UIRegistry.showError("You cannot delete the current Division."); // I18N
                    
                } else
                {
                    String sql = "SELECT count(*) FROM agent WHERE DivisionID = " + division.getId();
                    int count = BasicSQLUtils.getCount(sql);
                    if (count > 0)
                    {
                        String msg = String.format("There are too many agents associated with this the `%s` Division.\nDo you wish to delete them?", division.getName());
                        //UIRegistry.showError(); // I18N
                        if (UIRegistry.askYesNoLocalized("DELETE", "CANCEL", msg, "DELETE") == JOptionPane.YES_OPTION)
                        {
                            cont = true;
                        }
                    } else
                    {
                        cont = true;
                    }
                }
                    
                if (cont)
                {
                    final Integer divId = division.getId();
                    final SpecifyDeleteHelper delHelper = new SpecifyDeleteHelper();
                    
                    SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
                    {
                        /* (non-Javadoc)
                         * @see javax.swing.SwingWorker#doInBackground()
                         */
                        @Override
                        protected Integer doInBackground() throws Exception
                        {
                            try
                            {
                                delHelper.delRecordFromTable(Division.class, divId, true);
                                delHelper.done(false);
                                
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DivisionBusRules.class, ex);
                            }
                            return null;
                        }

                        /* (non-Javadoc)
                         * @see javax.swing.SwingWorker#done()
                         */
                        @Override
                        protected void done()
                        {
                            super.done();
                            
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                 // This is called instead of calling 'okToDelete' because we had the SpecifyDeleteHelper
                                    // delete the actual dataObj and now we tell the form to remove the dataObj from
                                    // the form's list and them update the controller appropriately
                                    if (formViewObj != null)
                                    {
                                        formViewObj.updateAfterRemove(true); // true removes item from list and/or set
                                    }
                                    
                                    UIRegistry.showLocalizedMsg("Specify.ABT_EXIT");
                                    CommandDispatcher.dispatch(new CommandAction(BaseTask.APP_CMD_TYPE, BaseTask.APP_REQ_EXIT));
                                }
                            });
                        }
                    };
                    String title = String.format("%s %s", getResourceString("DELETING"), DBTableIdMgr.getInstance().getTitleForId(Division.getClassTableId()));
                    JDialog dlg = delHelper.initProgress(worker, title);

                    worker.execute();
                    
                    UIHelper.centerAndShow(dlg);
                }
            } else
            {
                super.okToDelete(dataObj, session, deletable);
            }
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(Object dataObj, DataProviderSessionIFace session)
    {
        AppContextMgr.getInstance().setClassObject(Division.class, dataObj);
        
        return super.afterSaveCommit(dataObj, session);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isAction("DivisionSaved"))
        {
            Division division = (Division)cmdAction.getData();
            formViewObj.getMVParent().getMultiViewParent().setData(division);
            
        } else if (cmdAction.isAction("DivisionError"))
        {
        }
        
    }

}
