package at.medunigraz.imi.bst.trec.gsheets;

import java.io.IOException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsRequestInitializer;
import com.google.api.services.sheets.v4.model.ValueRange;

import at.medunigraz.imi.bst.config.TrecConfig;

public class SheetsReader {
	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Build and return an authorized Sheets API client service.
	 * 
	 * @return an authorized Sheets API client service
	 * @throws IOException
	 */
	public static Sheets getSheetsService() throws IOException {
		Credential credential = null;

		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(SheetsReader.class.getCanonicalName())
				.setGoogleClientRequestInitializer(new SheetsRequestInitializer(TrecConfig.GSHEETS_APIKEY)).build();
	}

	public static void main(String[] args) throws IOException {
		// Build a new authorized API client service.
		Sheets service = getSheetsService();

		String spreadsheetId = TrecConfig.GSHEETS_SHEETID;
		String range = "thyroid_pubmed!A2:D1000";
		ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
		List<List<Object>> values = response.getValues();
		if (values == null || values.size() == 0) {
			System.out.println("No data found.");
		} else {
			for (List row : values) {
				if (row.isEmpty() || row.size() <= 2) {
					break;
				}
				System.out.printf("%s, %s\n", row.get(0), row.get(2));
			}
		}
	}

}