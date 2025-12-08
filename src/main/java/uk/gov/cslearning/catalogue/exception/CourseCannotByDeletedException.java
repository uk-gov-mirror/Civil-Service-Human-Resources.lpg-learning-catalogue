package uk.gov.cslearning.catalogue.exception;

public class CourseCannotByDeletedException extends RuntimeException{
    public CourseCannotByDeletedException(){
        super("A course cannot be deleted if it has been previously published");
    }
}
