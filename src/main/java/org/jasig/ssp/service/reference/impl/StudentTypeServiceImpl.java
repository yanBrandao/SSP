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
package org.jasig.ssp.service.reference.impl;

import javax.validation.constraints.NotNull;
import org.jasig.ssp.dao.reference.StudentTypeDao;
import org.jasig.ssp.model.reference.StudentType;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.reference.StudentTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * StudentType service implementation
 * 
 * @author jon.adams
 * 
 */
@Service
@Transactional
public class StudentTypeServiceImpl extends
		AbstractReferenceService<StudentType>
		implements StudentTypeService {

	@Autowired
	transient private StudentTypeDao dao;

	@Override
	protected StudentTypeDao getDao() {
		return dao;
	}

	/**
	 * Sets the data access object
	 * 
	 * @param dao
	 *            the data access object
	 */
	protected void setDao(final StudentTypeDao dao) {
		this.dao = dao;
	}

	@Override
	public StudentType getByCode(@NotNull final String code) 
			throws ObjectNotFoundException {
		return this.dao.getByCode(code);
	}

	public StudentType getOrException(@NotNull UUID id) throws ObjectNotFoundException {
		StudentType studentType = super.get(id);
		if(studentType == null){
			throw new ObjectNotFoundException(
					"Unable to find a StudentType representing an early "
							+ "alert-assigned type.", "StudentType");
		} else {
			return studentType;
		}
	}
}
