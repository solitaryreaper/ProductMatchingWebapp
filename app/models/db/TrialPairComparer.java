package models.db;

import java.util.List;

import models.enums.DataSource;


/**
 * Displays the reasons for difference of match status for the same itempair across different trial
 * runs. This is very useful during the iterative evolution of rules, where you can modify a ruleset
 * and see how it compares with the previous ruleset results.
 * 
 * @author sprasa4
 *
 */
public class TrialPairComparer {
	// Basic information about the two trials
	private String sourceItemName;
	private String targetItemName;
	private DataSource sourceItemDataSource;
	private DataSource targetItemDataSource;
	private String trial1Name;
	private String tria12Name;
	private boolean trial1MatchStatus;
	private boolean trial2MatchStatus;
	
	// Reason and analysis of the difference
	private String diffReason;
	private String commonRuleName;
	private List<String> trial1DiffSubruleDefinitions;
	private List<String> trial2DiffSubruleDefinitions;
	
	public TrialPairComparer()
	{
		
	}
	
	public TrialPairComparer(String sourceItemName, String targetItemName,
			DataSource sourceDS, DataSource targetDS,
			String trial1Name, String tria12Name, boolean trial1MatchStatus,
			boolean trial2MatchStatus, String diffReason,
			String commonRuleName, 
			List<String> trial1DiffSubruleDefinitions,
			List<String> trial2DiffSubruleDefinitions) {
		super();
		this.sourceItemName = sourceItemName;
		this.targetItemName = targetItemName;
		this.sourceItemDataSource = sourceDS;
		this.targetItemDataSource = targetDS;
		this.trial1Name = trial1Name;
		this.tria12Name = tria12Name;
		this.trial1MatchStatus = trial1MatchStatus;
		this.trial2MatchStatus = trial2MatchStatus;
		this.diffReason = diffReason;
		this.commonRuleName = commonRuleName;
		this.trial1DiffSubruleDefinitions = trial1DiffSubruleDefinitions;
		this.trial2DiffSubruleDefinitions = trial2DiffSubruleDefinitions;
	}
	
	public String getSourceItemName() {
		return sourceItemName;
	}
	public void setSourceItemName(String sourceItemName) {
		this.sourceItemName = sourceItemName;
	}
	public String getTargetItemName() {
		return targetItemName;
	}
	public void setTargetItemName(String targetItemName) {
		this.targetItemName = targetItemName;
	}
	public DataSource getSourceItemDataSource() {
		return sourceItemDataSource;
	}

	public void setSourceItemDataSource(DataSource sourceItemDataSource) {
		this.sourceItemDataSource = sourceItemDataSource;
	}

	public DataSource getTargetItemDataSource() {
		return targetItemDataSource;
	}

	public void setTargetItemDataSource(DataSource targetItemDataSource) {
		this.targetItemDataSource = targetItemDataSource;
	}

	public String getTrial1Name() {
		return trial1Name;
	}
	public void setTrial1Name(String trial1Name) {
		this.trial1Name = trial1Name;
	}
	public String getTria12Name() {
		return tria12Name;
	}
	public void setTria12Name(String tria12Name) {
		this.tria12Name = tria12Name;
	}
	public boolean isTrial1MatchStatus() {
		return trial1MatchStatus;
	}
	public void setTrial1MatchStatus(boolean trial1MatchStatus) {
		this.trial1MatchStatus = trial1MatchStatus;
	}
	public boolean isTrial2MatchStatus() {
		return trial2MatchStatus;
	}
	public void setTrial2MatchStatus(boolean trial2MatchStatus) {
		this.trial2MatchStatus = trial2MatchStatus;
	}
	public String getDiffReason() {
		return diffReason;
	}
	public void setDiffReason(String diffReason) {
		this.diffReason = diffReason;
	}
	public String getCommonRuleName() {
		return commonRuleName;
	}
	public void setCommonRuleName(String commonRuleName) {
		this.commonRuleName = commonRuleName;
	}
	
	public List<String> getTrial1DiffSubruleDefinitions() {
		return trial1DiffSubruleDefinitions;
	}
	public void setTrial1DiffSubruleDefinitions(List<String> trial1DiffSubruleDefinitions) {
		this.trial1DiffSubruleDefinitions = trial1DiffSubruleDefinitions;
	}
	public List<String> getTrial2DiffSubruleDefinitions() {
		return trial2DiffSubruleDefinitions;
	}
	public void setTrial2DiffSubruleDefinitions(List<String> trial2DiffSubruleDefinitions) {
		this.trial1DiffSubruleDefinitions = trial2DiffSubruleDefinitions;
	}
}
