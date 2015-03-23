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
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.tasks.DualViewSearchable;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateToggleButton;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;

@SuppressWarnings("serial")
public class FindPanel extends JPanel implements TimingTarget
{
    protected DualViewSearchable views;
    
    protected JButton    closeButton;
    protected JLabel     findLabel;
    protected JTextField entryField;
    protected JButton    findButton;
    protected JButton    nextButton;
    protected MultiStateToggleButton whereToggleButton;
    protected JCheckBox  exactChk;
    
    protected int        mode;  
    protected boolean	 hasBeenContracted   = false;
    
    protected Dimension  prefSize;
    protected Dimension  contractedSize;
    protected boolean    animationInProgress = false;
    protected boolean    shrinking           = false;
    protected boolean    expanding           = false;
    
    public static final int EXPANDED = 1;
    public static final int CONTRACTED = -1;
    
    /**
     * @param views
     * @param startingMode
     */
    public FindPanel(DualViewSearchable views, int startingMode)
    {
        this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
        this.views = views;
        
        String find = getResourceString("FIND");
        String next = getResourceString("NEXT");

        // These should already be loaded
        //IconManager.setApplicationClass(Specify.class);
        //IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml"));
        //IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml"));

        Icon up    = IconManager.getIcon("Top",IconSize.Std16);
        Icon down  = IconManager.getIcon("Bottom",IconSize.Std16);
        Icon both  = IconManager.getIcon("Both",IconSize.Std16);
        
        closeButton = UIHelper.createIconBtn("Close", IconManager.IconSize.NonStd, "", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                contract();
            }
        });
        closeButton.setEnabled(true);
        closeButton.setFocusable(false);
        closeButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e)
            {
                ((JButton)e.getSource()).setIcon(IconManager.getIcon("CloseHover"));
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                ((JButton)e.getSource()).setIcon(IconManager.getIcon("Close"));
                super.mouseExited(e);
            }
            
        });
        
        findLabel = createLabel(find + ": ");
        entryField = createTextField(32);
        entryField.setMaximumSize(entryField.getPreferredSize());
        findButton = createButton(find);
        findButton.setEnabled(false);
        nextButton = createButton(next);
        nextButton.setEnabled(false);

        whereToggleButton = new MultiStateToggleButton(up,down,both);
        whereToggleButton.setStateIndex(0);
        whereToggleButton.setToolTipText(UIRegistry.getResourceString("FindPanel.WhereBtnTTUp"));
        whereToggleButton.addActionListener(new ActionListener(){

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				switch (whereToggleButton.getStateIndex()) {
				case 0:
					whereToggleButton.setToolTipText(UIRegistry.getResourceString("FindPanel.WhereBtnTTLow"));
					break;
				case 1:
					whereToggleButton.setToolTipText(UIRegistry.getResourceString("FindPanel.WhereBtnTTBoth"));
					break;
				case 2:
					whereToggleButton.setToolTipText(UIRegistry.getResourceString("FindPanel.WhereBtnTTUp"));
					break;
				default:
					whereToggleButton.setToolTipText(null);
				}
			}
        	
        });
        
        exactChk = UIHelper.createCheckBox(getResourceString("FindPanel.Exact"));
        exactChk.setSelected(AppPreferences.getLocalPrefs().getBoolean("FindPanel.Exact", true));
        exactChk.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				AppPreferences.getLocalPrefs().putBoolean("FindPanel.Exact", exactChk.isSelected());				
			}
        	
        });
        exactChk.setToolTipText(UIRegistry.getResourceString("FindPanel.ExactChkTT"));
        
        add(closeButton);
        add(Box.createRigidArea(closeButton.getPreferredSize()));
        add(findLabel);
        add(entryField);
        add(findButton);
        add(nextButton);
        add(whereToggleButton);
        add(exactChk);
        add(Box.createHorizontalGlue());
        
        ActionListener buttonListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if (ae.getSource() == findButton || (ae.getSource() == entryField && entryField.getText().length() > 0))
                {
                    findClicked();
                }
                else if (ae.getSource() == nextButton)
                {
                    nextClicked();
                }
            }
        };
        
        entryField.addActionListener(buttonListener);
        findButton.addActionListener(buttonListener);
        nextButton.addActionListener(buttonListener);
        
        entryField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                boolean enable = entryField.getText().length() > 0;
                findButton.setEnabled(enable);
                nextButton.setEnabled(enable);
            }
        });
        
        mode = EXPANDED;
        if (startingMode == CONTRACTED)
        {
            mode = startingMode;
        }
        prefSize = super.getPreferredSize();
        contractedSize = new Dimension(prefSize.width,0);
    }

    /**
     * 
     */
    public void expand()
    {
        if (mode == EXPANDED || expanding || shrinking)
        {
            return;
        }
        
        expanding = true;
        
        // start animation to expand the panel
        Animator expander = new Animator(450,this);
        expander.start();
    }

    public void contract()
    {
        if (mode == CONTRACTED || shrinking || expanding)
        {
            return;
        }
        
        shrinking = true;
        if (!hasBeenContracted)
        {
        	UIRegistry.displayInfoMsgDlgLocalized("FindPanel.RESTORE_HINT");
        	hasBeenContracted = true;
        }
        // start animation to shrink the panel
        Animator expander = new Animator(300,this);
        expander.start();
    }
    
    /**
     * 
     */
    protected void findClicked()
    {
        views.find(entryField.getText(), getWhere(), true, exactChk.isSelected());
    }
    
    /**
     * 
     */
    protected void nextClicked()
    {
        views.findNext(entryField.getText(), getWhere(), true, exactChk.isSelected());
    }
    
    /**
     * @return
     */
    protected int getWhere()
    {
        switch (whereToggleButton.getStateIndex())
        {
            case 0:
            {
                return DualViewSearchable.TOPVIEW;
            }
            case 1:
            {
                return DualViewSearchable.BOTTOMVIEW;
            }
            case 2:
            {
                return DualViewSearchable.BOTHVIEWS;
            }
            default:
            {
                return DualViewSearchable.TOPVIEW;
            }
        }
    }
    
    
	/* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        if (shrinking || expanding)
        {
            return prefSize;
        }
        
        if (mode == CONTRACTED)
        {
            return contractedSize;
        }
        
        return super.getPreferredSize();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getMaximumSize()
     */
    @Override
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }
    
    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#begin()
     */
    public void begin()
    {
        animationInProgress = true;
    }

    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#end()
     */
    public void end()
    {
        animationInProgress = false;
        
        if (expanding)
        {
            mode = EXPANDED;
            expanding = false;
            entryField.requestFocus();
        }
        if (shrinking)
        {
            mode = CONTRACTED;
            shrinking = false;
        }
        
        Component c = getParent();
        c.invalidate();
        c.doLayout();
        c.validate();
        c.repaint();
    }

    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#repeat()
     */
    public void repeat()
    {
        // never gets called
    }

    /* (non-Javadoc)
     * @see org.jdesktop.animation.timing.TimingTarget#timingEvent(float)
     */
    public void timingEvent(float fraction)
    {
        float sizeFrac = fraction;
        
        if (shrinking)
        {
            sizeFrac = 1 - fraction;
        }
        
        prefSize.height = (int)(super.getPreferredSize().height * sizeFrac);
        
        Component c = getParent();
        c.invalidate();
        c.doLayout();
        c.validate();
        c.repaint();
        
        this.invalidate();
        this.repaint();
        this.validate();
    }

    /**
     * @return the nextButton
     */
    public JButton getNextButton()
    {
        return nextButton;
    }
    
    
}
