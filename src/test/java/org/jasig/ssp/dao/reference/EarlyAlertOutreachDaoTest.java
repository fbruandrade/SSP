/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.dao.reference; // NOPMD by jon.adams on 5/24/12 2:05 PM

import static org.jasig.ssp.util.assertions.SspAssert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.model.reference.EarlyAlertOutreach;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.impl.SecurityServiceInTestEnvironment;
import org.jasig.ssp.util.collections.Pair;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortDirection;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Tests for {@link EarlyAlertOutreachDao}.
 * 
 * @author jon.adams
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("../dao-testConfig.xml")
@TransactionConfiguration
@Transactional
public class EarlyAlertOutreachDaoTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EarlyAlertOutreachDaoTest.class);

	@Autowired
	private transient EarlyAlertOutreachDao dao;

	@Autowired
	private transient SecurityServiceInTestEnvironment securityService;

	/**
	 * Setup the security service for the tests
	 */
	@Before
	public void setUp() {
		securityService.setCurrent(new Person(Person.SYSTEM_ADMINISTRATOR_ID));
	}

	/**
	 * Test {@link EarlyAlertOutreachDao#save(EarlyAlertOutreach)}
	 * 
	 * @throws ObjectNotFoundException
	 *             Should not be thrown for this test
	 */
	@Test
	public void testSaveNew() throws ObjectNotFoundException {
		UUID savedId;

		EarlyAlertOutreach obj = new EarlyAlertOutreach();
		obj.setName("new name");
		obj.setObjectStatus(ObjectStatus.ACTIVE);
		obj = dao.save(obj);

		savedId = obj.getId();

		LOGGER.debug(obj.toString());

		final EarlyAlertOutreach saved = dao.get(savedId);
		assertNotNull("Reloading did not return the correct saved data.", saved);
		assertNotNull("Reloaded data did not have a set Name property.",
				saved.getName());
		assertEquals("Reloaded data did not have matching data.",
				obj.getName(), saved.getName());

		final Collection<EarlyAlertOutreach> all = dao
				.getAll(ObjectStatus.ACTIVE)
				.getRows();
		assertNotNull("GetAll() should not have returned a null collection.",
				all);
		assertFalse(
				"GetAll() should not have returned an empty list. (This test assumes some sample reference data exists in the testing database.)",
				all.isEmpty());
		assertList(all);

		dao.delete(saved);
	}

	/**
	 * Test that {@link EarlyAlertOutreachDao#get(UUID)} returns null if
	 * identifier is not found.
	 * 
	 * @throws ObjectNotFoundException
	 *             Expected
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void testNotFoundIdReturnsNull() throws ObjectNotFoundException {
		final UUID id = UUID.randomUUID();
		final EarlyAlertOutreach earlyAlertOutreach = dao.get(id);

		assertNull(
				"Get() did not return null when a missing identifier was used.",
				earlyAlertOutreach);
	}

	/**
	 * Asserts that list contains objects with non-null identifiers.
	 * 
	 * @param objects
	 *            Collection of objects to assert have non-null identifiers.
	 */
	private void assertList(final Collection<EarlyAlertOutreach> objects) {
		assertFalse("List should not have been empty.", objects.isEmpty());

		for (final EarlyAlertOutreach object : objects) {
			assertNotNull("List item should not have a null id.",
					object.getId());
		}
	}

	/**
	 * Test UUID generation,
	 * {@link EarlyAlertOutreachDao#save(EarlyAlertOutreach)}.
	 */
	@Test
	public void uuidGeneration() {
		final EarlyAlertOutreach obj = new EarlyAlertOutreach();
		obj.setName("A name");
		obj.setObjectStatus(ObjectStatus.ACTIVE);
		dao.save(obj);

		assertNotNull("Transient instance was not assigned a new identifier.",
				obj.getId());
	}

	@Test
	public void testSortingInGetAll() {
		// default sort order ("sortOrder ASC")
		final PagingWrapper<EarlyAlertOutreach> data = dao
				.getAll(new SortingAndPaging(ObjectStatus.ALL));
		assertNotNull("Outreach data should not be null.", data);
		assertFalse("Outreach data should not be empty.", data.getRows()
				.isEmpty());

		final EarlyAlertOutreach obj = data.getRows().iterator().next();
		assertEquals("Default sorting did not return the correct order.",
				UUID.fromString("9842eff0-6557-4fb2-81c2-614991d5cbfb"),
				obj.getId());

		// custom sort order ("sortOrder DESC")
		final List<Pair<String, SortDirection>> sortFields = Lists
				.newArrayList();
		sortFields.add(new Pair<String, SortDirection>("sortOrder",
				SortDirection.DESC));
		final PagingWrapper<EarlyAlertOutreach> data2 = dao
				.getAll(new SortingAndPaging(ObjectStatus.ALL, null, null,
						sortFields, null, null));
		assertNotNull("Outreach data should not be null.", data2);
		assertFalse("Outreach data should not be empty.", data2.getRows()
				.isEmpty());

		final EarlyAlertOutreach obj2 = data2.getRows().iterator().next();
		assertEquals(
				"Descending sortOrder sorting did not return the correct order.",
				UUID.fromString("612ed2c5-6d9a-4cda-9007-b22756888ca8"),
				obj2.getId());
	}

	@Test
	public void testHashCode() throws ObjectNotFoundException {
		final EarlyAlertOutreach obj = new EarlyAlertOutreach(
				UUID.randomUUID(),
				"Name", "description", (short) 34); // NOPMD by jon.adams

		assertNotEquals("HashCodes should not have matched.", obj.hashCode(),
				new EarlyAlertOutreach().hashCode());
		assertEquals("HashCodes should have matched.",
				obj.hashCode(), obj.hashCode());
		assertEquals("HashCodes should have matched.",
				new EarlyAlertOutreach().hashCode(),
				new EarlyAlertOutreach().hashCode());
	}
}