package Controller;

import Model.users;

/**
 * Handles user roles and permission checks.
 */
public class RoleManager {

	public RoleManager() {
	}

	
	public void assignRole(users user, String role) {
		if (user == null || role == null) {
			return;
		}
		user.setRole(role);
	}


	public boolean checkPermission(users user, String permission) {
		if (user == null || permission == null) {
			return false;
		}

		String role = user.getRole();
		if (role == null) {
			return false;
		}

		
		String normalizedRole = role.toUpperCase();
		String normalizedPermission = permission.toUpperCase();

	
		if ("ADMIN".equals(normalizedRole)) {
			return true;
		}

	
		if ("USER".equals(normalizedRole)) {
			return "VIEW_PROFILE".equals(normalizedPermission)
					|| "UPDATE_PROFILE".equals(normalizedPermission)
					|| "RESET_PASSWORD".equals(normalizedPermission);
		}

		
		if ("GUEST".equals(normalizedRole)) {
			return "VIEW_PROFILE".equals(normalizedPermission);
		}

		
		return false;
	}
}
