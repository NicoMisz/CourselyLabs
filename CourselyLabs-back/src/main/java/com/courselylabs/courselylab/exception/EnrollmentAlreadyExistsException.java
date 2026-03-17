package com.courselylabs.courselylab.exception;

public class EnrollmentAlreadyExistsException extends RuntimeException {

    public EnrollmentAlreadyExistsException() {
        super("Ya estas inscrito en este curso");
    }
}