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
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.IconManager;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 18, 2011
 *
 */
public class ImageImportFrame extends CustomFrame
{
    protected WorkbenchPaneSS   wbPane;
    protected Workbench         workbench;
    protected boolean           allowCloseWindow = true;
    protected JRadioButton      oneImagePerRow;

    /**
     * Constructor. 
     */
    public ImageImportFrame(final WorkbenchPaneSS wbPane, 
                            final Workbench workbench)
    {
        super("Import Images", CustomFrame.OK_BTN, null);
        
        setOkLabel(getResourceString("CLOSE"));
        
        this.wbPane           = wbPane;
        this.workbench        = workbench;
        
        setIconImage(IconManager.getImage("AppIcon").getImage());
        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (allowCloseWindow)
                {
                    wbPane.toggleImageFrameVisible();
                }
            }
        });
        
        HelpMgr.setHelpID(this, "WorkbenchWorkingWithImages");
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomFrame#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("p, f:p:g", "p,2px,p,8px,p,8px,p"));
        CellConstraints cc      = new CellConstraints();
        
        oneImagePerRow = new JRadioButton("One Image Per Row");
        JRadioButton allImagesPerRow  = new JRadioButton("All Images In One Row");
        ButtonGroup grp = new ButtonGroup();
        grp.add(allImagesPerRow);
        grp.add(oneImagePerRow);
        
        JButton fileDlg = createI18NButton("FIles...");
        
        builder.add(oneImagePerRow, cc.xyw(1, 1, 2));
        builder.add(allImagesPerRow,  cc.xyw(1, 3, 2));
        builder.addSeparator("", cc.xyw(1, 5, 2));
        
        builder.add(fileDlg, cc.xy(1, 7));

        builder.setDefaultDialogBorder();
        
        contentPanel = builder.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
        
        oneImagePerRow.setSelected(true);
        
        fileDlg.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ((WorkbenchTask)wbPane.getTask()).importCardImages(workbench, isOneImagePerRow());          
            }
        });
    }
    
    /**
     * @return whether there should be one image per row
     */
    public boolean isOneImagePerRow()
    {
        return oneImagePerRow.isSelected();
    }
}
