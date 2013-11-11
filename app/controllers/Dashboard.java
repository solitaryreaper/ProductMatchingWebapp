package controllers;

import java.util.List;

import models.dao.ProductMatchTrialDAO;
import models.db.ProductMatchTrial;
import models.db.ProductMatchTrialPairCompare;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.dashboard.dashboard;
import views.html.dashboard.trialdiff;

public class Dashboard extends Controller {
    
	private static ProductMatchTrialDAO trialDao = new ProductMatchTrialDAO();
	private static List<ProductMatchTrial> productMatchTrials = null;

    /**
     * Renders the trial dashboard with all the information about the various trial runs.
     */
    public static Result index() {
		if(session("user") == null) {
			return unauthorized("Oops, you are not connected");
		}

    	productMatchTrials = trialDao.getProductMatchTrials(null);
    	List<String> trialNames = trialDao.getTrialNames(null);
        return ok(dashboard.render(productMatchTrials, trialNames));
    }

    /**
     * Generates the diff between two trials. Specifically,
     * 1) Compare the summary statistics of two trials.
     * 2) Show the itempairs with different status in each of the trial runs.
     */
    public static Result generateTrialDiff()
    {
		// Get form parameters
		DynamicForm dynamicForm = form().bindFromRequest();
		String trial1Name = dynamicForm.get("trial1_name");
		String trial2Name = dynamicForm.get("trial2_name");

		ProductMatchTrialPairCompare productMatchTrialDiff = 
			trialDao.getDiffBwProductMatchTrials(trial1Name, trial2Name, session("user"));
		return ok(trialdiff.render(productMatchTrialDiff));
    }
}