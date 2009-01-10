/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author timo
 * 
 */
public class DirectedGraph<E, F>
{
    protected Set<Vertex<E>>    vertices;
    protected Set<Edge<E, F>>   edges;
    private static final Logger log = Logger.getLogger(DirectedGraph.class);

    public DirectedGraph()
    {
        vertices = new HashSet<Vertex<E>>();
        edges = new HashSet<Edge<E, F>>();
    }

    /**
     * @return a copy of the the object with an 'opposite' edge added for each edge.
     * I.E. for an edge from v1 to v2, an additional edge from v2 to v1 will be added to the copy.
     * The copy is shallow: new objects are not created for vertex or edge data.
     * 
     * @throws DirectedGraphException
     */
    protected DirectedGraph<E, F> makeUndirectedCopy() throws DirectedGraphException
    {
        DirectedGraph<E, F> result = new DirectedGraph<E, F>();
        for (Vertex<E> v : vertices)
        {
            result.addVertex(new Vertex<E>(v.getLabel(), v.getData()));
        }
        for (Edge<E, F> e : edges)
        {
            result.addEdge(e.getPointA().getLabel(), e.getPointB().getLabel());
            result.addEdge(e.getPointB().getLabel(), e.getPointA().getLabel());
        }
        return result;
    }

    /**
     * @return a listing of all edges in the graph.
     */
    public Vector<String> listEdges()
    {
        SortedSet<String> lines = new TreeSet<String>();
        for (Edge<E, F> e : edges)
        {
            lines.add(e.getPointA().getLabel() + " --> " + e.getPointB().getLabel());
        }
        return new Vector<String>(lines);
    }

    /**
     * @param v
     * 
     * adds v to the graph.
     * 
     * @throws DirectedGraphException if a vertex with same label as v is already in the graph.
     */
    public void addVertex(Vertex<E> v) throws DirectedGraphException
    {
        if (getVertexByLabel(v.getLabel()) != null) { throw new DirectedGraphException(
                "vertex is already in graph"); }
        vertices.add(v);
    }

    /**
     * @param a
     * @param b
     * 
     * adds vertices a and b and adds an edge with null data from a to b.
     * 
     * @throws DirectedGraphException if a or b are already in the graph.
     */
    public void addEdge(Vertex<E> a, Vertex<E> b) throws DirectedGraphException
    {
        addEdge(a, b, null);
    }

    /**
     * @param a
     * @param b
     * @param data
     * 
     * adds vertices a and b and adds an edge with supplied data from a to b.
     * @throws DirectedGraphException if a or b are already in the graph.
     */
    public void addEdge(Vertex<E> a, Vertex<E> b, F data) throws DirectedGraphException
    {
        try
        {
            addVertex(a);
            addVertex(b);
            edges.add(new Edge<E, F>(a, b, data));
        }
        catch (DirectedGraphException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DirectedGraph.class, e);
            throw e;
        }
    }

    /**
     * @param aLabel
     * @param bLabel
     * @throws DirectedGraphException
     * 
     * adds an edge with null data between vertices with given labels. Throws exception if either vertex
     * is not in graph. Multiple edges between same vertices are possible.
     */
    public void addEdge(String aLabel, String bLabel) throws DirectedGraphException
    {
        addEdge(aLabel, bLabel, null);
    }

    /**
     * @param aLabel
     * @param bLabel
     * @param data
     * @throws DirectedGraphException
     * 
     * adds an edge with supplied data between vertices with given labels. Throws exception if either vertex
     * is not in graph. Multiple edges between same vertices are possible.
     */
    public void addEdge(String aLabel, String bLabel, F data) throws DirectedGraphException
    {
        Vertex<E> v1 = getVertexByLabel(aLabel);
        Vertex<E> v2 = getVertexByLabel(bLabel);
        if (v1 != null && v2 != null)
        {
            edges.add(new Edge<E, F>(v1, v2, data));
        }
        else
        {
            throw new DirectedGraphException("vertex does not exist in graph");
        }
    }

    /**
     * @param v1
     * @param v2
     * @return edge from v1 to v2 if it exists, or null.
     */
    private Edge<E, F> getEdge(Vertex<E> v1, Vertex<E> v2)
    {
        for (Edge<E, F> e : edges)
        {
            if (e.getPointA().equals(v1) && e.getPointB().equals(v2)) { return e; }
        }
        return null;
    }

    /**
     * @param v1
     * @param v2
     * @return edges from v1 to v2 if it exists.
     */
    private Vector<Edge<E, F>> getEdges(Vertex<E> v1, Vertex<E> v2)
    {
        Vector<Edge<E,F>> result = new Vector<Edge<E,F>>();
        for (Edge<E, F> e : edges)
        {
            if (e.getPointA().equals(v1) && e.getPointB().equals(v2)) 
            { 
                result.add(e); 
            }
        }
        return result;
    }

    /**
     * @param v1
     * @param v2
     * @return edge data for edge from v2 to v2. null if no edge exists from v2 to v2.
     */
    public F getEdgeData(Vertex<E> v1, Vertex<E> v2)
    {
        Edge<E, F> e = getEdge(v1, v2);
        if (e != null) { return e.getData(); }
        return null;
    }

    /**
     * @param v1
     * @param v2
     * @return edge data for all edges from v2 to v2. 
     */
    public Vector<F> getAllEdgeData(Vertex<E> v1, Vertex<E> v2)
    {
        Vector<Edge<E, F>> es = getEdges(v1, v2);
        Vector<F> result = new Vector<F>();
        for (Edge<E,F> e : es)
        {
            result.add(e.getData());
        }
        return result;
    }

    /**
     * @param vData1
     * @param vData2
     * @return edge data for edge from vertex with vData1 to vertex with vData2, or null if there is no such edge in graph.
     * @throws DirectedGraphException if vertices with given data are not in graph.
     */
    public F getEdgeData(E vData1, E vData2) throws DirectedGraphException
    {
        Vertex<E> v1 = getVertexByData(vData1);
        Vertex<E> v2 = getVertexByData(vData2);
        if (v1 == null || v2 == null) { throw new DirectedGraphException(
                "vertex does not exist in graph"); }
        return getEdgeData(v1, v2);
    }

    /**
     * @param vData1
     * @param vData2
     * @return edge data for all edgee from vertex with vData1 to vertex with vData2, or null if there is no such edge in graph.
     * @throws DirectedGraphException if vertices with given data are not in graph.
     */
    public Vector<F> getAllEdgeData(E vData1, E vData2) throws DirectedGraphException
    {
        Vertex<E> v1 = getVertexByData(vData1);
        Vertex<E> v2 = getVertexByData(vData2);
        if (v1 == null || v2 == null) { throw new DirectedGraphException(
                "vertex does not exist in graph"); }
        return getAllEdgeData(v1, v2);
    }

    /**
     * @return true if graph is strongly connected.
     * @throws DirectedGraphException
     */
    public boolean isStronglyConnected() throws DirectedGraphException
    {
        for (Vertex<E> v : vertices)
        {
            Set<Vertex<E>> g = from(v);
            g.addAll(to(v));
            g.add(v);
            if (!g.equals(vertices)) { return false; }
        }
        return true;
    }

    /**
     * @return true if graph is connected.
     * @throws DirectedGraphException
     */
    public boolean isConnected() throws DirectedGraphException
    {
        DirectedGraph<E, F> undirected = makeUndirectedCopy();
        if (undirected == null) { return false; }
        return undirected.isStronglyConnected();
    }

    /**
     * @param v
     * @return vector of vertices that are endpoints of the edges leaving v.
     */
    public Vector<Vertex<E>> getAdjacentVertices(final Vertex<E> v)
    {
        Vector<Vertex<E>> result = new Vector<Vertex<E>>();
        for (Edge<E, F> e : edges)
        {
            if (e.getPointA().equals(v))
            {
                result.add(e.getPointB());
            }
        }
        return result;
    }

    /**
     * @param v the vertex to remove.
     * 
     * Removes v and all edges that leave or enter v.
     */
    public void removeVertex(final Vertex<E> v)
    {
        List<Edge<E, F>> toRemove = new LinkedList<Edge<E, F>>();
        for (Edge<E, F> e : edges)
        {
             if (e.getPointA().equals(v) || e.getPointB().equals(v))
            {
                toRemove.add(e);
            }
        }
        for (Edge<E,F> e : toRemove)
        {
            edges.remove(e);
        }
        vertices.remove(v);
    }
    /**
     * @param v
     * @param visited
     * 
     * performs a topological sort from v. The resulting sort is stored in visited.
     */
    protected void topoSort(final Vertex<E> v, Vector<Vertex<E>> visited)
    {
        for (Vertex<E> a : getAdjacentVertices(v))
        {
            if (!visited.contains(a))
            {
                topoSort(a, visited);
            }
        }
        visited.insertElementAt(v, 0);
    }

    /**
     * @param v
     * @return a topological sort of the graph from v.
     */
    public Vector<Vertex<E>> topoSort(final Vertex<E> v)
    {
        Vector<Vertex<E>> result = new Vector<Vertex<E>>();
        topoSort(v, result);
        return result;
    }

    /**
     * @param vLabel
     * @return topological sort from vertex labeled vLabel.
     * @throws DirectedGraphException if no vertex labeled vLabel exists in graph.
     * 
     * Finds vertex with supplied label and returns the topologicial sort from v. 
     */
    public Vector<Vertex<E>> topoSort(final String vLabel) throws DirectedGraphException
    {
        Vertex<E> v = getVertexByLabel(vLabel);
        if (v == null) { throw new DirectedGraphException("vertex does not exist in graph"); }
        return topoSort(v);
    }

    /**
     * @param vLabel
     * @return vertex with given label.
     */
    public Vertex<E> getVertexByLabel(final String vLabel)
    {
        for (Vertex<E> v : vertices)
        {
            if (v.getLabel().equalsIgnoreCase(vLabel))
                return v;
        }
        return null;
    }

    /**
     * @param vData
     * @return vertex with data equal to vData.
     */
    public Vertex<E> getVertexByData(final E vData)
    {
        for (Vertex<E> v : vertices)
        {
            if (v.getData() == vData)
                return v;
        }
        return null;
    }

    /**
     * @return set of all vertices which have no edges leading into them.
     */
    public Set<Vertex<E>> sources()
    {
        Set<Vertex<E>> notSources = new HashSet<Vertex<E>>();
        for (Vertex<E> v : vertices)
        {
            notSources.addAll(getAdjacentVertices(v));
        }
        Set<Vertex<E>> result = new HashSet<Vertex<E>>();
        for (Vertex<E> v : vertices)
        {
            if (!notSources.contains(v))
            {
                result.add(v);
            }
        }
        return result;
    }

    /**
     * @param vertex
     * @return vertex in graph equal to vertex if it exists, or null.
     */
    protected Vertex<E> findVertexInGraph(final Vertex<E> vertex)
    {
        for (Vertex<E> v : vertices)
        {
            if (v.equals(vertex))
                return v;
        }
        return null;
    }

    /**
     * @param v
     * @return set of all vertices reachable from v.
     * 
     * @throws DirectedGraphException
     */
    public Set<Vertex<E>> from(final Vertex<E> v) throws DirectedGraphException
    {
        Set<Vertex<E>> result = new HashSet<Vertex<E>>();
        from2(v, result);
        return result;
    }

    /**
     * @param v
     * @param result
     * @return set of all vertices reachable from v.
     * @throws DirectedGraphException
     */
    protected Set<Vertex<E>> from2(final Vertex<E> v, Set<Vertex<E>> result)
            throws DirectedGraphException
    {
        if (findVertexInGraph(v) == null) { throw new DirectedGraphException(
                "vertex does not exist in graph"); }
        for (Vertex<E> a : getAdjacentVertices(v))
        {
            if (!result.contains(a))
            {
                result.add(a);
                result.addAll(from2(a, result));
            }
        }
        return result;
    }

    /**
     * @param vData
     * @return set of all vertices reachable from vertex with given data.
     * 
     *  If more than vertex exists with vData results will vary.
     *  
     * @throws DirectedGraphException
     */
    public Set<Vertex<E>> from(E vData) throws DirectedGraphException
    {
        Vertex<E> v = getVertexByData(vData);
        if (v == null) { throw new DirectedGraphException("vertex does not exist in graph"); }
        return from(v);
    }

    /**
     * @param v
     * @return set of all vertices for which there is a path to v.
     * @throws DirectedGraphException
     */
    public Set<Vertex<E>> to(Vertex<E> v) throws DirectedGraphException
    {
        if (findVertexInGraph(v) == null) { throw new DirectedGraphException(
                "vertex does not exist in graph"); }
        Set<Vertex<E>> result = new HashSet<Vertex<E>>();
        for (Vertex<E> u : vertices)
        {
            try
            {
                if (!u.equals(v) && from(u).contains(v))
                {
                    result.add(u);
                }
            }
            catch (DirectedGraphException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DirectedGraph.class, e);
                throw e; // this should never happen since u is right out of vertices.
            }
        }
        return result;
    }

    /**
     * @param vData
     * @return set of all vertices for which there is a path to vertex with given data.
     * @throws DirectedGraphException
     * 
     * If more than vertex exists with vData results will vary.
     */
    public Set<Vertex<E>> to(E vData) throws DirectedGraphException
    {
        Vertex<E> v = getVertexByData(vData);
        if (v == null) { throw new DirectedGraphException("vertex does not exist in graph"); }
        return to(v);
    }

    /**
     * @param v
     * @return set of all vertices from which there is an edge leading directly to v. 
     */
    public Set<Vertex<E>> into(Vertex<E> v)
    {
        Set<Vertex<E>> result = new HashSet<Vertex<E>>();
        for (Edge<E, F> e : edges)
        {
            if (e.getPointB().equals(v))
            {
                result.add(e.getPointA());
            }
        }
        return result;
    }

    /**
     * @param vData
     * @return set of all vertices from which there is an edge leading directly to vertex with given data. 
     * 
     * If more than vertex exists with vData results will vary.
     */
    public Set<Vertex<E>> into(E vData)
    {
        return into(getVertexByData(vData));
    }

    /**
     * @return a 'topological sort' of the entire graph.
     * @throws DirectedGraphException
     * 
     * Performs a topological sort from each source of the graph. Then merges the sorts together and returns the result.
     */
    public Vector<Vertex<E>> getTopoSort() throws DirectedGraphException
    {
        Vector<Vector<Vertex<E>>> sorts = new Vector<Vector<Vertex<E>>>();
        for (Vertex<E> v : sources())
        {
            log.debug("topoSorting: " + v.getLabel());
            sorts.add(topoSort(v));
        }
        Vector<Vertex<E>> result = new Vector<Vertex<E>>();
        for (Vertex<E> v : sorts.get(0))
        {
            result.add(v);
        }
        try
        {
            for (int s = 1; s < sorts.size(); s++)
            {
                mergeSort(sorts.get(s), result);
            }
        }
        catch (DirectedGraphException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DirectedGraph.class, e);
            throw e;
        }
        return result;
    }

    /**
     * @param merger
     * @param mergee
     * @throws DirectedGraphException
     * 
     * merger and mergee are sorted topologically. Merges vertices in merger into mergee such
     * that ordering of merger is maintained in mergee and vertices from merger are added to
     * mergee such that they precede any vertices that are members of their from() sets.  
     */
    protected void mergeSort(Vector<Vertex<E>> merger, Vector<Vertex<E>> mergee)
            throws DirectedGraphException
    {
        int startPnt = mergee.size() - 1;
        try
        {
            for (int t = merger.size() - 1; t >= 0; t--)
            {
                if (!mergee.contains(merger.get(t)))
                {
                    int mergeAt = 0;
                    for (int m = startPnt; m >= 0; m--)
                    {
                        if (from(merger.get(t)).contains(mergee.get(m)))
                        {
                            mergeAt = m;
                        }
                    }
                    mergee.insertElementAt(merger.get(t), mergeAt);
                    startPnt = mergeAt;
                }
            }
        }
        catch (DirectedGraphException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DirectedGraph.class, e);
            throw e;
        }
    }

    /**
     * @return the vertices
     */
    public final Set<Vertex<E>> getVertices()
    {
        return vertices;
    }

    /**
     * @return the edges
     */
    public final Set<Edge<E, F>> getEdges()
    {
        return edges;
    }

}
