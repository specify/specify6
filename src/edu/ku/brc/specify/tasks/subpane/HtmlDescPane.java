/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.tasks.subpane;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.util.AttachmentUtils;

/**
 * 
 * Creates a SubPane that display HTML.
 * 
 * @author jds
 *
 * @code_status Alpha
 *
 *
 */
public class HtmlDescPane extends SimpleDescPane implements HyperlinkListener
{
    /**
     * Creates a SubPane that display HTML.
     * @param name the name of the pane
     * @param task the owning task
     * @param htmlDesc the HTML to be rendered
     */
    public HtmlDescPane(final String name,
                        final Taskable task,
                        final String htmlDesc)
    {
        super(name,task,"");
        this.removeAll();
        JEditorPane htmlPane   = new JEditorPane("text/html", htmlDesc);
        JScrollPane scrollPane = new JScrollPane(htmlPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        System.out.println(htmlDesc);
        this.add(scrollPane, BorderLayout.CENTER);
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
     */
    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
        {
            try
            {
                AttachmentUtils.openURI(e.getURL().toURI());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
