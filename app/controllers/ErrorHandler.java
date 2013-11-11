package controllers;

import java.io.PrintWriter;
import java.io.StringWriter;

import play.mvc.Result;
import play.mvc.Controller;
import views.html.error; 

/**
 * Controller class to render apt error page in case of web application errors.
 * @author sprasa4
 *
 */
public class ErrorHandler extends Controller {
		
	public static Result showError(String errorOrigin, String errorReason, Exception errorException)
	{
		StringWriter errors = new StringWriter();
		errorException.printStackTrace(new PrintWriter(errors));		
		return internalServerError(error.render(errorOrigin, errorReason, errors.toString()));
	}
}
