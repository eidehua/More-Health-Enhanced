package com.nohero.morehealth;

import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FileUtils;
import scala.util.parsing.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Ed Xue on 11/13/2014.
 */
public class MCModGen {

	public static void main(String[] args) throws IOException{
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);

		jsonWriter.beginArray();

		jsonWriter.beginObject();
		jsonWriter.name("modid");
		jsonWriter.value(mod_moreHealthEnhanced.modid);

		jsonWriter.name("name");
		jsonWriter.value(mod_moreHealthEnhanced.name);

		jsonWriter.name("description");
		jsonWriter.value("More Health Mod");

		jsonWriter.name("version");
		jsonWriter.value(mod_moreHealthEnhanced.version);

		jsonWriter.name("mcversion");
		jsonWriter.value("");

		jsonWriter.name("url");
		jsonWriter.value("www.minecraftforum.net/topic/115172");

		jsonWriter.name("updateUrl");
		jsonWriter.value("");

		jsonWriter.name("authorList");
		jsonWriter.beginArray();
		jsonWriter.value("nohero");
		jsonWriter.endArray();

		jsonWriter.name("credits");
		jsonWriter.value("Created by nohero");

		jsonWriter.name("logoFile");
		jsonWriter.value("");

		jsonWriter.name("screenshots");
		jsonWriter.beginArray();
		jsonWriter.endArray();

		jsonWriter.name("dependencies");
		jsonWriter.beginArray();
		jsonWriter.endArray();

		jsonWriter.endObject();
		jsonWriter.endArray();

		jsonWriter.flush();

		String json = stringWriter.toString();
		jsonWriter.close();

		FileUtils.writeStringToFile(new File("mcmod.info"), json);
	}
}
