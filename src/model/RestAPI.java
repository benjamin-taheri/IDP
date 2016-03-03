package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestAPI {
	final static private String NY_SEARCH_URL = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
	final static private String NY_API_KEY = "710c2c2781353c71cf1ac36791e6a6e8:4:74521511";

	private static String readUrl(String urlString) {
		BufferedReader reader = null;
		try {
			try {
				URL url = new URL(urlString);
				////////
				URLConnection c = url.openConnection();
				c.setConnectTimeout(60000);
				c.setReadTimeout(60000);
				reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
				////////
				//	    		reader = new BufferedReader(new InputStreamReader(url.openStream()));
				StringBuffer buffer = new StringBuffer();
				int read;
				char[] chars = new char[1024];
				while ((read = reader.read(chars)) != -1) {
					buffer.append(chars, 0, read);
				}
				return buffer.toString();
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to read JSON from stream", e);
		}
	}

	public static long getHitsNum(String query, String beginDate, String endDate) {
		String request = NY_SEARCH_URL + "?q=" + query + "&begin_date=" + beginDate + "&end_date=" + endDate + 
				/* "&sort=newest" + "&page=" + */ "&api-key=" + NY_API_KEY;
		String json = readUrl(request);
		long hits = 0;
		try {
			JSONObject obj = new JSONObject(json);
			JSONObject response = obj.getJSONObject("response");
			JSONObject meta = response.getJSONObject("meta");
			hits = Long.parseLong(meta.getString("hits"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hits;
	}

	public static List<String> search(String query, String beginDate, String endDate, long page) {
		List<String> answer = new ArrayList<String>(11);
		String request = NY_SEARCH_URL + "?q=" + query + "&begin_date=" + beginDate + "&end_date=" + endDate + 
				/* "&sort=newest" + "&page=" + */ "&api-key=" + NY_API_KEY;
		long hits = 0;
		JSONObject obj;
		JSONObject response;
		String json = readUrl(request);
		try {
			obj = new JSONObject(json);
			response = obj.getJSONObject("response");
			JSONObject meta = response.getJSONObject("meta");
			hits = Long.parseLong(meta.getString("hits"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		long pageNum = hits/10;
		if (page <= pageNum) {
			request = NY_SEARCH_URL + "?q=" + query + "&begin_date=" + beginDate + "&end_date=" + endDate + 
					/*"&sort=newest" + */ "&page=" + page + "&api-key=" + NY_API_KEY;
			json = readUrl(request);
			try {
				obj = new JSONObject(json);
				response = obj.getJSONObject("response");
				JSONArray docs = response.getJSONArray("docs");			
				if (page == pageNum) {
					int num = docs.length();
					for (int j = 0; j < num; ++j) {
						JSONObject result = docs.getJSONObject(j);
						answer.add(result.getString("web_url"));
						answer.add(result.getString("pub_date"));
						answer.add(result.getString("snippet"));
						answer.add(result.getString("source"));
						answer.add(result.getString("document_type"));
						answer.add(result.getString("news_desk"));
						answer.add(result.getString("section_name"));
					}
				} else {
					for (int j = 0; j < 10; ++j) {
						JSONObject result = docs.getJSONObject(j);
						answer.add(result.getString("web_url"));
						answer.add(result.getString("pub_date"));
						answer.add(result.getString("snippet"));
						answer.add(result.getString("source"));
						answer.add(result.getString("document_type"));
						answer.add(result.getString("news_desk"));
						answer.add(result.getString("section_name"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return answer;
		} else {
			return null;
		}

	}

	public static void main(String[] args) throws Exception {
		String beginDate = "20150601";
		String endDate = "20150612";
		String query = "Microsoft";
		List<String> print = search(query, beginDate, endDate, 0);
		if (print != null) {
			for (int i = 0; i < print.size(); i = i+3) {
				System.out.println(i+1+ " " + print.get(i));
				System.out.println(print.get(i+1));
				System.out.println(print.get(i+2));
				System.out.println();
			}
		} else {
			System.out.println("No search result");
		}
	}
}
