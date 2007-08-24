/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.graph;


import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author timo
 *
 */
public class EdgeTest {
	protected Vertex<String> a;
	protected Vertex<String> b;
	protected Edge<String, String> tester;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		a = new Vertex<String>("a", "point a");
		b = new Vertex<String>("b", "point b");
		tester = new Edge<String, String>(a, b, "edgy");
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.Edge#edge()}.
	 */
	@Test
	public void testEdge() {
		assertTrue(tester.getPointA() == a);
		assertTrue(tester.getPointB() == b);
		assertTrue(tester.getData().equals("edgy"));
	}
	
	/**
	 * Test method for cost member
	 */
	@Test
	public void testCost() {
		tester.setCost(5);
		assertTrue(tester.getCost() == 5);
	}
	
}
