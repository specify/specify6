/**
 * 
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
