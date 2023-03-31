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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createProgressBar;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A Dialog displaying the progress.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 7, 2006
 *
 */
public class ProgressDialog extends JDialog
{
    protected JProgressBar overallProgress;
    protected JProgressBar processProgress;
    protected JLabel       desc;
    protected JButton      closeBtn;
    //protected JFrame       instance;
    
    protected boolean      isProcessPercent = false;
    protected int          origMax          = 0;
    
    /**
     * @param title
     * @param includeBothBars
     * @param includeClose
     */
    public ProgressDialog(final String  title, 
                          final boolean includeBothBars,
                          final boolean includeClose)
    {
        super(UIRegistry.getMostRecentWindow());
        String rowDef = "p,5px" + (includeBothBars ? ",p,5px" : "") + (includeClose ? ",p,10px" : "") + ",p";
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2px,f:p:g", rowDef));
        CellConstraints cc         = new CellConstraints();

        int y = 1;
        overallProgress = createProgressBar();
        desc            = createLabel("");
        desc.setHorizontalAlignment(SwingConstants.CENTER);
        builder.add( desc, cc.xywh(1,y,3,1)); y += 2;
        
        processProgress = createProgressBar(); //new JProgressBar();
        processProgress.setStringPainted(true);
        builder.add( createLabel("Process:"), cc.xy(1,y)); // I18N
        builder.add( processProgress, cc.xy(3,y));y += 2;
         
        if (includeBothBars)
        {
            builder.add( createLabel("Overall:"), cc.xy(1,y)); // I18N
            builder.add( overallProgress, cc.xy(3,y));y += 2;
            overallProgress.setIndeterminate(true);
        }
        createLabel("");

        if (includeClose)
        {
            closeBtn = createButton(UIRegistry.getResourceString("CANCEL"));
            builder.add( closeBtn, cc.xy(1,y));y += 2;
        }
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(builder.getPanel());
        
        pack();
        Dimension size = getPreferredSize();
        int heightNudge = closeBtn != null ? 40 : 20;
        setSize(new Dimension(500,size.height+heightNudge));
        
        setTitle(title);
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
        
        if (closeBtn != null)
        {
            closeBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    ProgressDialog.this.setVisible(false);
                }
            });
        }
        
        //setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    public synchronized void incOverall()
    {
        if (overallProgress != null)
        {
            overallProgress.setValue(overallProgress.getValue() + 1);
        }
    }
    
    public synchronized void setOverall(final int min, final int max)
    {
        if (overallProgress != null)
        {
            overallProgress.setIndeterminate(min == 0 && max == 0);
            overallProgress.setMinimum(min);
            overallProgress.setMaximum(max);
            overallProgress.setValue(min);
        }
    }
    
    public synchronized void setOverall(final int value)
    {
        if (overallProgress != null)
        {
            overallProgress.setValue(value);
        }

    }
    
    public synchronized int getOverall()
    {
        return overallProgress.getValue();
    }
    
    public synchronized void setProcess(final int min, final int max)
    {
        processProgress.setMinimum(isProcessPercent ? 0 : min);
        processProgress.setMaximum(isProcessPercent ? 100 : max);
        processProgress.setValue(min);
        processProgress.setString("");
        origMax = max;
    }
    
    public synchronized void setProcess(final int value)
    {
        if (processProgress.isIndeterminate())
        {
            processProgress.setIndeterminate(false);
        }
        
        if (isProcessPercent)
        {
            int percent = Math.min((int)(((double)value) / ((double)origMax) * 100.0), 100);
            processProgress.setValue(percent);
            processProgress.setString(value > 0 ? Integer.toString(percent) + "%" : "");
            
        } else
        {
            processProgress.setValue(value);
            processProgress.setString(value > 0 ? (processProgress.getValue() +" / "+ processProgress.getMaximum()) : "");
        }
    }
    
    public synchronized int getProcess()
    {
        return processProgress.getValue();
    }
    
    
    public synchronized void setDesc(final String text)
    {
        desc.setText(text);
    }
    
    public synchronized void processDone()
    {

        desc.setText(" ");
        
        processProgress.setMinimum(0);
        processProgress.setMaximum(0);
        processProgress.setValue(0);
        processProgress.setString(" ");
        
        if (closeBtn != null)
        {
            closeBtn.setText(UIRegistry.getResourceString("DONE"));
        }
    }
    
    public JButton getCloseBtn()
    {
        return closeBtn;
    }

    /**
     * @return the isProcessPercent
     */
    public boolean isProcessPercent()
    {
        return isProcessPercent;
    }

    /**
     * @param isProcessPercent the isProcessPercent to set
     */
    public void setProcessPercent(boolean isProcessPercent)
    {
        this.isProcessPercent = isProcessPercent;
    }
    
    public JProgressBar getProcessProgress()
    {
        return processProgress;
    }
}
