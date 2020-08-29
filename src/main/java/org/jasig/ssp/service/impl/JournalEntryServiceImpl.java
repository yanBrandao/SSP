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
package org.jasig.ssp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.ssp.dao.JournalEntryDao;
import org.jasig.ssp.dao.PersonDao;
import org.jasig.ssp.model.JournalEntry;
import org.jasig.ssp.model.JournalEntryDetail;
import org.jasig.ssp.model.ObjectStatus;
import org.jasig.ssp.model.Person;
import org.jasig.ssp.service.AbstractRestrictedPersonAssocAuditableService;
import org.jasig.ssp.service.JournalEntryService;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonProgramStatusService;
import org.jasig.ssp.transferobject.reports.BaseStudentReportTO;
import org.jasig.ssp.transferobject.reports.EntityCountByCoachSearchForm;
import org.jasig.ssp.transferobject.reports.EntityStudentCountByCoachTO;
import org.jasig.ssp.transferobject.reports.JournalCaseNotesStudentReportTO;
import org.jasig.ssp.transferobject.reports.JournalStepSearchFormTO;
import org.jasig.ssp.transferobject.reports.JournalStepStudentReportTO;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 	17 Pontos de complexidade cognitiva
 *
 * 	Meta Nº 1 - Atingir 75% dos pontos: 13 pontos
 * 			(Obtido com a criação de duas novas classes JournalCaseMap e StudentBelongsToSchool)
 * 	Meta Nº 2 - Atingir 50% dos pontos: 9 pontos
 * 	Meta Nº 3 - Atingir 25% dos pontos: 4 pontos (Objetivo final)
 */
@Service
@Transactional
public class JournalEntryServiceImpl
		extends AbstractRestrictedPersonAssocAuditableService<JournalEntry>
		implements JournalEntryService {

	//1
	@Autowired
	private transient JournalEntryDao dao;

	//1
	@Autowired
	private transient PersonProgramStatusService personProgramStatusService;

	//1
	@Autowired
	private transient PersonDao personDao;

	@Override
	protected JournalEntryDao getDao() {
		return dao;
	}

	@Override
	public JournalEntry create(final JournalEntry obj)
			throws ObjectNotFoundException, ValidationException {
		final JournalEntry journalEntry = getDao().save(obj);
		checkForTransition(journalEntry);
		return journalEntry;
	}

	@Override
	public JournalEntry save(final JournalEntry obj)
			throws ObjectNotFoundException, ValidationException {
		final JournalEntry journalEntry = getDao().save(obj);
		checkForTransition(journalEntry);
		return journalEntry;
	}

	private void checkForTransition(final JournalEntry journalEntry)
			throws ObjectNotFoundException, ValidationException {
		// search for a JournalStep that indicates a transition
		//1
		for (final JournalEntryDetail detail : journalEntry
				.getJournalEntryDetails()) {
			//1
			if (detail.getJournalStepJournalStepDetail().getJournalStep()
					.isUsedForTransition()) {
				// is used for transition, so attempt to set program status
				personProgramStatusService.setTransitionForStudent(journalEntry
						.getPerson());

				// exit early because no need to loop through others
				return;
			}
		}
	}
	
	@Override
	public Long getCountForCoach(Person coach, Date createDateFrom, Date createDateTo, List<UUID> studentTypeIds){
		return dao.getJournalCountForCoach(coach, createDateFrom, createDateTo, studentTypeIds);
	}

	@Override
	public Long getStudentCountForCoach(Person coach, Date createDateFrom, Date createDateTo, List<UUID> studentTypeIds) {
		return dao.getStudentJournalCountForCoach(coach, createDateFrom, createDateTo, studentTypeIds);
	}
	
	@Override
	public PagingWrapper<EntityStudentCountByCoachTO> getStudentJournalCountForCoaches(EntityCountByCoachSearchForm form){
		return dao.getStudentJournalCountForCoaches(form);
	}
	
	@Override
	public PagingWrapper<JournalStepStudentReportTO> getJournalStepStudentReportTOsFromCriteria(JournalStepSearchFormTO personSearchForm,  
			SortingAndPaging sAndP){
		return dao.getJournalStepStudentReportTOsFromCriteria(personSearchForm,  
				sAndP);
	}
	
 	@Override
 	public List<JournalCaseNotesStudentReportTO> getJournalCaseNoteStudentReportTOsFromCriteria
			(JournalStepSearchFormTO personSearchForm, SortingAndPaging sAndP) throws ObjectNotFoundException{
 		 final List<JournalCaseNotesStudentReportTO> personsWithJournalEntries = dao.getJournalCaseNoteStudentReportTOsFromCriteria(personSearchForm, sAndP);
 		 final JournalCaseMap map = new JournalCaseMap(personsWithJournalEntries);

 		 final SortingAndPaging personSAndP = SortingAndPaging.createForSingleSortAll(ObjectStatus.ACTIVE, "lastName", "DESC") ;
 		 final PagingWrapper<BaseStudentReportTO> persons = personDao.getStudentReportTOs(personSearchForm, personSAndP);

		//1
 		 if (persons == null) {
 			 return personsWithJournalEntries;
 		 }

		//1
 		 for (BaseStudentReportTO person:persons) {
 		 	//1
			StudentBelongsToSchool.checkStudentSchool(map, person, personSearchForm, personsWithJournalEntries,
					getDao());
 		 }
		 sortByStudentName(personsWithJournalEntries);

 		 return personsWithJournalEntries;
 	}
 		 
	private static void sortByStudentName(List<JournalCaseNotesStudentReportTO> toSort) {
		Collections.sort(toSort,  new Comparator<JournalCaseNotesStudentReportTO>() {
	        public int compare(JournalCaseNotesStudentReportTO p1, JournalCaseNotesStudentReportTO p2) {
	        	
	        	int value = p1.getLastName().compareToIgnoreCase(
	     	                    p2.getLastName());
				//1
	        	if(value != 0)
	        		return value;
	        	
	        	value = p1.getFirstName().compareToIgnoreCase(
 	                    p2.getFirstName());
				//1
		       if(value != 0)
        		 return value;
				//1
		       if(p1.getMiddleName() == null && p2.getMiddleName() == null)
		    	   return 0;
				//1
		       if(p1.getMiddleName() == null)
		    	   return -1;
				//1
		       if(p2.getMiddleName() == null)
		    	   return 1;
		       return p1.getMiddleName().compareToIgnoreCase(
	                    p2.getMiddleName());
	        }
	    });
	}

}