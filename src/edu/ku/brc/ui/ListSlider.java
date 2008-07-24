/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * 
 * @code_status Alpha
 * 
 * @author megkumin
 * 
 */
@SuppressWarnings("serial")
public class ListSlider extends JPanel
{
    private static final Logger log                  = Logger.getLogger(ListSlider.class);
    private JList               sourceList;
    private JList               destinationList;
    private String              sourceListTitle      = "";
    private String              destinationListTitle = "";

    private DefaultListModel    sourceListModel;
    private DefaultListModel    destinationListModel;
    // TO DO: I18N
    private String              addString            = ">";//XXX 
    private String              removeString         = "<";//XXX 
    private String              addAllString         = ">>";//XXX 
    private String              removeAllString      = "<<";//XXX 

    private JButton             addButton;                               
    private JButton             removeButton;                                
    private JButton             addAllButton;                              
    private JButton             removeAllButton;      
    private Dimension           preferredListSize = new Dimension(150,150);
    //private Dimension           minimumListSize = new Dimension(50,50);
    //@SuppressWarnings("unused")
    //private Dimension           maximumListSize = new Dimension(300,500);
    private boolean             isGrowableList = true;
    private boolean             listChanged = false;
    //private DefaultListCellRenderer cellRenderer = null;

    /**
     * Constructor
     */
    @SuppressWarnings("unused")
    private ListSlider(String[] sourceData, DefaultListModel sourceModel, String sourceListTitle,
            String[] destinationData, DefaultListModel destinationModel, String destinationListTitle)
    {
        this.sourceListTitle = sourceListTitle;
        this.destinationListTitle = destinationListTitle;
        this.sourceListModel = sourceModel;
        this.destinationListModel = destinationModel;
        addDataToListModel(sourceListModel, sourceData);
        addDataToListModel(destinationListModel, destinationData);
        setupListComponents(sourceListModel, destinationListModel);
    }
    /**
     * Constructor
     */
    @SuppressWarnings("unused")
    private ListSlider(Object[] sourceData, DefaultListModel sourceModel, String sourceListTitle,
    		Object[] destinationData, DefaultListModel destinationModel, String destinationListTitle)
    {
        this.sourceListTitle = sourceListTitle;
        this.destinationListTitle = destinationListTitle;
        this.sourceListModel = sourceModel;
        this.destinationListModel = destinationModel;
        addDataToListModel(sourceListModel, sourceData);
        addDataToListModel(destinationListModel, destinationData);
        setupListComponents(sourceListModel, destinationListModel);
    }
    /**
     * Constructor
     */
    @SuppressWarnings("unused")
    private ListSlider(String[] sourceData, String sourceListTitle, String[] destinationData,
            String destinationListTitle)
    {
        this.sourceListTitle = sourceListTitle;
        this.destinationListTitle = destinationListTitle;

        this.sourceListModel = new DefaultListModel();
        addDataToListModel(sourceListModel, sourceData);
        //SortedListModel sourceSortedListModel = new SortedListModel(sourceListModel);

        this.destinationListModel = new DefaultListModel();
        addDataToListModel(destinationListModel, destinationData);
        //SortedListModel destSortedListModel = new SortedListModel(destinationListModel);

        setupListComponents(sourceListModel, destinationListModel);
    }

    /**
     * Constructor
     */
    public ListSlider(List<?> sourceData, String sourceListTitle, List<?> destinationData,
            String destinationListTitle)
    {
        this.sourceListTitle = sourceListTitle;
        this.destinationListTitle = destinationListTitle;

        this.sourceListModel = new DefaultListModel();
        addDataToListModel(sourceListModel, sourceData.toArray());
        //SortedListModel sourceSortedListModel = new SortedListModel(sourceListModel);

        this.destinationListModel = new DefaultListModel();
        addDataToListModel(destinationListModel, destinationData.toArray());
        //SortedListModel destSortedListModel = new SortedListModel(destinationListModel);

        setupListComponents(sourceListModel, destinationListModel);
    }   
    
    /**
     * Constructor
     */
    @SuppressWarnings("unused")
    private ListSlider(Object[] sourceData, String sourceListTitle, Object[] destinationData,
            String destinationListTitle)
    {
        this.sourceListTitle = sourceListTitle;
        this.destinationListTitle = destinationListTitle;

        this.sourceListModel = new DefaultListModel();
        addDataToListModel(sourceListModel, sourceData);
        //SortedListModel sourceSortedListModel = new SortedListModel(sourceListModel);

        this.destinationListModel = new DefaultListModel();
        addDataToListModel(destinationListModel, destinationData);
        //SortedListModel destSortedListModel = new SortedListModel(destinationListModel);

        setupListComponents(sourceListModel, destinationListModel);
    }
    
//    /**
//     * Constructor
//     */
//    public void resetData(List<?> sourceData, List<?> destinationData
//           )
//    {
//        //this.sourceListTitle = sourceListTitle;
//        //this.destinationListTitle = destinationListTitle;
//
//        //this.sourceListModel = new DefaultListModel();
//        addDataToListModel(sourceListModel, sourceData.toArray());
//        //SortedListModel sourceSortedListModel = new SortedListModel(sourceListModel);
//
//        //this.destinationListModel = new DefaultListModel();
//        addDataToListModel(destinationListModel, destinationData.toArray());
//        //SortedListModel destSortedListModel = new SortedListModel(destinationListModel);
//
//        setupListComponents(sourceListModel, destinationListModel);
//    }   

    
    /**
     * @param cellRenderer
     */
    public void setDefaultListCellRenderer(DefaultListCellRenderer cellRenderer) 
    {
    	sourceList.setCellRenderer(cellRenderer);
    	destinationList.setCellRenderer(cellRenderer);
    }
    
    /**
     * @param sourceListModel
     * @param destListModel
     */
    private void setupListComponents(DefaultListModel sourceListModel, DefaultListModel destListModel)
    {
        sourceList = new JList(sourceListModel);
        //if(cellRenderer!=null) sourceList.setCellRenderer(cellRenderer);
        sourceList.setSelectedIndex(0);
        sourceList.addListSelectionListener(new SourceListSelectionHandler());
        sourceList.setVisibleRowCount(5);

        JPanel sourcePanel = getListPanel(sourceList, sourceListTitle);

        destinationList = new JList(destListModel);
        //if(cellRenderer!=null)destinationList.setCellRenderer(cellRenderer);
        destinationList.setSelectedIndex(0);
        destinationList.addListSelectionListener(new DestinationListSelectionHandler());
        destinationList.setVisibleRowCount(5);

        JPanel destinationPanel = getListPanel(destinationList, destinationListTitle);

        addButton       = UIHelper.createButton(addString);
        addAllButton    = UIHelper.createButton(addAllString);
        removeButton    = UIHelper.createButton(removeString);
        removeAllButton = UIHelper.createButton(removeAllString);
  
        AddListener addListener = new AddListener();
        addButton.addActionListener(addListener);
        addButton.setEnabled(true);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setMaximumSize(addButton.getPreferredSize());

        AddAllListener addAllListener = new AddAllListener();
        addAllButton.addActionListener(addAllListener);
        addAllButton.setEnabled(true);
        addAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addAllButton.setMaximumSize(addAllButton.getPreferredSize());
        
        RemoveListener removeListener = new RemoveListener();
        removeButton.addActionListener(removeListener);
        removeButton.setEnabled(false);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setMaximumSize(removeButton.getPreferredSize());
        
        RemoveAllListener removeAllListener = new RemoveAllListener(); 
        removeAllButton.addActionListener(removeAllListener);
        removeAllButton.setEnabled(true);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeAllButton.setMaximumSize(removeAllButton.getPreferredSize());

        ButtonStackBuilder builder = new ButtonStackBuilder();
        builder.addGridded(addAllButton);
        builder.addRelatedGap();
        builder.addGridded(addButton);
        builder.addRelatedGap();
        builder.addGridded(removeButton);
        builder.addRelatedGap();
        builder.addGridded(removeAllButton);
        JPanel buttonPanel  = builder.getPanel();
        PanelBuilder    panelBuilder;
        if(isGrowableList)
        {
         panelBuilder    = new PanelBuilder(
                new FormLayout("f:d:g(0.5),  7dlu,  c:p,  7dlu,  f:d:g(0.5)", 
                               "f:pref:g(0.5),  pref,  f:pref:g(0.5)"), this);
        
        }
        else
        {
              panelBuilder    = new PanelBuilder(
                    new FormLayout("f:d,  7dlu,  c:p,  7dlu,  f:d", 
                                   "t:d,  c:pref,  t:d"), this);            
        }
        CellConstraints cc = new CellConstraints();
        panelBuilder.add(sourcePanel, cc.xywh(1, 1,1,3));
        panelBuilder.add(buttonPanel, cc.xy(3, 2));
        panelBuilder.add(destinationPanel, cc.xywh(5, 1,1,3));  
        
        //cc.xy
       
    }

    public void setExternalActionListener(ActionListener listener)
    {
        addButton.addActionListener(listener);
        addAllButton.addActionListener(listener);
        removeButton.addActionListener(listener);
        removeAllButton.addActionListener(listener);
    }
    /**
     * @return
     */
    public Object[] getDestinationData()
    {
        if (destinationListModel == null)
        {
            log.debug("No data selected");
            return null;
        }
        
        int listSize = destinationListModel.getSize();
        Object[] values = new Object[listSize];
        for (int i = 0; i < listSize; i++)
        {
            values[i] = destinationListModel.getElementAt(i);
        }
        log.debug("The following data is in the destination list"); 
        for (int i = 0; i < values.length; i++)
            log.debug(values[i].toString());
        return values;
    }
    
    /**
     * @return
     */
    public Object[] getSourceData()
    {
        if (sourceListModel == null)
        {
            log.debug("No data selected");
            return null;
        }
        
        int listSize = sourceListModel.getSize();
        Object[] values = new Object[listSize];
        for (int i = 0; i < listSize; i++)
        {
            values[i] = sourceListModel.getElementAt(i);
        }
        log.debug("The following data is in the destination list"); 
        for (int i = 0; i < values.length; i++)
            log.debug(values[i].toString());
        return values;
    }
    
    /**
     * @return
     */
    public JList getDestinationList()
    {
        return new JList(destinationListModel);
    }
    
    /**
     * @return
     */
    public JList getSourceList()
    {
        return new JList(sourceListModel);
    }
    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the
     * event-dispatching thread.
     */
    public static void testListSlider()
    {
        String sTitle = "Available Collections";
        String dTitle = "Collections Assigned to User";
        String[] sData = { "ABC", "123", "xyz", "cat", "dog", "bird", "mammal", 
                "crocodile", "alligator", "monkey",  "giraffe", "lizard", "ostrich", 
                "kangaroo", "crocodile", "alligator", "monkey",  "giraffe"};
        String[] dData = { "" };

        ListSlider testSlider = new ListSlider(sData, sTitle, dData, dTitle);
        testSlider.setOpaque(true);
        
        JFrame frame = new JFrame("ListSlider");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(testSlider);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @param d
     */
    public void setPreferredListSize(Dimension d)
    {
        preferredListSize = d;

    }

    //public void setMinimumListSize(Dimension d)
    //{
    //    minimumListSize = d;
    //}

    public void setGrowableLists(boolean b)
    {
        isGrowableList = b;
    }

     /**
     * @param list
     * @param title
     * @return
     */
    public JPanel getListPanel(JList list, String title)
     {
         JScrollPane scrollPane = new JScrollPane(list);
         scrollPane.setPreferredSize(preferredListSize);
         //scrollPane.setMinimumSize(minimumListSize);
        // scrollPane.setMaximumSize(maximumListSize);
         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         panel.setBorder(BorderFactory.createTitledBorder(title));
         panel.add(scrollPane, BorderLayout.CENTER);
         return panel;
     }

    /**
     * @param args -
     *            void
     */
    public static void main(String[] args)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                testListSlider();
            }
        });
    }
    
    /**
     * @param ids
     */
    public void setSourceSelectedValues(Integer[] ids)
    {
        log.debug("setSourceSelectedValues");
        Object[] vals = getSourceData();
        List<Integer> indicesToMove = new ArrayList<Integer> ();
        for(int i =0; i < vals.length; i++)
        {
            Object o = vals[i];
            if(o instanceof SpPrincipal)
            {
                SpPrincipal spug = (SpPrincipal)o;
                Integer spugID = spug.getId();
                for(int j = 0 ; j < ids.length; j++)
                {
                    if(ids[j].intValue()== spugID.intValue())
                    {
                        log.debug("preparint to move: " + i);
                        indicesToMove.add(new Integer(i));
                    }
                }

            }
            else if(o instanceof Discipline)
            {
                Discipline disc = (Discipline)o;
                Integer ctID = disc.getId();
                for(int j = 0 ; j < ids.length; j++)
                {
                    if(ids[j].intValue()== ctID.intValue())
                    {
                        indicesToMove.add(new Integer(i));
                    }
                }
            }
            else if(o instanceof SpecifyUser)
            {
                SpecifyUser ct = (SpecifyUser)o;
                Integer ctID = ct.getId();
                for(int j = 0 ; j < ids.length; j++)
                {
                    if(ids[j].intValue()== ctID.intValue())
                    {
                        indicesToMove.add(new Integer(i));
                    }
                }
            }
        }
        int [] indices = new int [indicesToMove.size()];
        for(int i = 0; i< indicesToMove.size(); i ++)
        {
            indices[i] = (Integer)indicesToMove.get(i).intValue();    
            log.debug("selecting: " +  (Integer)indicesToMove.get(i).intValue() );
        }
        sourceList.setSelectedIndices(indices);
        moveSelectedSourceDataToDestination();
    }    

    /**
     * 
     */
    public void moveSelectedDestinationDataToSource()
    {
        Object[] selectedVals = destinationList.getSelectedValues();
        addDataToListModel(sourceListModel, selectedVals);
        removeDataFromListModel(destinationListModel, selectedVals);
        destinationList.setSelectedIndex(0);
        if(destinationList.getModel().getSize()==0) sourceList.setSelectedIndex(1);       
    }
   
    /**
     * 
     */
    public void moveAllDestinationDataToSource()
    {
        
        Object[] selectedVals = new Object[destinationList.getModel().getSize()];
        for(int i =0; i< destinationList.getModel().getSize(); i++) 
        {
            selectedVals[i] = destinationList.getModel().getElementAt(i);
        }
        addDataToListModel(sourceListModel, selectedVals);
        removeDataFromListModel(destinationListModel, selectedVals);
        destinationList.setSelectedIndex(0);
        if(destinationList.getModel().getSize()==0) sourceList.setSelectedIndex(1);       
    }
    
    /**
     * 
     */
    public void moveSelectedSourceDataToDestination()
    {
        Object[] selectedVals = sourceList.getSelectedValues();
        addDataToListModel(destinationListModel, selectedVals);
        removeDataFromListModel(sourceListModel, selectedVals);
        sourceList.setSelectedIndex(0);
        if(sourceList.getModel().getSize()==0)destinationList.setSelectedIndex(1);       
    }

    /**
     * 
     */
    public void moveAllSourceDataToDestination()
    {
        Object[] selectedVals = new Object[sourceList.getModel().getSize()];
        for(int i =0; i< sourceList.getModel().getSize(); i++) 
        {
            selectedVals[i] = sourceList.getModel().getElementAt(i);
        }
        addDataToListModel(destinationListModel, selectedVals);
        removeDataFromListModel(sourceListModel, selectedVals);
        sourceList.setSelectedIndex(0);
        if(sourceList.getModel().getSize()==0)destinationList.setSelectedIndex(1);       
    }
    
    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     */
    class AddListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            moveSelectedSourceDataToDestination();
            listChanged = true;
        }
    }

    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     */
    class RemoveListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            moveSelectedDestinationDataToSource();
            listChanged = true;
        }
    }
    
    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     */
    class AddAllListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            sourceList.setSelectionInterval(0, sourceList.getModel().getSize()-1);
            moveSelectedSourceDataToDestination();
            listChanged = true;
        }
    }

    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     */
    class RemoveAllListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            destinationList.setSelectionInterval(0, destinationList.getModel().getSize()-1);
            moveSelectedDestinationDataToSource();
            listChanged = true;
        }
    }
    
    /**
     * @param model
     * @param newValues
     */
    private void addDataToListModel(DefaultListModel model, Object newValues[])
    {
        for (int i = 0; i < newValues.length; i++)
        {
            model.addElement(newValues[i]);
        }
        getDestinationData();
    }

    /**
     * @param model
     * @param newValues
     */
    private void removeDataFromListModel(DefaultListModel model, Object newValues[])
    {
        for (int i = 0; i < newValues.length; i++)
        {
            model.removeElement(newValues[i]);
        }
        getDestinationData();
    }

    /**
     * @param e
     */
    private void setButtons(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting() == false)
        {
            if (sourceList.getSelectedIndex() == -1)
            {
                addButton.setEnabled(false);
                addAllButton.setEnabled(false);
            } else
            {
                addButton.setEnabled(true);
                addAllButton.setEnabled(true);
            }
            if (destinationList.getSelectedIndex() == -1)
            {
                removeButton.setEnabled(false);
                removeAllButton.setEnabled(false);
            } else
            {
                removeButton.setEnabled(true);
                removeAllButton.setEnabled(true);
            }
        }
    }
    
    /**
     * @param b
     */
    /*private void enableButtons(boolean b)
    {
        addButton.setEnabled(b);
        addAllButton.setEnabled(b);
        removeButton.setEnabled(b);
        removeAllButton.setEnabled(b);
    }*/

    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     */
    class SourceListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            setButtons(e);
        }
    }

    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     */
    class DestinationListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            setButtons(e);
        }
    }
    /**
     * @return the listChanged
     */
    public boolean isListChanged()
    {
        return listChanged;
    }
    /**
     * @param listChanged the listChanged to set
     */
    public void setListChanged(boolean listChanged)
    {
        this.listChanged = listChanged;
    }

}
