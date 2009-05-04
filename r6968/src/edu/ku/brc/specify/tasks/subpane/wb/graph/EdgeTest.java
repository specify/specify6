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
