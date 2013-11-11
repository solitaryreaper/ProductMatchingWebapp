use product_match_db;

-- List of data sources --
create table data_sources
(
	id int,
	title varchar(255),
	description varchar(8000),
	CONSTRAINT data_sources_pk PRIMARY KEY (id)
);
-- Trial tracker --
create table trial_info 
(
	id int,
	title varchar(255),
	description varchar(8000),
	run_time datetime,
	run_user varchar(255),
	run_status tinyint,
	CONSTRAINT trial_pk PRIMARY KEY (id),
	CONSTRAINT trial_name_uq UNIQUE (title)
);

-- Trial stats --
create table trial_stats 
(
	id int,
	num_matched_pairs int,
	num_mismatched_pairs int,
	CONSTRAINT trial_stats_pk PRIMARY KEY (id),
	FOREIGN KEY (id) REFERENCES trial_info(id)
);

-- Trial itempairs --
create table trial_itempairs
(
	id int,
	source_item_id int,
	source_item_data_source_id int,
	target_item_id int,
	target_item_data_source_id int,
	is_match tinyint,
	CONSTRAINT trial_itempairs_pk PRIMARY KEY (id),
	FOREIGN KEY (id) REFERENCES trial_info(id),
	FOREIGN KEY (source_item_data_source_id) REFERENCES data_sources(id),
	FOREIGN KEY (target_item_data_source_id) REFERENCES data_sources(id)
);
CREATE INDEX trial_itempairs_ix ON trial_itempairs(id);

