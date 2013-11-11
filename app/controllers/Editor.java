package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.editor.editor;

import com.walmart.productgenome.pairComparison.parser.ParseException;
import com.walmart.productgenome.pairComparison.service.ItemPairDataMatcher;

public class Editor extends Controller {

	private static String VALIDATION_SUCCESS = "RULE_VALIDATION_PASSED";
	private static String VALIDATION_FAILURE = "RULE_VALIDATION_FAILED";
	private static String EDITOR_INITIALZER  = "RULE_EDITOR_INITIALIZER";
	
	public static Result index() {
		if(session("user") == null) {
			return unauthorized("Oops, you are not connected");
		}

        return ok(editor.render("Please add product matching rules here !!", EDITOR_INITIALZER));
    }

	public static Result validateProductMatchingRules() {
		// Get form parameters
		DynamicForm dynamicForm = form().bindFromRequest();
		String matchingRules  = dynamicForm.get("matching_rules_editor");
		
		File ruleFile = getRuleFile(matchingRules);
		boolean areRulesValid = true;
		try {
			ItemPairDataMatcher matcher = new ItemPairDataMatcher(ruleFile);
			if(matcher.getRuleset() == null) {
				areRulesValid = false;
			}
		} catch (FileNotFoundException e) {
			areRulesValid = false;
			e.printStackTrace();
		} catch (ParseException e) {
			areRulesValid = false;
			e.printStackTrace();
		}
		
		Logger.info("Rules : " + matchingRules);
		Logger.info("Are rules valid ? " + areRulesValid);
		if(areRulesValid) {
			return ok(editor.render(matchingRules, VALIDATION_SUCCESS));			
		}
		else {
			return ok(editor.render(matchingRules, VALIDATION_FAILURE));
		}

	}
	
	// Host rules from web editor's textarea onto a temporary file for processing
	private static File getRuleFile(String matchingRules)
	{
		File ruleFile = new File("/tmp/rule_file.rl");
		FileWriter fw;
		try {
			fw = new FileWriter(ruleFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(matchingRules);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ruleFile;
	}
}
