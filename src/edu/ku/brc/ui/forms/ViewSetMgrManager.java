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
package edu.ku.brc.ui.forms;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewSet;
import org.apache.commons.io.FileUtils;

/**
 * This class manages one or more ViewSetMgrs as a "stack", this way there can be a "backstop" ViewSetMgr.<br>
 * The backstop ViewSetMgr is the MSM of last resort. Here is how it works: The application may contain several
 * ViewSets that can act like the "base" set of ViewSets. Then anyone overriding or creating their own set of ViewSets do not
 * need worry about re-creating these Views. They can simply let a request be serviced by the backstop or "base".<br><br>
 * For example, an application may want a set of Views for preferences or searches.
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ViewSetMgrManager
{
    public static final String BACKSTOP = "backstop";
    
    protected static Stack<ViewSetMgr> stack = new Stack<ViewSetMgr>();
    
    /**
     * Pushes a ViewSetMgr onto the stack, typically the backstop ViewSetMgr is pushed first.
     * 
     * @param viewMgr the viewMgr to be pushed onto the stack.
     */
    public static void pushViewMgr(final ViewSetMgr viewMgr)
    {
        stack.push(viewMgr);
    }
    
    /**
     * Search the entire stack of ViewMgrs for the view. Search down the stack because the "backstop" ViewSetMgr
     * if always at the bottom.
     * @param viewSetName the viewset name
     * @param viewName the view's name
     * @return the named view set
     */
    public static View getView(final String viewSetName, final String viewName)
    {
        for (int inx=stack.size()-1;inx>=0;inx--)
        {
            ViewSetMgr viewMgr = stack.get(inx);
            
            ViewSet viewSet = viewMgr.getViewSet(viewSetName);
            if (viewSet != null)
            {   
                if (viewSet.getName().equals(viewSetName))
                {
                    View view = viewSet.getView(viewName);
                    if (view != null)
                    {
                        return view;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Copies a ViewSet (which is a single file AND a registry entry) from one ViewSetMgr to another.
     * @param srcVM the source ViewSetMgr
     * @param dstVM the destination ViewSetMgr
     * @param viewSetName the name of the ViewSet
     * @param overwrite indicates that it is OK to find an existing ViewSet, if set to false then the location better be empty.
     */
    public static void copyViewSet(final ViewSetMgr srcVM, 
                                   final ViewSetMgr dstVM, 
                                   final String  viewSetName,
                                   final boolean overwrite)
    {
        if (overwrite || dstVM.getViewSet(viewSetName) == null)
        {
            ViewSet vs = srcVM.getViewSet(viewSetName);
            if (vs != null)
            {
                File srcFile = new File(srcVM.getContextDir().getAbsolutePath() + File.separator + vs.getFileName());
                File dstFile = new File(dstVM.getContextDir().getAbsolutePath() + File.separator + vs.getFileName());
                try
                {
                    FileUtils.copyFile(srcFile, dstFile);
                    
                    dstVM.addViewSetDef(vs.getType().toString(), vs.getName(), vs.getTitle(), vs.getFileName());
                    
                } catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
                
            } else
            {
                throw new RuntimeException("ViewSet ["+viewSetName+"] cannot be found in source ViewSet.");
            }
        } else
        {
            throw new RuntimeException("ViewSet ["+viewSetName+"] is already in the destination and override was false.");
        }
    }
    
}
