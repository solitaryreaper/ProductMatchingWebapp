package models.enums;

/**
 * Enum containing the data sources for product description.
 * 
 * @author sprasa4
 *
 */
public enum DataSource {
	CNET("CNET"),
	WALMART_SEARCH_EXTRACT("WALMART_SEARCH_EXTRACT"),
	BOWKER("BOWKER"),
	WALMART_STORES("WALMART_STORES"),
	UNKNOWN("UNKNOWN");
	
	String dataSource;
	
	DataSource(String dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public static DataSource getDataSourceEnum(String dataSource)
	{
		DataSource dataSourceEnum = DataSource.UNKNOWN;
		DataSource[] sources = DataSource.values();
		for(int i=0; i < sources.length; i++) {
			DataSource source = sources[i];
			if(source.name().equals(dataSource)) {
				dataSourceEnum = source;
			}
		}
		
		return dataSourceEnum;
	}
	
}
