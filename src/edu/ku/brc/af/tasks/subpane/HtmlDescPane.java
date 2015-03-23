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
package edu.ku.brc.af.tasks.subpane;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.AttachmentUtils;

/**
 * A {@link SubPaneIFace} that displays HTML content.
 * 
 * @author jds
 * @code_status Complete
 */
public class HtmlDescPane extends SimpleDescPane implements HyperlinkListener
{
    /**
     * Constructor.
     * 
     * @param name the name of the pane
     * @param task the owning task
     * @param htmlDesc the HTML to be rendered
     */
    public HtmlDescPane(final String name,
                        final Taskable task,
                        final String htmlDesc)
    {
        super(name,task,""); //$NON-NLS-1$
        this.removeAll();
        JEditorPane htmlPane   = new JEditorPane("text/html", htmlDesc); //$NON-NLS-1$
        final JScrollPane scrollPane = UIHelper.createScrollPane(htmlPane);
        this.add(scrollPane, BorderLayout.CENTER);
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                scrollPane.getVerticalScrollBar().setValue(0); 
                scrollPane.getHorizontalScrollBar().setValue(0); 
            }
        });
        
    }

    /* (non-Javadoc)
     * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
     */
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        // if a hyperlink was clicked, try to open it in an external viewer
        if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
        {
            try
            {
                AttachmentUtils.openURI(e.getURL().toURI());
            }
            catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HtmlDescPane.class, ex);
                ex.printStackTrace();
            }
        }
    }
}
