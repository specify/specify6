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
package edu.ku.brc.af.tasks.subpane.formeditor;

import java.util.Collections;
import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import edu.ku.brc.af.ui.forms.persist.AltView;
import edu.ku.brc.af.ui.forms.persist.FormRow;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.af.ui.forms.persist.TableViewDef;
import edu.ku.brc.af.ui.forms.persist.View;
import edu.ku.brc.af.ui.forms.persist.ViewDef;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSet;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 23, 2007
 *
 */
public class ViewSetTreeModel implements TreeModel
{
    protected Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();
    protected ViewSet              viewSet;
    protected Vector<ViewIFace>    views    = new Vector<ViewIFace>();
    protected Vector<ViewDefIFace> viewDefs = new Vector<ViewDefIFace>();
    
    public ViewSetTreeModel(ViewSet viewSet)
    {
        this.viewSet = viewSet;
        views.addAll(viewSet.getViews().values());
        viewDefs.addAll(viewSet.getViewDefs().values());
        
        Collections.sort(views);
        Collections.sort(viewDefs);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void addTreeModelListener(TreeModelListener l)
    {
        listeners.add(l);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index)
    {
        //System.out.println(parent + " " + index); //$NON-NLS-1$
        
        if (parent.toString().equals("Views")) //$NON-NLS-1$
        {
            return views.get(index);
            
        } else if (parent.toString().equals("ViewDefs")) //$NON-NLS-1$
        {
            return viewDefs.get(index);
            
        } else if (parent instanceof ViewSet)
        {
            return index == 0 ? "Views" : "ViewDefs"; //$NON-NLS-1$ //$NON-NLS-2$
            
        } else if (parent instanceof FormViewDef)
        {
            FormViewDef fvd = (FormViewDef)parent;
            return fvd.getRows().get(index);
            
        } else if (parent instanceof TableViewDef)
        {
            
        } else if (parent instanceof View)
        {
            return ((View)parent).getAltViews().get(index);
            
        } else if (parent instanceof ViewDef)
        {
            
        } else if (parent instanceof FormRow)
        {
            return ((FormRow)parent).getCells().get(index);
            
        } else if (parent instanceof AltView)
        {
            
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent)
    {
        if (parent.toString().equals("Views")) //$NON-NLS-1$
        {
            return viewSet.getViews().size();
            
        } else if (parent.toString().equals("ViewDefs")) //$NON-NLS-1$
        {
            return viewSet.getViewDefs().size();
            
        } else if (parent instanceof ViewSet)
        {
            return 2;
            
        } else if (parent instanceof FormViewDef)
        {
            return ((FormViewDef)parent).getRows().size();
            
        } else if (parent instanceof TableViewDef)
        {
            return 0;//((TableViewDef)parent).getRows().size();
            
        } else if (parent instanceof View)
        {
            return ((View)parent).getAltViews().size();
            
        } else if (parent instanceof FormRow)
        {
            System.out.println(((FormRow)parent).getCells().size());
            return ((FormRow)parent).getCells().size();
            
        } else if (parent instanceof ViewDef)
        {
            
        } else if (parent instanceof AltView)
        {
            
        }
       return 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child)
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot()
    {
        return viewSet;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node)
    {
        if (node.toString().equals("Views")) //$NON-NLS-1$
        {
            return viewSet.getViews().size() == 0;
            
        } else if (node.toString().equals("ViewDefs")) //$NON-NLS-1$
        {
            return viewSet.getViewDefs().size() == 0;
            
        } else if (node instanceof ViewSet)
        {
            return false;
            
        } else if (node instanceof FormViewDef)
        {
            return ((FormViewDef)node).getRows().size() == 0;
            
        } else if (node instanceof TableViewDef)
        {
            return true;
            
        } else if (node instanceof View)
        {
            return ((View)node).getAltViews().size() == 0;
            
        } else if (node instanceof FormRow)
        {
            return ((FormRow)node).getCells().size() == 0;
            
        } else if (node instanceof ViewDef)
        {
            return true;
            
        } else if (node instanceof AltView)
        {
            return true;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
     */
    public void removeTreeModelListener(TreeModelListener l)
    {
        listeners.remove(l);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        // TODO Auto-generated method stub
        
    }
}
