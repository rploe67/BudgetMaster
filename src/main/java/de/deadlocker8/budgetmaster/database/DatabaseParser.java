package de.deadlocker8.budgetmaster.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.deadlocker8.budgetmaster.database.legacy.LegacyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseParser
{
	final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private String jsonString;

	public DatabaseParser(String json)
	{
		this.jsonString = json;
	}

	public Database parseDatabaseFromJSON() throws IllegalArgumentException
	{
		JsonObject root = new JsonParser().parse(jsonString).getAsJsonObject();
		String type = root.get("TYPE").getAsString();
		if(!type.equals(JSONIdentifier.BUDGETMASTER_DATABASE.toString()))
		{
			throw new IllegalArgumentException("JSON is not of type BUDGETMASTER_DATABASE");
		}

		int version = root.get("VERSION").getAsInt();
		LOGGER.info("Parsing Budgetmaster database with version " + version);

		if(version == 2)
		{
			return new LegacyParser(jsonString).parseDatabaseFromJSON();
		}

		if(version == 3)
		{
			return new DatabaseParser_v3(jsonString).parseDatabaseFromJSON();
		}

		throw new IllegalArgumentException("unknown BUDGETMASTER_DATABASE version");
	}
}