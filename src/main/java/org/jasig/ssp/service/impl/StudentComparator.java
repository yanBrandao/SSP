package org.jasig.ssp.service.impl;

import org.jasig.ssp.transferobject.reports.JournalCaseNotesStudentReportTO;

public class StudentComparator {
    
    public static int compareNames(JournalCaseNotesStudentReportTO firstStudent,
                                    JournalCaseNotesStudentReportTO secondStudent){
        int value = firstStudent.getLastName().compareToIgnoreCase(
                secondStudent.getLastName());
        //1
        if(value != 0)
            return value;

        value = firstStudent.getFirstName().compareToIgnoreCase(
                secondStudent.getFirstName());
        //1
        if(value != 0)
            return value;
        //1
        if(firstStudent.getMiddleName() == null && secondStudent.getMiddleName() == null)
            return 0;
        //1
        if(firstStudent.getMiddleName() == null)
            return -1;
        //1
        if(secondStudent.getMiddleName() == null)
            return 1;
        return firstStudent.getMiddleName().compareToIgnoreCase(
                secondStudent.getMiddleName());
    }
}
