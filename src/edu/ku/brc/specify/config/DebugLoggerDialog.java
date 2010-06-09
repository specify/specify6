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
package edu.ku.brc.specify.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * 
 * (Checkbox tree code taken from (http://kidslovepc.com/javatable/java-jtree-checkbox.shtml)
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 31, 2007
 *
 */
public class DebugLoggerDialog extends CustomDialog
{
    protected static File               debugLogFile = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "debug_logger.init");
    
    protected Hashtable<String, Logger> loggers = new Hashtable<String, Logger>();
    protected Vector<String>            sortedNames;
    protected LoggerNode                root;
    
    /**
     * @param frame
     * @param title
     * @param isModal
     * @param contentPanel
     * @throws HeadlessException
     */
    public DebugLoggerDialog(Frame frame) throws HeadlessException
    {
        super(frame, "Debug Logger Configurer", true, null);
        
        loadLoggers();
    }
    
    /**
     * 
     */
    public void loadLoggers()
    {
        loggers.clear();
        for (Enumeration<?> e=LogManager.getCurrentLoggers(); e.hasMoreElements();)
        {
            Logger  logger = (Logger)e.nextElement();
            //System.out.println("putting["+logger.getName()+"]");
            loggers.put(logger.getName(), logger);
        }
        sortedNames = new Vector<String>(loggers.keySet());
        Collections.sort(sortedNames);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        configureLoggers();
        
        root = new LoggerNode(null, "Root");
        for (String loggerName : sortedNames)
        {
            buildTree(loggers.get(loggerName), root, StringUtils.split(loggerName, '.'), 0);
        }
        
        JTree tree = new JTree(root);
        tree.setCellRenderer(new CheckRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.addMouseListener(new NodeSelectionListener(tree));
        
        contentPanel = new JScrollPane(tree);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setSize(500,500);
    }
    
    protected void buildTree(final Logger           logger, 
                             DefaultMutableTreeNode parent, 
                             final String[]         names, 
                             final int              level)
    {
        for (int i=0;i<parent.getChildCount();i++)
        {
            LoggerNode node = (LoggerNode)parent.getChildAt(i);
            //System.out.println("["+node.toString()+"]["+names[level]+"]");
            if (node.toString().equals(names[level]))
            {
                buildTree(logger, node, names, level+1);
                return;
            }
        }
        
        LoggerNode newNode = new LoggerNode(logger, names[level]);
        newNode.setSelected(logger.getLevel() == Level.DEBUG);
        parent.add(newNode);
        newNode.setParent(parent);
        if (level < names.length-1)
        {
            buildTree(logger, newNode, names, level+1);
        }
    }


    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            save(sb, root);
            FileUtils.writeStringToFile(debugLogFile, sb.toString());
            System.out.println("Saved to["+debugLogFile.getAbsolutePath()+"]");
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DebugLoggerDialog.class, ex);
            ex.printStackTrace();
        }
        super.okButtonPressed();
    }

    /**
     * @param sb
     * @param dstPath
     * @param parent
     */
    protected void save(final StringBuilder sb, 
                        final LoggerNode    parent)
    {
        if (parent.isLeaf())
        {
            parent.getLogger().setLevel(parent.isSelected ? Level.DEBUG : Level.OFF);
            sb.append(parent.getLogger().getName());
            sb.append("=");
            sb.append(parent.isSelected);
            sb.append("\n"); 
            return;
        } 
        
        for (int i=0;i<parent.getChildCount();i++)
        {
            LoggerNode node = (LoggerNode)parent.getChildAt(i);
            save(sb, node);
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void configureLoggers()
    {
        if (configureLoggersInternal())
        {
            loadLoggers();
            configureLoggersInternal();
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public boolean configureLoggersInternal()
    {
        boolean loggersNotLoaded = false;
        
        if (debugLogFile.exists())
        {
            try
            {
                for (String line : (List<String>)FileUtils.readLines(debugLogFile))
                {
                    String[] toks = StringUtils.split(line, "=");
                    Logger logger = loggers.get(toks[0]);
                    if (logger != null)
                    {
                        logger.setLevel(Boolean.parseBoolean(toks[1]) ? Level.DEBUG : Level.OFF);
                        
                    } else
                    {
                        //System.err.println("Logger["+toks[0]+"] not found.");
                        loggersNotLoaded = true;
                        try
                        {
                            @SuppressWarnings("unused")
                            Class<?> cls = Class.forName(toks[0]);
                            
                        } catch (Exception ex)
                        {
                            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DebugLoggerDialog.class, ex);
                        }
                    }
                }
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DebugLoggerDialog.class, ex);
            }
        } else
        {
            System.out.println("Nothing to Configure.");
        }
        
        return loggersNotLoaded;
    }
    
    //----------------------------------------------------------------------------------
    //-- Inner Classes
    //----------------------------------------------------------------------------------
    
    class LoggerNode extends CheckNode
    {
        protected Logger  logger;
        protected String  shortName;
        
        /**
         * @param logger
         */
        public LoggerNode(Logger logger, final String shortName)
        {
            super();
            this.logger    = logger;
            this.shortName = shortName;
        }

        /**
         * @return the logger
         */
        public Logger getLogger()
        {
            return logger;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.tree.DefaultMutableTreeNode#toString()
         */
        public String toString()
        {
            return shortName;
        }
        
    }
    
    class NodeSelectionListener extends MouseAdapter
    {
        JTree tree;

        NodeSelectionListener(JTree tree)
        {
            this.tree = tree;
        }

        public void mouseClicked(MouseEvent e)
        {
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            
            // TreePath path = tree.getSelectionPath();
            
            if (path != null)
            {
                CheckNode node = (CheckNode)path.getLastPathComponent();
                boolean isSelected = !(node.isSelected());
                node.setSelected(isSelected);
                
                if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION)
                {
                    if (isSelected)
                    {
                        tree.expandPath(path);
                    } else
                    {
                        tree.collapsePath(path);
                    }
                }
                
                ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
                //         I need revalidate if node is root. but why?
                if (row == 0)
                {
                    tree.revalidate();
                    tree.repaint();
                }
            }
        }
    }

    public class CheckRenderer extends JPanel implements TreeCellRenderer
    {
        protected JCheckBox check;
        protected TreeLabel label;

        public CheckRenderer()
        {
            setLayout(null);
            add(check = UIHelper.createCheckBox(""));
            add(label = new TreeLabel());
            check.setBackground(UIManager.getColor("Tree.textBackground"));
            label.setForeground(UIManager.getColor("Tree.textForeground"));
        }

        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean isSelected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus)
        {
            String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row,hasFocus);
            setEnabled(tree.isEnabled());
            check.setSelected(((CheckNode)value).isSelected());
            label.setFont(tree.getFont());
            label.setText(stringValue);
            label.setSelected(isSelected);
            label.setFocus(hasFocus);
            if (leaf)
            {
                label.setIcon(UIManager.getIcon("Tree.leafIcon"));
            } else if (expanded)
            {
                label.setIcon(UIManager.getIcon("Tree.openIcon"));
            } else
            {
                label.setIcon(UIManager.getIcon("Tree.closedIcon"));
            }
            return this;
        }

        public Dimension getPreferredSize()
        {
            Dimension d_check = check.getPreferredSize();
            Dimension d_label = label.getPreferredSize();
            return new Dimension(d_check.width + d_label.width,
                    (d_check.height < d_label.height ? d_label.height : d_check.height));
        }

        public void doLayout()
        {
            Dimension d_check = check.getPreferredSize();
            Dimension d_label = label.getPreferredSize();
            int y_check = 0;
            int y_label = 0;
            if (d_check.height < d_label.height)
            {
                y_check = (d_label.height - d_check.height) / 2;
            } else
            {
                y_label = (d_check.height - d_label.height) / 2;
            }
            check.setLocation(0, y_check);
            check.setBounds(0, y_check, d_check.width, d_check.height);
            label.setLocation(d_check.width, y_label);
            label.setBounds(d_check.width, y_label, d_label.width, d_label.height);
        }

        public void setBackground(Color color)
        {
            super.setBackground(color instanceof ColorUIResource ? null : color);
        }

        public class TreeLabel extends JLabel
        {
            boolean isSelected;
            boolean hasFocus;

            public TreeLabel()
            {
            }

            public void setBackground(Color color)
            {
                super.setBackground(color instanceof ColorUIResource ? null : color);
            }

            public void paint(Graphics g)
            {
                String str;
                if ((str = getText()) != null)
                {
                    if (0 < str.length())
                    {
                        if (isSelected)
                        {
                            g.setColor(UIManager.getColor("Tree.selectionBackground"));
                        } else
                        {
                            g.setColor(UIManager.getColor("Tree.textBackground"));
                        }
                        Dimension d = getPreferredSize();
                        int imageOffset = 0;
                        Icon currentI = getIcon();
                        if (currentI != null)
                        {
                            imageOffset = currentI.getIconWidth()
                                    + Math.max(0, getIconTextGap() - 1);
                        }
                        g.fillRect(imageOffset, 0, d.width - 1 - imageOffset, d.height);
                        if (hasFocus)
                        {
                            g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
                            g.drawRect(imageOffset, 0, d.width - 1 - imageOffset, d.height - 1);
                        }
                    }
                }
                super.paint(g);
            }

            public Dimension getPreferredSize()
            {
                Dimension retDimension = super.getPreferredSize();
                if (retDimension != null)
                {
                    retDimension = new Dimension(retDimension.width + 3, retDimension.height);
                }
                return retDimension;
            }

            public void setSelected(boolean isSelected)
            {
                this.isSelected = isSelected;
            }

            public void setFocus(boolean hasFocus)
            {
                this.hasFocus = hasFocus;
            }
        }
    }
    
    public class CheckNode extends DefaultMutableTreeNode
    {

        public final static int SINGLE_SELECTION = 0;
        public final static int DIG_IN_SELECTION = 4;
        protected int           selectionMode;
        protected boolean       isSelected;

        public CheckNode()
        {
            this(null);
        }

        public CheckNode(Object userObject)
        {
            this(userObject, true, false);
        }

        public CheckNode(Object userObject, boolean allowsChildren, boolean isSelected)
        {
            super(userObject, allowsChildren);
            this.isSelected = isSelected;
            setSelectionMode(DIG_IN_SELECTION);
        }

        public void setSelectionMode(int mode)
        {
            selectionMode = mode;
        }

        public int getSelectionMode()
        {
            return selectionMode;
        }

        public void setSelected(boolean isSelected)
        {
            this.isSelected = isSelected;

            if ((selectionMode == DIG_IN_SELECTION) && (children != null))
            {
                Enumeration<?> menum = children.elements();
                while (menum.hasMoreElements())
                {
                    CheckNode node = (CheckNode)menum.nextElement();
                    node.setSelected(isSelected);
                }
            }
        }

        public boolean isSelected()
        {
            return isSelected;
        }

        // If you want to change "isSelected" by CellEditor,
        /*
         * public void setUserObject(Object obj) { if (obj instanceof Boolean) {
         * setSelected(((Boolean)obj).booleanValue()); } else { super.setUserObject(obj); } }
         */

    }


}
