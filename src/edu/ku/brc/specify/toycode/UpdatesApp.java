/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.toycode;

import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.ui.BrowseBtnPanel;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 14, 2008
 *
 */
public class UpdatesApp extends JPanel
{
    protected BrowseBtnPanel baseBBP    = null;
    protected BrowseBtnPanel baseUpBBP  = null;
    protected BrowseBtnPanel macFullBBP = null;
    protected BrowseBtnPanel macUpBBP   = null;
    protected BrowseBtnPanel outBBP     = null;
    
    protected JTextField baseTF       = new JTextField(40);
    protected JTextField baseUpTF     = new JTextField(40);
    protected JTextField macFullTF    = new JTextField(40);
    protected JTextField macUpTF      = new JTextField(40);
    protected JTextField outTF        = new JTextField(40);
    protected JTextField statusTF     = new JTextField();
    
    protected JTextField versionTF    = new JTextField(2);
    protected JTextField updateBaseTF = new JTextField();
    protected JButton    mergeBtn     = new JButton("Merge");
    
    protected JSpinner   verSub1;
    protected JSpinner   verSub2;
    
    protected UpdateDescriptor baseUpdateDesc;
    protected UpdateDescriptor baseUpdateUpDesc;
    protected UpdateDescriptor maxFullUpdateDesc;
    protected UpdateDescriptor maxUpUpdateDesc;
    
    protected Properties props = null;
    
    /**
     * 
     */
    public UpdatesApp()
    {
        
        baseBBP    = new BrowseBtnPanel(baseTF, false, true);
        baseUpBBP  = new BrowseBtnPanel(baseUpTF, false, true);
        macFullBBP = new BrowseBtnPanel(macFullTF, false, true);
        macUpBBP   = new BrowseBtnPanel(macUpTF, false, true);
        outBBP     = new BrowseBtnPanel(outTF, false, true);
        
        verSub1 = new JSpinner();
        verSub2 = new JSpinner();
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g,p", createDuplicateJGoodiesDef("p:g", "4px", 7)));
        CellConstraints cc = new CellConstraints();
        
        int y = 1;
        pb.add(createLabel("Linux/Win Full:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(baseBBP, cc.xyw(3, y, 3));
        y += 2;
        
        pb.add(createLabel("Linux/Win Update:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(baseUpBBP, cc.xyw(3, y, 3));
        y += 2;
        
        pb.add(createLabel("Mac Full:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(macFullBBP, cc.xyw(3, y, 3));
        y += 2;
        
        pb.add(createLabel("Mac Update:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(macUpBBP, cc.xyw(3, y, 3));
        y += 2;
        
        pb.add(createLabel("Output:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(outBBP, cc.xyw(3, y, 3));
        y += 2;
        
        updateBaseTF.setText("6.0.0");
        pb.add(createLabel("Update Base Version:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(updateBaseTF, cc.xy(3, y));
        y += 2;
        
        versionTF.setText("6");
        verSub1.setValue(99);
        verSub2.setValue(99);
        
        PanelBuilder vpb = new PanelBuilder(new FormLayout("p,p,p:g,p,p:g,f:p:g", "p"));
        vpb.add(versionTF,        cc.xy(1, 1));
        vpb.add(createLabel("."), cc.xy(2, 1));
        vpb.add(verSub1,          cc.xy(3, 1));
        vpb.add(createLabel("."), cc.xy(4, 1));
        vpb.add(verSub2,          cc.xy(5, 1));
        
        pb.add(createLabel("New Version:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(vpb.getPanel(), cc.xyw(3, y, 1));
        y += 2;
        
        PanelBuilder pb2 = new PanelBuilder(new FormLayout("p,2px,p,f:p:g,p", "p"));
        statusTF.setBackground(getBackground());
        pb2.add(statusTF, cc.xyw(1, 1, 4));
        pb2.add(mergeBtn, cc.xy(5, 1));
        
        pb.getPanel().setBorder(BorderFactory.createEmptyBorder(14, 14, 1, 14));
        
        setLayout(new BorderLayout());
        
        add(pb.getPanel(), BorderLayout.CENTER);
        add(pb2.getPanel(), BorderLayout.SOUTH);
        
        try
        {
            XStream xstream = new XStream();
            props = (Properties)xstream.fromXML(FileUtils.readFileToString(new File("props.init")));
            
        } catch (Exception ex) 
        {
            props = new Properties();
        }
        
        baseTF.setText(props.getProperty("baseTF",       "MacMedia/updates.xml.winlinfull.6.1.17"));
        baseUpTF.setText(props.getProperty("baseUpTF",   "MacMedia/updates.xml.winlinupdate.6.1.17"));
        macFullTF.setText(props.getProperty("macFullTF", "MacMedia/updates_mac.xml"));
        macUpTF.setText(props.getProperty("macUpTF",     "MacMedia/updates_mac_update.xml"));
        outTF.setText(props.getProperty("outTF",         "MacMedia/updates.xml"));
        updateBaseTF.setText(props.getProperty("updateBaseTF", "6.0.0"));
        versionTF.setText(props.getProperty("versionTF", "6"));
        
        verSub1.setValue(Integer.parseInt(props.getProperty("subVer1", "1")));
        verSub2.setValue(Integer.parseInt(props.getProperty("subVer2", "11")));
        
        mergeBtn.addActionListener(new ActionListener() {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                merge();
                doSave();
            }
            
        });
        
    }
    
    protected void merge()
    {
        try
        {
            props.setProperty("baseTF",    baseTF.getText());
            props.setProperty("baseUpTF",  baseUpTF.getText());
            props.setProperty("macFullTF", macFullTF.getText());
            props.setProperty("macUpTF",   macUpTF.getText());
            props.setProperty("outTF",     outTF.getText());
            props.setProperty("versionTF", versionTF.getText());
            
            props.setProperty("subVer1", Integer.toString((Integer)verSub1.getValue()));
            props.setProperty("subVer2", Integer.toString((Integer)verSub2.getValue()));
            
            XStream xstream = new XStream();
            FileUtils.writeStringToFile(new File("props.init"), xstream.toXML(props));
            
        } catch (Exception ex) 
        {
            ex.printStackTrace();
        }
        
        statusTF.setText("Merging...");
        
        baseUpdateDesc    = read(new File(baseTF.getText()));
        baseUpdateUpDesc  = read(new File(baseUpTF.getText()));
        maxFullUpdateDesc = read(new File(macFullTF.getText()));
        maxUpUpdateDesc   = read(new File(macUpTF.getText()));
        
        setAsFull(baseUpdateDesc, updateBaseTF.getText(), versionTF.getText(), (Integer)verSub1.getValue(), (Integer)verSub2.getValue());
        setAsUpdate(baseUpdateUpDesc, versionTF.getText(), (Integer)verSub1.getValue(), (Integer)verSub2.getValue());
        
        setAsFull(maxFullUpdateDesc, updateBaseTF.getText(), versionTF.getText(), (Integer)verSub1.getValue(), (Integer)verSub2.getValue());
        setAsUpdate(maxUpUpdateDesc, versionTF.getText(), (Integer)verSub1.getValue(), (Integer)verSub2.getValue());
        
        baseUpdateDesc.getEntries().addAll(baseUpdateUpDesc.getEntries());
        baseUpdateDesc.getEntries().addAll(maxFullUpdateDesc.getEntries());
        baseUpdateDesc.getEntries().addAll(maxUpUpdateDesc.getEntries());
        
        statusTF.setText("Merged.");

    }
    
    protected void setAsFull(final UpdateDescriptor upDesc, final String baseVer, final String ver, final Integer vs1, final Integer vs2)
    {
        int i2 = (vs2-2);
        if (i2 < 0)
        {
            i2 += 100;
        }
        String newVersion = ver + "." + vs1 + "." + vs2;
        String verMax     = ver + "." + vs1 + "." + i2;
        
        for (UpdateEntry entry : upDesc.getEntries())
        {
            if (!entry.getNewVersion().equals(newVersion))
            {
                String msg = String.format("The Full entry '%s'\n has the wrong version number '%s'\nThe version should be '%s'!", entry.getFileName(), entry.getNewVersion(), newVersion);
                JOptionPane.showConfirmDialog(null, msg, "Version Mismatch", JOptionPane.ERROR_MESSAGE);
            }
            entry.setUpdatableVersionMax(verMax);
            entry.setUpdatableVersionMin(baseVer);
        }
    }
    
    protected void setAsUpdate(final UpdateDescriptor upDesc, final String ver, final Integer vs1, final Integer vs2)
    {
        int i2 = (vs2-1);
        if (i2 < 0)
        {
            i2 += 100;
        }
        String newVersion = ver + "." + vs1 + "." + vs2;
        String verMin     = ver + "." + vs1 + "." + i2;
        
        for (UpdateEntry entry : upDesc.getEntries())
        {
            if (!entry.getNewVersion().equals(newVersion))
            {
                String msg = String.format("The Update entry '%s'\n has the wrong version number '%s'\nThe version should be '%s'!", entry.getFileName(), entry.getNewVersion(), newVersion);
                JOptionPane.showConfirmDialog(null, msg, "Version Mismatch", JOptionPane.ERROR_MESSAGE);
            }
            entry.setUpdatableVersionMax(verMin);
            entry.setUpdatableVersionMin(verMin);
        }
    }
    
    protected void doOpen()
    {
        
    }
    
    protected void doSave()
    {
        
        write(baseUpdateDesc, outTF.getText());
        
        statusTF.setText("Saved.");
    }
    
    protected void shutdown()
    {
        System.exit(0);
    }
    
    protected static UpdateDescriptor read(final File file)
    {
        XStream xstream = new XStream();
        UpdateDescriptor.config(xstream);
        UpdateEntry.config(xstream);
        
        if (file.exists())
        {
            try
            {
                UpdateDescriptor update = (UpdateDescriptor)xstream.fromXML(FileUtils.readFileToString(file));
                return update;
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            System.err.println("File: "+file.getAbsolutePath()+" doesn't exist.");
        }
        return null;
    }
    
    /**
     * @param update
     * @param outPath
     */
    protected static void write(final UpdateDescriptor update,
                                final String outPath)
    {
        XStream xstream = new XStream();
        
        UpdateDescriptor.config(xstream);
        UpdateEntry.config(xstream);
        
        System.out.println("Start");
        File file = new File(outPath);
        try
        {
            FileUtils.writeStringToFile(file, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+xstream.toXML(update));
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        System.out.println("Stop"); 
    }
    
    /**
     * Sets a mnemonic on a button.
     * @param btn the button
     * @param mnemonicKey the one char string.
     */
    public static void setMnemonic(final AbstractButton btn, final String mnemonic)
    {
        if (StringUtils.isNotEmpty(mnemonic) && btn != null)
        {
            btn.setMnemonic(mnemonic.charAt(0));
        }
    }
    
    public static JMenu createMenu(final JMenuBar menuBar, final String label, final String mneu)
    {
        JMenu menu = null;
        try
        {
            menu = menuBar.add(new JMenu(label));
            //if (oSType != OSTYPE.MacOSX)
            {
                setMnemonic(menu, mneu);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return menu;
    }

    
    public JMenuBar createMenus()
    {
        JMenuBar menuBar = null;
        
        
        /*UIHelper.createMenuItemWithAction(fileMenu, "Open", "O",  "", true, new AbstractAction() //$NON-NLS-1$
        {
            public void actionPerformed(ActionEvent e)
            {
                doOpen();
            }
        });
        
        JMenuItem saveMenuItem = UIHelper.createMenuItemWithAction(fileMenu, "Save", "S", "", true, new AbstractAction() //$NON-NLS-1$
        {
            public void actionPerformed(ActionEvent e)
            {
                doSave();
            }
            
        });
        saveMenuItem.setEnabled(false);
        */
        
        if (!UIHelper.isMacOS())
        {
            menuBar = new JMenuBar();
            JMenu fileMenu = createMenu(menuBar, "File", "F");
            fileMenu.addSeparator();
            
            UIHelper.createMenuItemWithAction(fileMenu, "Exit", "x",  "", true, new AbstractAction() //$NON-NLS-1$
            {
                public void actionPerformed(ActionEvent e)
                {
                    shutdown();
                }
            });
        }

        return menuBar;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        //UIManager.setLookAndFeel(new WindowsLookAndFeel());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        //UIManager.setLookAndFeel(new GTKLookAndFeel());
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        //PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
                        //PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        //PlasticLookAndFeel.setPlasticTheme(new DesertGreen());
                       
                    }
                }
                catch (Exception e)
                {
                    System.err.println("Can't change L&F: "+e); //$NON-NLS-1$
                }
                
                UpdatesApp panel = new UpdatesApp();
                JFrame frame = new JFrame();
                frame.setTitle("Install4J XML Updater");
                frame.setContentPane(panel);
                
                JMenuBar menuBar = panel.createMenus();
                if (menuBar != null)
                {
                    //top.add(menuBar, BorderLayout.NORTH);
                    frame.setJMenuBar(menuBar);
                }

                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                
            }
        });
    }

}
