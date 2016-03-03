package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class XmlExtractor {

    public static StringBuilder fetchXmlContent(String url) throws IOException {
        StringBuilder xmlContent = new StringBuilder();
        Document document = Jsoup.connect(url).get();
        xmlContent.append(document.body().html());
        return xmlContent;
    }

    public static void saveXmlFile(StringBuilder xmlContent, String saveLocation) throws IOException {
        FileWriter fileWriter = new FileWriter(saveLocation);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(xmlContent.toString());
        bufferedWriter.close();
        System.out.println("Downloading completed successfully..!");
    }

    public static void downloadXml() throws IOException {
//        String url = "http://api.worldbank.org/countries/GBR/indicators/NY.GDP.MKTP.KD.ZG?date=2004:2012";
        String url = "http://www.nytimes.com/2015/01/22/technology/personaltech/the-microsoft-hololens-a-sensational-vision-of-the-pcs-future.html";
//        String saveLocation = System.getProperty("java.io.tmpdir")+"sarath.xml";
        String saveLocation = "D:\\sarath.xml";
        XmlExtractor.saveXmlFile(XmlExtractor.fetchXmlContent(url), saveLocation);
    }

    public static void main(String[] args) throws IOException {
        XmlExtractor.downloadXml();
    }

}
