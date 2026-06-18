package com.epam.gymcrmspringboot.exception;

public class UserAlreadyRegisteredInOppositeRoleException extends RuntimeException {

	public UserAlreadyRegisteredInOppositeRoleException(String message) {
		super(message);
	}

}
