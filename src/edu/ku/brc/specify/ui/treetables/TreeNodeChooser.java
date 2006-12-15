/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class TreeNodeChooser <T extends Treeable<T,D,I>,
                                D extends TreeDefIface<T,D,I>,
                                I extends TreeDefItemIface<T,D,I>>
                                extends JPanel implements ActionListener
{
    protected EmbeddableTreeTableViewer<T, D, I> treeViewer;
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton newButton;
    protected JButton searchButton;
    protected JTextField searchBox;
    
    public TreeNodeChooser(D treeDef)
    {
        super();
        setLayout(new BorderLayout());
        
        treeViewer = new EmbeddableTreeTableViewer<T, D, I>(treeDef);
        
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.X_AXIS));
        
        okButton = new JButton(getResourceString("OK"));
        okButton.addActionListener(this);
        cancelButton = new JButton(getResourceString("Cancel"));
        cancelButton.addActionListener(this);
        newButton = new JButton(getResourceString("New"));
        newButton.addActionListener(this);
        String searchStr = getResourceString("Search");
        searchButton = new JButton(searchStr);
        searchButton.addActionListener(this);
        searchBox = new JTextField();
        searchBox.addActionListener(this);
        
        southPanel.add(new JLabel(searchStr));
        southPanel.add(searchBox);
        southPanel.add(searchButton);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(newButton);
        southPanel.add(cancelButton);
        southPanel.add(okButton);
        
        add(treeViewer,BorderLayout.NORTH);
        add(southPanel,BorderLayout.SOUTH);
    }
    
    public void doOK()
    {
        T selection = treeViewer.getSelectedNode();
        System.out.println(selection + " chosen");
    }
    
    public void doCancel()
    {
        System.out.println("Cancelled");
    }
    
    public void doNew()
    {
        System.out.println("New node");
    }
    
    public void doSearch()
    {
        String searchKey = searchBox.getText();
        if (searchKey.length()<1)
        {
            return;
        }
        
        treeViewer.setLeafNodeName(searchKey);
        treeViewer.repaint();
        repaint();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == okButton)
        {
            doOK();
        }
        else if (source == cancelButton)
        {
            doCancel();
        }
        else if (source == newButton)
        {
            doNew();
        }
        else if (source == searchBox || source == searchButton)
        {
            doSearch();
        }
    }
}
