/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.ui.UIHelper.createButton;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;

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
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.UIRegistry;

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
    
    protected Specify                specify;
    protected String                 setupXMLPath;
    
    /**
     * @param specify
     */
    public SetupDivsionCollection(final Specify specify)
    {
        super();
        
        setModal(true);
        
        this.specify = specify;
        
        setTitle("Configuring"); // I18N
        cardPanel = new JPanel(cardLayout);
        
        
        cancelBtn  = createButton(UIRegistry.getResourceString("CANCEL"));
        helpBtn    = createButton(UIRegistry.getResourceString("HELP"));
        
        JPanel btnBar;
        backBtn    = createButton(UIRegistry.getResourceString("BACK"));
        nextBtn    = createButton(UIRegistry.getResourceString("NEXT"));
        /*nextBtn    = new JButton("Next") {
            public void setEnabled(boolean enable)
        {
            super.setEnabled(enable);
            if (enable)
            {
                int x = 0;
                x++;
            }
        }
        };*/
        
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
        
        Collection col = new Collection();
        col.initialize();
        FormSetupPanel coll1 = new FormSetupPanel("Cln", 
                                                     null, 
                                                     "CollectionSetup", 
                                                     Division.class.getName(), 
                                                     true, 
                                                     MultiView.HIDE_SAVE_BTN, 
                                                     col,
                                                     nextBtn);
        panels.add(coll1);
        
        FormSetupPanel coll2 = new FormSetupPanel("Cln", 
                null, 
                "CollectionSetupABCD", 
                Division.class.getName(), 
                true, 
                MultiView.HIDE_SAVE_BTN, 
                col,
                nextBtn);
        panels.add(coll2);
        
        FormSetupPanel coll3 = new FormSetupPanel("Cln", 
                null, 
                "CollectionSetupNumSch", 
                Division.class.getName(), 
                true, 
                MultiView.HIDE_SAVE_BTN, 
                col,
                nextBtn);
        panels.add(coll3);
        
        /*panels.add(new GenericFormPanel(inst, "inst", 
                "Enter Institution Information",
                new String[] { "Name", "Title"}, 
                new String[] { "name", "title"}, 
                nextBtn));
         
        panels.add(new GenericFormPanel(division, "div", 
                "Enter Division Information", 
                new String[] { "Name", "Title", "Abbrev"}, 
                new String[] { "name", "title", "abbrev"}, 
                nextBtn));
        
        Collection collection = new Collection();
        collection.initialize();
        
        panels.add(new GenericFormPanel(collection, "collection", 
                "Enter Collection Information", 
                new String[] { "Prefix", "Name"}, 
                new String[] { "prefix", "name"}, 
                nextBtn));*/
         
         
        lastStep = panels.size();
        
        if (backBtn != null)
        {
            backBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    if (step > 0)
                    {
                        step--;
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
            props.storeToXML(new FileOutputStream(new File(setupXMLPath)), "SetUp Props");
            
        } catch (Exception ex)
        {
            
        }
        
        try
        {
            final SwingWorker worker = new SwingWorker()
            {
                protected boolean isOK = false;
                
                public Object construct()
                {
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
            ex.printStackTrace();
        }
    }
}
