package models.db;

import java.util.List;

import models.enums.DataSource;

/**
 * Contains all the details of a trial run for an itempair. It is basically a collection of the
 * all the match info for individual subrules.
 * @author sprasa4
 *
 */
public class TrialItemPairMatchDetails {
	// Name of the trial
	private String trialName;
	
	// Names and sources of the itempair
	private String sourceItemName;
	private DataSource sourceDataSource;
	private String targetItemName;
	private DataSource targetDataSource;
	
	// Overall summary status
	private boolean isItemPairMatch;
	private String rulesetName;
	
	// Match status and details for all the rules fired for this itempair.
	private List<TrialItemPairRuleMatchDetails> ruleMatchDetails;

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrialItemPairMatchDetails [trialName=")
				.append(trialName).append(", sourceItemName=")
				.append(sourceItemName).append(", sourceDataSource=")
				.append(sourceDataSource).append(", targetItemName=")
				.append(targetItemName).append(", targetDataSource=")
				.append(targetDataSource).append(", isItemPairMatch=")
				.append(isItemPairMatch).append(", rulesetName=")
				.append(rulesetName).append(", ruleMatchDetails=")
				.append(ruleMatchDetails).append("]");
		return builder.toString();
	}

	public String getTrialName() {
		return trialName;
	}

	public void setTrialName(String trialName) {
		this.trialName = trialName;
	}

	public String getSourceItemName() {
		return sourceItemName;
	}

	public void setSourceItemName(String sourceItemName) {
		this.sourceItemName = sourceItemName;
	}

	public DataSource getSourceDataSource() {
		return sourceDataSource;
	}

	public void setSourceDataSource(DataSource sourceDataSource) {
		this.sourceDataSource = sourceDataSource;
	}

	public String getTargetItemName() {
		return targetItemName;
	}

	public void setTargetItemName(String targetItemName) {
		this.targetItemName = targetItemName;
	}

	public DataSource getTargetDataSource() {
		return targetDataSource;
	}

	public void setTargetDataSource(DataSource targetDataSource) {
		this.targetDataSource = targetDataSource;
	}

	public boolean isItemPairMatch() {
		return isItemPairMatch;
	}

	public void setItemPairMatch(boolean isItemPairMatch) {
		this.isItemPairMatch = isItemPairMatch;
	}

	public String getRulesetName() {
		return rulesetName;
	}

	public void setRulesetName(String rulesetName) {
		this.rulesetName = rulesetName;
	}

	public List<TrialItemPairRuleMatchDetails> getRuleMatchDetails() {
		return ruleMatchDetails;
	}

	public void setRuleMatchDetails(
			List<TrialItemPairRuleMatchDetails> ruleMatchDetails) {
		this.ruleMatchDetails = ruleMatchDetails;
	}
}
