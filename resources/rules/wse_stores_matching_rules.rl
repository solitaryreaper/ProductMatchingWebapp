//------------- RULESET DEFAULTS -----------------------------------------------------------
// RULESET_ATTRIBUTES lists the meta information that you want to use for the entire ruleset. 
CREATE	DEFAULT_RULESET_ATTRIBUTES AS		
	COMPARER     					= EXACT;
	SOURCE_TOKENIZER  				= STANDARD_ANALYZER;
	TARGET_TOKENIZER 				= STANDARD_ANALYZER;
	MISSING_ATTRIBUTE_ALLOWED 		= FALSE;
	SCORE        					= 1.0;
END

//------------ COMMON SUBRULES -----------------------
CREATE SUBRULE upc_subrule AS 
	MATCH [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14] IN [req_upc_10,req_upc_11,req_upc_12,req_upc_13,req_upc_14, upc, upc_nbr,case_upc_nbr,whpk_upc_nbr,pd_title] 
		USING SOURCE_TOKENIZER=NONE;

/**
 * Two items match if they have the same UPC, same part number and similar title names. A part number
 * match along with UPC match is a strong rule. Just to avoid false positive, a title similarity
 * check has also been added.
 */
CREATE RULE upc_part_number_rule AS
	INCLUDE SUBRULE {#upc_subrule};
	MATCH [req_part_number OR req_raw_part_number] 
		IN [req_part_number, req_raw_part_number, vendor_stock_id,req_upc_10,req_upc_11,req_upc_12,req_upc_13, req_upc_14, customer_item_nbr, case_upc_nbr] 
		USING COMPARER=EXACT_VALUE AND SOURCE_TOKENIZER=NONE;
	MATCH [pd_title] IN [ALL_ITEMPAIR_ATTRIBUTES] USING COMPARER=FUZZY AND SCORE=0.80;		
END

/**
 * Two items with same UPC, same title and same color should be similar.
 */
CREATE RULE upc_title_with_color_rule AS
	INCLUDE SUBRULE {#upc_subrule};
	MATCH [pd_title] IN [ALL_ITEMPAIR_ATTRIBUTES] USING COMPARER=FUZZY AND SCORE=0.95;
	MATCH [req_color OR extracted_color] IN [req_color, extracted_color, req_description, signing_desc, static_color, Color, Primary Color] 
		USING COMPARER=FUZZY AND SCORE=0.95;				
END			

/**
 * If above rule failed it suggests that title representation is slightly different among the two
 * data sources. So, reduce the expected score for title a bit and try to match other extended
 * attributes like category etc.
 */  
CREATE RULE upc_title_brand_category_rule AS
	INCLUDE SUBRULE {#upc_subrule};
	MATCH [req_category] 
		IN [upc_desc, dept_desc, req_category, mdse_catg_desc, signing_desc, dept_subcatg_desc, req_description, dept_catg_grp_desc, mdse_subcatg_desc, dept_category_desc, fineline_desc]
		USING COMPARER=FUZZY AND SCORE=0.95;
	MATCH [req_brand_name] IN [ALL_ITEMPAIR_ATTRIBUTES] 
		USING COMPARER=FUZZY AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND SCORE=0.95;
	MATCH [pd_title] IN [ALL_ITEMPAIR_ATTRIBUTES] USING COMPARER=FUZZY AND SCORE=0.85;		
END

CREATE RULE upc_title_number_units_rule AS
	INCLUDE SUBRULE {#upc_subrule};
	MATCH [pd_title_number_units] IN [pd_title_number_units, sell_qty] USING COMPARER=EXACT_VALUE;
	MATCH [pd_title] IN [ALL_ITEMPAIR_ATTRIBUTES] USING COMPARER=FUZZY AND SCORE=0.85;	
END

CREATE RULE upc_title_variation_phrases_rule AS
	INCLUDE SUBRULE {#upc_subrule};
	MATCH [pd_title_variation_phrases] IN [pd_title_variation_phrases] USING SOURCE_TOKENIZER=SEPARATOR_TOKENIZER;
	MATCH [pd_title] IN [ALL_ITEMPAIR_ATTRIBUTES] USING COMPARER=FUZZY AND SCORE=0.85;	
END

//-------------- RULESET ----------------------------------------------------------------- 
CREATE RULESET wse_stores_matching_rules AS
	INCLUDE RULE upc_part_number_rule;
	INCLUDE RULE upc_title_with_color_rule;
	INCLUDE RULE upc_title_brand_category_rule;
	INCLUDE RULE upc_title_number_units_rule;
	INCLUDE RULE upc_title_variation_phrases_rule;	
END		