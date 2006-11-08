/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
    protected final Level[] levelsList = {Level.ALL, Level.DEBUG, Level.ERROR, Level.FATAL, Level.INFO, Level.OFF, Level.TRACE, Level.WARN};


    protected JButton        cancelBtn;
    protected JButton        okBtn;
    
    protected List<LoggerInfo> loggers = new Vector<LoggerInfo>();
    
    /**
     * Constructor.
     */
    public LoggerDialog(final Frame frame)
    {
        super(frame);
        createUI();
    }
    
    /**
     * Creates a list of Comboxes for setting the logging.
     */
    protected void createUI()
    {
        int cnt = 0;
        for (Enumeration e=LogManager.getCurrentLoggers(); e.hasMoreElements();)
        {
            Logger    logger = (Logger)e.nextElement();
            JComboBox cbx    = createCBX(logger.getLevel());
            loggers.add(new LoggerInfo(logger, cbx));
        }
        Collections.sort(loggers);

        PanelBuilder    builder = new PanelBuilder(new FormLayout("p,2px,p", UIHelper.createDuplicateJGoodiesDef("p", "4px", loggers.size())));
        CellConstraints cc      = new CellConstraints();
        
        // Bottom Button UI
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn     = new JButton(getResourceString("OK"));
        cnt = 1;
        for (LoggerInfo logInfo : loggers)
        {
            builder.add(new JLabel(logInfo.getLogger().getName()), cc.xy(1, cnt));
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

 
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        Dimension size = builder.getPanel().getPreferredSize();
        size.width  += 15;
        builder.getPanel().setPreferredSize(size);
        
        PanelBuilder outerPanel = new PanelBuilder(new FormLayout("p:g", "min(400px;p),10px,p"));
        outerPanel.add(new JScrollPane(builder.getPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cc.xy(1,1));
        outerPanel.add(btnBuilder.getPanel(), cc.xy(1, 3));
        outerPanel.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(outerPanel.getPanel());
        
        setTitle("Configure Log Levels");
        
        //setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setAlwaysOnTop(true);
        

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
        JComboBox cbx   = new JComboBox(levelsList);
        
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
         * Compable interface method
         * @param obj the objec to compare to
         * @return 0 if equals
         */
        public int compareTo(LoggerInfo obj)
        {
            return logger.getName().compareTo(obj.getLogger().getName());
        }
    }
}
