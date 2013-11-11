package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.dao.ProductMatchTrialDAO;
import models.enums.ProductMatchTrialRunStatus;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import views.html.matcher.dataform;
import views.html.matcher.fileform;
import views.html.matcher.results;
import views.html.matcher.itempair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmart.productgenome.pairComparison.audit.AuditDataCollector;
import com.walmart.productgenome.pairComparison.audit.ItemPairAuditDataCollector;
import com.walmart.productgenome.pairComparison.model.Constants;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntity;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntityPair;
import com.walmart.productgenome.pairComparison.service.ItemPairDataMatchService;

/**
 * Controller that co-ordinates the product matching trial run.
 * 
 * @author sprasa4
 * 
 */
public class Matcher extends Controller {
	
	// Renders the file based data matching form
	public static Result index() {
		if(session("user") == null) {
			return unauthorized("Oops, you are not connected");
		}
		
		return ok(fileform.render());
	}

	// Renders the ad hoc data matching form
	public static Result renderDataForm() {
		if(session("user") == null) {
			return unauthorized("Oops, you are not connected");
		}
		
		return ok(dataform.render());
	}
	
	/**
	 * Handles the submission of product matching job and renders the matching
	 * results. Also, inserts the trial related information and the information
	 * about itempairs in the database.
	 * 
	 * This method is used for matching larger datasets uploaded as a file in UI>
	 */
	public static Result invokeFileBasedDataMatch() {

		// Get form parameters
		DynamicForm dynamicForm = form().bindFromRequest();
		String trialName = dynamicForm.get("trial_name");
		String trialDesc = dynamicForm.get("trial_desc");
		Logger.info("Trial Id : " + trialName + ", Trial Desc " + trialDesc);

		// Get uploaded file parameters
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart ruleFilePath = body.getFile("rule_file_path");
		FilePart dataFilePath = body.getFile("data_file_path");
		File ruleFile = ruleFilePath.getFile();
		File dataFile = dataFilePath.getFile();

		return runProductDataMatch(ruleFile, dataFile, trialName, trialDesc);
	}

	/**
	 * Invokes product matching on small data set. This data is typically entered by pasting a
	 * couple of itempairs in UI textboxes.
	 * @return
	 */
	public static Result invokeAdHocDataMatch()
	{
		// Get form parameters
		DynamicForm dynamicForm = form().bindFromRequest();
		String ruleContents = dynamicForm.get("rule_data");
		String itemContents = dynamicForm.get("item_data");
		String trialName = dynamicForm.get("trial_name");
		String trialDesc = dynamicForm.get("trial_desc");
		
		File ruleFile = stageTextDataIntoFile(ruleContents, "rule_");
		File dataFile = stageTextDataIntoFile(itemContents, "data_");
		
		return runProductDataMatch(ruleFile, dataFile, trialName, trialDesc);
	}
	
	/**
	 * Displays the item pair information.
	 */
	public static Result showItemPairData()
	{
		DynamicForm dynamicForm = form().bindFromRequest();
		int trialId = Integer.parseInt(dynamicForm.get("trial_id"));
		String sourceItemId = dynamicForm.get("source_item_id");
		String targetItemId = dynamicForm.get("target_item_id");
		System.out.println("Inside show itempair data with trialid : " + trialId + ", sourceItemId " + sourceItemId + ", targetItemId " + targetItemId);
		
		ProductMatchTrialDAO trialDao = new ProductMatchTrialDAO();
		MatchEntityPair itemPair = trialDao.getItemPairData(trialId, sourceItemId, targetItemId);
		
		// Common attrs for the itempair which need to be shown first
		List<String> commonItemPairAttrs = 
			Lists.newArrayList("source_name", "pd_title", "req_brand_name", "req_description", "req_category", "req_upc_10", 
					"req_upc_11", "req_upc_12", "req_upc_13", "req_upc_14", "req_part_number", 
					"req_raw_part_number", "pd_title_number_units", "pd_title_variation_phrases",
					"req_color", "extracted_color");
		
		MatchEntity sourceItem = itemPair.getSourceItem();
		MatchEntity targetItem = itemPair.getTargetItem();
		
		Map<String, Set<String>> sourceItemAttrsMap = sourceItem.getAttributeNameValueSetMap();
		Map<String, Set<String>> targetItemAttrsMap = targetItem.getAttributeNameValueSetMap();
		
		// Populate the common attributes first
		Map<String, String> sourceItemCommonAttrsMap = Maps.newLinkedHashMap();
		Map<String, String> targetItemCommonAttrsMap = Maps.newLinkedHashMap();
		for(String commonAttr : commonItemPairAttrs) {
			String sourceItemCommonAttrValue = "NA";
			String targetItemCommonAttrValue = "NA";
			if(sourceItemAttrsMap.containsKey(commonAttr)) {
				sourceItemCommonAttrValue = sourceItem.getValuesForAttributeName(commonAttr).toString();
			}
			if(targetItemAttrsMap.containsKey(commonAttr)) {
				targetItemCommonAttrValue = targetItem.getValuesForAttributeName(commonAttr).toString();
			}
			sourceItemCommonAttrsMap.put(commonAttr, sourceItemCommonAttrValue);
			targetItemCommonAttrsMap.put(commonAttr, targetItemCommonAttrValue);
		}
		
		Map<String, String> sourceItemSpecificAttrsMap = Maps.newLinkedHashMap();
		Map<String, String> targetItemSpecificAttrsMap = Maps.newLinkedHashMap();
		
		// Now populate source specific attributes
		for(Map.Entry<String, Set<String>> entry1 : sourceItemAttrsMap.entrySet()) {
			String attrName = entry1.getKey();
			String attrValue = entry1.getValue().toString();
			if(!commonItemPairAttrs.contains(attrName)) {
				sourceItemSpecificAttrsMap.put(attrName, attrValue);
			}
		}
		for(Map.Entry<String, Set<String>> entry2 : targetItemAttrsMap.entrySet()) {
			String attrName = entry2.getKey();
			String attrValue = entry2.getValue().toString();
			if(!commonItemPairAttrs.contains(attrName)) {
				targetItemSpecificAttrsMap.put(attrName, attrValue);
			}			
		}
		
		String sourceDataSource = sourceItemAttrsMap.get("source_name").toString();
		String targetDataSource = targetItemAttrsMap.get("source_name").toString();
		return ok(itempair.render(sourceDataSource, targetDataSource, sourceItemCommonAttrsMap, targetItemCommonAttrsMap, sourceItemSpecificAttrsMap, targetItemSpecificAttrsMap));
	}
	
	private static Result runProductDataMatch(File ruleFile, File dataFile, String trialName, String trialDesc)
	{
		// Get audit information about the current match run
		AuditDataCollector collector = null;
		try {
			collector = ItemPairDataMatchService.runProductMatchJob(ruleFile, dataFile);			
		}
		catch(Exception e) {
			String errorOrigin = "New product match trial form encountered some errors !! ";
			String errorReason = e.getMessage(); 
			return controllers.ErrorHandler.showError(errorOrigin, errorReason, e);
		}

		// Product matching summary statistics
		Map<String, Integer> summaryMap = collector.getMatchSummaryStatistics();
		Integer matchItemPairs = summaryMap.get(Constants.MATCHED_ITEMPAIRS);
		Integer mismatchItemPairs = summaryMap.get(Constants.MISMATCHED_ITEMPAIRS);

		Map<String, String> passRulesStats = collector.getRuleSummaryStatistics();
		Map<String, String> passClausesStats = collector.getClauseSummaryStatistics();
		String rulesetUsed = collector.getRulesetUsed();

		List<ItemPairAuditDataCollector> allItemPairsMatchAuditData = collector.getItemPairAuditExtract();
		
		ProductMatchTrialDAO trialDao = new ProductMatchTrialDAO();
		int trialId = trialDao.insertTrialEntry(trialName, trialDesc, session("user"));
		
		Status formSubmitStatus = 
			ok(results.render(trialId, matchItemPairs, mismatchItemPairs, 
			      			   passRulesStats, passClausesStats, allItemPairsMatchAuditData)); 
				
		// Batch insert all the summary match audit information for all the itempairs
		trialDao.insertAllTrialItemPairSummaryAuditDetails(trialId, allItemPairsMatchAuditData);
		
		// Batch insert all the finer details for this itempair's match audit.
		trialDao.insertAllTrialMatchAuditDetails(trialId, rulesetUsed, allItemPairsMatchAuditData);
		
		// Batch insert all the attribute details for this itempair.
		trialDao.insertAllTrialItemPairAttributes(trialId, allItemPairsMatchAuditData);
		
		// Update the product match trial run status
		trialDao.insertTrialAuditSummary(trialId, matchItemPairs, mismatchItemPairs);
		trialDao.updateTrialStatus(trialId, ProductMatchTrialRunStatus.SUCCESS);
		
		return formSubmitStatus;		
	}
	
	// Stage the plain text data temporarily into a file
	private static File stageTextDataIntoFile(String textToStage, String fileNamePrefix)
	{
		String tempFileName = fileNamePrefix + "Temp" + ".txt";
		File tempFile = new File("/tmp/" + tempFileName);
		if(tempFile.exists()) {
			tempFile.delete();
			tempFile = new File("/tmp/" + tempFileName);
		}
		
		try {
			FileWriter fw = new FileWriter(tempFile);
			BufferedWriter bw = new BufferedWriter(fw);			
			bw.write(textToStage);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to stage the plaintext into temp file " + tempFile.getAbsolutePath());
		}
		
		Logger.info("File location " + tempFile.getAbsolutePath());
		return tempFile;
	}
}
