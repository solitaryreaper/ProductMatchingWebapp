package test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import java.util.Map;

import models.dao.ProductMatchTrialDAO;

import org.junit.Ignore;
import org.junit.Test;

import play.Logger;

import com.walmart.productgenome.pairComparison.model.rule.MatchAttribute;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntity;
import com.walmart.productgenome.pairComparison.model.rule.MatchEntityPair;


/**
 * Test cases for class see @{models.dao.ProductMatchTrialDao}
 * @author sprasa4
 *
 */
public class ProductMatchTrialDaoTest {
	
	private static int DUMMY_TRIAL_ID = 999;
	private static int TRIAL_ID = 369;
	private static String SOURCE_ITEM_ID = "89334848";
	private static String TARGET_ITEM_ID = "24360307";
	
	// Make sure that these trial ids are present in database
	/**
	 * This is not a full fledged test suite. This is just to test the functionality of API while
	 * developing them. For a fully functional integration test, we should ideally insert new trial ids
	 * in db and then try to fetch them.
	 */
	private static int DUMMY_TRIAL1_ID = 363;
	private static int DUMMY_TRIAL2_ID = 364;
	
	/**
	 * Tests if the attributes for an itempair get inserted properly
	 */
	@Ignore
	public void testInsertTrialItemPairAttributes() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				ProductMatchTrialDAO trialDao = new ProductMatchTrialDAO();
				MatchEntityPair dummyItemPair = getDummyItemPair();
								
				trialDao.insertTrialItemPairAttributes(DUMMY_TRIAL_ID, dummyItemPair);
			}
		});
	}
	
	/**
	 * Tests if the itempairs with different match status across trials can be found properly
	 */
	@Ignore
	public void testGetItemPairsWithDiffStatusAcrossTrials() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				ProductMatchTrialDAO trialDao = new ProductMatchTrialDAO();
				
				System.out.println("Finding itempairs with different match status across trials ..");
				Map<String, String> itempairsWithDiffMatchStatus = 
						trialDao.getItemPairsWithDiffStatusAcrossTrials(DUMMY_TRIAL1_ID, DUMMY_TRIAL2_ID);
				
				System.out.println("Found " + itempairsWithDiffMatchStatus.size() + " itempairs with different status");
				System.out.println(itempairsWithDiffMatchStatus.toString());
			}
		});
	}

	/**
	 * Tests if the itempair data is correctly fetched from database
	 */
	@Test
	public void testGetItemPairData() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				ProductMatchTrialDAO trialDao = new ProductMatchTrialDAO();
				
				System.out.println("Fetching itempair data from the database ..");
				MatchEntityPair itemPair = trialDao.getItemPairData(TRIAL_ID, SOURCE_ITEM_ID, TARGET_ITEM_ID);
				System.out.println(itemPair.toString());
				System.out.println(itemPair.getSourceItem().getAttributeNameValueSetMap().toString());
				System.out.println(itemPair.getTargetItem().getAttributeNameValueSetMap().toString());
			}
		});
	}

	private static MatchEntityPair getDummyItemPair()
	{
		MatchEntity sourceItem = new MatchEntity("519225", "WALMART_SEARCH_EXTRACT", "A History of Civilizations");
		sourceItem.addAttribute(new MatchAttribute("attrId", "req_description", "Paperback, Penguin Books, 1995, ISBN # 0140124896"));
		sourceItem.addAttribute(new MatchAttribute("attrId", "req_isbn_13", "9780140124897"));
		sourceItem.addAttribute(new MatchAttribute("attrId", "normalized_binding", "Paperback"));
		sourceItem.addAttribute(new MatchAttribute("attrId", "extracted_color", ""));
		
		MatchEntity targetItem = new MatchEntity("9780140124897", "BOWKER", "History of Civilizations");
		targetItem.addAttribute(new MatchAttribute("attrId", "req_isbn_13", "9780140124897"));
		targetItem.addAttribute(new MatchAttribute("attrId", "normalized_binding", "Paperback"));
		targetItem.addAttribute(new MatchAttribute("attrId", "extracted_publication_year", "1995"));
		targetItem.addAttribute(new MatchAttribute("attrId", "extracted_color", ""));
		
		MatchEntityPair itemPair = new MatchEntityPair(sourceItem, targetItem);
		return itemPair;
	}
}
