package org.jasig.ssp.service.impl;

import org.jasig.ssp.model.EarlyAlert;
import org.jasig.ssp.security.SspUser;
import org.jasig.ssp.service.ObjectNotFoundException;
import org.jasig.ssp.web.api.validation.ValidationException;

import java.util.UUID;

public class EarlyAlertValidation {

    public static void checkIsNull(EarlyAlert earlyAlert) {
        //1
        if (earlyAlert == null) {
            throw new IllegalArgumentException("EarlyAlert was missing.");
        }
    }

    public static void checkIsNullReturnNotFound(EarlyAlert earlyAlert, UUID earlyAlertId) throws ObjectNotFoundException {
        //1
        if (earlyAlert == null) {
            throw new ObjectNotFoundException(earlyAlertId, EarlyAlert.class.getName());
        }
    }

    public static void checkCreatedBy(EarlyAlert earlyAlert) {
        //1
        if (earlyAlert.getCreatedBy() == null) {
            throw new IllegalArgumentException(
                    "EarlyAlert.CreatedBy is missing.");
        }
    }

    public static void checkCampus(EarlyAlert earlyAlert){
        //1
        if (earlyAlert.getCampus() == null) {
            throw new IllegalArgumentException("EarlyAlert.Campus is missing.");
        }
    }

    public static void checkCampus(EarlyAlert earlyAlert, String message){
        //1
        if (earlyAlert.getCampus() == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkCampusAlertCoordinator(EarlyAlert earlyAlert) throws ValidationException {
        if (earlyAlert.getCampus().getEarlyAlertCoordinatorId() == null){
            throw new ValidationException(
                    "Could not determined the Early Alert Coordinator for this student. Ensure that a default coordinator is set globally and for all campuses.");
        }
    }

  public static void checkNames(EarlyAlert earlyAlert) {
    if ((earlyAlert.getCreatedBy() == null) || (earlyAlert.getCreatedBy().getFirstName() == null)) {
      //1
      if (earlyAlert.getCreatedBy() == null) {
        throw new IllegalArgumentException("EarlyAlert.CreatedBy is missing.");
      }
    }
   }

    public static void checkPerson(EarlyAlert earlyAlert){
        //1
        if (earlyAlert.getPerson() == null) {
            throw new IllegalArgumentException("EarlyAlert.Person is missing.");
        }
    }

  public static void checkIsNull(EarlyAlert earlyAlert, String message) {
    // 1
    if (earlyAlert == null) {
      throw new IllegalArgumentException(message);
    }
    }

    public static void checkPerson(EarlyAlert earlyAlert, String message) throws ValidationException {
        //1
        if (earlyAlert.getPerson() == null) {
            throw new ValidationException(message);
        }
    }

    public static void checkCurrentUser(SspUser sspUser) throws ValidationException {
        if ( sspUser == null ) {
            throw new ValidationException("Early Alert cannot be closed by a null User.");
        }
    }
}
