/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createButton;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.DataSetterForObj;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * (NOT IN USE)
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public class SetupDivsionCollection extends JDialog
{
    //private static final Logger log = Logger.getLogger(SetupDivsionCollection.class);
    
    protected Properties             props = new Properties();
    
    protected JButton                helpBtn;
    protected JButton                backBtn;
    protected JButton                nextBtn;
    protected JButton                cancelBtn;
    
    protected int                    step     = 0;
    protected int                    lastStep = 3;
    
    protected boolean                isCancelled;
    protected JPanel                 cardPanel;
    protected CardLayout             cardLayout = new CardLayout();
    protected Vector<SetupPanelIFace> panels     = new Vector<SetupPanelIFace>();
    
    protected String                 setupXMLPath;
    protected Collection             collection = null;
    
    /**
     * @param specify
     */
    public SetupDivsionCollection(final Division division)
    {
        super();
        
        setModal(true);
        
        props.put("division", division);
        
        setTitle("Configuring"); // I18N
        cardPanel = new JPanel(cardLayout);
        
        
        cancelBtn  = createButton(UIRegistry.getResourceString("CANCEL"));
        helpBtn    = createButton(UIRegistry.getResourceString("HELP"));
        
        JPanel btnBar;
        backBtn    = createButton(UIRegistry.getResourceString("BACK"));
        nextBtn    = createButton(UIRegistry.getResourceString("NEXT"));
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
        
        HelpMgr.registerComponent(helpBtn, "ConfiguringDatabase");
        CellConstraints cc = new CellConstraints();
        
        if (true)
        {
            PanelBuilder bbpb = new PanelBuilder(new FormLayout("f:p:g,p,4px,p,4px,p,4px,p,4px", "p"));
            bbpb.add(helpBtn, cc.xy(2,1));
            bbpb.add(backBtn, cc.xy(4,1));
            bbpb.add(nextBtn, cc.xy(6,1));
            bbpb.add(cancelBtn, cc.xy(8,1));
            
            btnBar = bbpb.getPanel();
            
        } else
        {
            btnBar = ButtonBarFactory.buildWizardBar(helpBtn, backBtn, nextBtn, cancelBtn);
        }
            
        //Institution inst     = AppContextMgr.getInstance().getClassObject(Institution.class);
        //Division    division = AppContextMgr.getInstance().getClassObject(Division.class);
        //DivisionSetupPanel div1 = new DivisionSetupPanel(nextBtn);
        //panels.add(div1);
        
        /*
        DisciplineSetupPanel dsp1 = new DisciplineSetupPanel(nextBtn);
        panels.add(dsp1);
        
        collection = new Collection();
        collection.initialize();
        FormSetupPanel coll1 = new FormSetupPanel("Cln1", 
                                                     null, 
                                                     "CollectionSetup", 
                                                     Division.class.getName(), 
                                                     true, 
                                                     MultiView.HIDE_SAVE_BTN, 
                                                     collection,
                                                     nextBtn);
        panels.add(coll1);
        
        FormSetupPanel coll2 = new FormSetupPanel("Cln2", 
                null, 
                "CollectionSetupABCD", 
                Division.class.getName(), 
                true, 
                MultiView.HIDE_SAVE_BTN, 
                collection,
                nextBtn);
        panels.add(coll2);
        
        
        NumberingSchemeSetup numSchemePanel = new NumberingSchemeSetup(nextBtn);
        panels.add(numSchemePanel);
        */
        lastStep = panels.size();
        
        if (backBtn != null)
        {
            backBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    if (step > 0)
                    {
                        step--;
                        panels.get(step).doingPrev();
                        cardLayout.show(cardPanel, Integer.toString(step));
                    }
                    updateBtnBar();
                }
            });
            
            backBtn.setEnabled(false);
        }
        
        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (step < lastStep-1)
                {
                    step++;
                    panels.get(step).doingNext();
                    cardLayout.show(cardPanel, Integer.toString(step));
                    updateBtnBar();
                      
                } else
                {
                    setVisible(false);
                    saveCollection();
                    dispose();
                }
            }
        });
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                isCancelled = true;
                setVisible(false);
                dispose();
            }
         });

        DataGetterForObj getter  = new DataGetterForObj();
        DataSetterForObj setter  = new DataSetterForObj();

        //boolean isAllOK = true;
        for (int i=0;i<panels.size();i++)
        {
            SetupPanelIFace panel = panels.get(i);
            cardPanel.add(Integer.toString(i), panel.getUIComponent());
            
            if (panels.get(i) instanceof GenericFormPanel)
            {
                GenericFormPanel p = (GenericFormPanel)panels.get(i);
                p.setGetter(getter);
                p.setSetter(setter);
            }
            
            panel.setValues(props);
        }
        
        panels.get(0).doingNext();
        cardLayout.show(cardPanel, "0");
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p"));
        builder.add(cardPanel, cc.xy(1, 1));
        builder.add(btnBar, cc.xy(1, 3));
        
        builder.setDefaultDialogBorder();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(builder.getPanel());
        
        pack();
        
        nextBtn.setEnabled(false);

    }
    
    protected void updateBtnBar()
    {
        if (step == lastStep-1)
        {
            nextBtn.setEnabled(panels.get(step).isUIValid());
            nextBtn.setText("Finished");
            
        } else
        {
            nextBtn.setEnabled(panels.get(step).isUIValid());
            nextBtn.setText("Next");
        }
        
        backBtn.setEnabled(step > 0); 
    }
    
    
    protected String stripSpecifyDir(final String path)
    {
        String appPath = path;
        int endInx = appPath.indexOf("Specify.app");
        if (endInx > -1)
        {
            appPath = appPath.substring(0, endInx-1);
        }
        return appPath;
    }

    /**
     * 
     */
    public void saveCollection()
    {
        try
        {
            for (SetupPanelIFace panel : panels)
            {
                panel.getValues(props);
            }
            //props.storeToXML(new FileOutputStream(new File(setupXMLPath)), "SetUp Props");
            
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SetupDivsionCollection.class, ex);
            
        }
        
        try
        {
            final SwingWorker worker = new SwingWorker()
            {
                //protected boolean isOK = false;
                
                public Object construct()
                {
                    Division            division   = (Division)props.get("division");
                    Discipline          discipline = (Discipline)props.get("discipline");
                    AutoNumberingScheme numScheme  = (AutoNumberingScheme)props.get("numScheme");

                    if (division != null && collection != null && numScheme != null)
                    {
                        DataProviderSessionIFace session = null;
                        try
                        {
                            Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
                            session = DataProviderFactory.getInstance().createSession();
                            
                            session.beginTransaction();
                            
                            if (division.getId() == null)
                            {
                                Institution institution = session.getData(Institution.class, "id", inst.getId(), DataProviderSessionIFace.CompareType.Equals);
                                institution.getDivisions().add(division);
                                division.setInstitution(institution);
                                
                                session.saveOrUpdate(institution);
                                session.saveOrUpdate(division);
                            } else
                            {
                                session.attach(division);
                            }
                            
                            if (discipline.getId() == null)
                            {
                                discipline.setDivision(division);
                                session.saveOrUpdate(discipline);
                            } else
                            {
                                discipline = session.merge(discipline);
                            }
                            
                            if (numScheme.getId() != null)
                            {
                                numScheme = session.merge(numScheme);
                            }
                            
                            session.saveOrUpdate(division);
                            
                            
                            collection.setDiscipline(discipline);
                            discipline.getCollections().add(collection);
                            
                            collection.getNumberingSchemes().add(numScheme);
                            numScheme.getCollections().add(collection);
                            
                            session.saveOrUpdate(collection);
                            session.saveOrUpdate(numScheme);
                            session.saveOrUpdate(discipline);
                            
                            session.commit();
                            session.flush();
                            
                            CommandDispatcher.dispatch(new CommandAction("SystemSetup", "DivisionSaved", division));
                            
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SetupDivsionCollection.class, ex);
                            ex.printStackTrace();
                            CommandDispatcher.dispatch(new CommandAction("SystemSetup", "DivisionError", division));
                            
                        } finally
                        {
                            if (session != null)
                            {
                                session.close();
                            }
                        }
                    }
                    
                    return null;
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                    
                }
            };
            worker.start();
        
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SetupDivsionCollection.class, ex);
            ex.printStackTrace();
        }
    }
}
