import org.apache.commons.validator.routines.UrlValidator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLFormatter {

    public URLFormatter(){}

    public String formatURL(String url){
        if(url.contains("http://") || url.contains("https")){
            return url;
        }
        else{
            return "http://" + url + "/";
        }
    }

    public boolean validateURL(String url){
        UrlValidator validator = new UrlValidator();
        return validator.isValid(url);
    }

    public boolean pingURL(String str_url) throws IOException {
        URL url = new URL(str_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        System.out.println(connection.getResponseCode());
        char codeType = Integer.toString(connection.getResponseCode()).charAt(0);
        return codeType == '2' || codeType == '3';
    }

}
