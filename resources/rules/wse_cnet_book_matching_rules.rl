/*
	Book product category matching rules.
*/
//------------- RULESET DEFAULTS ----------------------------------------------------------- 
CREATE DEFAULT_RULESET_ATTRIBUTES AS		
	COMPARER     					= EXACT;
	SOURCE_TOKENIZER  				= STANDARD_ANALYZER;
	TARGET_TOKENIZER 				= STANDARD_ANALYZER;
	EVALUATOR						= UNIDIRECTIONAL;
	MISSING_ATTRIBUTE_ALLOWED 		= FALSE;
	SCORE        					= 1.0;
END

//-------------- ATTRIBUTE SET VARIABLES -------------------------------------------------
// Declare attribute set variables here. These would be replaced inline in the rule clause.
CREATE VARIABLE upc_rule_common_src_attr_set AS
	req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14; 

CREATE VARIABLE upc_rule_common_target_attr_set_1 AS 
	req_upc_10,req_upc_11,req_upc_12,req_upc_13,req_upc_14,pd_title;

CREATE VARIABLE upc_rule_common_target_attr_set_2 AS 
	pd_title,req_description,req_brand_name,req_manufacturer,req_part_number,req_category;

CREATE VARIABLE isbn_rule_common_target_attr_set_1 AS
	req_isbn_13,req_upc_13,pd_title;
	
CREATE VARIABLE isbn_rule_common_target_attr_set_2 AS
	pd_title,req_description;
	
//-------------- COMMON RULE CLAUSES -----------------------------------------------------
// Declare common clauses as variables here. These would be replaced inline in the rule.
CREATE SUBRULE upc_rule_common_clause AS 
	MATCH [#upc_rule_common_src_attr_set] IN [#upc_rule_common_target_attr_set_1] USING SOURCE_TOKENIZER=NONE;

CREATE SUBRULE isbn_rule_common_clause_1 AS
	MATCH [req_isbn_13] IN [#isbn_rule_common_target_attr_set_1] USING SOURCE_TOKENIZER=NONE;
	
CREATE SUBRULE isbn_rule_common_clause_2 AS
	MATCH [pd_title] IN [#isbn_rule_common_target_attr_set_2] USING COMPARER=FUZZY AND SCORE=0.95;
			
//-------------- RULES --------------------------------------------------------------------
CREATE RULE upc-title-rule AS
	INCLUDE SUBRULE {#upc_rule_common_clause};
	MATCH [pd_title] IN [ALL_ITEMPAIR_ATTRIBUTES] USING COMPARER=FUZZY AND SCORE=0.95;
END

CREATE RULE upc-long-signing-desc-rule AS
	INCLUDE SUBRULE {#upc_rule_common_clause};
	MATCH [long_signing_desc] IN [#upc_rule_common_target_attr_set_2] USING COMPARER=FUZZY AND SCORE=0.95;
END

CREATE RULE upc-title-number-units-rule AS
	INCLUDE SUBRULE {#upc_rule_common_clause};
	MATCH [pd_title_number_units] IN [pd_title_number_units,pd_desc_number_units] USING SOURCE_TOKENIZER=SEPARATOR_TOKENIZER;
	MATCH [pd_title_without_number_units] IN [#upc_rule_common_target_attr_set_2] USING COMPARER=FUZZY AND SCORE=0.95;
END

CREATE RULE part-number-brand-manufacturer-title-rule AS
	MATCH [req_part_number] IN [pd_title,req_part_number,req_description] USING SOURCE_TOKENIZER=NONE;
	MATCH [pd_title] IN [#upc_rule_common_target_attr_set_2] USING COMPARER=FUZZY AND SCORE=0.95;
	MATCH [req_brand_name OR req_manufacturer] IN [#upc_rule_common_target_attr_set_2] USING COMPARER=FUZZY AND SCORE=0.95;
END

CREATE RULE isbn13-title-binding-rule AS
	INCLUDE SUBRULE {#isbn_rule_common_clause_1, #isbn_rule_common_clause_2};
	MATCH [req_binding] IN [pd_title, req_binding] USING COMPARER=FUZZY AND MISSING_ATTRIBUTE_ALLOWED=TRUE AND SCORE=0.85;
END

CREATE RULE isbn13-title-normalized-binding-rule AS
	INCLUDE SUBRULE {#isbn_rule_common_clause_1, #isbn_rule_common_clause_2};
	MATCH [normalized_binding] IN [normalized_binding] USING SOURCE_TOKENIZER=NONE;
END

//-------------- RULESET -----------------------------------------------------------------
// Ruleset : if any of the rules included in the set is successful, ruleset is successful. 
CREATE RULESET book_matching_rules AS
	INCLUDE RULE upc-title-rule;
	INCLUDE RULE upc-long-signing-desc-rule;
	INCLUDE RULE upc-title-number-units-rule;
	INCLUDE RULE part-number-brand-manufacturer-title-rule;
	INCLUDE RULE isbn13-title-binding-rule;
	INCLUDE RULE isbn13-title-normalized-binding-rule;
END
