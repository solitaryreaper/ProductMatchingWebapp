package models.db;

import java.util.List;

import models.enums.DataSource;

import org.apache.commons.collections.CollectionUtils;

/**
 * Model object to represent a diff of two trials.
 * 
 * @author sprasa4
 *
 */
public class ProductMatchTrialPairCompare {	
	
	// Metadata + summary information for the trials
	private ProductMatchTrial trial1Meta;
	private ProductMatchTrial trial2Meta;
	
	// Itempairs which differ between these two trials
	private List<TrialPairComparer> trialDiffItemPairs;
	
	public ProductMatchTrialPairCompare(ProductMatchTrial trial1Meta, ProductMatchTrial trial2Meta, 
										List<TrialPairComparer> trialDiffItemPairs)
	{
		this.trial1Meta = trial1Meta;
		this.trial2Meta = trial2Meta;
		this.trialDiffItemPairs = trialDiffItemPairs;
	}
	
	public ProductMatchTrial getTrial1Meta() {
		return trial1Meta;
	}
	public void setTrial1Meta(ProductMatchTrial trial1Meta) {
		this.trial1Meta = trial1Meta;
	}
	public ProductMatchTrial getTrial2Meta() {
		return trial2Meta;
	}
	public void setTrial2Meta(ProductMatchTrial trial2Meta) {
		this.trial2Meta = trial2Meta;
	}

	public List<TrialPairComparer> getTrialDiffItemPairs() {
		return trialDiffItemPairs;
	}

	public void setTrialDiffItemPairs(List<TrialPairComparer> trialDiffItemPairs) {
		this.trialDiffItemPairs = trialDiffItemPairs;
	}
	
	public String getSourceItemDataSource()
	{
		if(CollectionUtils.isEmpty(getTrialDiffItemPairs())) {
			return DataSource.UNKNOWN.toString();
		}
		
		return getTrialDiffItemPairs().get(0).getSourceItemDataSource().toString();
	}
	
	public String getTargetItemDataSource()
	{
		if(CollectionUtils.isEmpty(getTrialDiffItemPairs())) {
			return DataSource.UNKNOWN.toString();
		}
		
		return getTrialDiffItemPairs().get(0).getTargetItemDataSource().toString();
	}
}
