package models.db;

import java.util.List;

public class TrialItemPairRuleMatchDetails {
	private String ruleName;
	private boolean isRuleSuccess;
	private List<TrialItemPairSubruleMatchDetails> subruleMatchDetails;
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrialItemPairRuleMatchDetails [ruleName=")
				.append(ruleName).append(", isRuleSuccess=")
				.append(isRuleSuccess).append(", subruleMatchDetails=")
				.append(subruleMatchDetails).append("]");
		return builder.toString();
	}
	
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public boolean isRuleSuccess() {
		return isRuleSuccess;
	}
	public void setRuleSuccess(boolean isRuleSuccess) {
		this.isRuleSuccess = isRuleSuccess;
	}
	public List<TrialItemPairSubruleMatchDetails> getSubruleMatchDetails() {
		return subruleMatchDetails;
	}
	public void setSubruleMatchDetails(
			List<TrialItemPairSubruleMatchDetails> subruleMatchDetails) {
		this.subruleMatchDetails = subruleMatchDetails;
	}
}
