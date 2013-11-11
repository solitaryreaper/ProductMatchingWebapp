package models.db;

import java.util.List;

import com.google.common.base.Objects;


/**
 * Contains all the detailed information about the trial run for an itempair, including the 
 * pass/fail status of each rule, pass/fail status of each clause etc. This data is highly
 * denormalized for easy access of relevant information at subrule level.
 *  
 * @author sprasa4
 *
 */
public class TrialItemPairSubruleMatchDetails {
	private String subruleName;
	private boolean isSubruleSuccess;
	
	// Subrule attributes to be matched
	private List<String> sourceAttributes;
	private List<String> targetAttributes;
	
	// Subrule metadata
	private String comparer;
	private String evaluator;
	private String sourceTokenizer;
	private String targetTokenizer;
	private double scoreThreshold;
	private boolean isMissingAttributeAllowed;
	
	public TrialItemPairSubruleMatchDetails()
	{
		
	}
	
	/*
	@Override
	public boolean equals(Object obj)
	{
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
		TrialItemPairSubruleMatchDetails other = (TrialItemPairSubruleMatchDetails) obj;
		return 	Objects.equal(this.subruleName, other.subruleName) &&
				Objects.equal(this.sourceAttributes, other.sourceAttributes) &&
				Objects.equal(this.targetAttributes, other.targetAttributes);

	}
	*/
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrialItemPairSubruleMatchDetails [subruleName=")
				.append(subruleName).append(", isSubruleSuccess=")
				.append(isSubruleSuccess).append(", sourceAttributes=")
				.append(sourceAttributes).append(", targetAttributes=")
				.append(targetAttributes).append(", comparer=")
				.append(comparer).append(", evaluator=").append(evaluator)
				.append(", sourceTokenizer=").append(sourceTokenizer)
				.append(", targetTokenizer=").append(targetTokenizer)
				.append(", scoreThreshold=").append(scoreThreshold)
				.append(", isMissingAttributeAllowed=")
				.append(isMissingAttributeAllowed).append("]");
		return builder.toString();
	}

	//------ Getters & Setters ------------
	public String getSubruleName() {
		return subruleName;
	}
	public void setSubruleName(String subruleName) {
		this.subruleName = subruleName;
	}
	public boolean isSubruleSuccess() {
		return isSubruleSuccess;
	}
	public void setSubruleSuccess(boolean isSubruleSuccess) {
		this.isSubruleSuccess = isSubruleSuccess;
	}
	public List<String> getSourceAttributes() {
		return sourceAttributes;
	}

	public void setSourceAttributes(List<String> sourceAttributes) {
		this.sourceAttributes = sourceAttributes;
	}

	public List<String> getTargetAttributes() {
		return targetAttributes;
	}

	public void setTargetAttributes(List<String> targetAttributes) {
		this.targetAttributes = targetAttributes;
	}

	public String getComparer() {
		return comparer;
	}
	public void setComparer(String comparer) {
		this.comparer = comparer;
	}
	public String getEvaluator() {
		return evaluator;
	}
	public void setEvaluator(String evaluator) {
		this.evaluator = evaluator;
	}
	public String getSourceTokenizer() {
		return sourceTokenizer;
	}
	public void setSourceTokenizer(String sourceTokenizer) {
		this.sourceTokenizer = sourceTokenizer;
	}
	public String getTargetTokenizer() {
		return targetTokenizer;
	}
	public void setTargetTokenizer(String targetTokenizer) {
		this.targetTokenizer = targetTokenizer;
	}
	public double getScoreThreshold() {
		return scoreThreshold;
	}
	public void setScoreThreshold(double scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
	}
	public boolean isMissingAttributeAllowed() {
		return isMissingAttributeAllowed;
	}
	public void setMissingAttributeAllowed(boolean isMissingAttributeAllowed) {
		this.isMissingAttributeAllowed = isMissingAttributeAllowed;
	}
}
