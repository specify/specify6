/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * An extension of IconTray to be used for displaying ordered items.
 *
 * @author jstewart
 * @code_status Complete
 */
public class OrderedIconTray extends IconTray<OrderedTrayable> implements ActionListener
{
    /** A JPanel containing the order manipulation buttons. */
    protected JPanel orderControlPanel;
    /** A button that moves the selection to the start of the order. */
    protected JButton toStartButton;
    /** A button that moves the selection to the left in the order. */
    protected JButton moveLeftButton;
    /** A button that moves the selection to the right in the order. */
    protected JButton moveRightButton;
    /** A button that moves the selection to the end of the order. */
    protected JButton toEndButton;
    
    /**
     * Creates a new instance containing zero items.
     * 
     */
    public OrderedIconTray()
    {
        super();
        
        listModel = new ReorderableListModel<OrderedTrayable>();
        iconListWidget.setModel(listModel);
        
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
        
        this.add(orderControlPanel,BorderLayout.SOUTH);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        ReorderableListModel<OrderedTrayable> model = (ReorderableListModel<OrderedTrayable>)listModel;

        if(e.getSource() == toStartButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            model.moveToStart(selection);
            iconListWidget.setSelectedIndex(0);
            setOrderIndices();
            return;
        }
        if(e.getSource() == moveLeftButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            model.shiftLeft(selection);
            if(selection != 0)
            {
                iconListWidget.setSelectedIndex(selection-1);
            }
            setOrderIndices();
            return;
        }
        if(e.getSource() == moveRightButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            model.shiftRight(selection);
            if(selection != model.getSize()-1)
            {
                iconListWidget.setSelectedIndex(selection+1);
            }
            setOrderIndices();
            return;
        }
        if(e.getSource() == toEndButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            model.moveToEnd(selection);
            iconListWidget.setSelectedIndex(model.getSize()-1);
            setOrderIndices();
            return;
        }
    }
    
    /**
     * Calls {@link OrderedTrayable#setOrderIndex(int)} on each item in
     * the tray, based on the order in the visible list.
     */
    protected synchronized void setOrderIndices()
    {
        for(int i = 0; i < listModel.getSize(); ++i )
        {
            OrderedTrayable item = listModel.getElementAt(i);
            item.setOrderIndex(i);
        }
    }
}
