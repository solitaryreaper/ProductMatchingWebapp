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

//-------------- RULES --------------------------------------------------------------------

//-------- Rule 1 : 
CREATE RULE isbn13-title-binding-rule AS
	MATCH [req_isbn_13] IN [req_isbn_13,req_upc_13,pd_title] 
		USING COMPARER=EXACT AND SOURCE_TOKENIZER=NONE AND SCORE=1.0;
			  
	BIDIRECTIONAL MATCH [pd_title] IN [pd_title,req_description,Volume_Number,Number_of_Volumes,req_series_title,req_publisher] 
		USING COMPARER=FUZZY AND SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
				
	MATCH [req_binding] IN [pd_title, req_binding] 
		USING COMPARER=FUZZY AND SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.90;
		
	MATCH [extracted_publication_year] IN [pd_title, req_description] 
		USING COMPARER=EXACT AND SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=1.0;	
END

//-------- Rule 2 : 
CREATE RULE isbn13-title-normalized-binding-rule AS
	MATCH [req_isbn_13] IN [req_isbn_13,req_upc_13,pd_title] 
		USING COMPARER=EXACT AND SOURCE_TOKENIZER=NONE AND SCORE=1.0;
		
	BIDIRECTIONAL MATCH [pd_title] IN [pd_title,req_description,Volume_Number,Number_of_Volumes,req_series_title,req_publisher] 
		USING COMPARER=FUZZY AND SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=0.95;
				
	BIDIRECTIONAL MATCH [normalized_binding] IN [normalized_binding] 
		USING COMPARER=EXACT AND SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=1.0;
		      
	MATCH [extracted_publication_year] IN [pd_title, req_description] 
		USING COMPARER=EXACT AND SOURCE_TOKENIZER=STANDARD_ANALYZER AND SCORE=1.0;	
END

//-------------- RULESET ----------------------------------------------------------------- 
CREATE RULESET wse_bowker_matching_ruleset AS
	INCLUDE RULE isbn13-title-binding-rule;
	INCLUDE RULE isbn13-title-normalized-binding-rule;
END
