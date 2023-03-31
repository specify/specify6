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
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * Displays a dialog with all the registered loggers and allows your to dynamically change there settings for debugging.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 8, 2006
 *
 */
public class LoggerDialog extends JDialog implements ActionListener
{
    protected final Level[] levelsList = {Level.OFF, Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL};


    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JComboBox      setAllCBX;
    
    protected List<LoggerInfo> loggers = new Vector<LoggerInfo>();
    
    /**
     * Constructor.
     */
    public LoggerDialog(final Frame frame)
    {
        super(frame);
        createUI();
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
    }
    
    /**
     * Creates a list of Comboxes for setting the logging.
     */
    protected void createUI()
    {
        int cnt = 0;
        for (Enumeration<?> e=LogManager.getCurrentLoggers(); e.hasMoreElements();)
        {
            Logger    logger = (Logger)e.nextElement();
            JComboBox cbx    = createCBX(logger.getLevel());
            loggers.add(new LoggerInfo(logger, cbx));
        }
        Collections.sort(loggers);

        PanelBuilder    builder = new PanelBuilder(new FormLayout("p,2px,p", UIHelper.createDuplicateJGoodiesDef("p", "4px", loggers.size())));
        CellConstraints cc      = new CellConstraints();
        
        // Bottom Button UI
        cancelBtn = createButton(getResourceString("CANCEL"));
        okBtn     = createButton(getResourceString("OK"));
        cnt = 1;
        for (LoggerInfo logInfo : loggers)
        {
            builder.add(createLabel(logInfo.getLogger().getName()), cc.xy(1, cnt));
            builder.add(logInfo.getCbx(), cc.xy(3, cnt));
            cnt += 2;
        }
        
        okBtn.addActionListener(this);
        okBtn.setEnabled(false);
        
        getRootPane().setDefaultButton(okBtn);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn });
       
        
        okBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                save();
                setVisible(false);
            }
        });

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                setVisible(false);
            }
        });
        
        setAllCBX = createComboBox(levelsList);
        setAllCBX.setSelectedIndex(-1);
        setAllCBX.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e)
           {
               Level level = (Level)((JComboBox)e.getSource()).getSelectedItem();
               for (LoggerInfo logInfo : loggers)
               {
                   logInfo.getCbx().setSelectedItem(level);
               }
           }
        });
 
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        Dimension size = builder.getPanel().getPreferredSize();
        size.width  += 15;
        builder.getPanel().setPreferredSize(size);
        JScrollPane  scroller   = new JScrollPane(builder.getPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        PanelBuilder outerPanel = new PanelBuilder(new FormLayout("p,2px,p,p:g", "min(400px;p):g,5px,p,10px,p"));
        outerPanel.add(scroller, cc.xywh(1, 1, 4, 1));
        outerPanel.add(createLabel("Set All To:"), cc.xy(1, 3));
        outerPanel.add(setAllCBX, cc.xy(3, 3));
        outerPanel.add(btnBuilder.getPanel(), cc.xywh(1, 5, 4, 1));
        outerPanel.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(outerPanel.getPanel());

        setTitle("Configure Log Levels");
        
        //setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }
    
    /**
     * Sets the enabled/disabled state for the OK button.
     */
    protected void updateUI()
    {
        boolean enable = false;
        for (LoggerInfo logInfo : loggers)
        {
            Level cbxLevel = (Level)logInfo.getCbx().getSelectedItem();
            if (cbxLevel != logInfo.getLogger().getLevel())
            {
                enable = true;
                break;
            }
        }
        okBtn.setEnabled(enable);
    }
    
    /**
     * Creates a combobox for the Log Level.
     * @param level the log level to be set
     * @return the new Combobox
     */
    protected void save()
    {
        for (LoggerInfo logInfo : loggers)
        {
            Level cbxLevel = (Level)logInfo.getCbx().getSelectedItem();
            if (cbxLevel != logInfo.getLogger().getLevel())
            {
                logInfo.getLogger().setLevel(cbxLevel);
            }
        }
    }
    
    /**
     * Creates a combobox for the Log Level.
     * @param levelArg the log level to be set
     * @return the new Combobox
     */
    protected JComboBox createCBX(final Level levelArg)
    {
        Level     level = levelArg;
        JComboBox cbx   = createComboBox(levelsList);
        
        if (level == null)
        {
            level = Level.ERROR;
        }
        
        int inx = 0;
        for (Level lvl : levelsList)
        {
            if (level.toInt() == lvl.toInt())
            {
                cbx.setSelectedIndex(inx);
            }
            inx++;
        }
        
        cbx.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                updateUI();
            }
        });
        return cbx;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK and Cancel buttons.
       setVisible(false);
    }
    
    class LoggerInfo  implements Comparable<LoggerInfo>
    {
        protected Logger    logger;
        protected JComboBox cbx;
        
        public LoggerInfo(Logger logger, JComboBox cbx)
        {
            super();
            this.logger = logger;
            this.cbx = cbx;
        }

        public JComboBox getCbx()
        {
            return cbx;
        }

        public Logger getLogger()
        {
            return logger;
        }
        
        /**
         * Comparable interface method
         * @param obj the objec to compare to
         * @return 0 if equals
         */
        public int compareTo(LoggerInfo obj)
        {
            return logger.getName().compareTo(obj.getLogger().getName());
        }
    }
}
