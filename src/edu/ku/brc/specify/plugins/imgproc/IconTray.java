/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.imgproc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseListener;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.DefaultModifiableListModel;
import edu.ku.brc.ui.ModifiableListModel;
import edu.ku.brc.ui.renderers.TrayListCellRenderer;
import edu.ku.brc.util.FormDataObjComparator;
import edu.ku.brc.util.Orderable;

/**
 * A GUI component for use in displaying a collection of associated objects.  The related
 * objects have to implement the {@link Trayable} interface.
 *
 * @author jstewart
 * @code_status Complete
 */
public class IconTray<Trayable> extends JPanel implements ChangeListener
{
    /** A logger for emitting errors, warnings, etc. */
    protected static final Logger log = Logger.getLogger(IconTray.class);
    
    public static final int SINGLE_ROW = 1;
    public static final int MULTIPLE_ROWS = 2;
    
    protected int minHeight = 64;
    protected int maxWidth = 750;

    /** A JList used to display the icons representing the items. */
    protected JList iconListWidget;
    /** The model holding the included items. */
    protected ModifiableListModel<Object> listModel;
    /** A JScrollPane containing the iconListWidget. */
    protected JScrollPane listScrollPane;
    
    protected int style;

    /**
     * Creates a new IconTray containing zero items.
     */
    public IconTray(int layoutStyle)
    {
        style = layoutStyle;
        listModel = new DefaultModifiableListModel<Object>();
        ListCellRenderer renderer = new TrayListCellRenderer(this);
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
    
    /* (non-Javadoc)
     * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
     */
    @Override
    public synchronized void addMouseListener(MouseListener l)
    {
        super.addMouseListener(l);
        iconListWidget.addMouseListener(l);
        
    }
    
    /**
     * @param listener
     */
    public void addListSelectionListener(final ListSelectionListener listener)
    {
        iconListWidget.addListSelectionListener(listener);
    }

    /**
     * @param listener
     */
    public void removeListSelectionListener(final ListSelectionListener listener)
    {
        iconListWidget.removeListSelectionListener(listener);
    }

    /**
     * @return
     */
    public Trayable getSelectedValue()
    {
        return (Trayable)iconListWidget.getSelectedValue();
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
     * Reorders all the items.
     */
    protected void reorder()
    {
        for (int i=0;i<listModel.getSize();i++)
        {
            Object obj = listModel.getElementAt(i);
            if (obj instanceof Orderable)
            {
                ((Orderable)obj).setOrderIndex(i);
            }
        }
    }
    
    /**
     * Adds the specified item to the end of this tray. 
     *
     * @param item the item to be added
     * @see DefaultListModel#addElement(Object)
     */
    public synchronized void addItem(final Trayable item)
    {
        listModel.add(item);
        //System.out.println(listModel.getSize());
        reorder();
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
    public synchronized boolean removeItem(final Trayable item)
    {
        boolean retVal = listModel.remove(item);
        iconListWidget.clearSelection();
        reorder();
        return retVal;
    }
    
    /**
     * Removes all the items from the IconTray.
     */
    public synchronized void removeAllItems()
    {
        while (listModel.getSize() > 0)
        {
            listModel.clear();
        }
    }
    
    /**
     * Returns a Set containing all items in the tray.
     * 
     * @return a Set containing all items in the tray
     */
    public synchronized Set<Object> getItems()
    {
        HashSet<Object> set = new HashSet<Object>();
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
        // need to set the min height to something other than 0 so
        // that empty trays don't get flattened by containers that
        // use preferred size
        
        Dimension d = super.getPreferredSize();
        d.height = Math.max(minHeight, d.height);
        d.width = Math.min(maxWidth, d.width);
        return d;
    }
    
    /**
     * @return
     */
    public Trayable getSelection()
    {
        Object selection = iconListWidget.getSelectedValue();
        if (selection == null)
        {
            return null;
        }
        
        return (Trayable)selection;
    }
    
    /**
     * @param index index to be selected
     */
    public void setSelectedIndex(final int index)
    {
        iconListWidget.setSelectedIndex(-1);
    }
    
    /**
     * Sorts the set of values passed in during a call to setValue.  The sort
     * is done using a {@link FormDataObjComparator}.
     * 
     * @param values the Set of {@link Trayable} objects
     * @return a Vector of {@link Trayable} that is sorted according to a {@link FormDataObjComparator}
     */
    protected Vector<Trayable> sortSet(Set<?> values)
    {
        Vector<Trayable> tmpList = new Vector<Trayable>(values.size());
        for (Object o: values)
        {
            Trayable obj = (Trayable)o;
            tmpList.add(obj);
        }
        //Collections.sort(tmpList, new FormDataObjComparator());
        return tmpList;
    }
    

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e)
    {
    }
}
