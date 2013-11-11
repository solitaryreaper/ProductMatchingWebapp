//------------- RULESET DEFAULTS ------------------
CREATE DEFAULT_RULESET_ATTRIBUTES AS
	COMPARER = 						EXACT;
	SOURCE_TOKENIZER = 				STANDARD_ANALYZER;
	TARGET_TOKENIZER = 				STANDARD_ANALYZER;
	EVALUATOR = 					UNIDIRECTIONAL;
	MISSING_ATTRIBUTE_ALLOWED = 	FALSE;
	SCORE = 						1.0;
END

//------------- RULE DEFINITION ------------------
CREATE RULE upc-title-rule AS 
	 MATCH  [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14]  IN [req_upc_10, req_upc_11, req_upc_12, req_upc_13, req_upc_14, pd_title]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0 ;

	 MATCH  [pd_title]  IN [ pd_title,req_description,req_brand_name,req_manufacturer,req_part_number,req_category]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95 ;
END

//------------- RULE DEFINITION ------------------
CREATE RULE upc-long-signing-desc-rule AS 
	 MATCH  [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14]  IN [req_upc_10, req_upc_11, req_upc_12, req_upc_13, req_upc_14, pd_title]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0 ;

	 MATCH  [long_signing_desc]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95 ;
END

//------------- RULE DEFINITION ------------------
CREATE RULE upc-title-number-units-rule AS 
	 MATCH  [req_upc_10 OR req_upc_11 OR req_upc_12 OR req_upc_13 OR req_upc_14]  IN [req_upc_10, req_upc_11, req_upc_12, req_upc_13, req_upc_14, pd_title]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0 ;

	 MATCH  [pd_title_number_units]  IN [pd_title_number_units, pd_desc_number_units]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=SEPARATOR_TOKENIZER AND  SCORE=1.0 ;

	 MATCH  [pd_title_without_number_units]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95 ;
END

//------------- RULE DEFINITION ------------------
CREATE RULE part-number-brand-manufacturer-title-rule AS 
	 MATCH  [req_part_number]  IN [pd_title, req_part_number, req_description]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0 ;

	 MATCH  [pd_title]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95 ;

	 MATCH  [req_brand_name OR req_manufacturer]  IN [pd_title, req_description, req_brand_name, req_manufacturer, req_part_number, req_category]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95 ;
END

//------------- RULE DEFINITION ------------------
CREATE RULE isbn13-title-binding-rule AS 
	 MATCH  [req_isbn_13]  IN [req_isbn_13, req_upc_13, pd_title]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0 ;

	 MATCH  [pd_title]  IN [pd_title, req_description]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95 ;

	 MATCH  [req_binding]  IN [pd_title, req_binding]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  MISSING_ATTRIBUTE_ALLOWED=TRUE AND  SCORE=0.85 ;
END

//------------- RULE DEFINITION ------------------
CREATE RULE isbn13-title-normalized-binding-rule AS 
	 MATCH  [req_isbn_13]  IN [req_isbn_13, req_upc_13, pd_title]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0 ;

	 MATCH  [pd_title]  IN [pd_title, req_description]
		 USING  COMPARER=FUZZY AND  SOURCE_TOKENIZER=STANDARD_ANALYZER AND  SCORE=0.95 ;

	 MATCH  [normalized_binding]  IN [normalized_binding]
		 USING  COMPARER=EXACT AND  SOURCE_TOKENIZER=NONE AND  SCORE=1.0 ;
END

//------------- RULESET DEFINITION ------------------
CREATE RULESET book_matching_rules AS 
	INCLUDE RULE upc-title-rule ;
	INCLUDE RULE upc-long-signing-desc-rule ;
	INCLUDE RULE upc-title-number-units-rule ;
	INCLUDE RULE part-number-brand-manufacturer-title-rule ;
	INCLUDE RULE isbn13-title-binding-rule ;
	INCLUDE RULE isbn13-title-normalized-binding-rule ;
END
