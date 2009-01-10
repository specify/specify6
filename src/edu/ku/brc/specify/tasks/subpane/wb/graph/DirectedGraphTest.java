/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.graph;

import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author timo
 *
 */
public class DirectedGraphTest {
	protected static DirectedGraph<String, String> disConnect;
	protected static DirectedGraph<String, String> connect;
	protected static Vertex<String> a;
	protected static Vertex<String> b;
	protected static Vertex<String> c;
	protected static Vertex<String> d;
	protected static Vertex<String> e;
	protected static Vertex<String> f;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		a = new Vertex<String>("a", "a");
		b = new Vertex<String>("b", "b");
		c = new Vertex<String>("c", "c");
		d = new Vertex<String>("d", "d");
		e = new Vertex<String>("e", "e");
		f = new Vertex<String>("f", "f");

		disConnect = new DirectedGraph<String, String>();
		disConnect.addVertex(a);
		disConnect.addVertex(b);
		disConnect.addVertex(c);
        disConnect.addVertex(d);
        disConnect.addVertex(e);
        disConnect.addVertex(f);
		disConnect.addEdge(a.getLabel(), c.getLabel());
		disConnect.addEdge(d.getLabel(), e.getLabel());
		disConnect.addEdge(e.getLabel(), f.getLabel());
		
		connect = new DirectedGraph<String, String>();
		connect.addVertex(a);
		connect.addVertex(b);
		connect.addVertex(c);
		connect.addVertex(d);
		connect.addVertex(e);
		connect.addVertex(f);
		connect.addEdge("a", "c");
		connect.addEdge("c", "b");
		connect.addEdge("a", "d");
		connect.addEdge("b", "d");
		connect.addEdge("d", "e");
		connect.addEdge("a", "f");
		connect.addEdge("e", "f");
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph#isConnected()}.
	 */
	@Test
	public void testIsConnected() throws DirectedGraphException
    {
        assertTrue(!disConnect.isStronglyConnected());
        assertTrue(connect.isStronglyConnected());
    }

	/**
     * Test method for
     * {@link edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph#getAdjacentVertices(edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex)}.
     */
	@Test
	public void testGetAdjacentVertices() {
		assertTrue(disConnect.getAdjacentVertices(a).contains(c));
		assertTrue(connect.getAdjacentVertices(a).contains(c));
		assertTrue(connect.getAdjacentVertices(a).contains(d));
		assertTrue(connect.getAdjacentVertices(a).contains(f));
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph#topoSort(edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex, java.util.Vector)}.
	 */
	@Test
	public void testTopoSortVertexOfEVectorOfVertex() {
		Vector<Vertex<String>> result = new Vector<Vertex<String>>();
		connect.topoSort(a, result);
 		assertTrue(result.get(0) == a);
		assertTrue(result.get(1) == c);
		assertTrue(result.get(2) == b);
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph#topoSort(edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex)}.
	 */
	@Test
	public void testTopoSortVertexOfE() {
		Vector<Vertex<String>> result = connect.topoSort(a);
		assertTrue(result.get(0) == a);
		assertTrue(result.get(1) == c);
		assertTrue(result.get(2) == b);
		assertTrue(result.get(3) == d);
		assertTrue(result.get(result.size()-1) == f);
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph#sources()}.
	 */
	@Test
	public void testSources() {
		Set<Vertex<String>> result = connect.sources();
		assertTrue(result.size() == 1);
		for (Vertex<String> v : result) {
			assertTrue(v == a);
		}
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph#from(edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex)}.
	 */
	@Test
	public void testFrom() {
        try
        {
            Set<Vertex<String>> froms = connect.from(a);
            assertTrue(froms.contains(c));
            assertTrue(froms.contains(b));
            froms = disConnect.from(a);
            assertTrue(!froms.contains(b));
            froms = connect.from(b);
            assertTrue(froms.contains(d));
        }
        catch (DirectedGraphException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DirectedGraphTest.class, ex);
            //huh?
        }
    }

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph#to(edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex)}.
	 */
	@Test
	public void testTo()
    {
        try
        {
            Set<Vertex<String>> t = connect.to(b);
            assertTrue(t.contains(a));
            assertTrue(t.contains(c));

            t = disConnect.to(b);
            assertTrue(t.size() == 0);
        }
        catch (DirectedGraphException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DirectedGraphTest.class, ex);
            //what?
        }

    }

}
