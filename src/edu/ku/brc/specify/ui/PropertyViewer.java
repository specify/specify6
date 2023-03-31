/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.dom4j.Node;

/**
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PropertyViewer extends JPanel
{
    //private static final Logger log = Logger.getLogger(PropertyViewer.class);
    
    protected CardLayout    cardLayout   = new CardLayout();
    protected JPanel        groupContainer; 
    protected JTree         tree;
    protected JSplitPane    splitPane;
    protected JLabel        titleLabel;
    protected JPanel        iconPanel; 
    
    public PropertyViewer()
    {
        super(new BorderLayout());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(titleLabel = createLabel(""), BorderLayout.NORTH);
        panel.add(groupContainer = new JPanel(cardLayout), BorderLayout.NORTH);
        panel.setMinimumSize(new Dimension(300,300));

        iconPanel = new JPanel() {
            Insets insets = new Insets(4, 4, 4, 4);
            public Insets getInsets() {
                return insets;
            }
        };
        
        JScrollPane scrollPane = new JScrollPane(iconPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        //chkBoxOuterPanel.add(scrollPane);
        //chkBoxOuterPanel.add(Box.createRigidArea(HGAP10));
        //Dimension HGAP10 = new Dimension(10, 1);
        
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));

        add(scrollPane, BorderLayout.WEST);
        
        scrollPane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.EAST);
        
        
        buildXMLPropsViewer();

    }
    
    protected void buildPlainPropsViewer()
    {
        
    }
    
    protected void buildXMLPropsViewer()
    {
        /*try
        {
            Document doc = ((XMLConfiguration)config).getDocument();
            XMLHelper.printNode(doc, 0);
            NodeList groups = XPathAPI.selectNodeList(doc, "config/groups/group");
            System.out.println(groups.getLength());
            for (int i=0;i<groups.getLength();i++)
            {
                Node group = groups.item(i);
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new TreeNode(XMLHelper.getNodeValue(XPathAPI.selectSingleNode(group, "name")), group));
                rootNode.add(groupNode);
                
                NodeList fields = XPathAPI.selectNodeList(group, "prefs/pref");
                for (int j=0;j<fields.getLength();j++)
                {
                    Node pref = fields.item(j);
                    //System.out.println("  "+XMLHelper.getNodeValue(XMLHelper.findNode(field, "name")));
                    DefaultMutableTreeNode prefNode = new DefaultMutableTreeNode(new TreeNode(XMLHelper.getNodeValue(XMLHelper.findNode(pref, "name")), group));
                    groupNode.add(prefNode);
                    //System.out.println(group.getNodeValue()+"  "+field.getNodeValue());
                }
            }
        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PropertyViewer.class, e);
            log.error("Can't create the tree.");
        }*/
    }
    
    protected void buildXMLPropsViewer2()
    {
        groupContainer = new JPanel(cardLayout);
        
        tree = new JTree(createTreeFromPrefs());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //Listen for when the selection changes.
        tree.addTreeSelectionListener(new TreeSelectionListener() {
              public void valueChanged(TreeSelectionEvent e) 
              {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

                  if (node == null) return;

                  /*Object nodeInfo = node.getUserObject();
                  if (nodeInfo instanceof ViewNode)
                  {
                      showCard("");
                  }*/
              }
          });

        // Create a split pane with the two scroll panes in it.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, groupContainer);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        add(splitPane, BorderLayout.CENTER);

        //        Provide minimum sizes for the two components in the split pane
        //Dimension minimumSize = new Dimension(100, 50);
        //listScrollPane.setMinimumSize(minimumSize);
        //pictureScrollPane.setMinimumSize(minimumSize);
    }
    
    protected DefaultMutableTreeNode createTreeFromPrefs()
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Properties");
        /* XXXX this needs to be re-done
        try
        {
            Document doc = ((XMLConfiguration)config).getDocument();
            NodeList groups = XPathAPI.selectNodeList(doc, "config/groups/group");
            //System.out.println(groups.getLength());
            for (int i=0;i<groups.getLength();i++)
            {
                Node group = groups.item(i);
                DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new TreeNode(XMLHelper.getNodeValue(XPathAPI.selectSingleNode(group, "name")), group));
                rootNode.add(groupNode);
                
                NodeList fields = XPathAPI.selectNodeList(group, "prefs/pref");
                for (int j=0;j<fields.getLength();j++)
                {
                    Node pref = fields.item(j);
                    //System.out.println("  "+XMLHelper.getNodeValue(XMLHelper.findNode(field, "name")));
                    DefaultMutableTreeNode prefNode = new DefaultMutableTreeNode(new TreeNode(XMLHelper.getNodeValue(XMLHelper.findNode(pref, "name")), group));
                    groupNode.add(prefNode);
                    //System.out.println(group.getNodeValue()+"  "+field.getNodeValue());
                }
            }
        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PropertyViewer.class, e);
            log.error("Can't create the tree.");
        }*/
        return rootNode;
    }
    
    protected void showCard(String aName)
    {
        cardLayout.show(groupContainer, aName);
    }
    
    class TreeNode
    {
        public String name;
        public Node node;
        
        public TreeNode(String aName, Node aNode)
        {
            name = aName;
            node = aNode;
        }
        
        public String toString()
        {
            
            return name;
        }
    }

}
