/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.renderers.TrayListCellRenderer;
import edu.ku.brc.util.FormDataObjComparator;

/**
 * A GUI component for use in displaying a collection of associated objects.  The related
 * objects have to implement the {@link DataModelObjBase} interface.
 *
 * @author jstewart
 * @code_status Complete
 */
public class IconTray extends JPanel implements GetSetValueIFace
{
    /** A logger for emitting errors, warnings, etc. */
    protected static final Logger log = Logger.getLogger(IconTray.class);

    /** A JList used to display the icons representing the items. */
    protected JList iconListWidget;
    /** A button to trigger the creation and addition of a new item. */
    protected JButton addButton;
    /** The model holding the included items. */
    protected ModifiableListModel<FormDataObjIFace> listModel;
    /** A JScrollPane containing the iconListWidget. */
    protected JScrollPane listScrollPane;
    /** The set of datamodel objects to be displayed. */
    protected Set<Object> dataSet;
    /** A JPanel to hold the 'new' and 'edit' buttons. */
    protected JPanel southPanel;
    /** A JButton used to edit the selected record. */
    protected JButton editButton;
    /** A JButton used to create a new record. */
    protected JButton newButton;
    
    /**
     * Creates a new IconTray containing zero items.
     */
    public IconTray()
    {
        listModel = new DefaultModifiableListModel<FormDataObjIFace>();
        ListCellRenderer renderer = new TrayListCellRenderer();
        iconListWidget = new JList(listModel);
        iconListWidget.setCellRenderer(renderer);
        iconListWidget.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        iconListWidget.setVisibleRowCount(1);
        iconListWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JPanel listPanel = new JPanel();
        listPanel.setBackground(iconListWidget.getBackground());
        listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.LINE_AXIS));
        listPanel.add(Box.createHorizontalGlue());
        listPanel.add(iconListWidget);
        listPanel.add(Box.createHorizontalGlue());
        
        // this layout stretches, but doesn't center
        //listPanel.setLayout(new GridLayout(1,1));
        //listPanel.add(iconListWidget);
        
        // this layout stretches, but doesn't center
        //listPanel.setLayout(new BorderLayout());
        //listPanel.add(iconListWidget,BorderLayout.CENTER);
        
        listScrollPane = new JScrollPane(listPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.LINE_AXIS));
        editButton = createButton("EditForm", getResourceString("EditRecord"));
        newButton = createButton("CreateObj", getResourceString("NewRecord"));
        
        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                Object selection = iconListWidget.getSelectedValue();
                if (selection==null)
                {
                    return;
                }
                
                FormDataObjIFace formObj = (FormDataObjIFace)selection;
                
                //TODO: display an edit dialog for the selected object
                log.warn("Display edit dialog for " + formObj.getIdentityTitle());
            }
        });

        newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //TODO: create a new object
                //FormHelper.createAndNewDataObj(classObj);
                //TODO: display an edit dialog for a new object
                log.warn("Display edit dialog for a new object");
            }
        });

        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(editButton);
        southPanel.add(newButton);
        
        this.setLayout(new BorderLayout());
        this.add(listScrollPane,BorderLayout.CENTER);
        this.add(southPanel,BorderLayout.SOUTH);
    }
    
    /**
     * A utility method used to create the 'edit' and 'new' buttons.
     * 
     * @param iconName the name of the icon to use for the button
     * @param toolTip the tooltip text for the button
     * @return a button
     */
    protected JButton createButton(String iconName, String toolTip)
    {
        JButton btn = new JButton(IconManager.getIcon(iconName, IconManager.IconSize.Std16));
        btn.setToolTipText(toolTip);
        btn.setFocusable(false);
        btn.setMargin(new Insets(1,1,1,1));
        btn.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        return btn;
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
     * Removes the index-th item from the tray.
     * 
     * @param index the index of the item to remove
     * @return the item that was removed
     */
    public synchronized FormDataObjIFace removeItem(int index)
    {
        FormDataObjIFace removedObj = listModel.remove(index);
        return removedObj;
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
    
    /**
     * Clears the contents of the tray.
     */
    public synchronized void clear()
    {
        listModel.clear();
    }
    
    /**
     * Adds all of the objects in the given collection to the tray.
     * 
     * @param items a collection of FormDataObjIFace objects
     */
    public synchronized void addAll(Collection<? extends FormDataObjIFace> items)
    {
        for (FormDataObjIFace item: items)
        {
            listModel.add(item);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        dataSet.clear();
        dataSet.addAll(getItems());
        return dataSet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object value, String defaultValue)
    {
        listModel.clear();
        if (!(value instanceof Set))
        {
            throw new IllegalArgumentException("value must be an instance of java.util.Set");
        }
        
        dataSet = (Set)value;

        Vector<FormDataObjIFace> tmpList = sortSet(dataSet);
        for (FormDataObjIFace dataObj: tmpList)
        {
            listModel.add(dataObj);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        Graphics graphics = getGraphics();
        if (graphics == null)
        {
            return new Dimension(100,100);
        }
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int fontHeight = fontMetrics.getHeight();
        int minIconHeight = 32;
        int scrollbarHeight = listScrollPane.getHorizontalScrollBar().getHeight();
        int southPanelHeight = southPanel.getPreferredSize().height;
        int minHeight = fontHeight + minIconHeight + scrollbarHeight + southPanelHeight;
        return new Dimension(listScrollPane.getWidth(),minHeight);
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
