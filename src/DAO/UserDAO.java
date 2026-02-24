package DAO;

import java.util.ArrayList;
import java.util.List;

import Model.users;

/**
 * Data Access Object for {@link users}.
 * 
 * For this assignment, this implementation uses an in-memory list
 * instead of a real database.
 */
public class UserDAO {

	private final List<users> userList;

	public UserDAO() {
		this.userList = new ArrayList<>();
	}

	
	public void addUser(users user) {
		if (user != null) {
			userList.add(user);
		}
	}

	
	public boolean updateUser(users updatedUser) {
		if (updatedUser == null) {
			return false;
		}

		for (int i = 0; i < userList.size(); i++) {
			users current = userList.get(i);
			if (current.getUserId() == updatedUser.getUserId()) {
				userList.set(i, updatedUser);
				return true;
			}
		}
		return false;
	}

	public boolean deactivateUser(int userId) {
		for (users u : userList) {
			if (u.getUserId() == userId) {
				u.setStatus("inactive");
				return true;
			}
		}
		return false;
	}

	
	public users getUserById(int userId) {
		for (users u : userList) {
			if (u.getUserId() == userId) {
				return u;
			}
		}
		return null;
	}


	public users getUserByEmail(String email) {
		if (email == null) {
			return null;
		}

		for (users u : userList) {
			if (email.equalsIgnoreCase(u.getEmail())) {
				return u;
			}
		}
		return null;
	}
}
