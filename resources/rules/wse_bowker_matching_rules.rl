//------------- RULESET DEFAULTS ----------------------------------------------------------- 
CREATE DEFAULT_RULESET_ATTRIBUTES AS		
	COMPARER     					= EXACT;
	SOURCE_TOKENIZER  				= STANDARD_ANALYZER;
	TARGET_TOKENIZER 				= STANDARD_ANALYZER;
	EVALUATOR						= UNIDIRECTIONAL;
	MISSING_ATTRIBUTE_ALLOWED 		= FALSE;
	SCORE        					= 1.0;
END

//-------------- COMMON SUBRULES -----------------------------------------------------
CREATE SUBRULE isbn13_subrule AS 
	MATCH [req_isbn_13] IN [req_isbn_13,req_upc_13,pd_title] USING SOURCE_TOKENIZER=NONE;

CREATE SUBRULE title_subrule AS 
	BIDIRECTIONAL MATCH [pd_title] IN [pd_title,req_description,Volume_Number,Number_of_Volumes,req_series_title,req_publisher] 
		USING COMPARER=FUZZY AND SCORE=0.95;

CREATE SUBRULE publication_year_subrule AS 
	MATCH [extracted_publication_year] IN [pd_title, req_description] USING COMPARER=EXACT;

//-------------- RULES --------------------------------------------------------------------
CREATE RULE isbn13-title-binding-rule AS
	INCLUDE SUBRULE {#isbn13_subrule, #title_subrule, #publication_year_subrule};
	MATCH [req_binding] IN [pd_title, req_binding] USING COMPARER=FUZZY AND SCORE=0.90;
END

CREATE RULE isbn13-title-normalized-binding-rule AS
	INCLUDE SUBRULE {#isbn13_subrule, #title_subrule, #publication_year_subrule};
	BIDIRECTIONAL MATCH [normalized_binding] IN [normalized_binding] USING COMPARER=EXACT;
END

//-------------- RULESET ----------------------------------------------------------------- 
CREATE RULESET wse_bowker_matching_ruleset AS
	INCLUDE RULE isbn13-title-binding-rule;
	INCLUDE RULE isbn13-title-normalized-binding-rule;
END
