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
package edu.ku.brc.specify.utilapps;

import com.thoughtworks.xstream.XStream;

/**
 * Used for reading the taxon load files with xstream.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 21, 2009
 *
 */
public class TaxonFileDesc
{
    protected String discipline;
    protected String title;
    protected String coverage;
    protected String fileName;
    protected String description;
    protected String src;
    
    /**
     * @return the discipline
     */
    public String getDiscipline()
    {
        return discipline;
    }
    /**
     * @param discipline the discipline to set
     */
    public void setDiscipline(String discipline)
    {
        this.discipline = discipline;
    }
    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    /**
     * @return the coverage
     */
    public String getCoverage()
    {
        return coverage;
    }
    /**
     * @param coverage the coverage to set
     */
    public void setCoverage(String coverage)
    {
        this.coverage = coverage;
    }
    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * @return the src
     */
    public String getSrc()
    {
        return src;
    }
    /**
     * @param src the src to set
     */
    public void setSrc(String src)
    {
        this.src = src;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return title;
    }
    
    /**
     * Configures the XStream for I/O.
     * @param xstream the stream
     */
    public static void configXStream(final XStream xstream)
    {
        // Aliases
        xstream.alias("file",   TaxonFileDesc.class); //$NON-NLS-1$
        
        xstream.useAttributeFor(TaxonFileDesc.class, "discipline");
        xstream.useAttributeFor(TaxonFileDesc.class, "title");
        xstream.useAttributeFor(TaxonFileDesc.class, "coverage");
        xstream.useAttributeFor(TaxonFileDesc.class, "fileName");
        xstream.useAttributeFor(TaxonFileDesc.class, "src");
               
        xstream.aliasAttribute(TaxonFileDesc.class, "fileName", "file");
        
    }
}
