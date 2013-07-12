/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.util.Orderable;

/**
 * An extension of IconTray to be used for displaying ordered items.
 *
 * @author jstewart
 * @code_status Complete
 */
public class OrderedIconTray extends IconTray implements ActionListener, ListSelectionListener
{
    /** A logger for emitting errors, warnings, etc. */
    //private static final Logger oitLogger = Logger.getLogger(OrderedIconTray.class);
    
    /** A panel to hold the order manipulation controls. */
    protected JPanel southPanel;
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
    
    /** boolean to indicate whether it should be in "orderable mode" */
    protected boolean isOrderable = true;;
    
    /**
     * Creates a new instance containing zero items.
     * 
     */
    public OrderedIconTray(final int layoutStyle, final int defWidth, final int defHeight)
    {
        super(layoutStyle, defWidth, defHeight);
        
        listModel = new ReorderableTrayListModel<Object>();
        iconListWidget.setModel(listModel);
        
        iconListWidget.addListSelectionListener(this);
        
        // rebuild the south panel to include the order manipulation buttons
        southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.LINE_AXIS));
    
        toStartButton = new JButton(IconManager.getIcon("FirstRec", IconSize.NonStd));
        toStartButton.setSize(20,20);
        toStartButton.setToolTipText("Move selection to first position"); // I18N
        moveLeftButton = new JButton(IconManager.getIcon("PrevRec", IconSize.NonStd));
        moveLeftButton.setSize(20,20);
        moveLeftButton.setToolTipText("Move selection left one position");// I18N
        moveRightButton = new JButton(IconManager.getIcon("NextRec", IconSize.NonStd));
        moveRightButton.setSize(20,20);
        moveRightButton.setToolTipText("Move selection right one position");// I18N
        toEndButton = new JButton(IconManager.getIcon("LastRec", IconSize.NonStd));
        toEndButton.setSize(20,20);
        toEndButton.setToolTipText("Move selection to last position");// I18N
        
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
        
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(toStartButton);
        southPanel.add(moveLeftButton);
        southPanel.add(moveRightButton);
        southPanel.add(toEndButton);
        southPanel.add(Box.createHorizontalGlue());
        
        //this.add(southPanel,BorderLayout.SOUTH);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb2 = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,4px,p"), this);
        pb2.add(listScrollPane, cc.xyw(1,2,3));
        pb2.add(southPanel, cc.xy(2,4));
    }
    
    /**
     * @param enable
     */
    public void enableOrderButtons(boolean enable)
    {
        toStartButton.setEnabled(enable);
        moveLeftButton.setEnabled(enable);
        moveRightButton.setEnabled(enable);
        toEndButton.setEnabled(enable);
    }

    /**
     * @param isOrderable the isOrderable to set
     */
    public void setOrderable(boolean isOrderable)
    {
        this.isOrderable = isOrderable;
        if (!isOrderable)
        {
            southPanel.setVisible(false);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        ReorderableTrayListModel<Object> model = (ReorderableTrayListModel<Object>)listModel;

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
            if (selRect != null)
            {
                listScrollPane.scrollRectToVisible(selRect);
            }
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
                if (selRect != null)
                {
                    listScrollPane.scrollRectToVisible(selRect);
                }
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
                if (selRect != null)
                {
                    listScrollPane.scrollRectToVisible(selRect);
                }
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
            if (selRect != null)
            {
                listScrollPane.scrollRectToVisible(selRect);
            }
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
        boolean changed = false;
        for(int i = 0; i < listModel.getSize(); ++i )
        {
            Object o = listModel.getElementAt(i);
            if (o instanceof Orderable)
            {
                Orderable orderable = (Orderable)o;
                if (i != orderable.getOrderIndex())
                {
                    orderable.setOrderIndex(i);
                    changed = true;
                }
            }
            
            if (changed)
            {
                // The values from this event should not be used for anything.
                // However, if they are the same, the PropertyChange framework quietly drops this event.
                firePropertyChange("item order", true, false);
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.IconTray#addItem(edu.ku.brc.ui.forms.FormDataObjIFace)
     */
    @Override
    public synchronized void addItem(final FormDataObjIFace item)
    {
        addItemInternal(item);
        if (isOrderable)
        {
            setOrderIndices();
        }
        
        // This is insane that I have to do the following
        // just to get it to resize the cell and scroll right.
        SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
        {
            @Override
            protected Boolean doInBackground() throws Exception
            {
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}

                return null;
            }

            @Override
            protected void done()
            {
                final int inx = listModel.getSize()-1;
                iconListWidget.setSelectedIndex(inx);
                toEndButton.doClick();
            }
        };
        worker.execute();

        //scrollToEnd();

//        
//        SwingUtilities.invokeLater(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try
//                {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {}
//                JScrollBar horz = listScrollPane.getHorizontalScrollBar();
//                System.out.println(horz.getMaximum());
//                horz.setValue( horz.getMaximum() );
//                
//                Dimension dim = listScrollPane.getViewport().getViewSize();
//                listScrollPane.getViewport().scrollRectToVisible(new Rectangle(dim.width-5, 0, 5, 5));
//            }
//        });
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
    /*@Override
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
    }*/
}
