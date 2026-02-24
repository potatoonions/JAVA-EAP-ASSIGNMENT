package Model;

/**
 * Represents system users.
 */
public class users {
	
	private int userId;
	private String name;
	private String email;
	private String password;
	private String role;
	private String status;
	
	// Constructor
	public users() {
	}
	
	// Constructor with all parameters
	public users(int userId, String name, String email, String password, String role, String status) {
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.status = status;
	}
	
	// Getters and Setters
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "users [userId=" + userId + ", name=" + name + ", email=" + email + ", role=" + role + ", status=" + status + "]";
	}
}
