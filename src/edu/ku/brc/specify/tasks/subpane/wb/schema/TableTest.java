/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.schema;

import static org.junit.Assert.*;

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
