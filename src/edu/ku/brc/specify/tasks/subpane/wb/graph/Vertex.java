/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.wb.graph;


/**
 * @author timo
 *
 * Represents a vertex with associated data.
 */
public class Vertex<E>
{
    /**
     * A label for the vertex.
     */
    protected String label;
    /**
     * Data associated with the vertex. Currently used to store info on database tables.
     */
    protected E      data;

    public Vertex(final String label, final E data)
    {
        this.label = label;
        this.data = data;
    }

    /**
     * @return the label.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return the data.
     */
    public E getData()
    {
        return data;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object vertexObj)
    {
        if (vertexObj == null)
            return false;

        if (this.getClass() != vertexObj.getClass())
            return false;

        Vertex<E> vertex = (Vertex<E>) vertexObj;
        return getLabel().equalsIgnoreCase(vertex.getLabel())
                && dataIsEqual(getData(), vertex.getData());
    }

    /**
     * @param data1
     * @param data2
     * @return true if data1 equals data2 in the context of the Vertex.equals method.
     */
    private boolean dataIsEqual(final E data1, final E data2)
    {
        if (data1 == null && data2 == null)
            return true;
        if (data1 != null && data2 != null)
            return data1.equals(data2);
        return false;
    }
}
