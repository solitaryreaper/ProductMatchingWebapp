package controllers;

import models.dao.LoginDao;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.login;

public class Login extends Controller{

	private static LoginDao loginDao = new LoginDao();
	
	// Default landing page asking user to login
	public static Result index()
	{
		return ok(login.render());
	}
	
	// Attempts to login user and sets the session variables
	public static Result login()
	{
		// Get form parameters
		DynamicForm dynamicForm = form().bindFromRequest();
		String username = dynamicForm.get("username");
		String password = dynamicForm.get("password");

		boolean isLoginSuccess = loginDao.verifyLogin(username, password);
		if(isLoginSuccess) {
			session("user", username);
			return redirect("/matcher");			
		}
		else {
			return unauthorized("Oops, you are not connected");
		}
	}
	
	// Create a new user account
	public static Result createAccount()
	{
		DynamicForm dynamicForm = form().bindFromRequest();
		String username = dynamicForm.get("username");
		String password = dynamicForm.get("password");
		
		boolean isAccountCreated = loginDao.createLogin(username, password);
		if(isAccountCreated) {
			Logger.info("Successfully created the account !!");
			session("user", username);
			return redirect("/matcher");
		}
		else {
			Logger.error("Failed to create the account !!");
			return redirect("/");
		}
		
	}
	
	// Logs out the user and clears the session variables
	public static Result logout()
	{
		// Clear all the session variables
		session().clear();
		
		// Redirect the user to login page
		return redirect("/");
	}
	 
}
