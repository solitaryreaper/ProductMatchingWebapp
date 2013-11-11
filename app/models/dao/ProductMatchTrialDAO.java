package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import models.db.ProductMatchTrial;
import models.db.ProductMatchTrialPairCompare;
import models.db.TrialItemPairMatchDetails;
import models.db.TrialItemPairRuleMatchDetails;
import models.db.TrialItemPairSubruleMatchDetails;
import models.db.TrialPairComparer;
import models.enums.ProductMatchTrialRunStatus;
import models.utils.TrialPairDiffUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.DB;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.walmart.productgenome.pairComparison.audit.ClauseAuditEntity;
import com.walmart.productgenome.pairComparison.audit.ItemPairAuditDataCollector;
import com.walmart.productgenome.pairComparison.audit.RuleAuditEntity;
import com.walmart.productgenome.pairComparison.model.MatchStatus;
import com.walmart.productgenome.pairComparison.model.rule.AttributeMatchClause;
import com.walmart.productgenome.pairComparison.model.rule.AttributeMatchClauseMeta;
import com.walmart.productgenome.pairComparison.model.rule.MatchAttribute;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntity;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntityPair;
import com.walmart.productgenome.pairComparison.utils.comparers.ComparersFactory;
import com.walmart.productgenome.pairComparison.utils.data.DataCleanupUtils;
import com.walmart.productgenome.pairComparison.utils.evaluators.ClauseEvaluatorFactory;
import com.walmart.productgenome.pairComparison.utils.tokenizers.TokenizerFactory;

/**
 * This class represents a single trial run for product matching.
 * 
 * @author sprasa4
 *
 */
public class ProductMatchTrialDAO{
	
	private static Connection dbConn = null;

	private static String TRIAL_INFO_TABLE = "product_match_db.trial_info";
	private static String TRIAL_STATS_TABLE = "product_match_db.trial_stats";
	private static String TRIAL_ITEMPAIRS_TABLE = "product_match_db.trial_itempairs";
	private static String TRIAL_ITEMPAIRS_MATCH_AUDIT_DETAIL = "product_match_db.trial_itempair_match_info";
	private static String TRIAL_ITEMPAIR_ATTRIBUTES_TABLE = "product_match_db.trial_itempair_attributes";
	
	private static String ATTRIBUTE_VALUE_NOT_AVAILABLE = "NA";
	
	private static int BATCH_SIZE = 100;
	
	// Data source of an item
	private static String SOURCE_NAME 	= "source_name";
	private static String ITEM_ID		= "item_id";
	
	public ProductMatchTrialDAO() 
	{
		DataSource ds = DB.getDataSource();
		try {
			dbConn = ds.getConnection();
		} catch (SQLException e) {
			Logger.error("Failed to get connection to database.");
			System.exit(1);
		}
	}
	
	// Inserts the initial trial entry at the start of job run in database
	public int insertTrialEntry(String trialName, String trialDesc, String runUser)
	{
		Integer newTrialId = null;
		
		ProductMatchTrialRunStatus status = ProductMatchTrialRunStatus.INITIALIZED;
	
		StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO ").append(TRIAL_INFO_TABLE);
		builder.append("(title, description, run_time, run_user, run_status)");
		builder.append("VALUES (?, ?, ?, ?, ? )");
		
		PreparedStatement preparedSQL;
		try {
			preparedSQL = dbConn.prepareStatement(builder.toString());
			
			preparedSQL.setString(1, trialName);
			preparedSQL.setString(2, trialDesc);
			preparedSQL.setString(3, getCurrentTime());
			preparedSQL.setString(4, runUser);
			preparedSQL.setString(5, status.toString());
			
			Logger.info("Prepared Statement : " + preparedSQL.toString());
			
			int rowsUpdated = preparedSQL.executeUpdate();
			if(rowsUpdated == 0) {
				Logger.error("Failed to insert the trial record in database !!");
			}

			newTrialId = getMaxTrialId();			
		} catch (SQLException e) {
			Logger.error("SQL query failed !! " + e.getStackTrace().toString());
			e.printStackTrace();
		}
		
		return newTrialId;
	}
	
	// Get current time in MySQL datetime format
	private static String getCurrentTime()
	{
		java.util.Date dt = new java.util.Date();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return sdf.format(dt);		
	}
	/**
	 * Returns the maximum trial id.
	 * 
	 * @return
	 */
	private static int getMaxTrialId()
 	{
		Integer maxTrialId = null;
		String maxIdSql = "SELECT MAX(id) AS max_id FROM " + TRIAL_INFO_TABLE;
		try {
			PreparedStatement sql = dbConn.prepareStatement(maxIdSql);
			ResultSet rs = sql.executeQuery();
			while(rs.next()) {
				maxTrialId = rs.getInt("max_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Logger.info("Found max trial id : " + maxTrialId);
		return maxTrialId;
	}
	
	// Updates the final status of the trial in database.
	public boolean updateTrialStatus(int trialId, ProductMatchTrialRunStatus status)
	{
		boolean isTrialStatusUpdateSuccess = true;
		
		String trialStatusUpdateSql = "UPDATE " + TRIAL_INFO_TABLE + " SET run_status = ? " +  " WHERE id = ?";
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(trialStatusUpdateSql);
			preparedSql.setString(1, status.toString());
			preparedSql.setInt(2, trialId);

			int rowsUpdated = preparedSql.executeUpdate();
			if(rowsUpdated == 0) {
				Logger.error("Failed to update any rows !!");
				isTrialStatusUpdateSuccess = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			isTrialStatusUpdateSuccess = false;
		}
		
		return isTrialStatusUpdateSuccess;
	}
	
	/**
	 * Inserts all the audit information related to the trial run in the database.
	 * Includes : 
	 * 
	 * 	1)	summary statistics of matched/mismatched itempairs.
	 *  2)  detailed audit statistics about which itempairs did match and which did not.
	 * 
	 * @return
	 */
	public boolean insertTrialItemPairAuditDetails(int trialId, boolean isMatched,
										   String sourceItemId, int sourceItemSourceId, 
										   String targetItemId, int targetItemSourceId,
										   String sourceItemName, String targetItemName)
	{
		boolean isTrialItemPairAuditDetailInserted = true;
		String auditSummaryInsertSql = 
			"INSERT INTO " + TRIAL_ITEMPAIRS_TABLE + " (id, source_item_id, source_item_data_source_id, target_item_id, target_item_data_source_id, is_match, source_item_name, target_item_name)" +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(auditSummaryInsertSql);
			preparedSql.setInt(1, trialId);
			preparedSql.setString(2, sourceItemId);
			preparedSql.setInt(3, sourceItemSourceId);
			preparedSql.setString(4, targetItemId);
			preparedSql.setInt(5, targetItemSourceId);
			preparedSql.setInt(6, (isMatched ? 1 : 0));
			preparedSql.setString(7, sourceItemName);
			preparedSql.setString(8, targetItemName);
			
			preparedSql.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			isTrialItemPairAuditDetailInserted = false;
		}

		return isTrialItemPairAuditDetailInserted;
	}

	// Inserts summary statistics for the current trial run
	public boolean insertTrialAuditSummary(int trialId, int numMatchedItemPairs, int numMismatchedItemPairs)
	{
		boolean isTrialAuditSummaryInserted = true;
		String auditSummaryInsertSql = 
			"INSERT INTO " + TRIAL_STATS_TABLE + " (id, num_matched_pairs, num_mismatched_pairs)" +
			"VALUES(?, ?, ?)";
		
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(auditSummaryInsertSql);
			preparedSql.setInt(1, trialId);
			preparedSql.setInt(2, numMatchedItemPairs);
			preparedSql.setInt(3, numMismatchedItemPairs);
			
			preparedSql.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			isTrialAuditSummaryInserted = false;
		}

		return isTrialAuditSummaryInserted;
	}

	// Insert all the match audit details of this itempair match for this trial run.
	public void insertTrialMatchAuditDetails(int trialId, String rulesedUsed, ItemPairAuditDataCollector auditData)
	{
		String auditDetailInsertSql = 
			"INSERT INTO " + TRIAL_ITEMPAIRS_MATCH_AUDIT_DETAIL + " (trial_id, source_item_id, target_item_id, source_item_ds_id, target_item_ds_id, ruleset_name, rule_name, subrule_name, is_match, is_rule_success, is_subrule_success, source_attributes, target_attributes, comparer, evaluator, source_tokenizer, target_tokenizer, score_threshold, is_missing_attribute_allowed)" +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		String sourceItemId = auditData.getItemPair().getSourceItem().getItemID();
		String targetItemId = auditData.getItemPair().getTargetItem().getItemID();
		
		String sourceItemDS = auditData.getItemPair().getSourceItem().getSource();
		String targetItemDS = auditData.getItemPair().getTargetItem().getSource();
		int sourceItemDSId = findDataSourceIdByName(sourceItemDS);
		int targetItemDSId = findDataSourceIdByName(targetItemDS);

		boolean isMatch = auditData.getStatus().equals(MatchStatus.SUCCESS);
		
		PreparedStatement preparedSql = null;
		try {
			preparedSql = dbConn.prepareStatement(auditDetailInsertSql);
			dbConn.setAutoCommit(false);
		}
		catch (SQLException e) {
			Logger.error("Failed to prepare SQL statement : " + auditDetailInsertSql);
			e.printStackTrace();
			return;
		}

		List<RuleAuditEntity> ruleAuditValues = auditData.getRuleAuditValues();
		for(RuleAuditEntity ruleAudit : ruleAuditValues) {
			String ruleName = ruleAudit.getRule().getRuleName();
			boolean isRuleSuccess = ruleAudit.getStatus().equals(MatchStatus.SUCCESS);
			for(ClauseAuditEntity clauseAudit : ruleAudit.getClauseAuditValues()) {
				String subruleName = "NA"; // TODO : How to get clause name ?
				boolean isSubruleSuccess = clauseAudit.getStatus().equals(MatchStatus.SUCCESS);
				
				AttributeMatchClause subrule = clauseAudit.getClause();
				AttributeMatchClauseMeta subruleMeta = subrule.getAttributeMatchClauseMeta();
				
				List<String> sourceAttrs = subrule.getSourceItemAttributes();
				List<String> targetAttrs = subrule.getTargetItemAttributes();
				String comparer = ComparersFactory.getComparersSubType(subruleMeta.getComparers());
				String evaluator = ClauseEvaluatorFactory.getEvaluatorType(subruleMeta.getEvaluator());
				String sourceTokenizer = TokenizerFactory.getTokenizerType(subruleMeta.getSourceTokenizer());
				String targetTokenizer = TokenizerFactory.getTokenizerType(subruleMeta.getTargetTokenizer());
				double scoreThreshold = subruleMeta.getScoreThreshold();
				boolean isMissingOkay = subruleMeta.isMissingOkay();
				
				try {
					preparedSql.setInt(1, trialId);
					preparedSql.setString(2, sourceItemId);
					preparedSql.setString(3, targetItemId);
					preparedSql.setInt(4, sourceItemDSId);
					preparedSql.setInt(5, targetItemDSId);
					preparedSql.setString(6, rulesedUsed);
					preparedSql.setString(7, ruleName);
					preparedSql.setString(8, subruleName);
					preparedSql.setInt(9, isMatch ? 1:0);
					preparedSql.setInt(10, isRuleSuccess ? 1:0);
					preparedSql.setInt(11, isSubruleSuccess ? 1:0);
					preparedSql.setString(12, sourceAttrs.toString());
					preparedSql.setString(13, targetAttrs.toString());
					preparedSql.setString(14, comparer);
					preparedSql.setString(15, evaluator);
					preparedSql.setString(16, sourceTokenizer);
					preparedSql.setString(17, targetTokenizer);
					preparedSql.setDouble(18, scoreThreshold);
					preparedSql.setInt(19, isMissingOkay ? 1:0);
					
					preparedSql.addBatch();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			} // End of subrule iteration
		} // End of rule iteration

		// Execute the statements in a batch now
		try {
			preparedSql.executeBatch();
			dbConn.commit();
			preparedSql.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				dbConn.setAutoCommit(true);
			} catch (SQLException e) {
				Logger.error("Failed to reset the connection parameters");
			}
		}

	}
	
	/**
	 * Returns the product match trials for trial identifiers. Returns all the trials if no filter
	 * is specified.
	 * 
	 * @return
	 */
	public List<ProductMatchTrial> getProductMatchTrials(List<Integer> trialIds)
	{
		List<ProductMatchTrial> productMatchTrials = Lists.newArrayList();
		
		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT ").append("\n");
		builder.append(" trial.id, trial.title, trial.description, trial.run_time, trial.run_user, ").append("\n");
		builder.append(" trial_stats.num_matched_pairs, trial_stats.num_mismatched_pairs ").append("\n");
		builder.append(" FROM product_match_db.trial_info as trial ").append("\n");
		builder.append(" JOIN product_match_db.trial_stats as trial_stats ").append("\n");
		builder.append(" ON (trial.id = trial_stats.id) ").append("\n");
		
		// Add filter for specific trial ids, if passed
		if(trialIds != null && !trialIds.isEmpty()) {
			String trialIdsCSV = trialIds.toString();
			trialIdsCSV = trialIdsCSV.replace("[", "");
			trialIdsCSV = trialIdsCSV.replace("]", "");
			builder.append("WHERE trial.id IN (").append(trialIdsCSV).append(")").append("\n");
		}
		
		String allTrialsSql = builder.toString();
		
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(allTrialsSql);
			
			ResultSet allProductMatchTrials = preparedSql.executeQuery();
			ProductMatchTrial trial = null;
			while(allProductMatchTrials.next()) {
				trial = new ProductMatchTrial();
				
				// set the trial metadata here
				trial.setTrialId(allProductMatchTrials.getInt("id"));
				trial.setTrialName(allProductMatchTrials.getString("title"));
				trial.setTrialDescription(allProductMatchTrials.getString("description"));
				trial.setRunTime(allProductMatchTrials.getDate("run_time"));
				trial.setRunUser(allProductMatchTrials.getString("run_user"));
				
				// set the trial summary stats here
				trial.setMatchedPairs(allProductMatchTrials.getInt("num_matched_pairs"));
				trial.setMismatchedPairs(allProductMatchTrials.getInt("num_mismatched_pairs"));
				
				productMatchTrials.add(trial);
			}
			
		} catch (SQLException e) {
			Logger.error("Failed to fetch all product match trials.");
			e.printStackTrace();
		}
		
		return productMatchTrials;
	}
	
	private ProductMatchTrial getProductMatchTrial(int trialId)
	{
		List<ProductMatchTrial> productMatchTrials = getProductMatchTrials(Lists.newArrayList(trialId));
		return productMatchTrials.get(0);
	}
	
	public static int findTrialIdByNameAndUser(String trialName, String trialUser)
	{
		int trialId = -1;
		
		String trialIdSearchSql = "SELECT DISTINCT id FROM " + TRIAL_INFO_TABLE + " WHERE title = ?";
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(trialIdSearchSql);
			preparedSql.setString(1, trialName);
			//preparedSql.setString(2, trialUser);
			
			ResultSet rs = preparedSql.executeQuery();
			while(rs.next()) {
				trialId = rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return trialId;
	}
		
	/**
	 * Gets the diff between two product trial runs
	 * @param trial1Name	Name of first trial run.
	 * @param trial2Name	Name of second trial run.
	 * @param trialUser		Name of user whose trials have to be searched.
	 */
	public ProductMatchTrialPairCompare getDiffBwProductMatchTrials(String trial1Name, String trial2Name, String trialUser)
	{
		int trial1Id = findTrialIdByNameAndUser(trial1Name, trialUser);
		int trial2Id = findTrialIdByNameAndUser(trial2Name, trialUser);

		ProductMatchTrial trial1Meta = getProductMatchTrial(trial1Id);
		ProductMatchTrial trial2Meta = getProductMatchTrial(trial2Id);

		/**
		 * Ideally, we should query the trial audit details for all the itempairs in batch instead
		 * of making a db query for each itempair. But modelling the flat db audit table in code
		 * was getting tricky when fetching multiple itempairs.
		 * 
		 * TODO : Use Ibatis or some other ORM  to simplify the db layer
		 */
		List<TrialPairComparer> trialAllMismatchItemPairsDiff = Lists.newArrayList();
		Map<String, String> itempairsWithDiffStatus = getItemPairsWithDiffStatusAcrossTrials(trial1Id, trial2Id);
		for(Map.Entry<String, String> entry : itempairsWithDiffStatus.entrySet()) {
			String sourceItemId = entry.getKey();
			String targetItemId = entry.getValue();
			
			TrialItemPairMatchDetails trial1ItemPairDetail = getItemPairMatchDetails(trial1Id, sourceItemId, targetItemId);
			TrialItemPairMatchDetails trial2ItemPairDetail = getItemPairMatchDetails(trial2Id, sourceItemId, targetItemId);
			if(trial1ItemPairDetail == null || trial2ItemPairDetail == null ||
			   CollectionUtils.isEmpty(trial1ItemPairDetail.getRuleMatchDetails()) || CollectionUtils.isEmpty(trial2ItemPairDetail.getRuleMatchDetails())) 
			{
				continue;
			}
			
			TrialPairComparer trialMismatchItemPairDiff = TrialPairDiffUtils.computeTrialPairDiff(trial1ItemPairDetail, trial2ItemPairDetail);
			trialAllMismatchItemPairsDiff.add(trialMismatchItemPairDiff);			
		}

		return new ProductMatchTrialPairCompare(trial1Meta, trial2Meta, trialAllMismatchItemPairsDiff);
	}

	// Returns the list of itempair ids that have a different match status across two trials
	public Map<String, String> getItemPairsWithDiffStatusAcrossTrials(int trial1Id, int trial2Id)
	{
		String sql = " SELECT" +
					 " DISTINCT trial1.source_item_id, trial1.target_item_id " +
					 " FROM (SELECT * FROM trial_itempair_match_info WHERE trial_id = ?) trial1 " +
					 " JOIN (SELECT * FROM trial_itempair_match_info WHERE trial_id = ?) trial2 " +
					 " ON ((trial1.source_item_id = trial2.source_item_id AND trial1.target_item_id = trial2.target_item_id) OR " + 
					 " (trial1.source_item_id = trial2.target_item_id AND trial1.target_item_id = trial2.source_item_id)) " +
					 " WHERE trial1.is_match <> trial2.is_match";
		
		Map<String, String> diffItemPairMap = Maps.newHashMap();
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(sql);
			preparedSql.setInt(1, trial1Id);
			preparedSql.setInt(2, trial2Id);
			ResultSet rs = preparedSql.executeQuery();			
			while(rs.next()) {
				diffItemPairMap.put(rs.getString("source_item_id"), rs.getString("target_item_id"));
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}

		Logger.info("Found " + diffItemPairMap.size() + 
				" itempairs with different match status between trial1 " + trial1Id + " and trial2 " + trial2Id);
		return diffItemPairMap;
	}

	/**
	 * Gets the audit details for an itempair for a specific trial.
	 * 
	 * @param trialId
	 * @param sourceItemId
	 * @param targetItemId
	 * @return
	 */
	private TrialItemPairMatchDetails getItemPairMatchDetails(int trialId, String sourceItemId, String targetItemId)
	{
		String sql = 
				" SELECT DISTINCT trial_summary.title AS trial_name, itempair_name.source_item_name, trial_details.ruleset_name, itempair_name.target_item_name, ds_source.title AS source_ds, ds_target.title as target_ds, rule_name, subrule_name, trial_details.is_match, is_rule_success, is_subrule_success, source_attributes, target_attributes, comparer, evaluator, source_tokenizer, target_tokenizer, score_threshold, is_missing_attribute_allowed " + 
				" FROM trial_itempair_match_info trial_details " +
				" JOIN trial_info trial_summary " +
					" ON (trial_summary.id = trial_details.trial_id) " +
				" JOIN trial_itempairs itempair_name " +
					" ON(itempair_name.source_item_id = trial_details.source_item_id AND itempair_name.target_item_id = trial_details.target_item_id) " +
				" JOIN data_sources ds_source " +
					" ON (ds_source.id = trial_details.source_item_ds_id) " +
				" JOIN data_sources ds_target " +
				"	ON (ds_target.id = trial_details.target_item_ds_id) " +
				" WHERE trial_details.trial_id = ? AND itempair_name.source_item_id = ? AND itempair_name.target_item_id = ? " + 
				" ORDER BY rule_name, source_attributes, target_attributes ";
		//System.out.println(sql);
		
		TrialItemPairMatchDetails itemPairMatchDetails = new TrialItemPairMatchDetails();
		List<TrialItemPairRuleMatchDetails> itemPairAllRuleMatchDetails = Lists.newArrayList();
		TrialItemPairRuleMatchDetails itempairRuleAuditDetails = null;
		List<TrialItemPairSubruleMatchDetails> itempairRuleAllSubruleDetails = null;
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(sql);
			preparedSql.setInt(1, trialId);
			preparedSql.setString(2, sourceItemId);
			preparedSql.setString(3, targetItemId);
			
			ResultSet rs = preparedSql.executeQuery();
			Set<String> visitedRules = Sets.newHashSet();
			int numDBRows = 0;
			while(rs.next()) {
				++numDBRows;
				// Set the basic audit information about this itempair
				itemPairMatchDetails.setRulesetName(rs.getString("ruleset_name"));
				itemPairMatchDetails.setTrialName(rs.getString("trial_name"));
				itemPairMatchDetails.setSourceItemName(rs.getString("source_item_name"));
				itemPairMatchDetails.setTargetItemName(rs.getString("target_item_name"));
				itemPairMatchDetails.setSourceDataSource(models.enums.DataSource.getDataSourceEnum(rs.getString("source_ds")));
				itemPairMatchDetails.setTargetDataSource(models.enums.DataSource.getDataSourceEnum(rs.getString("target_ds")));
				itemPairMatchDetails.setItemPairMatch(rs.getBoolean("is_match"));
				
				String currRule = rs.getString("rule_name");
				// If a new rule audit db row is found, reset some of the state variables
				if(!visitedRules.contains(currRule)) {
					if(itempairRuleAuditDetails != null) {
						itempairRuleAuditDetails.setSubruleMatchDetails(itempairRuleAllSubruleDetails);
						itemPairAllRuleMatchDetails.add(itempairRuleAuditDetails);
					}
					
					// Reset it to contain audit info for next rule matching
					visitedRules.add(currRule);
					itempairRuleAuditDetails = new TrialItemPairRuleMatchDetails();
					itempairRuleAllSubruleDetails = Lists.newArrayList();
				}
				
				// Set the basic rule information for this itempair
				itempairRuleAuditDetails.setRuleName(rs.getString("rule_name"));
				itempairRuleAuditDetails.setRuleSuccess(rs.getBoolean("is_rule_success"));

				// Collect the detail for a new subrule audit for the above rule and itempair
				TrialItemPairSubruleMatchDetails subruleAuditDetails = new TrialItemPairSubruleMatchDetails();
				subruleAuditDetails.setSubruleName(rs.getString("subrule_name"));
				subruleAuditDetails.setSourceAttributes(parseStringToList(rs.getString("source_attributes")));
				subruleAuditDetails.setTargetAttributes(parseStringToList(rs.getString("target_attributes")));
				subruleAuditDetails.setComparer(rs.getString("comparer"));
				subruleAuditDetails.setEvaluator(rs.getString("evaluator"));
				subruleAuditDetails.setScoreThreshold(rs.getDouble("score_threshold"));
				subruleAuditDetails.setSourceTokenizer(rs.getString("source_tokenizer"));
				subruleAuditDetails.setTargetTokenizer(rs.getString("target_tokenizer"));
				subruleAuditDetails.setMissingAttributeAllowed(rs.getBoolean("is_missing_attribute_allowed"));
				subruleAuditDetails.setSubruleSuccess(rs.getBoolean("is_subrule_success"));
				
				itempairRuleAllSubruleDetails.add(subruleAuditDetails);
			}
			
			if(numDBRows > 0) {
				// Add the audit details of last rule matching here
				itempairRuleAuditDetails.setSubruleMatchDetails(itempairRuleAllSubruleDetails);
				itemPairAllRuleMatchDetails.add(itempairRuleAuditDetails);

				itemPairMatchDetails.setRuleMatchDetails(itemPairAllRuleMatchDetails);								
			} 
		}
		catch(SQLException e) {
			e.printStackTrace();			
		}
		
		return itemPairMatchDetails;
	}
	
	private static List<String> parseStringToList(String valueToParse)
	{
		String[] attributes = valueToParse.split(",");
		List<String> attributeValues = Lists.newArrayList();
		for(String attrValue : attributes) {
			attributeValues.add(attrValue.trim());
		}
		
		return attributeValues;
	}
	
	public int findDataSourceIdByName(String dataSourceName)
	{
		int dataSourceId = 999;
		String sql = "SELECT id FROM product_match_db.data_sources WHERE title = ?";
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(sql);
			preparedSql.setString(1, dataSourceName);
			
			ResultSet rs = preparedSql.executeQuery();
			while(rs.next()) {
				dataSourceId = rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return dataSourceId;
	}
		
	/**
	 * Returns all the trials name for the user. If no user is specified, it returns all the distinct
	 * trial names.
	 * @param trialUser
	 * @return
	 */
	public List<String> getTrialNames(String trialUser)
	{
		List<String> trialNames = Lists.newArrayList();
		String trialNamesSql = "SELECT DISTINCT title from " + TRIAL_INFO_TABLE;
		if(trialUser != null) {
			trialNamesSql += " WHERE run_user = ?";
		}
		
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(trialNamesSql);
			if(trialUser != null) {
				preparedSql.setString(1, trialUser);
			}
				
				ResultSet rs = preparedSql.executeQuery();
				while(rs.next()) {
					trialNames.add(rs.getString("title"));
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return trialNames;
	}
	
	// Inserts all the attributes for the current itempair
	public void insertTrialItemPairAttributes(int trialId, MatchEntityPair itemPair)
	{
		MatchEntity sourceItem = itemPair.getSourceItem();
		MatchEntity targetItem = itemPair.getTargetItem();
		
		Map<String, Set<String>> sourceItemAttrValuesMap = sourceItem.getAttributeNameValueSetMap();
		Map<String, Set<String>> targetItemAttrValuesMap = targetItem.getAttributeNameValueSetMap();
		
		
		String sourceItemId = sourceItem.getItemID();
		String targetItemId = targetItem.getItemID();

		// Add some extra attributes in the db
		sourceItemAttrValuesMap.put(SOURCE_NAME, Sets.newHashSet(sourceItem.getSource()));
		sourceItemAttrValuesMap.put(ITEM_ID, Sets.newHashSet(sourceItemId));
		targetItemAttrValuesMap.put(SOURCE_NAME, Sets.newHashSet(targetItem.getSource()));
		targetItemAttrValuesMap.put(ITEM_ID, Sets.newHashSet(targetItemId));

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(" INSERT INTO ").append(TRIAL_ITEMPAIR_ATTRIBUTES_TABLE).append(" ");
		sqlBuilder.append(" (trial_id, item_id, attr_name, attr_values)").append(" ");
		sqlBuilder.append(" VALUES(?, ?, ?, ?)");
		
		PreparedStatement preparedSqlSourceItemAttrs = null;
		PreparedStatement preparedSqlTargetItemAttrs = null;
		
		try {
			dbConn.setAutoCommit(false);
			
			preparedSqlSourceItemAttrs = dbConn.prepareStatement(sqlBuilder.toString());
			preparedSqlTargetItemAttrs = dbConn.prepareStatement(sqlBuilder.toString());

			// insert all the source item attribute values
			for(Map.Entry<String, Set<String>> entry : sourceItemAttrValuesMap.entrySet()) {
				String attributeName = entry.getKey();
				if(StringUtils.isBlank(attributeName)) {
					continue;
				}
				String attributeValues = ATTRIBUTE_VALUE_NOT_AVAILABLE;
				Set<String> values = entry.getValue();
				values = DataCleanupUtils.removeNullOrEmptyStrings(values);
				if(CollectionUtils.isNotEmpty(values)) {
					attributeValues = values.toString();
				}
				
				preparedSqlSourceItemAttrs.setInt(1, trialId);
				preparedSqlSourceItemAttrs.setString(2, sourceItemId);
				preparedSqlSourceItemAttrs.setString(3, attributeName);
				preparedSqlSourceItemAttrs.setString(4, attributeValues);
				
				preparedSqlSourceItemAttrs.addBatch();
			}
			
			// insert all the target item attribute values
			for(Map.Entry<String, Set<String>> entry : targetItemAttrValuesMap.entrySet()) {
				String attributeName = entry.getKey();
				if(StringUtils.isBlank(attributeName)) {
					continue;
				}				
				String attributeValues = ATTRIBUTE_VALUE_NOT_AVAILABLE;
				Set<String> values = entry.getValue();
				values = DataCleanupUtils.removeNullOrEmptyStrings(values);
				if(CollectionUtils.isNotEmpty(values)) {
					attributeValues = values.toString();
				}
	
				preparedSqlTargetItemAttrs.setInt(1, trialId);
				preparedSqlTargetItemAttrs.setString(2, targetItemId);
				preparedSqlTargetItemAttrs.setString(3, attributeName);
				preparedSqlTargetItemAttrs.setString(4, attributeValues);
				
				preparedSqlTargetItemAttrs.addBatch();
			}
			
			// Now execute the statements in batch mode
			preparedSqlSourceItemAttrs.executeBatch();
			preparedSqlSourceItemAttrs.close();
			
			preparedSqlTargetItemAttrs.executeBatch();
			preparedSqlTargetItemAttrs.close();
			
			// Commit all the changes explicitly here
			dbConn.commit();
			
			// Make sure that the auto-commit is reset to its default state at the end of the batch
			dbConn.setAutoCommit(true);
		} catch (SQLException e) {
			Logger.error("Failed to insert attributes for  itempair : " + sourceItemId + ", " + targetItemId);
			e.printStackTrace();
		}
		finally {
			try {
				dbConn.setAutoCommit(true);
			} catch (SQLException e) {
				Logger.error("Failed to reset connection parameters");
			}
		}
	}
	
	/**
	 * Batch insert all the summary match information about each itempair for the specified trial.
	 */
	public void insertAllTrialItemPairSummaryAuditDetails(int trialId, List<ItemPairAuditDataCollector> allItemPairsMatchAuditData)
	{
		long startTime = System.currentTimeMillis();
		String auditSummaryInsertSql = 
				"INSERT INTO " + TRIAL_ITEMPAIRS_TABLE + " (id, source_item_id, source_item_data_source_id, target_item_id, target_item_data_source_id, is_match, source_item_name, target_item_name)" +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(auditSummaryInsertSql);
			dbConn.setAutoCommit(false);
			
			int currBatchCount = 0;
			for(ItemPairAuditDataCollector itemPairAuditCollector : allItemPairsMatchAuditData) {
				boolean isMatched = (itemPairAuditCollector.getStatus() == MatchStatus.SUCCESS) ? true : false;			
				MatchEntityPair itemPair = itemPairAuditCollector.getItemPair();
				
				String sourceItemId = itemPair.getSourceItem().getItemID();
				String targetItemId = itemPair.getTargetItem().getItemID();
				
				String sourceItemDataSource = itemPairAuditCollector.getItemPair().getSourceItem().getSource();
				String targetItemDataSource = itemPairAuditCollector.getItemPair().getTargetItem().getSource();
				
				Integer sourceItemSourceId = findDataSourceIdByName(sourceItemDataSource);
				Integer targetItemSourceId = findDataSourceIdByName(targetItemDataSource);
				
				String sourceItemName = itemPair.getSourceItem().getName();
				String targetItemName = itemPair.getTargetItem().getName();
				
				preparedSql.setInt(1, trialId);
				preparedSql.setString(2, sourceItemId);
				preparedSql.setInt(3, sourceItemSourceId);
				preparedSql.setString(4, targetItemId);
				preparedSql.setInt(5, targetItemSourceId);
				preparedSql.setInt(6, (isMatched ? 1 : 0));
				preparedSql.setString(7, sourceItemName);
				preparedSql.setString(8, targetItemName);

				preparedSql.addBatch();
				if(++currBatchCount == BATCH_SIZE) {
					preparedSql.executeBatch(); // Persist the current batch in database
					preparedSql.clearBatch(); // Reset the batch to start a fresh execution cycle
					dbConn.commit(); // Explicitly commit this batch
					
					currBatchCount = 0; // Reset the batch counter
				}
			}
			
			// This is for the last batch
			preparedSql.executeBatch();
			preparedSql.clearBatch();
			dbConn.commit();
			
			dbConn.setAutoCommit(true);
		}
		catch(SQLException e) {
			Logger.error("Failed to insert audit summary information for itempairs for trial " + trialId);
			e.printStackTrace();
		}
		finally {
			try {
				dbConn.setAutoCommit(true);
			} catch (SQLException e) {
				Logger.error("Failed to reset the connection parameters");
			}
		}

		long endTime = System.currentTimeMillis();
		double timeTaken = (double)(endTime - startTime)/1000.0;
		Logger.info("Inserted audit summary details for " + allItemPairsMatchAuditData.size() + 
				" itempairs in " + timeTaken + " seconds");
	}
	
	/**
	 * Batch insert the match details of all itempairs match audit information for the specified trial.
	 */
	public void insertAllTrialMatchAuditDetails(int trialId, String ruleset, List<ItemPairAuditDataCollector> allItemPairsMatchAuditData)
	{
		long startTime = System.currentTimeMillis();
		for(ItemPairAuditDataCollector itemPairAuditCollector : allItemPairsMatchAuditData) {
			insertTrialMatchAuditDetails(trialId, ruleset, itemPairAuditCollector);
		}
		long endTime = System.currentTimeMillis();
		double timeTaken = (double)(endTime - startTime)/1000.0;
		Logger.info("Inserted itempair audit details for " + allItemPairsMatchAuditData.size() + 
				" itempairs in " + timeTaken + " seconds");
	}
	
	/**
	 * Batch insert the attribute data of all the itempairs for the specified trial.
	 */
	public void insertAllTrialItemPairAttributes(int trialId, List<ItemPairAuditDataCollector> allItemPairsMatchAuditData)
	{
		long startTime = System.currentTimeMillis();
		for(ItemPairAuditDataCollector itemPairAuditCollector : allItemPairsMatchAuditData) {
			insertTrialItemPairAttributes(trialId, itemPairAuditCollector.getItemPair());
		}
		long endTime = System.currentTimeMillis();
		double timeTaken = (double)(endTime - startTime)/1000.0;
		Logger.info("Inserted itempair attributes for " + allItemPairsMatchAuditData.size() + 
					" itempairs in " + timeTaken + " seconds ");
	}
	
	/**
	 * Fetches all the attributes for the itempair for a specific trial.
	 * @param trialId
	 * @param sourceItemId
	 * @param targetItemId
	 */
	public MatchEntityPair getItemPairData(int trialId, String sourceItemId, String targetItemId)
	{ 
		MatchEntity sourceItem = getItemData(trialId, sourceItemId);
		MatchEntity targetItem = getItemData(trialId, targetItemId);
		return new MatchEntityPair(sourceItem, targetItem);
	}
	
	/**
	 * Gets the detailed item data along with all its attributes.
	 * 
	 * @param trialId
	 * @param itemId
	 * @return
	 */
	private MatchEntity getItemData(int trialId, String itemId)
	{
		MatchEntity item = null;
		String itemAttrsFetchSql = 
				"SELECT attr_name, attr_values FROM " + TRIAL_ITEMPAIR_ATTRIBUTES_TABLE + 
				" WHERE trial_id = ? AND item_id = ?";
		
		PreparedStatement preparedSql = null;
		try {
			preparedSql = dbConn.prepareStatement(itemAttrsFetchSql);
			preparedSql.setInt(1, trialId);
			preparedSql.setString(2, itemId);
			
			Set<MatchAttribute> attributeSet = Sets.newHashSet();
			String source = null;
			String name = null;
			ResultSet rs = preparedSql.executeQuery();
			while(rs.next()) {
				String attributeName = rs.getString("attr_name");
				String attributeValues = getCleanedupAttributeValues(rs.getString("attr_values"));
				attributeSet.add(new MatchAttribute(attributeName, attributeName, attributeValues));
				
				if(attributeName.equals(SOURCE_NAME)) {
					source = getCleanedupAttributeValues(attributeValues);
				}
				if(attributeName.equals("pd_title")) {
					name = getCleanedupAttributeValues(attributeValues);
				}
			}
			
			item = new MatchEntity(itemId, source, name, attributeSet);
		} catch (SQLException e) {
			Logger.error("Failed to fetch item attributes for item " + itemId + " for trial " + trialId);
			e.printStackTrace();
		}
		
		return item;
	}
	
	/**
	 * Remove square brackets from the string
	 * @param attributeValues
	 */
	private static String getCleanedupAttributeValues(String attributeValues)
	{
		try {
			if(attributeValues != null) {
				attributeValues = attributeValues.replaceAll("\\[", "");
				attributeValues = attributeValues.replace("]", "");
				attributeValues = attributeValues.trim();
			}			
		}
		catch(Exception e) {
			System.out.println("Failed to cleanup for value " + attributeValues);
			Logger.error("Failed to cleanup for value " + attributeValues);
		}
		
		return attributeValues;
	}
}
