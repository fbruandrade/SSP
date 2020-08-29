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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class JournalEntryServiceImpl
		extends AbstractRestrictedPersonAssocAuditableService<JournalEntry>
		implements JournalEntryService {

	private final transient JournalEntryDao dao;

	private final transient PersonProgramStatusService personProgramStatusService;
	
	private final transient PersonDao personDao;

	public JournalEntryServiceImpl(JournalEntryDao dao, PersonProgramStatusService personProgramStatusService, PersonDao personDao) {
		this.dao = dao;
		this.personProgramStatusService = personProgramStatusService;
		this.personDao = personDao;
	}

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
		for (final JournalEntryDetail detail : journalEntry
				.getJournalEntryDetails()) {
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
 	public List<JournalCaseNotesStudentReportTO> getJournalCaseNoteStudentReportTOsFromCriteria(JournalStepSearchFormTO personSearchForm, SortingAndPaging sAndP) throws ObjectNotFoundException{
 		 final List<JournalCaseNotesStudentReportTO> personsWithJournalEntries = dao.getJournalCaseNoteStudentReportTOsFromCriteria(personSearchForm, sAndP);
 		 final Map<String, JournalCaseNotesStudentReportTO> map = new HashMap<>();

 		 for(JournalCaseNotesStudentReportTO entry:personsWithJournalEntries){
 			 map.put(entry.getSchoolId(), entry);
 		 }

 		 final SortingAndPaging personSAndP = SortingAndPaging.createForSingleSortAll(ObjectStatus.ACTIVE, "lastName", "DESC") ;
 		 final PagingWrapper<BaseStudentReportTO> persons = personDao.getStudentReportTOs(personSearchForm, personSAndP);
 		
 		 if (persons == null) {
 			 return personsWithJournalEntries;
 		 }

 		 for (BaseStudentReportTO person:persons) {
			 if (!map.containsKey(person.getSchoolId()) && StringUtils.isNotBlank(person.getCoachSchoolId())) {
				 boolean addStudent = true;
				 if (personSearchForm.getJournalSourceIds()!=null) {
					if (getDao().getJournalCountForPersonForJournalSourceIds(person.getId(), personSearchForm.getJournalSourceIds()) == 0) {
						addStudent = false;
					}
				 }
			 	 if (addStudent) {
					 final JournalCaseNotesStudentReportTO entry = new JournalCaseNotesStudentReportTO(person);
					 personsWithJournalEntries.add(entry);
					 map.put(entry.getSchoolId(), entry);
				 }
 			}
 		 }
		 sortByStudentName(personsWithJournalEntries);

 		 return personsWithJournalEntries;
 	}
 		 
	private static void sortByStudentName(List<JournalCaseNotesStudentReportTO> toSort) {
		toSort.sort((p1, p2) -> compareStudent(p1.getLastName(), p2.getLastName(), p1.getFirstName(), p2.getFirstName(), p1.getMiddleName(), p2.getMiddleName(), p1, p2));
	}

	public static int compareStudent(String lastName, String lastName2, String firstName, String firstName2, String middleName, String middleName2, JournalCaseNotesStudentReportTO p1, JournalCaseNotesStudentReportTO p2) {
		int value = lastName.compareToIgnoreCase(
				lastName2);
		if (value != 0)
			return value;

		value = firstName.compareToIgnoreCase(
				firstName2);
		if (value != 0)
			return value;
		if (middleName == null && middleName2 == null)
			return 0;
		if (middleName == null)
			return - 1;
		if (middleName2 == null)
			return 1;
		return middleName.compareToIgnoreCase(
				middleName2);
	}

}