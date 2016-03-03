package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.UUID;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SaveResults {
	public void saveToFile(String urlString, String searchKey, int src, String date, String dest, String source,
			String documentType, String newsDesk, String sectionName, boolean includingBlogs, int keywordRepeatNr)
					 {
		String result = null;
		Document doc = null;
		UUID id = UUID.randomUUID();
		String searchPublishDate = null;
		if (src == 1) {
			if (date != null) {
				searchPublishDate = date.substring(0, 10);
			}
			try {
				Connection.Response loginForm = Jsoup.connect("https://myaccount.nytimes.com/auth/login")
						.followRedirects(true)
						.userAgent("Mozilla/5.0")
						.method(Connection.Method.GET)
						.timeout(60000)
						.execute();
				doc = Jsoup.connect(urlString)
			            .followRedirects(true)
			            .cookies(loginForm.cookies())
			            .method(Connection.Method.POST)
			            .data("userid", "klausureinsicht@googlemail.com")
			            .data("password", "nyt_fm")
			            .referrer(urlString)
			            .timeout(60000)
			            .ignoreHttpErrors(true)
			            .post();
//			            .data("error_return_url", "/index.php?mid=free&act=dispMemberLoginForm")
//			            .data("mid", "free")
//			            .data("vid", "")
//			            .data("ruleset", "@login")
//			            .data("success_return_url", "")
//			            .data("act", "procMemberLogin")
			} catch(Exception ioe) {
				ioe.printStackTrace();
			}
			if (doc != null) {
				Elements el = doc.body().select("p.story-body-text.story-content");
				if (el.isEmpty() && includingBlogs) {
					el = doc.body().select("p.story-body-text");
				} else if (el.isEmpty() && !includingBlogs) {
					return;
				}
				result = doc.title();
				int index = 0;
				while(index != el.size()) {
					result += "\n" + el.get(index).text();
					index++;
				}
				if (keywordRepeatNr != 0) {
					String[] arr = result.split(" ");
					int detectedRepeatNr = 0;
					for ( String ss : arr) {
						if (ss.toLowerCase().contains(searchKey.toLowerCase())) {
							detectedRepeatNr++;
						}
					}
					if (detectedRepeatNr < keywordRepeatNr) {
						return;
					}
				}
				String temp = result + "\n},";
				result = "{\n\"body\": {\n";
				result += temp;
				result += "\n\"miscellaneous\": {\n";
				result += "\"source\":" + source + ",\n";
				result += "\"pub_date\":" + searchPublishDate + ",\n";
				result += "\"document_type\":" + documentType + ",\n";
				result += "\"news_desk\":" + newsDesk + ",\n";
				result += "\"section_name\":" + sectionName + "\n";
				result += "}\n}";
//				System.out.println(result);
			}			
		} else if (src == 2) {
			String html = null;
			try {
				html = Jsoup.connect(urlString).maxBodySize(Integer.MAX_VALUE).timeout(600000).get().html();
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}
			doc = Jsoup.parse(html);
			result = doc.title() + "\n";
			result += doc.body().text();
			searchPublishDate = doc.body().select("span.pb-timestamp").text();
		}
		if (result != null) {
			try {			
				//save to this filename
				String fileName = dest + "\\" + searchKey + " " + searchPublishDate + " " + id + ".txt";
				File file = new File(fileName);
				if (!file.exists()) {
					file.createNewFile();
				}
				//use FileWriter to write file
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(result);
				bw.close();
				System.out.println("Done");
			} catch (Exception e) {
				e.printStackTrace();
			}  
		}
	}
	
	public static void main(String args[]) throws Exception {
		SaveResults gsc = new SaveResults();
//		String destTest = "D:\\";
		String destTest = "/Users/taheritanjani/Downloads/";
		gsc.saveToFile(
				"http://www.nytimes.com/video/technology/personaltech/1231545958094/windows-7-beta.html",
				"Microsoft", 1, null, destTest, null, null, null , null, true, 0);
	}
}
