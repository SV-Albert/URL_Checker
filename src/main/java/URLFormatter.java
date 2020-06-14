import org.apache.commons.validator.routines.UrlValidator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * URLFormatter class is responsible for formatting and verifying the connection
 * to the URLs that the user inputs
 *
 * @version 0.1
 * @author Albert Shakirzianov
 */
public class URLFormatter {

    /**
     * Constructor for the URLFormatter class
     */
    public URLFormatter(){}

    /**
     * A method that takes in the URL String and returns a formatted version
     * in the form http://url/
     *
     * @param url String input
     * @return formatted URL String
     */
    public String formatURL(String url){
        if(url.contains("http://") || url.contains("https://")){
            return url;
        }
        else{
            return "http://" + url + "/";
        }
    }

    /**
     * Check if the URL is valid
     *
     * @param url to check
     * @return true if valid
     */
    public boolean validateURL(String url){
        UrlValidator validator = new UrlValidator();
        return validator.isValid(url);
    }

    /**
     * Check if an HTTP connection could be established
     *
     * @param str_url String URL
     * @return true if the HTTP response code is 2** or 3**
     * @throws IOException
     */
    public boolean pingURL(String str_url) throws IOException {
        URL url = new URL(str_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        System.out.println(connection.getResponseCode());
        char codeType = Integer.toString(connection.getResponseCode()).charAt(0);
        return codeType == '2' || codeType == '3';
    }

}
