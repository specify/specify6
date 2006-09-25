/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * A GUI component for use in displaying a collection of associated objects.  The related
 * objects have to implement the {@link Trayable} interface.
 *
 * @author jstewart
 * @code_status Complete
 */
public class IconTray<T extends Trayable> extends JPanel
{
    /** A JList used to display the icons representing the items. */
    protected JList iconListWidget;
    /** The model holding the included items. */
    protected ModifiableListModel<T> listModel;
    /** A JScrollPane containing the iconListWidget. */
    protected JScrollPane listScrollPane;
    /** A cell renderer capable of correctly rendering {@link Trayable} objects. */
    protected TrayableListCellRenderer renderer;
    
    /**
     * Creates a new IconTray containing zero items.
     */
    public IconTray()
    {
        listModel = new DefaultModifiableListModel<T>();
        renderer = new TrayableListCellRenderer();
        iconListWidget = new JList(listModel);
        iconListWidget.setCellRenderer(renderer);
        iconListWidget.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        iconListWidget.setVisibleRowCount(1);
        
        JPanel listPanel = new JPanel();
        listPanel.setBackground(iconListWidget.getBackground());
        listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.LINE_AXIS));
        listPanel.add(Box.createHorizontalGlue());
        listPanel.add(iconListWidget);
        listPanel.add(Box.createHorizontalGlue());
        
        listScrollPane = new JScrollPane(listPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        this.setLayout(new BorderLayout());
        this.add(listScrollPane,BorderLayout.CENTER);
    }
    
    /**
     * Sets the height of every cell in the list.  If <code>height</code>
     * is -1, cell heights are computed by applying <code>getPreferredSize</code>
     * to the <code>cellRenderer</code> component for each list element.
     * <p>
     * The default value of this property is -1.
     * <p>
     *
     * @param height an integer giving the height, in pixels, for all cells 
     *        in this list
     * @see JList#getPrototypeCellValue
     * @see JList#setFixedCellWidth
     * @see JComponent#addPropertyChangeListener
     */
    public synchronized void setFixedCellHeight(int height)
    {
        iconListWidget.setFixedCellHeight(height);
    }
    
    /**
     * Adds the specified item to the end of this tray. 
     *
     * @param trayable the item to be added
     * @see DefaultListModel#addElement(Object)
     */
    public synchronized void addItem(T t)
    {
        listModel.add(t);
    }
    
    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this list.
     *
     * @param   trayable the component to be removed
     * @return  <code>true</code> if the argument was a component of this
     *          tray; <code>false</code> otherwise
     * @see DefaultListModel#removeElement(Object)
     */
    public synchronized boolean removeItem(T t)
    {
        return ((DefaultListModel)listModel).removeElement(t);
    }
    
    /**
     * Removes the index-th item from the tray.
     * 
     * @param index the index of the item to remove
     * @return the item that was removed
     */
    public synchronized T removeItem(int index)
    {
        return listModel.remove(index);
    }
    
    /**
     * Returns a Set containing all items in the tray.
     * 
     * @return a Set containing all items in the tray
     */
    public synchronized Set<T> getItems()
    {
        HashSet<T> set = new HashSet<T>();
        for(int i = 0; i < listModel.getSize(); ++i)
        {
            set.add(listModel.getElementAt(i));
        }
        return set;
    }
}
