package models.utils;

import java.util.List;
import java.util.Map;

import models.db.TrialItemPairMatchDetails;
import models.db.TrialItemPairRuleMatchDetails;
import models.db.TrialItemPairSubruleMatchDetails;
import models.db.TrialPairComparer;
import models.enums.DataSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Utility methods to correctly compute the difference between two trial runs for the same dataset
 * and slightly different rules.
 * 
 * @author sprasa4
 *
 */
public class TrialPairDiffUtils {

	private static String MISMATCH_NEW_RULE = "NEW RULE ADDED";
	private static String MISMATCH_SAME_RULE_CHANGED_SUBRULES = "SAME RULE, NEW SUBRULES";
	
	private static String NOT_APPLICABLE = "NOT_APPLICABLE";
	
	private static String PASSED_SUBRULES_DIFF = "passed";
	private static String FAILED_SUBRULES_DIFF = "failed";
	
	/**
	 * Computes the difference between two trial runs for the same itempair.
	 */
	public static TrialPairComparer computeTrialPairDiff(
			TrialItemPairMatchDetails trial1Details, TrialItemPairMatchDetails trial2Details)
	{		
		/*
		System.out.println("Trial1 itempair " + trial1Details.getSourceItemName() + "#" + trial1Details.getTargetItemName() + 
			"==>" + trial1Details.getRuleMatchDetails().size());
		System.out.println("Trial2 itempair " + trial2Details.getSourceItemName() + "#" + trial2Details.getTargetItemName() + 
			"==>" + trial2Details.getRuleMatchDetails().size());
		*/
		
		// Get the basic details to show in the diff first
		String sourceItemName = trial1Details.getSourceItemName();
		String targetItemName = trial1Details.getTargetItemName();
		DataSource sourceDataSource = trial1Details.getSourceDataSource();
		DataSource targetDataSource = trial2Details.getTargetDataSource();
		String trial1Name = trial1Details.getTrialName();
		String trial2Name = trial2Details.getTrialName();
		boolean trial1MatchStatus = trial1Details.isItemPairMatch();
		boolean trial2MatchStatus = trial2Details.isItemPairMatch();
		
		// Now figure out the exact reason why they mismatched
		// Reasons can be : NEW RULE, SAME RULE NEW SUBRULE, SAME RULE CHANGED SUBRULE
		
		List<TrialItemPairRuleMatchDetails> passedRules = null;
		List<TrialItemPairRuleMatchDetails> failedRules = null;
		if(trial1MatchStatus) {
			passedRules = trial1Details.getRuleMatchDetails();
			failedRules = trial2Details.getRuleMatchDetails();
		}
		else {
			passedRules = trial2Details.getRuleMatchDetails();
			failedRules = trial1Details.getRuleMatchDetails();			
		}
		
		// Find the rule that passed
		TrialItemPairRuleMatchDetails passedRule = null;
		List<TrialItemPairSubruleMatchDetails> passedRuleSubrules = null;
		for(TrialItemPairRuleMatchDetails rule : passedRules) {
			if(rule.isRuleSuccess()) {
				passedRule = rule;
				passedRuleSubrules = rule.getSubruleMatchDetails();
				break;
			}
		}
		
		// Locate this passed rule in the set of failed rules to find the exact mismatch reason
		TrialItemPairRuleMatchDetails matchingFailedRule = null;
		List<TrialItemPairSubruleMatchDetails> matchingFailedRuleSubrules = null;
		for(TrialItemPairRuleMatchDetails rule : failedRules) {
			if(rule.getRuleName().equals(passedRule.getRuleName()) && !rule.isRuleSuccess()) {
				matchingFailedRule = rule;
				matchingFailedRuleSubrules = rule.getSubruleMatchDetails();
				break;
			}
		}
		
		// These parameters capture the exact reason for mismatch between the itempairs across
		// two trial runs.
		String diffReason = null;
		String diffRuleName = null;
		List<String> diffSubrulesTrial1 = null;
		List<String> diffSubrulesTrial2 = null;
		
		diffRuleName = passedRule.getRuleName();
		// Implies a new rule was added for passed trial that led to itempair matching
		if(matchingFailedRule == null) {
			diffReason = MISMATCH_NEW_RULE;
			diffSubrulesTrial1 = Lists.newArrayList();
			diffSubrulesTrial1 = Lists.newArrayList();
		}
		// Same rule has been found, but the exact reason for difference is at the subrule level.
		else {
			diffReason = MISMATCH_SAME_RULE_CHANGED_SUBRULES;
			Map<String, List<TrialItemPairSubruleMatchDetails>> diffSubrulesMap = 
					getDiffSubrules(passedRuleSubrules, matchingFailedRuleSubrules);
			
			List<TrialItemPairSubruleMatchDetails> passedDiffSubrulesToShow = diffSubrulesMap.get(PASSED_SUBRULES_DIFF);			
			List<TrialItemPairSubruleMatchDetails> failedDiffSubrulesToShow = diffSubrulesMap.get(FAILED_SUBRULES_DIFF);
			
			diffSubrulesTrial1 = getSubruleDefinitions(passedDiffSubrulesToShow);
			diffSubrulesTrial2 = getSubruleDefinitions(failedDiffSubrulesToShow);
		}
		
		return new TrialPairComparer(
			sourceItemName, targetItemName, sourceDataSource, targetDataSource, trial1Name, trial2Name, 
			trial1MatchStatus, trial2MatchStatus, diffReason, diffRuleName, diffSubrulesTrial1, diffSubrulesTrial2
		);
	}
	
	private static List<String> getSubruleDefinitions(List<TrialItemPairSubruleMatchDetails> subrules)
	{
		List<String> subruleDefinitions = Lists.newArrayList();
		for(TrialItemPairSubruleMatchDetails subruleDetails : subrules) {
			StringBuilder subruleDef = new StringBuilder();
			subruleDef.append(subruleDetails.getSourceAttributes()).append(" MATCH ").append(subruleDetails.getTargetAttributes());
			subruleDefinitions.add(subruleDef.toString());
		}
		return subruleDefinitions;
	}
	
	// Get the different subrules between the two lists
	private static Map<String, List<TrialItemPairSubruleMatchDetails>> getDiffSubrules(
		List<TrialItemPairSubruleMatchDetails> passedSubrules, 
		List<TrialItemPairSubruleMatchDetails> failedSubrules)
	{
		Map<String, List<TrialItemPairSubruleMatchDetails>> diffSubrulesMap = Maps.newHashMap();
		
		List<TrialItemPairSubruleMatchDetails> passedDiffSubrules = Lists.newArrayList();
		List<TrialItemPairSubruleMatchDetails> failedDiffSubrules = Lists.newArrayList();
		
		// Add all the passed subrules that are either not present in the failed subrules or
		// have a failure status in failed subrules.
		for(TrialItemPairSubruleMatchDetails passedSubrule : passedSubrules) {
			boolean isSameSubruleFoundWithDiffMatchStatus = false;
			boolean isSameSubruleFound = false;
			for(TrialItemPairSubruleMatchDetails failedSubrule : failedSubrules) {
				if(isSubruleWithSameSourceAttrs(passedSubrule, failedSubrule)) {
					isSameSubruleFound = true;
					if(!failedSubrule.isSubruleSuccess()) {
						isSameSubruleFoundWithDiffMatchStatus = true;
						failedDiffSubrules.add(failedSubrule);
						break;						
					}
				}
			}
			
			if(!isSameSubruleFound || isSameSubruleFoundWithDiffMatchStatus) {
				passedDiffSubrules.add(passedSubrule);
			}
		}

		// What about the case when a rule has passed because some subrules have been removed from
		// it which led to passing of the rule.
		for(TrialItemPairSubruleMatchDetails failedSubrule : failedSubrules) {
			boolean isFailedSubrulePresentinPassedSubrules = false;
			for(TrialItemPairSubruleMatchDetails passedSubrule : passedSubrules) {
				if(isSubruleWithSameSourceAttrs(passedSubrule, failedSubrule)) {
					isFailedSubrulePresentinPassedSubrules = true;
					break;
				}
			}
			if(!isFailedSubrulePresentinPassedSubrules) {
				failedDiffSubrules.add(failedSubrule);
			}
		}
		
		diffSubrulesMap.put(PASSED_SUBRULES_DIFF, passedDiffSubrules);
		diffSubrulesMap.put(FAILED_SUBRULES_DIFF, failedDiffSubrules);
			
		return diffSubrulesMap;
	}
	
	// Determines if two subrules across different rules are same. Two subrules can be considered
	// same within a rule, if they have the same source attributes.
	private static boolean  isSubruleWithSameSourceAttrs(
		TrialItemPairSubruleMatchDetails passedSubrule, TrialItemPairSubruleMatchDetails failedSubrule)
	{
		List<String> passedSubruleSrcAttrs = passedSubrule.getSourceAttributes();
		List<String> failedSubruleSrcAttrs = failedSubrule.getSourceAttributes();
		
		if(passedSubruleSrcAttrs.equals(failedSubruleSrcAttrs)) {
			return true;
		}
		
		return false;
	}
}
