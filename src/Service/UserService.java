package Service;

import DAO.UserDAO;
import Model.users;

/**
 * Service layer for user-related operations.
 */
public class UserService {

	private final UserDAO userDAO;

	public UserService() {
		this.userDAO = new UserDAO();
	}

	public void createUserAccount(users user) {
		userDAO.addUser(user);
	}

	public boolean updateUserAccount(users user) {
		return userDAO.updateUser(user);
	}

	public boolean deactivateAccount(int userId) {
		return userDAO.deactivateUser(userId);
	}

	public boolean resetPassword(int userId, String newPassword) {
		users u = userDAO.getUserById(userId);
		if (u == null || newPassword == null) {
			return false;
		}
		u.setPassword(newPassword);
		return userDAO.updateUser(u);
	}

	public boolean recoverAccount(String email) {
		users u = userDAO.getUserByEmail(email);
		if (u == null) {
			return false;
		}
		u.setStatus("active");
		return userDAO.updateUser(u);
	}

	public users getUserByEmail(String email) {
		return userDAO.getUserByEmail(email);
	}
}
