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
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;

import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class ObjectAttachmentIconMapper implements ObjectIconMapper, ObjectTextMapper
{
    protected AttachmentIconMapper attachmentIconMapper;
    
    public ObjectAttachmentIconMapper()
    {
        attachmentIconMapper = new AttachmentIconMapper();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    @Override
    public ImageIcon getIcon(final Object obj, final ChangeListener listener)
    {
        if (obj instanceof ObjectAttachmentIFace<?>)
        {
            ObjectAttachmentIFace<?> oa = (ObjectAttachmentIFace<?>)obj;
            return attachmentIconMapper.getIcon(oa.getAttachment(), listener);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    @Override
    public String getString(final Object o)
    {
        if (o instanceof ObjectAttachmentIFace<?>)
        {
            ObjectAttachmentIFace<?> oa = (ObjectAttachmentIFace<?>)o;
            
            String title = oa.getAttachment().getTitle();
            String filename = oa.getAttachment().getOrigFilename();
            if (filename != null)
            {
                int lastWinSepIndex = filename.lastIndexOf('\\');
                int lastUnixSepIndex = filename.lastIndexOf('/');
                int lastIndex = Math.max(lastWinSepIndex, lastUnixSepIndex);
                if (lastIndex != -1)
                {
                   filename = filename.substring(lastIndex+1);
                }
            }
            
            return (title != null && title.trim().length() > 0) ? title : filename;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    @Override
    public Class<?>[] getMappedClasses()
    {
        Class<?>[] mappedClasses = new Class[1];
        mappedClasses[0] = ObjectAttachmentIFace.class;
        return mappedClasses;
    }
}
