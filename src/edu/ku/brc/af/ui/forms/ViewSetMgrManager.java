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
package edu.ku.brc.af.ui.forms;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;
import edu.ku.brc.helpers.XMLHelper;

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
        //System.err.println("Pushing ["+viewMgr.getName()+"]");
        stack.push(viewMgr);
    }
    
    /**
     * Clears all ViewSetMgrs except the BackStop and if the backstop isn't loaded it loaded it.
     */
    public static void refresh()
    {
        clear(true);
        
        // The very first time that refresh is called the stack may be empty
        // so we want to make sure we have the backstop loaded  
        // Or the stack just may have been cleared
        if (stack.size() == 0)
        {
            ViewSetMgrManager.pushViewMgr(new ViewSetMgr("BackStop", new File(XMLHelper.getConfigDirPath(File.separator + ViewSetMgrManager.BACKSTOP)), false));
        }
    }
    
    /**
     * Empties the stack with optionally keep the backstop
     * @param keepBackStop true keeps the backstop, false removes it
     */
    public static void clear(final boolean keepBackStop)
    {
        while ((keepBackStop && stack.size() > 1) ||
                !keepBackStop && stack.size() > 0)
        {
            ViewSetMgr vm = stack.pop();
            vm.clearAll();
        }
        
        // If the remaining VSM is not the BackStop then remove it.
        if (stack.size() > 0)
        {
            ViewSetMgr vsm = stack.peek();
            if (vsm != null)
            {
                File bsDir = new File(XMLHelper.getConfigDirPath(File.separator + ViewSetMgrManager.BACKSTOP));
                
                if (!vsm.getContextDir().getAbsoluteFile().equals(bsDir.getAbsoluteFile()))
                {
                    vsm.clearAll();
                    stack.pop();
                }
            }
        }
    }
    
    /**
     * Search the entire stack of ViewMgrs for the view. Search down the stack because the "backstop" ViewSetMgr
     * if always at the bottom.
     * @param viewSetName the viewset name
     * @param viewName the view's name
     * @return the named view set
     */
    public static ViewIFace getView(final String viewSetName, final String viewName)
    {
        for (int inx=stack.size()-1;inx>=0;inx--)
        {
            ViewSetMgr viewMgr = stack.get(inx);
            
            ViewSetIFace viewSet = viewMgr.getViewSet(viewSetName);
            if (viewSet != null)
            {   
                ViewIFace view = viewSet.getView(viewName);
                if (view != null)
                {
                    return view;
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
            ViewSetIFace vs = srcVM.getViewSet(viewSetName);
            if (vs != null)
            {
                File srcFile = new File(srcVM.getContextDir().getAbsolutePath() + File.separator + vs.getFileName());
                File dstFile = new File(dstVM.getContextDir().getAbsolutePath() + File.separator + vs.getFileName());
                try
                {
                    FileUtils.copyFile(srcFile, dstFile);
                    
                    dstVM.addViewSetDef(vs.getType().toString(), vs.getName(), vs.getTitle(), vs.getFileName(), vs.getI18NResourceName());
                    
                } catch (IOException ex)
                {
                    FormDevHelper.appendFormDevError("IOException: ", ex);
                }
                
            } else
            {
                FormDevHelper.appendFormDevError("ViewSet ["+viewSetName+"] cannot be found in source ViewSet.");
            }
        } else
        {
            FormDevHelper.appendFormDevError("ViewSet ["+viewSetName+"] is already in the destination and override was false.");
        }
    }
}
