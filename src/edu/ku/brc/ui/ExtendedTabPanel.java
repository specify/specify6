/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 27, 2008
 *
 */
@SuppressWarnings("serial")
public class ExtendedTabPanel extends JPanel
{
    protected JLabel textLabel;
    
    public ExtendedTabPanel(final Component comp,
                             final String    title,
                             final Icon      icon)
    {
        super(new BorderLayout());
        
        JLabel closeBtn = new JLabel(IconManager.getIcon("Close"));
        closeBtn.setBorder(null);
        closeBtn.setOpaque(false);
        closeBtn.addMouseListener(new TabMouseAdapter(comp, closeBtn));
        
        if (UIHelper.isMacOS())
        {
            setOpaque(false);
        }
        add(textLabel = new JLabel(title, icon, SwingConstants.RIGHT), BorderLayout.WEST);
        add(new JLabel(" "), BorderLayout.CENTER);
        add(closeBtn, BorderLayout.EAST);
    }
    
    /**
     * @param titleStr
     */
    public void setTitle(final String titleStr)
    {
        textLabel.setText(titleStr);
    }
    
    class TabMouseAdapter extends MouseAdapter
    {
        protected Component tabComp;
        protected JLabel    closeBtn;
        
        public TabMouseAdapter(final Component tabComp, final JLabel closeBtn) 
        {
            this.tabComp  = tabComp;
            this.closeBtn = closeBtn;
        }
        
        @Override
        public void mouseClicked(MouseEvent e)
        {
            SubPaneIFace subPane = (SubPaneIFace)tabComp;
            SubPaneMgr.getInstance().showPane(subPane);
            SubPaneMgr.getInstance().closeCurrent();
        }
        @Override
        public void mouseEntered(MouseEvent e)
        {
            closeBtn.setIcon(IconManager.getIcon("CloseHover"));
            closeBtn.repaint();
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            closeBtn.setIcon(IconManager.getIcon("Close"));
            closeBtn.repaint();
        }
    }
}
