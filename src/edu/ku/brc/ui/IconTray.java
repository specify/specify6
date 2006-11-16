/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.renderers.TrayListCellRenderer;
import edu.ku.brc.util.FormDataObjComparator;

/**
 * A GUI component for use in displaying a collection of associated objects.  The related
 * objects have to implement the {@link FormDataObjIFace} interface.
 *
 * @author jstewart
 * @code_status Complete
 */
public class IconTray extends JPanel
{
    /** A logger for emitting errors, warnings, etc. */
    protected static final Logger log = Logger.getLogger(IconTray.class);
    
    public static final int SINGLE_ROW = 1;
    public static final int MULTIPLE_ROWS = 2;

    /** A JList used to display the icons representing the items. */
    protected JList iconListWidget;
    /** The model holding the included items. */
    protected ModifiableListModel<FormDataObjIFace> listModel;
    /** A JScrollPane containing the iconListWidget. */
    protected JScrollPane listScrollPane;
    
    protected int style;

    /**
     * Creates a new IconTray containing zero items.
     */
    public IconTray(int layoutStyle)
    {
        style = layoutStyle;
        listModel = new DefaultModifiableListModel<FormDataObjIFace>();
        ListCellRenderer renderer = new TrayListCellRenderer();
        iconListWidget = new JList(listModel);
        iconListWidget.setCellRenderer(renderer);
        iconListWidget.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        if (style == SINGLE_ROW)
        {
            iconListWidget.setVisibleRowCount(1);
        }
        iconListWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
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
    
    @Override
    public synchronized void addMouseListener(MouseListener l)
    {
        super.addMouseListener(l);
        iconListWidget.addMouseListener(l);
    }

    public FormDataObjIFace getSelectedValue()
    {
        return (FormDataObjIFace)iconListWidget.getSelectedValue();
    }
    
    /**
     * Sets the cell renderer used by the tray to render the individual objects listed.
     * 
     * @param renderer a cell renderer
     */
    public void setCellRenderer(ListCellRenderer renderer)
    {
        iconListWidget.setCellRenderer(renderer);
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
     * @param item the item to be added
     * @see DefaultListModel#addElement(Object)
     */
    public synchronized void addItem(FormDataObjIFace item)
    {
        listModel.add(item);
    }
    
    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this list.
     *
     * @param   item the component to be removed
     * @return  <code>true</code> if the argument was a component of this
     *          tray; <code>false</code> otherwise
     * @see DefaultListModel#removeElement(Object)
     */
    public synchronized boolean removeItem(FormDataObjIFace item)
    {
        boolean retVal = listModel.remove(item);
        return retVal;
    }
    
    /**
     * Removes all the items from the IconTray.
     */
    public synchronized void removeAllItems()
    {
        while (listModel.getSize() > 0)
        {
            listModel.remove(0);
        }
    }
    
    /**
     * Returns a Set containing all items in the tray.
     * 
     * @return a Set containing all items in the tray
     */
    public synchronized Set<FormDataObjIFace> getItems()
    {
        HashSet<FormDataObjIFace> set = new HashSet<FormDataObjIFace>();
        for(int i = 0; i < listModel.getSize(); ++i)
        {
            set.add(listModel.getElementAt(i));
        }
        return set;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        int scrollbarHeight = listScrollPane.getHorizontalScrollBar().getHeight();
        int minRowsVis = (style == SINGLE_ROW) ? 1 : 3;
        int minHeight = minRowsVis*48 + scrollbarHeight;
        return new Dimension(this.getWidth(),minHeight);
    }
    
    public FormDataObjIFace getSelection()
    {
        Object selection = iconListWidget.getSelectedValue();
        if (selection==null)
        {
            return null;
        }
        
        return (FormDataObjIFace)selection;
    }
    
    /**
     * Sorts the set of values passed in during a call to setValue.  The sort
     * is done using a {@link FormDataObjComparator}.
     * 
     * @param values the Set of {@link FormDataObjIFace} objects
     * @return a Vector of {@link FormDataObjIFace} that is sorted according to a {@link FormDataObjComparator}
     */
    protected Vector<FormDataObjIFace> sortSet(Set<?> values)
    {
        Vector<FormDataObjIFace> tmpList = new Vector<FormDataObjIFace>(values.size());
        for (Object o: values)
        {
            FormDataObjIFace obj = (FormDataObjIFace)o;
            tmpList.add(obj);
        }
        Collections.sort(tmpList, new FormDataObjComparator());
        return tmpList;
    }
}
