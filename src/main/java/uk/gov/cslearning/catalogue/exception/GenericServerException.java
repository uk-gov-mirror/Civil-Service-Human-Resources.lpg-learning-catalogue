package uk.gov.cslearning.catalogue.exception;

public class GenericServerException extends RuntimeException {
    public GenericServerException(String message) {
        super(message);
    }
}
