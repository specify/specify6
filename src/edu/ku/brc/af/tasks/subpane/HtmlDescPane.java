/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
