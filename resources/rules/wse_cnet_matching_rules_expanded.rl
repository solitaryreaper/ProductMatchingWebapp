//------------- RULESET DEFAULTS -----------------------------------------------------------
// RULESET_ATTRIBUTES lists the meta information that you want to use for the entire ruleset. 
CREATE	DEFAULT_RULESET_ATTRIBUTES AS		
	COMPARER   					= EXACT;
	SOURCE_TOKENIZER  				= STANDARD_ANALYZER;
	TARGET_TOKENIZER 				= STANDARD_ANALYZER;
	EVALUATOR						= UNIDIRECTIONAL;
	MISSING_ATTRIBUTE_ALLOWED 		= FALSE;
	SCORE    					= 1.0;
END

//-------------- RULES ---------------------------------------------------------------------

//---- Rule 1 :
CREATE RULE upc-title-brand-color-numberunits-rule-1 AS 
	 MATCH  [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14]  IN [req_upc_10, req_upc_11, req_upc_12, req_upc_13, req_upc_14, pd_title] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND SCORE=1.0;
	 	
	 MATCH  [pd_title_without_number_units_and_variations]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category, req_amalgammated_attribute, req_raw_part_number] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95;
	 	
	 MATCH  [req_brand_name OR req_manufacturer]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
	 MATCH  [extracted_color]  IN [extracted_color] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=0.95;
	 	
	 MATCH  [pd_title_variation_phrases]  IN [pd_title_variation_phrases] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=1.0;
	 	
	 BIDIRECTIONAL MATCH  [pd_title_number_units]  IN [pd_title_number_units, pd_all_number_units] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=SEPARATOR_TOKENIZER  AND  MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=1.0;
END

//---- Rule 2 :
CREATE RULE upc-title-brand-color-numberunits-rule-2 AS 
	 MATCH  [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14]  IN [req_upc_10, req_upc_11, req_upc_12, req_upc_13, req_upc_14, pd_title] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND SCORE=1.0;
	 		 
	 MATCH  [pd_title_without_number_units_and_variations]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category, req_amalgammated_attribute, req_raw_part_number] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
	 MATCH  [pd_title_without_number_units_and_variations]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category, req_amalgammated_attribute, req_raw_part_number] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 		 
	 MATCH  [req_brand_name OR req_manufacturer]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category] USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;	 MATCH  [extracted_color]  IN [extracted_color] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND SCORE=0.95;
	 	
	 MATCH  [extracted_color]  IN [extracted_color] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=0.95;
	 		 
	 MATCH  [pd_title_variation_phrases]  IN [pd_title_variation_phrases] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND  MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=1.0;
	 	
	 MATCH  [pd_title_number_units]  IN [pd_title_number_units, pd_all_number_units] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND  MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=1.0;
END

//---- Rule 3 :
CREATE RULE upc-brand-title-number-units-color-rule AS 
	 MATCH  [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14]  IN [req_upc_10, req_upc_11, req_upc_12, req_upc_13, req_upc_14, pd_title] USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0;	 MATCH  [pd_title_without_number_units]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category, req_amalgammated_attribute, req_raw_part_number] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
	 MATCH  [pd_title_without_number_units]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category, req_amalgammated_attribute, req_raw_part_number] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;	 
	 	
	 MATCH  [req_brand_name OR req_manufacturer]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
	 MATCH  [req_color]  IN [req_color, pd_title, req_description] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=0.95;
	 	
	 MATCH  [pd_title_number_units]  IN [pd_title_number_units, pd_all_number_units] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND  SCORE=1.0;
END

//---- Rule 4 :
CREATE RULE part-number-brand-manufacturer-title-rule AS 
	 MATCH  [req_brand_name OR req_manufacturer]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
	 MATCH  [req_color]  IN [req_color, pd_title, req_description] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=0.95;
	 	
	 MATCH  [req_part_number]  IN [pd_title, req_part_number, req_description] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0;
	 	
	 MATCH  [pd_title]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category, req_amalgammated_attribute, req_raw_part_number] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
END

//---- Rule 5 :
CREATE RULE upc-part-number-brand-manufacturer-title-rule AS 
	 MATCH  [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14]  IN [req_upc_10, req_upc_11, req_upc_12, req_upc_13, req_upc_14, pd_title] USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0;	 MATCH  [req_brand_name]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
	 MATCH  [req_brand_name OR req_manufacturer]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
	 	
	 MATCH  [req_color]  IN [req_color, pd_title, req_description] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=0.95;
	 	
	 MATCH  [req_part_number]  IN [pd_title, req_part_number, req_description] 
	 	USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0;
	 	
	 MATCH  [pd_title]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category, req_amalgammated_attribute, req_raw_part_number] 
	 	USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.8;
END

//-------------------- RULESET -----------------------------------------------------------------
CREATE RULESET wse_cnet_matching_rules AS 
	INCLUDE RULE upc-title-brand-color-numberunits-rule-1;
	INCLUDE RULE upc-title-brand-color-numberunits-rule-2;
	//INCLUDE RULE upc-brand-title-number-units-color-rule;
	//INCLUDE RULE part-number-brand-manufacturer-title-rule;
	//INCLUDE RULE upc-part-number-brand-manufacturer-title-rule;
END
