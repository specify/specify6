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
package edu.ku.brc.specify.tasks.subpane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;

/*
 * @code_status Alpha
 **
 * @author rods
 *
 */
public class SearchFormPane extends FormPane
{
    private static final Logger log  = Logger.getLogger(SearchFormPane.class);

    protected FormViewObj formViewObj;
    protected Hashtable<String, Hashtable<String, String>> hashTables = new Hashtable<String, Hashtable<String, String>>();


    /**
     * Creates a a Search Pane "Form". 
     * @param name the name of the SubPane
     * @param task the task
     * @param viewSetName the viewset name
     * @param viewName the view name
     */
    public SearchFormPane(final String   name,
                          final Taskable task,
                          final String   viewSetName,
                          final String   viewName)
    {
        super(name, task, viewSetName, viewName, AltViewIFace.CreationMode.SEARCH.toString(), null, MultiView.IS_NEW_OBJECT);

        Viewable viewable = multiView.getCurrentView();
        if (viewable instanceof FormViewObj)
        {
            formViewObj = (FormViewObj)viewable;

        } else
        {
            throw new RuntimeException("The form didn't create a FormViewObj for the Viewable!.");
        }
        walkMultiViewsSetHash(multiView);

        JComponent saveComp = formViewObj.getSaveComponent();
        if (saveComp instanceof JButton)
        {
            JButton saveBtn = (JButton)saveComp;
            saveBtn.setEnabled(true);
            saveBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    doSearch();
                }
            });
        } else
        {
            throw new RuntimeException("The Save Component MUST be a button for this form!");
        }
    }

    protected void walkMultiViewsSetHash(final MultiView multiViewArg)
    {
        Hashtable<String, String> formData        = new Hashtable<String, String>();
        Viewable                  currentViewable = multiViewArg.getCurrentView();

        //System.out.println("["+currentViewable.getView().getName()+"]");
        hashTables.put(currentViewable.getView().getName(), formData);
        multiViewArg.setData(formData);

        for (MultiView mv : multiViewArg.getKids())
        {
            walkMultiViewsSetHash(mv);
        }
    }

    protected void walkMultiViewsGetValues(final MultiView multiViewArg)
    {
        //Viewable currentViewable = multiView.getCurrentView();
        multiViewArg.getCurrentView().getDataFromUI();

        for (MultiView mv : multiViewArg.getKids())
        {
            walkMultiViewsGetValues(mv);
        }
    }

    protected void doSearch()
    {
        walkMultiViewsGetValues(multiView);

        String[] nameMap = {"Collection Object Search",
                            "CollectionObject"};

        for (int i=0;i<nameMap.length;i++)
        {
            String nameStr    = nameMap[i++];
            //String objName = nameMap[i];

            Hashtable<String, String> formData = hashTables.get(nameStr);

            for (Enumeration<String> e = formData.keys(); e.hasMoreElements();)
            {
                String key   = e.nextElement();
                String value = formData.get(key);
                log.debug("["+key+"]["+value+"]");
            }
        }

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#shutdown()
     */
    public void shutdown()
    {
        super.shutdown();
        formViewObj = null;
        super.shutdown(); // closes session
    }

}
