/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.collab;

import static edu.ku.brc.ui.UIRegistry.clearSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.writeSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.writeTimedSimpleGlassPaneMsg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.DragAndDropLock;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 28, 2011
 *
 */
public class CollabNode extends JPanel implements GhostActionable
{
    public static final DataFlavor DROPPABLE_PANE_FLAVOR = new DataFlavor(CollabNode.class, "CollabNode"); //$NON-NLS-1$
    // DnD
    protected List<DataFlavor>        dropFlavors      = new ArrayList<DataFlavor>(); 
    protected GhostMouseInputAdapter  mouseDropAdapter = null;
    private boolean                   isDndActive      = false;
    private boolean                   isOver           = false;

    private String title;
    private Point orig;
    private Point dest;
    private JLabel iconLabel;
    
    private boolean         initialized = false;
    private boolean         isNodeActive    = true;
    private Vector<String>  users       = new Vector<String>(); 
    
    private static final ImageIcon activeIcon  = IconManager.getIcon("laptop");
    private static final ImageIcon disabledIcon = IconManager.getIcon("laptop_disabled");
    
    /**
     * @param title
     * @param orig
     * @param dest
     */
    public CollabNode(final String title, 
                      final Point orig, 
                      final Point dest)
    {
        super();
        this.title = title;
        this.dest = dest;
        this.orig = orig;
        
        dropFlavors.add(RecordSetTask.RECORDSET_FLAVOR);
        //dropFlavors.add(new DataFlavorTableExt(RecordSetTask.class, RecordSetTask.RECORD_SET, 0));
        this.createMouseInputAdapter();
        
        //this.icon = IconManager.getIcon(DATA_ENTRY, IconManager.IconSize.Std16);
        //setBackground(Color.WHITE);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p,4px,p"), this);
        iconLabel = new JLabel(activeIcon);
        pb.add(iconLabel, cc.xy(2, 1));
        pb.add(UIHelper.createLabel(title, SwingConstants.CENTER), cc.xyw(1, 3, 3));
        
        iconLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (initialized && isNodeActive && users.size() > 0)
                {
                    showUsers(false);
                }
            }
            
        });
        
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (isEnabled())
                {
                    isOver = true;
                    repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                isOver = false;
                repaint();
            }
          };
      addMouseListener (mouseListener);
      iconLabel.addMouseListener(mouseListener);
    }
    
    /**
     * @param doSelect
     * @return
     */
    private String showUsers(final boolean doSelectUser)
    {
        JList list = new JList(users);
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,f:p:g"));
        pb.add(UIHelper.createLabel("Active Users"), cc.xy(1,1));
        pb.add(UIHelper.createScrollPane(list), cc.xy(1,3));
        pb.setDefaultDialogBorder();
        CustomDialog dlg = new CustomDialog((Frame)null, title, true, doSelectUser ? CustomDialog.OKCANCEL : CustomDialog.OK_BTN, pb.getPanel());
        if (!doSelectUser)
        {
            dlg.setOkLabel("Close");
        }
        dlg.createUI();
        UIHelper.centerWindow(dlg, 300, 300);
        dlg.setVisible(true);
        if (doSelectUser && !dlg.isCancelled())
        {
            return (String)list.getSelectedValue();
        }
        return null;
    }
    
    /**
     * @param userStrs
     * @param num
     */
    public void addUsers(final String[] userStrs, final int num)
    {
        for (int i=0;i<num;i++)
        {
            users.add(userStrs[i]);
        }
    }

    /**
     * @param initialized the initialized to set
     */
    public void setInitialized(boolean initialized)
    {
        this.initialized = initialized;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * @param isNodeActive
     */
    public void setNodeActive(final boolean isNodeActive)
    {
        this.isNodeActive = isNodeActive;
        iconLabel.setIcon(this.isNodeActive ? activeIcon : disabledIcon);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (initialized && isNodeActive && users.size() > 0)
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.addRenderingHints(ShowNetworkPanel.getHints());
            g2d.setColor(Color.RED);
            //g2d.setStroke(new BasicStroke(2)); // make the arrow head solid even if dash pattern has been specified
            
            Dimension sz = getSize();
            
            Font f = g.getFont();
            g.setFont(f.deriveFont(14.0f));
            FontMetrics fm = g.getFontMetrics();
            
            String numUsersStr = String.format("%d", users.size());
            int len = fm.stringWidth(numUsersStr);
            int x = (sz.width - len) / 2;
            int y = 12+ fm.getHeight();//activeIcon.getIconHeight();
            g.drawString(numUsersStr, x, y);
        }
        
        if (isNodeActive && (isDndActive || isOver))
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.addRenderingHints(ShowNetworkPanel.getHints());
 
            Color color;
            if (isDndActive)
            {
                color = DragAndDropLock.isDragAndDropStarted() && isOver ? RolloverCommand.getDropColor() : RolloverCommand.getActiveColor();
            } else
            {
                color = RolloverCommand.getHoverColor();
            }
            g.setColor(color);
            
            Dimension size   = getSize();
            
            //g.drawRect(insets.left, insets.top, size.width-insets.right-insets.left, size.height-insets.bottom-insets.top);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0, 0, size.width-1, size.height-1, 10, 10);
            g2d.draw(rr);
            rr = new RoundRectangle2D.Double(1, 1, size.width-3, size.height-3, 10, 10);
            g2d.draw(rr);
        }
    }

    public synchronized void move(final double percent)
    {
        double xDiff = ((double)Math.abs(dest.x - orig.x)) * percent;
        double yDiff = ((double)Math.abs(dest.y - orig.y)) * percent;
        
        xDiff *= (dest.x > orig.x) ? 1.0 : -1.0;  
        yDiff *= (dest.y > orig.y) ? 1.0 : -1.0;  
        
        int xx = orig.x + (int)Math.round(xDiff);
        int yy = orig.y + (int)Math.round(yDiff);
        
        setLocation(xx, yy);
        
        //System.out.println(hashCode()+"  "+title+": "+getLocation()+String.format("   %d, %d", (int)xDiff, (int)yDiff)+"  "+getPreferredSize());
    }

    //-------------------------------------------------------------------------------------
    //
    //-------------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    @Override
    public void doAction(GhostActionable source)
    {
        String user = showUsers(true);
        if (user == null)
        {
            return;
        }
        
        final String userStr = user;
        
        String msg = String.format("Sending data to %s at %s...", userStr, CollabNode.this.title);
        writeSimpleGlassPaneMsg(msg, 24);

        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    Thread.sleep(2000);
                } catch (Exception ex) {}
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                clearSimpleGlassPaneMsg();
                
                String msg = String.format("Done sending data to %s at %s.", userStr, CollabNode.this.title);
                writeTimedSimpleGlassPaneMsg(msg, Color.BLACK);
            }
        };
        worker.execute();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    @Override
    public void setActive(boolean isActive)
    {
        this.isDndActive = isActive;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    @Override
    public void setData(Object data)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    @Override
    public Object getData()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    @Override
    public Object getDataForClass(Class<?> classObj)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
     */
    @Override
    public void createMouseInputAdapter()
    {
        mouseDropAdapter = new GhostMouseInputAdapter(UIRegistry.getGlassPane(), "action", this); //$NON-NLS-1$
        //addMouseListener(mouseDropAdapter);
        //addMouseMotionListener(mouseDropAdapter);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getMouseInputAdapter()
     */
    @Override
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return mouseDropAdapter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
     */
    @Override
    public BufferedImage getBufferedImage()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDropDataFlavors()
     */
    @Override
    public List<DataFlavor> getDropDataFlavors()
    {
        return dropFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
     */
    @Override
    public List<DataFlavor> getDragDataFlavors()
    {
        return null;
    }
    
    
}
