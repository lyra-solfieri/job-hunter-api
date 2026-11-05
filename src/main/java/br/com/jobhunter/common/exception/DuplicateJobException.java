package br.com.jobhunter.common.exception;

public class DuplicateJobException extends RuntimeException {

    public DuplicateJobException(String message) {
        super(message);
    }
}
