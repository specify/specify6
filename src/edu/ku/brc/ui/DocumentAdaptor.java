/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 19, 2008
 *
 */
public class DocumentAdaptor implements DocumentListener
{
    /**
     * 
     */
    public DocumentAdaptor()
    {
        super();
    }
    
    protected void changed(final DocumentEvent e)
    {
        
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void changedUpdate(DocumentEvent e)
    {
        changed(e);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void insertUpdate(DocumentEvent e)
    {
        changed(e);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    public void removeUpdate(DocumentEvent e)
    {
        changed(e);
    }

}
