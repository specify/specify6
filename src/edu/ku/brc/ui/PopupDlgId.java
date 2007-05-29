/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.ui;

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
