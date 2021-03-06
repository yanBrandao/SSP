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
package org.jasig.ssp.web.api.reference; // NOPMD by jon.adams

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.impl.SecurityServiceInTestEnvironment;
import org.jasig.ssp.transferobject.PagedResponse;
import org.jasig.ssp.transferobject.ServiceResponse;
import org.jasig.ssp.transferobject.reference.CategoryTO;
import org.jasig.ssp.transferobject.reference.ChallengeTO;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Category controller tests
 * 
 * @author daniel.bower
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("../../ControllerIntegrationTests-context.xml")
@TransactionConfiguration
@Transactional
public class CategoryControllerIntegrationTest {

	@Autowired
	private transient CategoryController controller;

	@Autowired
	private transient SessionFactory sessionFactory;

	private static final UUID CATEGORY_ID = UUID
			.fromString("5d24743a-a11e-11e1-a9a6-0026b9e7ff4c");

	private static final String CATEGORY_NAME = "Test Category";

	private static final UUID CHALLENGE_WITHCATEGORY = UUID
			.fromString("b9ac1cb5-d40a-4451-8ec2-08240698aaf3");

	@Autowired
	private transient SecurityServiceInTestEnvironment securityService;

	private static final String TEST_STRING1 = "testString1";

	private static final String TEST_STRING2 = "testString1";

	/**
	 * Setup the security service with the admin user for use by
	 * {@link #testControllerCreateAndDelete()} that checks that the Auditable
	 * auto-fill properties are correctly filled.
	 */
	@Before
	public void setUp() {
		securityService.setCurrent(new Person(Person.SYSTEM_ADMINISTRATOR_ID));
	}

	/**
	 * Test the {@link CategoryController#get(UUID)} action.
	 * 
	 * @throws ObjectNotFoundException
	 *             If lookup data can not be found.
	 * @throws ValidationException
	 *             If there are any validation errors.
	 */
	@Test
	public void testControllerGet() throws ObjectNotFoundException,
			ValidationException {
		final CategoryTO obj = controller.get(CATEGORY_ID);

		assertNotNull(
				"Returned CategoryTO from the controller should not have been null.",
				obj);

		assertEquals("Returned Category.Name did not match.", CATEGORY_NAME,
				obj.getName());
	}

	@Test(expected = ValidationException.class)
	public void testControllerCreateOfInvalid() throws ObjectNotFoundException,
			ValidationException {
		assertNotNull(
				"Controller under test was not initialized by the container correctly.",
				controller);

		// Check validation of 'no ID for create()'
		final CategoryTO category = new CategoryTO(UUID.randomUUID(),
				TEST_STRING1,
				TEST_STRING2);
		controller.create(category);
		fail("Calling create with an object with an ID should have thrown a validation excpetion.");
	}

	/**
	 * Test that the {@link CategoryController#get(UUID)} action returns the
	 * correct validation errors when an invalid ID is sent.
	 * 
	 * @throws ObjectNotFoundException
	 *             If lookup data can not be found.
	 * @throws ValidationException
	 *             If there are any validation errors.
	 */
	@Test(expected = ObjectNotFoundException.class)
	public void testControllerGetOfInvalidId() throws ObjectNotFoundException,
			ValidationException {
		assertNotNull(
				"Controller under test was not initialized by the container correctly.",
				controller);

		final CategoryTO obj = controller.get(UUID.randomUUID());

		assertNull(
				"Returned CategoryTO from the controller should have been null.",
				obj);
	}

	/**
	 * Test the {@link CategoryController#create(CategoryTO)} and
	 * {@link CategoryController#delete(UUID)} actions.
	 * 
	 * @throws ObjectNotFoundException
	 *             If lookup data can not be found.
	 * @throws ValidationException
	 *             If there are any validation errors.
	 */
	@Test
	public void testControllerCreateAndDelete() throws ObjectNotFoundException,
			ValidationException {
		assertNotNull(
				"Controller under test was not initialized by the container correctly.",
				controller);

		// Create a valid Category
		final CategoryTO category = new CategoryTO(null, TEST_STRING1,
				TEST_STRING2);
		final CategoryTO reloaded = controller.create(category);

		assertNotNull(
				"Returned CategoryTO from the controller should not have been null.",
				reloaded);
		assertNotNull(
				"Returned CategoryTO.ID from the controller should not have been null.",
				reloaded.getId());
		assertEquals(
				"Returned CategoryTO.Name from the controller did not match.",
				TEST_STRING1, reloaded.getName());
		assertEquals(
				"Returned CategoryTO.CreatedBy was not correctly auto-filled for the current user (the administrator in this test suite).",
				Person.SYSTEM_ADMINISTRATOR_ID, reloaded.getCreatedBy().getId());

		assertTrue("Delete action did not return success.",
				controller.delete(reloaded.getId()).isSuccess());
	}

	/**
	 * Test the
	 * {@link CategoryController#getAll(ObjectStatus, Integer, Integer, String, String)}
	 * action.
	 */
	@Test
	public void testControllerAll() {
		final Collection<CategoryTO> list = controller.getAll(
				ObjectStatus.ACTIVE, null, null, null, null).getRows();

		assertNotNull("List should not have been null.", list);
		assertFalse("List action should have returned some objects.",
				list.isEmpty());
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testGetChallengesForInvalidCategory()
			throws ObjectNotFoundException {
		// arrange, act
		final PagedResponse<ChallengeTO> challenges = controller
				.getChallengesForCategory(UUID.randomUUID(), ObjectStatus.ALL,
						0, 10, null, null);

		// assert
		assertEquals("No results should not have been returned.", 0,
				challenges.getResults());
	}

	@Test
	public void testGetChallengesForCategory() throws ObjectNotFoundException {
		// arrange, act
		final PagedResponse<ChallengeTO> challenges = controller
				.getChallengesForCategory(CATEGORY_ID, null, null,
						null, null, null);

		// assert
		assertEquals("Category should have included 1 instance.", 1,
				challenges.getResults());
	}

	@Test
	public void testDeleteChallengeForCategory() throws ObjectNotFoundException {
		// arrange, act
		final ServiceResponse response = controller
				.removeChallengeFromCategory(CATEGORY_ID,
						CHALLENGE_WITHCATEGORY);

		// assert
		assertTrue("Deletion should have returned true.", response.isSuccess());

		final Session session = sessionFactory.getCurrentSession();
		session.flush();
		session.clear();

		final PagedResponse<ChallengeTO> challenges = controller
				.getChallengesForCategory(CATEGORY_ID, null, null, null, null,
						null);

		// assert
		assertEquals("Category should have included 0 instance.", 0,
				challenges.getResults());
	}

	/**
	 * Test that getLogger() returns the matching log class name for the current
	 * class under test.
	 */
	@Test
	public void testLogger() {
		final Logger logger = controller.getLogger();
		assertEquals("Log class name did not match.", controller.getClass()
				.getName(), logger.getName());
	}
}