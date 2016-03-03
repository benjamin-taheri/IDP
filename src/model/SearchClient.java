package model;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import java.util.List;

public class SearchClient {

	//final private String GOOGLE_SEARCH_URL = "https://www.googleapis.com/customsearch/v1?";

	//api key
	final private String API_KEY = "AIzaSyB6DBffDC1PRsJXoALX3mUjIdbeHlrpCyw";
	//custom search engine ID
	final private String NYTIMES_SEARCH_ENGINE_ID = "005906253170799005812:lppqr4q4ynm";
	final private String WASHIGTONPOST_SEARCH_ENGINE_ID = "005906253170799005812:yu9br2zfzg8";
	final private String YAHOO_SEARCH_ENGINE_ID = "005906253170799005812:dtxvtuz0hgc";
	final private String ALL_SEARCH_ENGINE_ID = "005906253170799005812:f05ka-7pk04";

	//final private String FINAL_URL = GOOGLE_SEARCH_URL + "key=" + API_KEY + "&cx=" + SEARCH_ENGINE_ID;

	public static void main(String[] args) throws Exception{
		SearchClient gsc = new SearchClient();
		String searchKeyWord = "Microsoft";
		List<Result> resultList = gsc.getSearchResult(searchKeyWord, 1, 1);
		if(resultList != null && resultList.size() > 0){
			for(Result result: resultList){
				System.out.println(result.getHtmlTitle());
				System.out.println(result.getFormattedUrl());
				//System.out.println(result.getHtmlSnippet());
				System.out.println("----------------------------------------");
			}
		}
	}

	public List<Result> getSearchResult(String keyword, int srcNum, long pageNum){
		// Set up the HTTP Transport and JSON Factory
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		//HttpRequestInitializer initializer = (HttpRequestInitializer)new CommonGoogleClientRequestInitializer(API_KEY);
		Customsearch customsearch = new Customsearch(httpTransport, jsonFactory,null);
		String SEARCH_ENGINE_ID = ALL_SEARCH_ENGINE_ID;
		switch (srcNum) {
		case 1 : SEARCH_ENGINE_ID = NYTIMES_SEARCH_ENGINE_ID;
		break;
		case 2 : SEARCH_ENGINE_ID = WASHIGTONPOST_SEARCH_ENGINE_ID;
		break;
		case 3 : SEARCH_ENGINE_ID = YAHOO_SEARCH_ENGINE_ID;
		break;
		}
		List<Result> resultList = null;
		try {
			Customsearch.Cse.List list = customsearch.cse().list(keyword);
			list.setKey(API_KEY);
			list.setCx(SEARCH_ENGINE_ID);
			//Number Of Results Per Page
			list.setNum(10L);
			//For Pagination
			list.setStart(pageNum);
			//For lower bound and upper bound of search results
			//list.setLowRange(lowRange + "");
			//list.setHighRange(highRange + "");
			Search results = list.execute();
			resultList = results.getItems();
			if (resultList != null) {
				System.out.println(results.getItems().size());
			} else {
				System.out.println(0);
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}
}
