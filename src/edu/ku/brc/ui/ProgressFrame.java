/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A Frame displaying the progress of the conversion process.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 7, 2006
 *
 */
public class ProgressFrame extends JFrame
{
    protected JProgressBar overallProgress;
    protected JProgressBar processProgress;
    protected JLabel       desc;
    protected JLabel       overallLbl;
    protected JButton      closeBtn;
    protected JFrame       instance;
    
    protected boolean      isProcessPercent = false;
    protected boolean      isDoingPercent   = false;  
    protected int          origMax          = 0;
    
    /**
     * @param title
     */
    public ProgressFrame(final String title)
    {
        createUI(title, null);
    }
    
    /**
     * @param title
     */
    public ProgressFrame(final String title, final String iconName)
    {
        createUI(title, iconName);
    }
    
    /**
     * @param title
     * @param iconName
     */
    protected void createUI(final String title, final String iconName)
    {
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,5px,p,5px,p,10px,p"));
        CellConstraints cc         = new CellConstraints();

        overallProgress = new JProgressBar();
        processProgress = new JProgressBar();
        desc            = createLabel("");
        closeBtn        = createButton("Cancel");

        processProgress.setStringPainted(true);
        overallProgress.setStringPainted(true);
 
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        builder.add( desc, cc.xywh(1,1,3,1));
        
        builder.add( createLabel("Process:"), cc.xy(1,3)); // I18N
        builder.add( processProgress, cc.xy(3,3));
        
        builder.add( overallLbl = createLabel("Overall:"), cc.xy(1,5)); // I18N
        builder.add( overallProgress, cc.xy(3,5));
        
        builder.add( closeBtn, cc.xy(1,7));
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        if (StringUtils.isNotEmpty(iconName))
        {
            PanelBuilder    iconBldr    = new PanelBuilder(new FormLayout("8px, f:p:g,130px,f:p:g", "8px,f:p:g,130px,f:p:g, 8px"));
            iconBldr.add(new JLabel(IconManager.getIcon(iconName)), cc.xy(3, 3));
            mainPanel.add(iconBldr.getPanel(), BorderLayout.WEST);
            mainPanel.add(builder.getPanel(), BorderLayout.CENTER);
            
        } else
        {
            mainPanel = builder.getPanel();
        }
        
        setContentPane(mainPanel);
        
        setSize(new Dimension(500,125));
        
        setTitle(title);
        
        overallProgress.setIndeterminate(true);
        
        instance = this;
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                instance.setVisible(false);
                System.exit(0);
            }
        });
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }

        
        pack();
    }
    
    /**
     * Pack and then sets the width to 500px.
     */
    public void adjustProgressFrame()
    {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        pack();
        Dimension size = getSize();
        size.width = Math.max(size.width+100, 600);
        size.height += 20;
        setSize(size);
    }
    
    /**
     * 
     */
    public void turnOffOverAll()
    {
        overallProgress.setVisible(false);
        overallLbl.setVisible(false);
    }
    
    /**
     * 
     */
    public void turnOnOverAll()
    {
        overallProgress.setVisible(true);
        overallLbl.setVisible(true);
    }
    
    /**
     * 
     */
    public synchronized void incOverall()
    {
        overallProgress.setValue(overallProgress.getValue() + 1);
    }
    
    /**
     * @param min
     * @param max
     */
    public synchronized void setOverall(final int min, final int max)
    {
        overallProgress.setIndeterminate(min == 0 && max == 0);
        overallProgress.setMinimum(min);
        overallProgress.setMaximum(max);
        overallProgress.setValue(min);

    }
    
    public synchronized void setOverall(final int value)
    {
        overallProgress.setValue(value);
    }
    
    /**
     * @return the origMax
     */
    public synchronized int getOrigMax()
    {
        return origMax;
    }

    /**
     * @param origMax the origMax to set
     */
    public synchronized void setOrigMax(int origMax)
    {
        this.origMax = origMax;
    }

    /**
     * @param min
     * @param max
     */
    public synchronized void setProcess(final int min, final int max)
    {
        isProcessPercent = isDoingPercent || max == 100;
        processProgress.setMinimum(isProcessPercent ? 0 : min);
        processProgress.setMaximum(isProcessPercent ? 100 : max);
        processProgress.setValue(min);
        processProgress.setString("");
        origMax = max;
    }
    
    /**
     * @param value
     */
    public synchronized void setProcess(final int value)
    {
        if (processProgress.isIndeterminate())
        {
            processProgress.setIndeterminate(false);
        }
        
        if (isProcessPercent)
        {
            if (value > origMax || value < 0)
            {
                processProgress.setString("100%");
                
            } else
            {
                int percent = (int)(((double)value) / ((double)origMax) * 100.0);
                processProgress.setValue(percent);
                processProgress.setString(value > 0 ? Integer.toString(percent) + "%" : "");
            }
            
        } else 
        {
            int maxVal = processProgress.getMaximum();
            if (value >=  maxVal)
            {
                processProgress.setString(maxVal +" / "+maxVal);
            } else
            {
                processProgress.setValue(value);
                processProgress.setString(value > 0 ? (processProgress.getValue() +" / "+ processProgress.getMaximum()) : "");
            }
        }
    }
    
    /**
     * @param text
     */
    public synchronized void setDesc(final String text)
    {
        desc.setText(text);
    }
    
    /**
     * 
     */
    public synchronized void processDone()
    {

        desc.setText(" ");
        processProgress.setMinimum(0);
        processProgress.setMaximum(0);
        processProgress.setValue(0);
        processProgress.setString(" ");
        closeBtn.setText("Done");
    }
    
    public JButton getCloseBtn()
    {
        return closeBtn;
    }

    /**
     * @param isDoingPercent the isDoingPercent to set
     */
    public void setProcessPercent(boolean isDoingPercent)
    {
        this.isDoingPercent = isDoingPercent;
    }
    
    /**
     * @return
     */
    public JProgressBar getProcessProgress()
    {
        return processProgress;
    }    
    /**
     * @return
     */
    public JProgressBar getOverallProgress()
    {
        return overallProgress;
    }
}
