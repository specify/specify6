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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.DataSetterForObj;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.UIRegistry;

public class SetupDivsionCollection extends JFrame
{
    private static final Logger log = Logger.getLogger(SetupDivsionCollection.class);
    
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
    protected Vector<GenericFormPanel> panels     = new Vector<GenericFormPanel>();
    
    protected Specify                specify;
    protected String                 setupXMLPath;
    
    /**
     * @param specify
     */
    public SetupDivsionCollection(final Specify specify)
    {
        super();
        
        this.specify = specify;
        
        setTitle("Configuring"); // I18N
        cardPanel = new JPanel(cardLayout);
        
        
        cancelBtn  = createButton(UIRegistry.getResourceString("CANCEL"));
        helpBtn    = createButton(UIRegistry.getResourceString("HELP"));
        
        JPanel btnBar;
        backBtn    = createButton(UIRegistry.getResourceString("BACK"));
        nextBtn    = createButton(UIRegistry.getResourceString("NEXT"));
        
        HelpMgr.registerComponent(helpBtn, "ConfiguringDatabase");
        CellConstraints cc = new CellConstraints();
        
        if (false)
        {
            PanelBuilder bbpb = new PanelBuilder(new FormLayout("f:p:g,p,4px,p,4px,p,4px,p,4px", "p"));
            bbpb.add(helpBtn, cc.xy(2,1));
            bbpb.add(backBtn, cc.xy(4,1));
            bbpb.add(nextBtn, cc.xy(6,1));
            bbpb.add(cancelBtn, cc.xy(8,1));
            
        } else
        {
            btnBar = ButtonBarFactory.buildWizardBar(helpBtn, backBtn, nextBtn, cancelBtn);
        }
            
        Institution inst     = AppContextMgr.getInstance().getClassObject(Institution.class);
        Division    division = AppContextMgr.getInstance().getClassObject(Division.class);
        
        panels.add(new GenericFormPanel(inst, "inst", 
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
                nextBtn));
         
         
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
                    configureDatabase();
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
            cardPanel.add(Integer.toString(i), panels.get(i));
            
            GenericFormPanel p = panels.get(i);
            p.setGetter(getter);
            p.setSetter(setter);
            p.setValues(props);
            
            if (!p.isUIValid())
            {
                //isAllOK = false;
            }
        }
        cardLayout.show(cardPanel, "0");
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p"));
        builder.add(cardPanel, cc.xy(1, 1));
        builder.add(btnBar, cc.xy(1, 3));
        
        builder.setDefaultDialogBorder();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(builder.getPanel());
        
        pack();

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
    public void configureDatabase()
    {
        try
        {
            for (BaseSetupPanel panel : panels)
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
