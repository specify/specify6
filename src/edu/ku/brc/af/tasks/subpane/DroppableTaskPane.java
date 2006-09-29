/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.af.tasks.subpane;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;

/**
 * A Task SubPane that can have forms (DroppableFormObject) dropped onto it so it can display a form. 
 * This also dislays a text string in the middle of the ane.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public abstract class DroppableTaskPane extends BaseSubPane implements GhostActionable, CommandListener
{
    // Static Data Members
    public static final DataFlavor DROPPABLE_PANE_FLAVOR = new DataFlavor(DroppableTaskPane.class, "DroppablePane");
    
    // Data Members
    protected String                 desc                 = null; 
    
    // DnD
    protected List<DataFlavor>       dropFlavors         = new ArrayList<DataFlavor>(); 
    protected GhostMouseInputAdapter  mouseDropAdapter = null;

    /**
     * Constructor.
     * @param name the name of the subpane
     * @param task the owning task
     * @param desc string displayed in th center of the pane
     */
    public DroppableTaskPane(final String name, 
                             final Taskable task,
                             final String   desc)
    {
        this(null, name, task, desc);
    }
    
    /**
     * Constructor.
     * @param session the DB session to use
     * @param name the name of the subpane
     * @param task the owning task
     * @param desc string displayed in th center of the pane
     */
    public DroppableTaskPane(final Session session,
                             final String name, 
                             final Taskable task,
                             final String   desc)
    {
        super(session, name, task);
        this.desc = desc;
        //this.progressLabel.setText(desc);
        //this.progressBar.setIndeterminate(false);
        //remove(this.progressBar);
        removeAll();
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (desc != null)
        {
            Dimension size = getSize();
            FontMetrics fm = g.getFontMetrics();
            int width = fm.stringWidth(desc);
            g.drawString(desc, (size.width-width)/2, (size.height - fm.getHeight()) / 2);
        }

    }
    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------
    public abstract void doCommand(CommandAction cmdAction);
    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    public abstract void doAction(GhostActionable src);

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
       // no-op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    public Object getDataForClass(Class<?> classObj)
    {
        return null;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseDropAdapter()
     */
    public void createMouseInputAdapter()
    {
        mouseDropAdapter = new GhostMouseInputAdapter(UICacheManager.getGlassPane(), "action", this);
        addMouseListener(mouseDropAdapter);
        addMouseMotionListener(mouseDropAdapter);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getMouseInputAdapter()
     */
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
     */
    public BufferedImage getBufferedImage() 
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataFlavor()
     */
    public List<DataFlavor> getDropDataFlavors()
    {
        return dropFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
     */
    public List<DataFlavor> getDragDataFlavors()
    {
        return null; // this is not draggable
    }   

}
