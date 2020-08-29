package org.jasig.ssp.service.impl;

import org.jasig.ssp.transferobject.reports.JournalCaseNotesStudentReportTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalCaseMap {

    public Map<String, JournalCaseNotesStudentReportTO> map = new HashMap<>();

    public JournalCaseMap(List<JournalCaseNotesStudentReportTO> personsWithJournalEntries){
        personsWithJournalEntries.forEach(entry -> map.put(entry.getSchoolId(), entry));
    }
}
