/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.graph;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author timo
 *
 */
public class VertexTest {

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		Vertex<String> v = new Vertex<String>("testing", "nodule number nine");
		assertTrue(v.getLabel().equals("testing"));
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.graph.Vertex#getData()}.
	 */
	@Test
	public void testGetData() {
		Vertex<String> v = new Vertex<String>("testing", "nodule number nine");
		assertTrue(v.getData().equals("nodule number nine"));
	}

}
