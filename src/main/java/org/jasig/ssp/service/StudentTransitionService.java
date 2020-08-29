package org.jasig.ssp.service;

import org.jasig.ssp.model.JournalEntry;
import org.jasig.ssp.transferobject.reports.BaseStudentReportTO;
import org.jasig.ssp.transferobject.reports.JournalStepSearchFormTO;
import org.jasig.ssp.util.sort.PagingWrapper;
import org.jasig.ssp.util.sort.SortingAndPaging;
import org.jasig.ssp.web.api.validation.ValidationException;

public interface StudentTransitionService {
    void setProgramStatus(JournalEntry journalEntry) throws ValidationException, ObjectNotFoundException;
    PagingWrapper<BaseStudentReportTO> getBaseStudentReport(JournalStepSearchFormTO personSearchForm,
                                                            SortingAndPaging personSAndP) throws ObjectNotFoundException;
}
