//------------- RULESET DEFAULTS -----------------------------------------------------------
// RULESET_ATTRIBUTES lists the meta information that you want to use for the entire ruleset. 
CREATE	DEFAULT_RULESET_ATTRIBUTES AS		
	COMPARER     					= EXACT;
	SOURCE_TOKENIZER  				= STANDARD_ANALYZER;
	TARGET_TOKENIZER 				= STANDARD_ANALYZER;
	EVALUATOR						= UNIDIRECTIONAL;
	MISSING_ATTRIBUTE_ALLOWED 		= FALSE;
	SCORE        					= 1.0;
END

//-------------- ATTRIBUTE SET VARIABLES -------------------------------------------------
CREATE VARIABLE attr_set_1 AS pd_title,req_description,req_brand_name,req_manufacturer,req_part_number,req_category;
CREATE VARIABLE attr_set_2 AS pd_title,req_description,req_brand_name,req_manufacturer,req_part_number,req_category,req_amalgammated_attribute,req_raw_part_number;

//-------------- COMMON RULE CLAUSES -----------------------------------------------------
CREATE SUBRULE upc_clause AS 
	MATCH [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14] IN [req_upc_10,req_upc_11,req_upc_12,req_upc_13,req_upc_14,pd_title] 
		USING SOURCE_TOKENIZER=NONE;

CREATE SUBRULE pd_title_wo_number_units_variations_clause AS 
	MATCH [pd_title_without_number_units_and_variations] IN [#attr_set_2] USING COMPARER=FUZZY AND SCORE=0.95; 

CREATE SUBRULE pd_title_wo_number_units_clause AS 
	MATCH [pd_title_without_number_units] IN [#attr_set_2] USING COMPARER=FUZZY AND SCORE=0.95; 

CREATE SUBRULE req_brand_name_or_manufacturer_clause AS 
	MATCH [req_brand_name OR req_manufacturer] IN [#attr_set_1] USING COMPARER=FUZZY AND SCORE=0.95;
		
CREATE SUBRULE extracted_color_clause AS
	MATCH [extracted_color] IN [extracted_color] USING COMPARER=FUZZY AND SCORE=0.95 AND MISSING_ATTRIBUTE_ALLOWED=TRUE;
	
CREATE SUBRULE pd_title_variation_phrases_clause AS
	MATCH [pd_title_variation_phrases] IN [pd_title_variation_phrases] USING SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE;

CREATE SUBRULE part_number_clause AS
	MATCH [req_part_number] IN [pd_title,req_part_number,req_description] USING SOURCE_TOKENIZER=NONE;
	
CREATE SUBRULE color_clause AS
	MATCH [req_color] IN [req_color, pd_title, req_description]  USING COMPARER=FUZZY AND SCORE=0.95 AND MISSING_ATTRIBUTE_ALLOWED=TRUE;
					
//-------------- RULES --------------------------------------------------------------------

/**
  * This rule states that that the upc, title and the brand/manufacturer have to match and
  * the color has to match if present.  
  */
CREATE RULE upc-title-brand-color-numberunits-rule-1 AS
	INCLUDE SUBRULE {#upc_clause, #pd_title_wo_number_units_variations_clause, #req_brand_name_or_manufacturer_clause, #extracted_color_clause, #pd_title_variation_phrases_clause};
	BIDIRECTIONAL MATCH [pd_title_number_units] IN [pd_title_number_units,pd_all_number_units] 
		USING SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE;
END		


CREATE RULE upc-title-brand-color-numberunits-rule-2 AS
	INCLUDE SUBRULE {#upc_clause, #pd_title_wo_number_units_variations_clause, #req_brand_name_or_manufacturer_clause, #extracted_color_clause, #pd_title_variation_phrases_clause};
	MATCH [pd_title_number_units] IN [pd_title_number_units,pd_all_number_units] 
		USING SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND MISSING_ATTRIBUTE_ALLOWED=TRUE;
END

CREATE RULE upc-brand-title-number-units-color-rule AS
	INCLUDE SUBRULE {#upc_clause, #pd_title_wo_number_units_clause, #req_brand_name_or_manufacturer_clause, #color_clause};
	MATCH [pd_title_number_units] IN [pd_title_number_units,pd_all_number_units] USING SOURCE_TOKENIZER=SEPARATOR_TOKENIZER;
END

CREATE RULE part-number-brand-manufacturer-title-rule AS
	INCLUDE SUBRULE {#req_brand_name_or_manufacturer_clause, #color_clause, #part_number_clause};
	MATCH [pd_title] IN [#attr_set_2]  USING COMPARER=FUZZY AND SCORE=0.95;	
END

/**
  *	This rule will apply when upcs are not present in the pair of items. Therefore the
  *	the part-number has to match along with the title and the brand/manufacturer. The color
  *	must match if present.
  */
CREATE RULE upc-part-number-brand-manufacturer-title-rule AS
	INCLUDE SUBRULE {#upc_clause, #req_brand_name_or_manufacturer_clause, #color_clause, #part_number_clause};
	MATCH [pd_title] IN [#attr_set_2]  USING COMPARER=FUZZY AND SCORE=0.80;	
END

//-------------- RULESET ----------------------------------------------------------------- 
CREATE RULESET wse_cnet_matching_rules AS
	INCLUDE RULE upc-title-brand-color-numberunits-rule-1;
	INCLUDE RULE upc-title-brand-color-numberunits-rule-2;
	//INCLUDE RULE upc-brand-title-number-units-color-rule;
	//INCLUDE RULE part-number-brand-manufacturer-title-rule;
	//INCLUDE RULE upc-part-number-brand-manufacturer-title-rule;
END
