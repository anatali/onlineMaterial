/*
 * ------------------------------------------------------------------------
 * Used by the RestController HealthAdapterMIController 
 * ------------------------------------------------------------------------
 */
package it.unibo.HealthAdapterFacade;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ca.uhn.fhir.context.FhirContext;
 

@Service
public class HealthService {
	
	public static enum HealthCenterType{ FHIR, HL7, OTHER};
	public final static FhirContext fhirctx = FhirContext.forR4();
/*
 * --------------------------------------------------------------------------------------------
 * WARNING: these URI are used in src\main\resourcestemplates\indexHealthAdapterFacade.html	
 * --------------------------------------------------------------------------------------------
 */

/*	
 * SYNCH
 */
	public static final String selectHealthCenterUri    ="/selectHealthCenterSynch";
	
	public static final String createResourceUriSynch   ="/createResourceSynch";
	public static final String readResourceUriSynch     ="/readResourceSynch";
	public static final String searchResourceUriSynch	="/searchResourceSynch";
	public static final String updateResourceUriSynch	="/updateResourceSynch";
	public static final String deleteResourceUriSynch   ="/deleteResourceSynch";
	
/*
 * ASYNCH 
 */
	public static final String createResourceUri   		="/createResource"; 	//Patient/...???
	public static final String readResourceUri   		="/readResource";
	public static final String searchResourceUri  		="/searchResource";
	public static final String updateResourceUri  		="/updateResource";
	public static final String deleteResourceUri 		="/deleteResource";

/*
 * FLUX related (experiments) 
*/	
	public static final String startDatafluxUri			="/startDataflux";
	public static final String stopDatafluxUri 			="/stopDataflux";
	public static final String subscribehotfluxUri   	="/subscribeflux";
	
/*
 * CONVERSION 
 */	
	public static final String cvtHL7ToFHIRUri   		="/cvthl7tofhir";
	
	/*
	 * UTILITIES : 	
	 */
	 	public static String cvt( IBaseResource theResource,  boolean useJson ) {
	 		if( useJson ) return cvtJson(theResource);
	 		else return cvtXml(theResource);
	 	}
	 	public static String cvt( Bundle bundle,  boolean useJson ) {
	 		if( useJson ) return fhirctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
	 		else return fhirctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle);
	 	}

	 	public static String cvtJson( IBaseResource theResource ) {
	 		return fhirctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(theResource);
	 	}

	 	public static String cvtXml( IBaseResource theResource ) {
	 		return fhirctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(theResource);
	 	}
	 	
	 	public static String getResourceType( String resourceJsonStr ) {
 			try {
				JSONObject json = new JSONObject(resourceJsonStr);
				return json.getString("resourceType");
			} catch (JSONException e) {
				return "" ;
 			}	 			 		
	 	}
	 	
	 	public static String prettyJson(String jsonStr) {
	 		JSONObject json;
			try {
				json = new JSONObject(jsonStr);
				return json.toString(2);
			} catch (JSONException e) {
				return "prettyJson ERROR for"+jsonStr;
 			}	 		
	 	}
	 	
	 	public static void delay( int dt ) {
	 		try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
 				e.printStackTrace();
			}
	 	}
	
/*
 * THE COMPONENT	
 */
	private HealthServiceInterface service;
	
	public HealthService() {	//Autoconfiguration, just to start ...		
 		System.out.println("HealthService Autoconfiguration " );
		buildHealthService( HealthCenterType.FHIR, "https://hapi.fhir.org/baseR4" );
	}
	
	public HealthServiceInterface setHealthService(String choice, String serverAddr) { 
 		System.out.println("HealthService setHealthService " + serverAddr);
		if( choice.equals("FHIR") ) buildHealthService( HealthCenterType.FHIR, serverAddr );
		else if( choice.equals("HL7") ) buildHealthService( HealthCenterType.HL7, serverAddr );
		return getdHealthService();
	}
	
	public HealthServiceInterface getdHealthService() { return service; }
	
	//Called by a system configuration
	public void buildHealthService( HealthCenterType hct, String serverBase ) {
		System.out.println("buildHealthService " + hct + " serverBase=" + serverBase);
		switch( hct ) { 
			case FHIR  : service =  new HealthServiceFhir( serverBase );break;  
			case HL7   : service =  new HealthServiceHL7( serverBase );break;
			default    : service =  new HealthServiceFhir( serverBase );
		}		
	}
	
}
