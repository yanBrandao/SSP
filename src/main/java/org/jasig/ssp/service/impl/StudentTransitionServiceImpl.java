package org.jasig.ssp.service.impl;

import org.jasig.ssp.model.JournalEntry;
import org.jasig.ssp.model.JournalEntryDetail;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.service.PersonProgramStatusService;
import org.jasig.ssp.service.StudentTransitionService;
import org.jasig.ssp.web.api.validation.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class StudentTransitionServiceImpl implements StudentTransitionService {

    public StudentTransitionServiceImpl(PersonProgramStatusService personProgramStatusService){
        this.personProgramStatusService = personProgramStatusService;
    }

    private final PersonProgramStatusService personProgramStatusService;

    @Override
    public void setProgramStatus(JournalEntry journalEntry) throws ValidationException, ObjectNotFoundException {
        //1
        // search for a JournalStep that indicates a transition
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
}
