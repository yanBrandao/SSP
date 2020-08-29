package org.jasig.ssp.service;

import org.jasig.ssp.model.JournalEntry;
import org.jasig.ssp.web.api.validation.ValidationException;

public interface StudentTransitionService {
    void setProgramStatus(JournalEntry journalEntry) throws ValidationException, ObjectNotFoundException
}
