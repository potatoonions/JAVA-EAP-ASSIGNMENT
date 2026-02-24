package Controller;

import Model.users;
import Service.UserService;

/**
 * Controller responsible for authentication-related actions.
 */
public class AuthController {

	private final UserService userService;
	private users loggedInUser;

	public AuthController() {
		this.userService = new UserService();
		this.loggedInUser = null;
	}

	
	public boolean login(String email, String password) {
		if (validateCredentials(email, password)) {
			loggedInUser = userService.getUserByEmail(email);
			return true;
		}
		return false;
	}

	public void logout() {
		loggedInUser = null;
	}

	
	public boolean validateCredentials(String email, String password) {
		if (email == null || password == null) {
			return false;
		}

		users u = userService.getUserByEmail(email);
		if (u == null) {
			return false;
		}

		boolean passwordMatches = password.equals(u.getPassword());
		boolean isActive = "active".equalsIgnoreCase(u.getStatus());

		return passwordMatches && isActive;
	}

	
	public users getLoggedInUser() {
		return loggedInUser;
	}
}
