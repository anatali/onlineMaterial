package it.unibo.HealthAdapter.Clients;

import java.io.BufferedReader;
 
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
 

 

//import org.apache.commons.io.IOUtils;
 
public class HttpClient {
 
	//From https://www.baeldung.com/java-http-request
	public static String get(String uri)  {
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			System.out.println( "HttpSupport get " + url +" status=" + status);
			if( status >= 300) return "ERROR:"+status;
			BufferedReader in = new BufferedReader(
			new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			return content.toString();
		}catch(Exception e) {
 			return "";
		}
	}

	//From https://www.baeldung.com/java-http-request
	public static String post(String uri, String body, String contentType )  { 
		//contentType: "application/json; utf-8" "plain/text; utf-8"
		System.out.println( "HttpSupport POST " + uri + " contentType=" + contentType  ); //+" body=" + body
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", contentType);
 			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);

//write the body
 			OutputStream os = con.getOutputStream();
			byte[] input    = body.getBytes("utf-8");
			os.write(input, 0, input.length);

			int status = con.getResponseCode();
			System.out.println( "HttpSupport POST " + url +" status=" + status);
//read the answer			
			BufferedReader in = new BufferedReader(
			new InputStreamReader( con.getInputStream(), "utf-8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine.trim());
			}
			in.close();
			return response.toString();
		}catch(Exception e) {
			System.out.println( "HttpSupport POST ERROR " +e.getMessage() );
 			return "";
		}		
	}
	
 
	public static String put(String uri, String body, String contentType )  { 
		//contentType: "application/json; utf-8" "plain/text; utf-8"
		System.out.println( "HttpSupport PUT " + uri + " contentType=" + contentType  ); //+" body=" + body
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("PUT");
			con.setRequestProperty("Content-Type", contentType);
 			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);

//write the body
 			OutputStream os = con.getOutputStream();
			byte[] input    = body.getBytes("utf-8");
			os.write(input, 0, input.length);

			int status = con.getResponseCode();
			System.out.println( "HttpSupport PUT " + url +" status=" + status);
//read the answer			
			BufferedReader in = new BufferedReader(
			new InputStreamReader( con.getInputStream(), "utf-8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine.trim());
			}
			in.close();
			return response.toString();
		}catch(Exception e) {
			System.out.println( "HttpSupport PUT ERROR " +e.getMessage() );
 			return "";
		}		
	}	

	public static String delete(String uri)  {
		HttpURLConnection con = null;
		String result="delete " + uri + " done";
		try {
			URL url = new URL(uri);
			con = (HttpURLConnection) url.openConnection();
		    con.setRequestProperty("Content-Type",
		                "application/x-www-form-urlencoded");
		    con.setRequestMethod("DELETE");
		    System.out.println("delete response code=" + con.getResponseCode());
		} catch (Exception e) {
			result = "delete " + uri + "error " + e.getMessage();
		    //exception.printStackTrace();
		} finally {         
		    if (con != null) {
		        con.disconnect();
		    }
		}
	return result;
	}

	/* 	
	public static String readFromFileJson(String fileName) {
		try {			
			FileInputStream fis = new FileInputStream(fileName);
		    String data         = IOUtils.toString(fis, "UTF-8");
 		    return data;
		} catch (Exception e) {
			System.out.println("readPatientFromFileJson fileName=" + fileName + " ERROR "+ e.getMessage() );
			return null;
		}
	}
	

	public static String prettyJson( String sjson ) {
		String result = "";
		try {
		    ObjectMapper mapper = new ObjectMapper();
		    JsonFactory factory = mapper.getFactory();
			JsonParser parser   = factory.createParser( sjson );
		    JsonNode actualObj  = mapper.readTree(parser);
		    result              = actualObj.toPrettyString() ;		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	*/

	public static void do_test() {
		String args   = "{\"pid\": \"12345\"}";
		String answer = post("http://localhost:8080/test", args, "application/json");
		System.out.println( answer  );
	}

	   public static void main(String[] args)   {
	    	do_test();
//	    	do_importPatient( );
//	    	HealthService.delay(10000);
	    	System.out.println( "BYE");		
	    }
	
}
