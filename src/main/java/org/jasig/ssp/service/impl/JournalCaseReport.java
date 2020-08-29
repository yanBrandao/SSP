package org.jasig.ssp.service.impl;

import org.jasig.ssp.dao.JournalEntryDao;
import org.jasig.ssp.transferobject.reports.BaseStudentReportTO;
import org.jasig.ssp.transferobject.reports.JournalCaseNotesStudentReportTO;
import org.jasig.ssp.transferobject.reports.JournalStepSearchFormTO;
import org.jasig.ssp.util.sort.PagingWrapper;

import java.util.List;

public class JournalCaseReport {

    public static List<JournalCaseNotesStudentReportTO> getReport(
            List<JournalCaseNotesStudentReportTO> personsWithJournalEntries,
            JournalCaseMap map,
            PagingWrapper<BaseStudentReportTO> persons,
            JournalStepSearchFormTO personSearchForm,
            JournalEntryDao dao
    ){
        //1
        if (persons == null) {
            return personsWithJournalEntries;
        }

        //1
        for (BaseStudentReportTO person:persons) {
            //1
            StudentBelongsToSchool.checkStudentSchool(map, person, personSearchForm, personsWithJournalEntries,
                    dao);
        }
        return personsWithJournalEntries;
    }
}
