package edu.ku.brc.af.tasks.subpane;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.util.AttachmentUtils;

public class HtmlDescPane extends SimpleDescPane implements HyperlinkListener
{
    public HtmlDescPane(final String name,
                        final Taskable task,
                        final String htmlDesc)
    {
        super(name,task,htmlDesc);
        this.removeAll();
        
        JEditorPane htmlPane=null;
        htmlPane = new JEditorPane("text/html",htmlDesc);
        this.add(htmlPane,BorderLayout.CENTER);
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);
    }

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
