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
package edu.ku.brc.af.ui;

import org.apache.log4j.Logger;

/**
 * Class generates unique Ids for dialogs based on the content of the dialog
 * being displayed, as well as a unique defined String from that class.
 * 
 * @code_status Alpha
 * 
 * @author megkumin
 * 
 */
@SuppressWarnings("unused")
public class PopupDlgId
{
    private static final Logger log = Logger.getLogger(PopupDlgId.class);
    private String idString = "";
    private String title, message, uniqueIdentifier = "";
    int callingLinenumber;
    int id = -1;

    /**
     * Constructor
     * 
     * @param title -
     *            title of the dialog being dispalyed
     * @param message -
     *            the message being displayed in the dialog
     * @param uniqueIdentifier -
     *            a unique string (typically classname + dialog number)
     */
    public PopupDlgId(String title, String message, String uniqueIdentifier)
    {
        this.title = title;
        this.message = message;
        this.uniqueIdentifier = uniqueIdentifier;
        idString = "title:" + title + ";" + "message:" + message + ";"
                        + "uniqueIdentifier:" + uniqueIdentifier;
        id = idString.hashCode();
        log.debug("id [" + id + "] created for dialog [" + idString + "]");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return idString;
    }

    /**
     * @return int - the unique id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param args
     *            void
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
