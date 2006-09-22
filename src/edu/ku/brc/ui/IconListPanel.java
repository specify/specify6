/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author jstewart
 * @code_status Alpha
 */
public class IconListPanel extends JPanel implements ActionListener
{
    protected ReorderableListModel<Icon> model;
    protected JList iconDisplay;
    protected JScrollPane listScrollPane;
    protected int maxIconHeight;
    protected JPanel orderControlPanel;
    
    protected JButton toStartButton;
    protected JButton moveLeftButton;
    protected JButton moveRightButton;
    protected JButton toEndButton;
    
    public IconListPanel()
    {
        maxIconHeight = 64;
        
        model = new ReorderableListModel<Icon>();
        iconDisplay = new JList(model);
        iconDisplay.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        iconDisplay.setVisibleRowCount(1);
        iconDisplay.setFixedCellHeight(maxIconHeight);
        listScrollPane = new JScrollPane(iconDisplay,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        orderControlPanel = new JPanel();
        orderControlPanel.setLayout(new BoxLayout(orderControlPanel,BoxLayout.LINE_AXIS));
    
        toStartButton = new JButton(new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\start.png"));
        toStartButton.setSize(20,20);
        toStartButton.setToolTipText("Move selection to first position");
        moveLeftButton = new JButton(new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\1leftarrow.png"));
        moveLeftButton.setSize(20,20);
        moveLeftButton.setToolTipText("Move selection left one position");
        moveRightButton = new JButton(new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\1rightarrow.png"));
        moveRightButton.setSize(20,20);
        moveRightButton.setToolTipText("Move selection right one position");
        toEndButton = new JButton(new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\finish.png"));
        toEndButton.setSize(20,20);
        toEndButton.setToolTipText("Move selection to last position");
        
        toStartButton.addActionListener(this);
        moveLeftButton.addActionListener(this);
        moveRightButton.addActionListener(this);
        toEndButton.addActionListener(this);
        
        orderControlPanel.add(Box.createHorizontalGlue());
        orderControlPanel.add(toStartButton);
        orderControlPanel.add(moveLeftButton);
        orderControlPanel.add(moveRightButton);
        orderControlPanel.add(toEndButton);
        orderControlPanel.add(Box.createHorizontalGlue());
        
        this.setLayout(new BorderLayout());
        this.add(listScrollPane,BorderLayout.CENTER);
        this.add(orderControlPanel,BorderLayout.SOUTH);
    }
    
    public void addIcon(ImageIcon icon)
    {
        if(icon.getIconHeight() <= 0)
        {
            throw new IllegalArgumentException("Icon must have a height > 0");
        }
        
        float scaleFactor = 1f;
        scaleFactor = (float)maxIconHeight / (float)icon.getIconHeight();
        int newWidth = (int)(scaleFactor * icon.getIconWidth());
        ImageIcon scaled = new ImageIcon(icon.getImage().getScaledInstance(newWidth, maxIconHeight, Image.SCALE_SMOOTH));
        model.add(scaled);
    }
    
    public void removeIcon(int iconIndex)
    {
        model.remove(iconIndex);
    }

    public int getMaxIconHeight()
    {
        return maxIconHeight;
    }

    public void setMaxIconHeight(int maxIconHeight)
    {
        this.maxIconHeight = maxIconHeight;
        iconDisplay.setFixedCellHeight(maxIconHeight);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == toStartButton)
        {
            int selection = iconDisplay.getSelectedIndex();
            model.moveToStart(selection);
            iconDisplay.setSelectedIndex(0);
            return;
        }
        if(e.getSource() == moveLeftButton)
        {
            int selection = iconDisplay.getSelectedIndex();
            model.shiftLeft(selection);
            if(selection != 0)
            {
                iconDisplay.setSelectedIndex(selection-1);
            }
            return;
        }
        if(e.getSource() == moveRightButton)
        {
            int selection = iconDisplay.getSelectedIndex();
            model.shiftRight(selection);
            if(selection != model.getSize()-1)
            {
                iconDisplay.setSelectedIndex(selection+1);
            }
            return;
        }
        if(e.getSource() == toEndButton)
        {
            int selection = iconDisplay.getSelectedIndex();
            model.moveToEnd(selection);
            iconDisplay.setSelectedIndex(model.getSize()-1);
            return;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        IconListPanel ilp = new IconListPanel();
        ilp.addIcon(new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\me.jpg"));
        ilp.addIcon(new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\radioactive.png"));
        ilp.addIcon(new ImageIcon("C:\\Documents and Settings\\jstewart\\Desktop\\me_infrared.jpg"));
        
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        f.add(ilp);
        f.setSize(200,200);
        f.setVisible(true);
    }
}
