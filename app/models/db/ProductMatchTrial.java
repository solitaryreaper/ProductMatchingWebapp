package models.db;

import java.util.Date;

import com.walmart.productgenome.pairComparison.utils.NumberUtils;

/**
 * A model object to represent a product match trial
 * @author sprasa4
 *
 */
public class ProductMatchTrial {
	// Trial meta information
	private int trialId;
	private String trialName;
	private String trialDescription;
	private Date runTime;
	private String runUser;
	
	// Trial stats
	private int matchedPairs;
	private int mismatchedPairs;
	
	public ProductMatchTrial()
	{
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProductMatchTrial [trialId=").append(trialId)
				.append(", trialName=").append(trialName)
				.append(", trialDescription=").append(trialDescription)
				.append(", runTime=").append(runTime).append(", runUser=")
				.append(runUser).append(", matchedPairs=").append(matchedPairs)
				.append(", mismatchedPairs=").append(mismatchedPairs)
				.append("]");
		return builder.toString();
	}

	public int getTrialId() {
		return trialId;
	}

	public void setTrialId(int trialId) {
		this.trialId = trialId;
	}

	public String getTrialName() {
		return trialName;
	}

	public void setTrialName(String trialName) {
		this.trialName = trialName;
	}

	public String getTrialDescription() {
		return trialDescription;
	}

	public void setTrialDescription(String trialDescription) {
		this.trialDescription = trialDescription;
	}

	public Date getRunTime() {
		return runTime;
	}

	public void setRunTime(Date runTime) {
		this.runTime = runTime;
	}

	public String getRunUser() {
		return runUser;
	}

	public void setRunUser(String runUser) {
		this.runUser = runUser;
	}

	public int getMatchedPairs() {
		return matchedPairs;
	}

	public void setMatchedPairs(int matchedPairs) {
		this.matchedPairs = matchedPairs;
	}

	public int getMismatchedPairs() {
		return mismatchedPairs;
	}

	public void setMismatchedPairs(int mismatchedPairs) {
		this.mismatchedPairs = mismatchedPairs;
	}

	public String getMatchedItemPairsPercentage()
	{
		double percentage = (double)matchedPairs*100/(double)(matchedPairs + mismatchedPairs);
		return NumberUtils.formatDouble(percentage);
	}
	
	public String getMismatchedItemPairsPercentage()
	{
		double percentage = (double)mismatchedPairs*100/(double)(matchedPairs + mismatchedPairs);
		return NumberUtils.formatDouble(percentage);
	}
}
