package org.jasig.ssp.service.impl;

import org.apache.commons.lang.StringUtils;
import org.jasig.ssp.dao.JournalEntryDao;
import org.jasig.ssp.transferobject.reports.BaseStudentReportTO;
import org.jasig.ssp.transferobject.reports.JournalCaseNotesStudentReportTO;
import org.jasig.ssp.transferobject.reports.JournalStepSearchFormTO;

import java.util.List;
import java.util.Map;

public class StudentBelongsToSchool {

    public static void  checkStudentSchool(
            //1
            JournalCaseMap journalCaseMap,
            //1
            BaseStudentReportTO person,
            //1
            JournalStepSearchFormTO personSearchForm,
            //1
            List<JournalCaseNotesStudentReportTO> personsWithJournalEntries,
            //1
            JournalEntryDao dao
    ){
        //1
        if (!journalCaseMap.map.containsKey(person.getSchoolId()) && StringUtils.isNotBlank(person.getCoachSchoolId())) {
            boolean addStudent = true;
            //1
            if (personSearchForm.getJournalSourceIds()!=null) {
                //1
                if (dao.getJournalCountForPersonForJournalSourceIds(person.getId(),
                        personSearchForm.getJournalSourceIds()) == 0) {
                    addStudent = false;
                }
            }
            //1
            if (addStudent) {
                final JournalCaseNotesStudentReportTO entry = new JournalCaseNotesStudentReportTO(person);
                personsWithJournalEntries.add(entry);
                journalCaseMap.map.put(entry.getSchoolId(), entry);
            }
        }
    }
}
