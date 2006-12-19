/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.ui.UICacheManager;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class TreeNodeChooser extends JPanel implements ActionListener, ListSelectionListener
{
    protected EmbeddableTreeTableViewer<?,?,?> treeViewer;
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton newButton;
    protected JButton searchButton;
    protected JTextField searchBox;

    protected JDialog dialog;
    protected Object returnValue;
    
    @SuppressWarnings("unchecked")
    public TreeNodeChooser(TreeDefIface<?,?,?> treeDef)
    {
        super();
        setLayout(new BorderLayout());
        
        treeViewer = new EmbeddableTreeTableViewer(treeDef);
        treeViewer.setMinimumSize(new Dimension(400,200));
        treeViewer.addListSelectionListener(this);
        
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.X_AXIS));
        
        okButton = new JButton(getResourceString("OK"));
        okButton.addActionListener(this);
        okButton.setEnabled(false);
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
        
        add(treeViewer,BorderLayout.CENTER);
        add(southPanel,BorderLayout.SOUTH);
        
        JFrame topFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
        dialog = new JDialog(topFrame,"Find and select a node",true);
        dialog.pack();
        dialog.setSize(treeViewer.getMinimumSize().width, dialog.getHeight() + treeViewer.getMinimumSize().height);
        dialog.setLocationRelativeTo(topFrame);
        dialog.setContentPane(this);
    }
    
    public Object showChooser()
    {
        dialog.setVisible(true);
        return returnValue;
    }
    
    public void doOK()
    {
        returnValue = treeViewer.getSelectedNode();;
        dialog.setVisible(false);
    }
    
    public void doCancel()
    {
        returnValue = null;
        dialog.setVisible(false);
    }
    
    public void doNew()
    {
        //TODO: implement this somehow
        System.out.println("TODO: New node");
    }
    
    public void doSearch()
    {
        String searchKey = searchBox.getText();
        if (searchKey.length()<1)
        {
            return;
        }
        
        treeViewer.setLeafNodeName(searchKey);
        dialog.repaint();
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

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting())
        {
            return;
        }
        
        okButton.setEnabled((treeViewer.getSelectedNode()==null) ? false : true);
    }
}
