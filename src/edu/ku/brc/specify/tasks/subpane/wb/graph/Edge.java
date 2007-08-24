/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.graph;

/**
 * @author timo
 * 
 * An object of this class represents a directed edge from pointA to pointB. 
 *
 */
public class Edge<E, F>
{
    /**
     * The cost or length or whatever of the edge (currently not used).
     */
    protected Integer   cost;
    /**
     * Data associated with the edge. Currently used to store relationships between database tables.
     */
    protected F         data;
    /**
     * The starting point of the edge.
     */
    protected Vertex<E> pointA;
    /**
     * The end point of the edge.
     */
    protected Vertex<E> pointB;

    public Edge(final Vertex<E> pointA, final Vertex<E> pointB)
    {
        this.cost = null;
        this.pointA = pointA;
        this.pointB = pointB;
        this.data = null;
    }

    public Edge(final Vertex<E> pointA, final Vertex<E> pointB, final F data)
    {
        this.cost = null;
        this.pointA = pointA;
        this.pointB = pointB;
        this.data = data;
    }

    /**
     * @return the cost.
     */
    public Integer getCost()
    {
        return cost;
    }

    /**
     * @param cost
     * 
     * Set the cost.
     */
    public void setCost(final Integer cost)
    {
        this.cost = cost;
    }

    /**
     * @return the starting point.
     */
    public Vertex<E> getPointA()
    {
        return pointA;
    }

    /**
     * @return the end point.
     */
    public Vertex<E> getPointB()
    {
        return pointB;
    }

    /**
     * @return the data.
     */
    public F getData()
    {
        return data;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object edgeObj)
    {
        if (edgeObj == null)
            return false;

        if (this.getClass() != edgeObj.getClass())
            return false;

        Edge<E, F> edge = (Edge<E, F>) edgeObj;
        return cost.equals(edge.cost) && pointA.equals(edge.getPointA())
                && pointB.equals(edge.getPointB()) && dataIsEqual(data, edge.getData());
    }

    /**
     * @param data1
     * @param data2
     * @return true if data1 equals data2 in the context of the Edge.equals method.
     */
    private boolean dataIsEqual(final F data1, final F data2)
    {
        if (data1 == null && data2 == null)
            return true;
        if (data1 != null && data2 != null)
            return data1.equals(data2);
        return false;
    }
}
