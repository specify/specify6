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
package edu.ku.brc.specify.tasks.subpane.wb.schema;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author timo
 *
 */
public class TableTest {
	protected static Table testtable;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testtable = new Table(null, "testtable");
		Field taxonNameId = new Field("taxonNameId", "id");
		testtable.addField(taxonNameId);
		testtable.addField(new Field("taxonName", "string"));
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.schema.Table#getFields()}.
	 */
	@Test
	public void testGetFields() {
		assertTrue(testtable.getFields().size() == 2);
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.schema.Table#getName()}.
	 */
	@Test
	public void testGetName() {
		assertTrue(testtable.getName().equals("testtable"));
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.schema.Table#getRelationships()}.
	 */
	@Test
	public void testGetRelationships() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.schema.Table#addField(edu.ku.brc.specify.tasks.subpane.wb.schema.Field)}.
	 */
	@Test
	public void testAddField() {
		testtable.addField(new Field("commonName", "string"));
		assertTrue(testtable.getFields().size() == 3);
	}

	/**
	 * Test method for {@link edu.ku.brc.specify.tasks.subpane.wb.schema.Table#getField(java.lang.String)}.
	 */
	@Test
	public void testGetField() {
		Field got = testtable.getField("taxonName");
		assertTrue(got.getName().equals("taxonName"));
	}

}
