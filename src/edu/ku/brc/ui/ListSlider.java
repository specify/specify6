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

import javax.swing.BorderFactory;
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
    private String              addString            = "Add";
    private String              removeString         = "Remove";
    private String              addAllString         = "Add All";
    private String              removeAllString     = "Remove All";

    private JButton             addButton;                               
    private JButton             removeButton;                                
    private JButton             addAllButton;                              
    private JButton             removeAllButton;      
    private Dimension           preferredListSize = new Dimension(200,300);
    private Dimension           minimumListSize = new Dimension(50,50);
    private Dimension           maximumListSize = new Dimension(500,500);
    private boolean             isGrowableList = true;

    /**
     * Constructor
     */
    public ListSlider(String[] sourceData, DefaultListModel sourceModel, String sourceListTitle,
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
    public ListSlider(String[] sourceData, String sourceListTitle, String[] destinationData,
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

    private void setupListComponents(DefaultListModel sourceListModel, DefaultListModel destListModel)
    {
        sourceList = new JList(sourceListModel);
        sourceList.setSelectedIndex(0);
        sourceList.addListSelectionListener(new SourceListSelectionHandler());
        sourceList.setVisibleRowCount(5);

        JPanel sourcePanel = getListPanel(sourceList, sourceListTitle);

        destinationList = new JList(destListModel);
        destinationList.setSelectedIndex(0);
        destinationList.addListSelectionListener(new DestinationListSelectionHandler());
        destinationList.setVisibleRowCount(5);

        JPanel destinationPanel = getListPanel(destinationList, destinationListTitle);

        addButton = new JButton(addString);
        addAllButton = new JButton(addAllString);
        removeButton = new JButton(removeString);
        removeAllButton = new JButton(removeAllString);
  
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

    public void setPreferredListSize(Dimension d)
    {
        preferredListSize = d;

    }

    public void setMinimumListSize(Dimension d)
    {
        minimumListSize = d;
    }

    public void setMaximumListSize(Dimension d)
    {
        maximumListSize = d;
    }

    public void setGrowableLists(boolean b)
    {
        isGrowableList = b;
    }

     public JPanel getListPanel(JList list, String title)
     {
         JScrollPane scrollPane = new JScrollPane(list);
         scrollPane.setPreferredSize(preferredListSize);
         scrollPane.setMinimumSize(minimumListSize);
         scrollPane.setMaximumSize(maximumListSize);
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
    

    public void moveDestinationDataToSource()
    {
        Object[] selectedVals = destinationList.getSelectedValues();
        addDataToListModel(sourceListModel, selectedVals);
        removeDataFromListModel(destinationListModel, selectedVals);
        destinationList.setSelectedIndex(0);
        if(destinationList.getModel().getSize()==0) sourceList.setSelectedIndex(1);       
    }
    
    public void moveSourceDataToDestination()
    {
        Object[] selectedVals = sourceList.getSelectedValues();
        addDataToListModel(destinationListModel, selectedVals);
        removeDataFromListModel(sourceListModel, selectedVals);
        sourceList.setSelectedIndex(0);
        if(sourceList.getModel().getSize()==0)destinationList.setSelectedIndex(1);       
    }

    class AddListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            moveSourceDataToDestination();
        }
    }

    class RemoveListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            moveDestinationDataToSource();
        }
    }
    class AddAllListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            sourceList.setSelectionInterval(0, sourceList.getModel().getSize()-1);
            moveSourceDataToDestination();
        }
    }

    class RemoveAllListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            destinationList.setSelectionInterval(0, destinationList.getModel().getSize()-1);
            moveDestinationDataToSource();
        }
    }
    private void addDataToListModel(DefaultListModel model, Object newValues[])
    {
        for (int i = 0; i < newValues.length; i++)
        {
            model.addElement(newValues[i]);
        }
        getDestinationData();
    }

    private void removeDataFromListModel(DefaultListModel model, Object newValues[])
    {
        for (int i = 0; i < newValues.length; i++)
        {
            model.removeElement(newValues[i]);
        }
        getDestinationData();
    }

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

    class SourceListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            setButtons(e);
        }
    }

    class DestinationListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            setButtons(e);
        }
    }

}
