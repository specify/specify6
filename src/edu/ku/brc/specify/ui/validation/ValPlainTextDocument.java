/* Filename:    $RCSfile: ValPlainTextDocument.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/16 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.validation;

import javax.swing.event.DocumentEvent;
import javax.swing.text.PlainDocument;

/**
 * Simple PlainDocument that enables the shutting off of notifications on document changes
 * 
 * @author rods
 *
 */
public class ValPlainTextDocument extends PlainDocument
{
    protected boolean ignoreNotify = false;


    /**
     * Creates a simle PlainDocument
     */
    public ValPlainTextDocument()
    {
        super();
    }
    
    public boolean isIgnoreNotify()
    {
        return ignoreNotify;
    }

    public void setIgnoreNotify(boolean ignoreNotify)
    {
        this.ignoreNotify = ignoreNotify;
    }

    protected void fireChangedUpdate(DocumentEvent e)
    {
        if (!ignoreNotify)
        {
            super.fireChangedUpdate(e);
        }
    }

    protected void fireInsertUpdate(DocumentEvent e)
    {
        if (!ignoreNotify)
        {
            super.fireInsertUpdate(e);
        }
    }

    protected void fireRemoveUpdate(DocumentEvent e)
    {
        if (!ignoreNotify)
        {
            super.fireRemoveUpdate(e);
        }
    }
    
    
}