/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.validation;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Simple PlainDocument that enables the shutting off of notifications on document changes.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class ValPlainTextDocument extends PlainDocument
{
    protected boolean ignoreNotify = false;
    protected int     limit        = -1;

    /**
     * Constructor.
     */
    public ValPlainTextDocument()
    {
        super();
    }
    
    /**
     * Constructor.
     * @param limit the number of characters the document can hold.
     */
    public ValPlainTextDocument(final int limit)
    {
        super();
        this.limit = limit;
    }
    
    /**
     * @param limit the number of characters the document can hold.
     */
    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    /**
     * @return the limit
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * @return whether document notifications are being blocked.
     */
    public boolean isIgnoreNotify()
    {
        return ignoreNotify;
    }

    /**
     * Used to block notifications when the document changes.
     * @param ignoreNotify true no notifications take place.
     */
    public void setIgnoreNotify(boolean ignoreNotify)
    {
        this.ignoreNotify = ignoreNotify;
    }

    /* (non-Javadoc)
     * @see javax.swing.text.AbstractDocument#fireChangedUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    protected void fireChangedUpdate(DocumentEvent e)
    {
        if (!ignoreNotify)
        {
            super.fireChangedUpdate(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.AbstractDocument#fireInsertUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    protected void fireInsertUpdate(DocumentEvent e)
    {
        if (!ignoreNotify)
        {
            super.fireInsertUpdate(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.AbstractDocument#fireRemoveUpdate(javax.swing.event.DocumentEvent)
     */
    @Override
    protected void fireRemoveUpdate(DocumentEvent e)
    {
        if (!ignoreNotify)
        {
            super.fireRemoveUpdate(e);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.text.Document#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
     */
    @Override
    public void insertString(final int offset, final String strArg, final AttributeSet attr) throws BadLocationException
    {
        if (limit == -1)
        {
            super.insertString(offset, strArg, attr);
           
        } else if (strArg != null && strArg.length() + this.getLength() <= limit)
        {
            super.insertString(offset, strArg, attr);
        }
    }
}
