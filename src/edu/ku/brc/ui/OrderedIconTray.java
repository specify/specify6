/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.util.FormDataObjComparator;
import edu.ku.brc.util.Orderable;
import edu.ku.brc.util.OrderableComparator;

/**
 * An extension of IconTray to be used for displaying ordered items.
 *
 * @author jstewart
 * @code_status Complete
 */
public class OrderedIconTray extends IconTray implements ActionListener, ListSelectionListener
{
    /** A logger for emitting errors, warnings, etc. */
    private static final Logger oitLogger = Logger.getLogger(OrderedIconTray.class);
    
    /** A button that moves the selection to the start of the order. */
    protected JButton toStartButton;
    /** A button that moves the selection to the left in the order. */
    protected JButton moveLeftButton;
    /** A button that moves the selection to the right in the order. */
    protected JButton moveRightButton;
    /** A button that moves the selection to the end of the order. */
    protected JButton toEndButton;
    /** An array of the buttons used to rearrange elements.  For ease of disabling/enabling all buttons. */
    protected JButton[] orderButtons;
    
    /**
     * Creates a new instance containing zero items.
     * 
     */
    public OrderedIconTray(String parentClass, String setName)
    {
        super(parentClass,setName);
        
        listModel = new ReorderableTrayListModel<FormDataObjIFace>();
        iconListWidget.setModel(listModel);
        
        iconListWidget.addListSelectionListener(this);
        
        // rebuild the south panel to include the order manipulation buttons
        southPanel.removeAll();
        southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.LINE_AXIS));
    
        toStartButton = new JButton(IconManager.getIcon("move_to_start", IconSize.Std16));
        toStartButton.setSize(20,20);
        toStartButton.setToolTipText("Move selection to first position");
        moveLeftButton = new JButton(IconManager.getIcon("move_left", IconSize.Std16));
        moveLeftButton.setSize(20,20);
        moveLeftButton.setToolTipText("Move selection left one position");
        moveRightButton = new JButton(IconManager.getIcon("move_right", IconSize.Std16));
        moveRightButton.setSize(20,20);
        moveRightButton.setToolTipText("Move selection right one position");
        toEndButton = new JButton(IconManager.getIcon("move_to_end", IconSize.Std16));
        toEndButton.setSize(20,20);
        toEndButton.setToolTipText("Move selection to last position");
        
        orderButtons = new JButton[4];
        orderButtons[0] = toStartButton;
        orderButtons[1] = moveLeftButton;
        orderButtons[2] = moveRightButton;
        orderButtons[3] = toEndButton;

        // to initialize the buttons as disabled
        valueChanged(null);
        
        toStartButton.addActionListener(this);
        moveLeftButton.addActionListener(this);
        moveRightButton.addActionListener(this);
        toEndButton.addActionListener(this);
        
        southPanel.add(Box.createRigidArea(newButton.getSize()));
        southPanel.add(Box.createRigidArea(editButton.getSize()));
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(toStartButton);
        southPanel.add(moveLeftButton);
        southPanel.add(moveRightButton);
        southPanel.add(toEndButton);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(editButton);
        southPanel.add(newButton);
        
        this.add(southPanel,BorderLayout.SOUTH);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        ReorderableTrayListModel<FormDataObjIFace> model = (ReorderableTrayListModel<FormDataObjIFace>)listModel;

        if(e.getSource() == toStartButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            if (selection == -1)
            {
                return;
            }
            model.moveToStart(selection);
            iconListWidget.setSelectedIndex(0);
            Rectangle selRect = iconListWidget.getUI().getCellBounds(iconListWidget, selection-1, selection-1);
            listScrollPane.scrollRectToVisible(selRect);
            setOrderIndices();
            return;
        }
        if(e.getSource() == moveLeftButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            if (selection == -1)
            {
                return;
            }
            model.shiftLeft(selection);
            if(selection != 0)
            {
                iconListWidget.setSelectedIndex(selection-1);
                Rectangle selRect = iconListWidget.getUI().getCellBounds(iconListWidget, 0, 0);
                listScrollPane.scrollRectToVisible(selRect);
            }
            setOrderIndices();
            return;
        }
        if(e.getSource() == moveRightButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            if (selection == -1)
            {
                return;
            }
            model.shiftRight(selection);
            if(selection != model.getSize()-1)
            {
                iconListWidget.setSelectedIndex(selection+1);
                Rectangle selRect = iconListWidget.getUI().getCellBounds(iconListWidget, selection+1, selection+1);
                listScrollPane.scrollRectToVisible(selRect);
            }
            setOrderIndices();
            return;
        }
        if(e.getSource() == toEndButton)
        {
            int selection = iconListWidget.getSelectedIndex();
            if (selection == -1)
            {
                return;
            }
            model.moveToEnd(selection);
            iconListWidget.setSelectedIndex(model.getSize()-1);
            Rectangle selRect = iconListWidget.getUI().getCellBounds(iconListWidget, model.getSize()-1, model.getSize()-1);
            listScrollPane.scrollRectToVisible(selRect);
            setOrderIndices();
            return;
        }
    }
    
    /**
     * Calls {@link OrderableFormDataObj#setOrderIndex(int)} on each item in
     * the tray, based on the order in the visible list.
     */
    protected synchronized void setOrderIndices()
    {
        for(int i = 0; i < listModel.getSize(); ++i )
        {
            FormDataObjIFace o = listModel.getElementAt(i);
            if (o instanceof Orderable)
            {
                Orderable orderable = (Orderable)o;
                orderable.setOrderIndex(i);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
        boolean enable = true;
        if (iconListWidget.getSelectedIndex() == -1)
        {
            enable = false;
        }
        
        for (JButton button: orderButtons)
        {
            button.setEnabled(enable);
        }
    }

    /**
     * Sorts the set of {@link FormDataObjIFace} objects passed in during a call
     * to setValue.  The sort is done using an {@link OrderableComparator} instead
     * of the more generic {@link FormDataObjComparator}.
     * 
     * @see edu.ku.brc.ui.IconTray#sortSet(java.util.Set)
     * @param values the unsorted set of {@link Orderable} objects
     * @return a sorted Vector of {@link FormDataObjIFace} objects
     */
    @Override
    protected Vector<FormDataObjIFace> sortSet(Set<?> values)
    {
        Vector<Orderable> tmpList = new Vector<Orderable>(values.size());
        for (Object o: values)
        {
            try
            {
                Orderable obj = (Orderable)o;
                tmpList.add(obj);
            }
            catch( ClassCastException cce )
            {
                // if we get here, somebody tried to use an OrderedIconTray for a Set of items
                // that didn't all implement the Orderable interface
                oitLogger.warn("OrderedIconTray being used for non-Orderable data set");
                return super.sortSet(values);
            }
        }
        Collections.sort(tmpList, new OrderableComparator());
        
        Vector<FormDataObjIFace> retVec = new Vector<FormDataObjIFace>(tmpList.size());
        for (Orderable o: tmpList)
        {
            FormDataObjIFace fo = (FormDataObjIFace)o;
            retVec.add(fo);
        }
        
        return retVec;
    }
}
