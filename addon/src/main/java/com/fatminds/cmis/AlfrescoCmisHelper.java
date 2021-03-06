/*******************************************************************************
 * Copyright 2012 Fatminds, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.fatminds.cmis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.client.AlfrescoAspects;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.util.StringUtils;

public class AlfrescoCmisHelper {

	private static final Logger log = LoggerFactory.getLogger(AlfrescoCmisHelper.class);
	
	protected static final ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Cache typeinfo, it's expensive to retrieve
	 */
	protected static final Map<String, AlfrescoCmisTypeInfo> typeInfo = new HashMap<String, AlfrescoCmisTypeInfo>();
	
	public static final String ALF_DICT_API_BASE =  "/alfresco/service/api/classes/";
	
	public static final String DASH_SEP_STRING = "---";

	public static final String ALF_CATEGORY_API_BASE =  "/alfresco/service/fatminds/categories/";

	public static final String ALF_CATEGORY_CRUD_API_BASE =  "/alfresco/service/fatminds/category";
	
	/**
	 * 
	 * @param f Excel file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ContentStream getContentStream(File f) throws FileNotFoundException{
		if (null == f)
			return null;
		
		FileInputStream fis = new FileInputStream(f);
		ContentStream cs = new ContentStreamImpl(f.getName(), null, "application/excel", fis);
		return cs;
	}
	
	/**
	 * Split and return an array of trimmed tokens
	 * @param in
	 * @param splitBy regex
	 * @return
	 */
	public static String[] splitTrimString(String in, String splitBy){
		if (!StringUtils.hasText(in) || !StringUtils.hasText(splitBy)){
			return new String[]{};
		}
		String[] tokens = in.split(splitBy);
		if (null == tokens){
			return new String[]{};
		}
		for (int i=0; i<tokens.length; i++){
			tokens[i] = tokens[i].trim();
		}
		return tokens;
	}
		
	/*********************************************************************************************\
	 * CMIS object (and tree) cloning                                                            *
	 *                                                                                           *
	\*********************************************************************************************/
	
	public static Document cloneDocument(AlfrescoCmisSessionDataSource dataSource, Document source, Folder destination, Map<String, Object> replaceProps){
		return (Document)cloneObject(dataSource, source, destination, replaceProps, false);
	}
	public static Folder cloneFolder(AlfrescoCmisSessionDataSource dataSource, Folder source, Folder destination, Map<String, Object> replaceProps){
		return (Folder)cloneObject(dataSource, source, destination, replaceProps, false);
	}
	
	/*
	 * @param source - the Folder or Document you wish to clone
	 * @param replacementProps - properties that will replace values from the source object in the cloned
	 * object. Note ***MUST*** contain an entry for cmis:name if you are copying the object into the same space as its source, 
	 * and that entry must be unique according to the rules for cmis:name. 
	 * @return the cloned object, or null if any of the inputs were null
	 * 
	 */
	protected static CmisObject cloneObject(AlfrescoCmisSessionDataSource dataSource, CmisObject source, Folder destFolder, Map<String, Object> replacementProps, boolean recurse){
		if (null == source || null == replacementProps || null == dataSource || null == destFolder){
			return null;
		}

		// Set up properties for new object. Start by copying the old.
		Map<String, Object> newProps = cloneProps(source);
		// Now copy over replacement properties supplied by caller (which had best include
		// a (different) value for cmis:name, unless this is a recursed call for a child of some
		// copied node). 
		for (String k: replacementProps.keySet()){
			newProps.put(k, replacementProps.get(k));
		}
		// Finally, calculate and apply the correct Alfresco-oriented cmis:objectTypeId set
		AlfrescoCmisTypeInfo info = getTypeAndMandatoryAspectProperyDefinitions(dataSource, (String)newProps.get(PropertyIds.OBJECT_TYPE_ID));
		newProps.put(PropertyIds.OBJECT_TYPE_ID, info.getCmisTypeIdWithMandatoryAspects());
		
		CmisObject newObject;
		if (source instanceof Document){
			Document srcDoc = (Document) source;
			newObject = destFolder.createDocument(newProps, null, VersioningState.MAJOR);
		}
		else { // source is a Folder
			Folder srcFolder = (Folder) source;
			newObject = destFolder.createFolder(newProps);
			if (recurse){
				for (CmisObject obj : srcFolder.getChildren()){
					Map<String, Object> emptyProps = new HashMap<String, Object>();
					cloneObject(dataSource, obj, ((Folder)newObject), emptyProps, true);
				}
			}
			Folder newFolder = (Folder)newObject;
		}
		for (Relationship r : source.getRelationships()){
			if (r.getSource() == source){ // Copy only source relationships
		    	Map<String, Object> props = new HashMap<String, Object>();
		    	props.put(PropertyIds.SOURCE_ID, newObject.getId());
		    	props.put(PropertyIds.TARGET_ID, r.getTargetId().toString());
		    	props.put(PropertyIds.OBJECT_TYPE_ID, r.getPropertyValue(PropertyIds.OBJECT_TYPE_ID));
		    	ObjectId newRelationId = dataSource.getSession().createRelationship(props);
			}
		}
		return newObject;
	}
	/**
	 * 
	 * @param toBeCloned
	 * @return shallow clone of the map
	 */
	protected static Map<String, Object> cloneProps(CmisObject obj){
		Map<String, Object> cloned = new HashMap<String, Object>();
		if (null == obj){
			return cloned;
		}
		for (Property p : obj.getProperties()){
			if (null != p.getValue()){
				cloned.put(p.getId(), p.getValue());
				log.debug("Cloned property " + p.getId() + ", val = " + p.getValueAsString());
			}
		}
		return cloned;
	}
	
	/**
	 * Try several different date formats (in order of descending specificity) to parse a potential
	 * date string.
	 * @param inVal
	 * @return
	 * @throws IllegalArgumentException if the string cannot be successfully parsed into a date.
	 */
	public static GregorianCalendar attemptDateParse(String inVal) {
		if (null == inVal || inVal.length() < 1)
			throw new IllegalArgumentException("Cannot parse null date/time string");

		// Attempt formats in order of reducing specificity
			try {
				DateTime d = ISODateTimeFormat.dateTime().parseDateTime(inVal);
				return d.toGregorianCalendar();
			}
			catch (IllegalArgumentException e) {
				try {
					LocalDate d = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate(inVal);
					return d.toDateTimeAtMidnight().toGregorianCalendar();
				}
				catch (IllegalArgumentException e2) {
					try {
						LocalDate d = DateTimeFormat.forPattern("MM/dd/yyyy").parseLocalDate(inVal);
						return d.toDateTimeAtMidnight().toGregorianCalendar();
					}
					catch (IllegalArgumentException e3) {
						try {
							LocalDate d = DateTimeFormat.forPattern("MM/dd/yy").parseLocalDate(inVal);
							return d.toDateTimeAtMidnight().toGregorianCalendar();
						}
						catch (IllegalArgumentException e4){
						}
					}
				}
			}
		log.error("Cannot parse date/time string " + inVal);
		throw new IllegalArgumentException("Cannot parse date/time string " + inVal);
	}

	public static String[] parseList(String inVal){
		if (null == inVal)
			throw new IllegalArgumentException("Empty input");
		return inVal.split("---");
	}
	
	public static Object cmisConvert(String inVal, PropertyDefinition<?> targetType) {
		if (null == inVal || null == targetType) {
			//log.info("inVal " + inVal + " or outClass " + outClass + " are null");
			throw new IllegalArgumentException("All convert arguments must be non-null");
		}
		List<Object> retVals= new ArrayList<Object>();
		
		PropertyType type = targetType.getPropertyType();
		String[] inVals;
		boolean multiple = targetType.getCardinality().equals(Cardinality.MULTI);
		if (multiple) {
			inVals = parseList(inVal);
		}
		else {
			inVals = new String[] {inVal};
		}

		for (String token: inVals) {
			if (PropertyType.BOOLEAN.value() == type.value()) {
				retVals.add(Boolean.valueOf(token));
			}
			else if (PropertyType.DATETIME.value() == type.value()) {
				retVals.add(attemptDateParse(token));
			}
			else if (PropertyType.DECIMAL.value() == type.value()) {
				retVals.add(new BigDecimal(token));
			}
			else if (PropertyType.HTML.value() == type.value()) {
				retVals.add(token);
			}
			else if (PropertyType.ID.value() == type.value()) {
				retVals.add(new ObjectIdImpl(token));
			}
			else if (PropertyType.INTEGER.value() == type.value()) {
				retVals.add(new Long(new BigDecimal(token).longValue())); // Handles dotted "integers" (sigh) like "15.0"
			}
			else if (PropertyType.STRING.value() == type.value()) {
				retVals.add(token);
			}
			else if (PropertyType.URI.value() == type.value()) {
				retVals.add(token);
			}
		}
		if (retVals.size() < 1)
			throw new IllegalArgumentException("Couldn't convert field of type " + type.name() + ", value = " + inVal);
		
		if (multiple)
			return retVals;
		
		return retVals.get(0);
	}
	
	public static Object convert(Object inVal, Class<?> outClass){
		if (null == inVal || null == outClass) {
			//log.info("inVal " + inVal + " or outClass " + outClass + " are null");
			return null;
		}		
		//log.info("Converting " + inVal.getClass() + " to " + outClass);
		if (outClass.isAssignableFrom(inVal.getClass()))
			return inVal;
		
		Object outVal;
		try {
			Constructor<?> c = outClass.getConstructor(inVal.getClass());
			outVal = c.newInstance(inVal);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			//log.info("Failed to find constructor of " + outClass.toString() 
			//		+ " with input parameter " + inVal.getClass().toString()
			//		+ ", returning unconverted value");
			outVal = inVal;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		//log.info("Returning an instance of " + outVal.getClass());
		return outVal;
	}
	
	@SuppressWarnings("rawtypes")
	public static Class getPropertyClass (PropertyDefinition pd) {
		
		PropertyType type = pd.getPropertyType();
		//boolean multiple = pd.getCardinality().equals(Cardinality.MULTI);
		Class c=null;
		
		if (PropertyType.BOOLEAN.value() == type.value()) {
			c = Boolean.class;
		}
		else if (PropertyType.DATETIME.value() == type.value()) {
			c = GregorianCalendar.class;
		}
		else if (PropertyType.DECIMAL.value() == type.value()) {
			c = BigDecimal.class;
		}
		else if (PropertyType.HTML.value() == type.value()) {
			c = String.class;
		}
		else if (PropertyType.ID.value() == type.value()) {
			c = ObjectId.class;
		}
		else if (PropertyType.INTEGER.value() == type.value()) {
			c = Long.class;
		}
		else if (PropertyType.STRING.value() == type.value()) {
			c = String.class;
		}
		else if (PropertyType.URI.value() == type.value()) {
			c = String.class;
		}
		
		if (null != c) {
			return c;
		}
		log.error("Unsupported property type " + type.value());
		throw new RuntimeException("Unsupported property type " + type.value());
	}
	
	/**
	 * 
	 * @param alfrescoHostAndPort (i.e. dev2-instance.fatminds.com:8080)
	 * @param cmisType (i.e. F:fminstitution:institution)
	 * @return the set of all properties carried by each mandatory aspect defined in the alfresco
	 * data model for the given cmisType
	 */
	public static Set<String> getMandatoryAspects(JsonNode root) {
		if (null == root || !root.has("defaultAspects")) {
			throw new RuntimeException("root must not be null, and must contain /defaultAspects path");
		}
		HashSet<String> aspects = new HashSet<String>();
		Iterator<String> i = root.path("defaultAspects").getFieldNames();
		while (i.hasNext()) {
			String aspect = i.next();
			// TODO: clean this up
			if (aspect.startsWith("sys"))
				continue;
			if (aspect.startsWith("cm:auditable"))
				continue;
			aspects.add(aspect);
		}
		return aspects;
	}

	public static String sanitizeForCmisName(String in) {
		if (null == in || in.isEmpty()) {
			return null;
		}
		
		in = in.replaceAll("[^a-zA-Z_0-9]", "_")
		.replaceAll("_{2,}+", "_");
		return in;
	}
	
	public static String cmisToAlfrescoTypename(String cmisTypename) {
		if (null == cmisTypename)
			return null; 
		
		// Mangle name according to alf API rules: [FDPA]:nsprefix:localname -> nsprefix_localname
		StringBuilder sb = new StringBuilder();
		int secondColon = cmisTypename.indexOf(':', 2);
		sb.append(cmisTypename.substring(2, secondColon)); // sb == nsprefix
		sb.append('_');
		sb.append(cmisTypename.substring(secondColon+1));
		return sb.toString();
	}
	
	public static String stripCmisTypeSpecifier(String typeSpec) {
		if (null == typeSpec || typeSpec.isEmpty()) {
			return null;
		}
		int pos = typeSpec.indexOf(':', 2);
		if (pos > 0) {
			return typeSpec.substring(2);
		}
		return typeSpec;
	}
	
	public static String alfModelToApiClassname(String modelClassname) {
		return modelClassname.replace(':', '_');
	}
	
	/**
	 * This is here and needed because Alfresco CMIS and Chemistry don't play nice together for relationship deletes
	 * as of Alfresco 3.4.2-5 & Chemistry 0.3.0. The problem is that for whatever reason Chemistry strips the "assoc:" prefix
	 * off of the relationship ID that ends up on the end of the Delete URL, like this - /alfresco/service/cmis/rel/assoc:363.
	 * Left to its own devices, Relationship.delete() will end up issuing a URL like /alfresco/service/cmis/rel/363, and Alfresco
	 * will barf. This will hopefully be fixed in Alfesco 4.x.
	 * @param proto
	 * @param host
	 * @param port
	 * @param user
	 * @param pass
	 * @param assocId
	 * @return
	 */
	public static boolean deleteRelationship(String proto, String host, int port, String user, String pass, String assocId) {
		DefaultHttpClient client = new DefaultHttpClient();
		try {
           client.getCredentialsProvider().setCredentials(
                    new AuthScope(host, port),
                    new UsernamePasswordCredentials(user, pass));
           HttpDelete delete = new HttpDelete(proto + "://" + host + ":" + port + "/alfresco/service/cmis/rel/" + assocId);
           log.info("Sending " + delete.toString());
           HttpResponse resp = client.execute(delete);
           if (resp.getStatusLine().getStatusCode() > 299) { // Alf returns "204 No Content" for success... :-(
        	   throw new RuntimeException("Get failed (" + resp.getStatusLine().getStatusCode() + ") because " + resp.getStatusLine().getReasonPhrase());
           }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			client.getConnectionManager().shutdown();
		}		
		return true;
	}
	
	public static JsonNode getJsonNodeFromHttpGetResponse(String proto, String host, int port, String user, String pass, String path, HashMap params) throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		try {
           client.getCredentialsProvider().setCredentials(
                    new AuthScope(host, port),
                    new UsernamePasswordCredentials(user, pass));
           String paramStr = "";
           if (params != null && params.size() >0){
        	   Iterator<String> iter = params.keySet().iterator();
        	   while(iter.hasNext()){
        		   String key = iter.next();
        		   //get.getParams().setParameter(key, params.get(key));
        		   if (!paramStr.equals("")){
        			   paramStr += "&";  
        		   }
        		   paramStr += key + "=" + params.get(key);
        	   }
           }
           
           
           if (!paramStr.equals("")){
        	   path += "?" + URLEncoder.encode(paramStr, "UTF-8").replace("+", "%20");
           }
           HttpGet get = new HttpGet(proto + host + ":" + port + path );
           log.info("Getting JsonNode for " + get.toString());
           HttpResponse resp = client.execute(get);
           if (resp.getStatusLine().getStatusCode() != 200) {
        	   throw new RuntimeException("Get failed, can't build JsonNode because: " + resp.getStatusLine().getReasonPhrase());
           }
           org.apache.http.HttpEntity entity = resp.getEntity();
           return mapper.readValue(entity.getContent(), JsonNode.class);
		}
		finally {
			client.getConnectionManager().shutdown();
		}
	}

	public static JsonNode getJsonNodeFromHttpGetResponse(String proto, String host, int port, String user, String pass, String path) throws ClientProtocolException, IOException {
	 return getJsonNodeFromHttpGetResponse( proto,  host,  port,  user,  pass,  path, null);
	}

	public static void main(String[] args) {
    	String user = "admin";
    	String pass = "admin";
    	String host = "localhost";
    	int port = 8080;
    	
    	Set<String> props = getMandatoryAspects("F:fmbase:educationiteminstance", "http://", host, port, user, pass);
    	System.out.println("Mandatory aspects");
    	for (String s: props) {
    		System.out.println("\t " + s);
    	}
	}
	
	public static JsonNode getFatmindsCategories(AlfrescoCmisSessionDataSource cmisDataSource){
	  	System.out.println("Getting Fatminds Category Tree");
    	try {

	  		return getJsonNodeFromHttpGetResponse(
				"http://", 
				cmisDataSource.getHostname(), 
				cmisDataSource.getPort(),
				cmisDataSource.getUsername(),
				cmisDataSource.getPassword(),
				ALF_CATEGORY_API_BASE);
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Could not retrieve Fatminds Category Tree.", e);
    	}
	}
	
	
	public static JsonNode crudFatmindsCategory(AlfrescoCmisSessionDataSource cmisDataSource, String path, String action){
	  	System.out.println("CRUD Fatminds Category");
    	try {
    		HashMap params = new HashMap();
    		params.put("path", path);
    		params.put("action", action);
	  		return getJsonNodeFromHttpGetResponse(
				"http://", 
				cmisDataSource.getHostname(), 
				cmisDataSource.getPort(),
				cmisDataSource.getUsername(),
				cmisDataSource.getPassword(),
				ALF_CATEGORY_CRUD_API_BASE, params );
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Could not CRUD Fatminds Category", e);
    	}
	}

	public static Set<String> getMandatoryAspects(AlfrescoCmisSessionDataSource cmisDataSource, String cmisTypename){
		return getMandatoryAspects(
				cmisTypename, 
				"http://", 
				cmisDataSource.getHostname(), 
				cmisDataSource.getPort(),
				cmisDataSource.getUsername(),
				cmisDataSource.getPassword());
	}

    public static Set<String> getMandatoryAspects(String cmisTypename, String proto, String host, int port, String user, String pass) {
    	String apiCall = ALF_DICT_API_BASE + cmisToAlfrescoTypename (cmisTypename);
    	System.out.println("Getting " + apiCall);
    	try {
	    	JsonNode root = getJsonNodeFromHttpGetResponse("http://", host, port, user, pass, apiCall, null);
	    	
	    	log.info("Got JSON root " + root.path("name").getTextValue());
	    	return getMandatoryAspects(root);
		}
    	catch (Exception e) {
    		throw new RuntimeException("Could not retrieve mandatory aspect properties.", e);
    	}
    }
    
	public static Set<String> getMandatoryAspectProperties(AlfrescoCmisSessionDataSource cmisDataSource, String cmisTypename){
		return getMandatoryAspectProperties(
				cmisTypename, 
				"http://", 
				cmisDataSource.getHostname(), 
				cmisDataSource.getPort(),
				cmisDataSource.getUsername(),
				cmisDataSource.getPassword());	
	}
	
    public static Set<String> getMandatoryAspectProperties(String cmisTypename, String proto, String host, int port, String user, String pass) {
    	String apiCall = ALF_DICT_API_BASE + cmisToAlfrescoTypename (cmisTypename);
    	System.out.println("Getting " + apiCall);
    	try {
	    	JsonNode root = getJsonNodeFromHttpGetResponse("http://", host, port, user, pass, apiCall);
	    	
	    	log.info("Got JSON root " + root.path("name").getTextValue());
	    	
	    	HashSet<String> mandatoryAspectProperties = new HashSet<String>(); // add properties to this as they're found
	    	Set<String> aspects = getMandatoryAspects(root);
	    	for (String s : aspects) {
	    		String propCall = ALF_DICT_API_BASE + alfModelToApiClassname(s) + "/properties";
	    		log.info("Retrieving properties for mandatory aspect " + s + " at API url " + propCall);
	        	JsonNode propArray = getJsonNodeFromHttpGetResponse("http://", host, port, user, pass, propCall);
	        	for (JsonNode n : propArray) {
	        		String propName = n.path("name").getTextValue();
	        		log.info("Found aspect property " + propName + " in aspect " + s);
	        		mandatoryAspectProperties.add(propName);
	        	}
	    		
	    	}
	    	return mandatoryAspectProperties;
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Could not retrieve mandatory aspect properties.", e);
    	}
    }

	/**
     * @param objectId of an existing CmisObject
     * @param aspects - a list of aspect names that should be checked for existence on the retrieved object;
     * @return The object, with ALL MANDATORY ASPECTS APPLIED if not found to exist already on each object. This will not
     * have any effect in normal operation, but you will really appreciate it if and when you change a content model to include
     * a new mandatory aspect that isn't already applied to existing content. Hello, CmisExceptions on commit. 
     */

	public static CmisObject getObjectByIdAndAddAspects(Session session, String objectId, Set<String> aspects) {
		OperationContext oc = session.createOperationContext();
		oc.setIncludeRelationships(IncludeRelationships.SOURCE);
		CmisObject obj = session.getObject(session.createObjectId(objectId), oc);
		if (!obj.getRelationships().isEmpty()) {
			log.info("Found relationships on " + obj.getName());
		}
		if (null == obj)
			return null;
		
		if (obj instanceof AlfrescoAspects) {
			AlfrescoAspects as = (AlfrescoAspects) obj;
			if (null != aspects) {
				for (String aspect : aspects) {
					if (!as.hasAspect(aspect)) {
						as.addAspect("P:" + aspect);
					}
				}
			}
		}
		return obj;
	}

	/**
	 * Look up the CMIS type or policy that owns the specified property ID
	 * @param session - Valid CMIS Session
	 * @param propId - Name of the property for which we want the owning (base) class
	 * @param baseTypeCandidates -- List of candidate types to search - only leaf nodes in the type hierarchy need be specified
	 * @return
	 */
   public static ObjectType findPropertyOwnerClass(Session session, String propId, String[] baseTypeCandidates) {
	   if (null == propId) {
		   throw new IllegalArgumentException("Can't pass null propId");
	   }
	   if (null == baseTypeCandidates || 0 == baseTypeCandidates.length) {
		   throw new IllegalArgumentException("Must pass at least one base type candidate");
	   }
	   for (String candidate : baseTypeCandidates) {
		   ObjectType ot = session.getTypeDefinition(candidate);
		   if (null == ot) {
			   throw new IllegalArgumentException("Cannot find type definition for candidate type " + candidate);
		   }
		   if (ot.getPropertyDefinitions().containsKey(propId)) {
			   // Find defining class
			   if (ot.getPropertyDefinitions().get(propId).isInherited()) {
				   while (ot.getPropertyDefinitions().get(propId).isInherited()) {
					   ot = ot.getParentType();
				   }
			   }
			   return ot;
		   }
	   }
	   log.warn("No property owner class found for " + propId);
	   return null;
   }

   /**
    * Represent complete CMIS type ID (inclusive of comma-separated mandatory aspect list) suitable for passing to 
    * Session.create*, and full property definition set for this complete ID (i.e. also includes aspect properties). 
    * 
    * throws RuntimeException if it fails, otherwise is guaranteed to return non-null result. 
    * 
    * @author aaronlee
    *
    */
   public static class AlfrescoCmisTypeInfo{
	   protected String baseCmisTypeId = "";
	   protected String cmisTypeIdWithMandatoryAspects = "";
	   protected Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();
	   
	   public AlfrescoCmisTypeInfo() {
	   }
	   
	   public AlfrescoCmisTypeInfo(String baseTypeId, String typeId, Map<String, PropertyDefinition<?>> props) {
		   this.baseCmisTypeId = baseTypeId;
		   this.cmisTypeIdWithMandatoryAspects = typeId;
		   this.propertyDefinitions = props;
	   }
	   
	/**
	 * @return the cmisTypeIdWithMandatoryAspects
	 */
	public String getCmisTypeIdWithMandatoryAspects() {
		return cmisTypeIdWithMandatoryAspects;
	}
	/**
	 * @return the propertyDefinitions
	 */
	public Map<String, PropertyDefinition<?>> getPropertyDefinitions() {
		return propertyDefinitions;
	}
	/**
	 * @param cmisTypeIdWithMandatoryAspects the cmisTypeIdWithMandatoryAspects to set
	 */
	public void setCmisTypeIdWithMandatoryAspects(
			String cmisTypeIdWithMandatoryAspects) {
		this.cmisTypeIdWithMandatoryAspects = cmisTypeIdWithMandatoryAspects;
	}
	/**
	 * @param propertyDefinitions the propertyDefinitions to set
	 */
	public void setPropertyDefinitions(
			Map<String, PropertyDefinition<?>> propertyDefinitions) {
		this.propertyDefinitions = propertyDefinitions;
	}

	/**
	 * @return the baseCmisTypeId
	 */
	public String getBaseCmisType() {
		return baseCmisTypeId;
	}

	/**
	 * @param baseCmisTypeId the baseCmisTypeId to set
	 */
	public void setBaseCmisType(String baseCmisType) {
		this.baseCmisTypeId = baseCmisType;
	}
	   
   }
   
   /**
    * 
    * @param cmisDataSource
    * @param cmisType
    * @return properties and the Alfresco-formatted cmis type Id, including mandatory aspects. i.e. 
    * 	- input cmisType: F:myapp:mytype
    *   - output cmisType: F:myapp:mytype,P:myapp:mymandatoryaspect
    */
	public static AlfrescoCmisTypeInfo getTypeAndMandatoryAspectProperyDefinitions(AlfrescoCmisSessionDataSource cmisDataSource, String cmisType) {
		if (null == cmisDataSource || null == cmisType)
			throw new RuntimeException("cmisDataSource or cmisType was null");
		
		if (null != typeInfo.get(cmisType)){
			return typeInfo.get(cmisType);
		}
		
		Session session = cmisDataSource.getSession();
		
		ObjectType typeDef = session.getTypeDefinition(cmisType);
		if (null == typeDef) {
			throw new RuntimeException("No type definition found for cmisType " + cmisType);
		}

		String cmisTypeIdWithAspects=cmisType;
		Map<String, PropertyDefinition<?>> returnProps = new HashMap<String, PropertyDefinition<?>>();

		Map<String, PropertyDefinition<?>> sourceDefs = typeDef.getPropertyDefinitions();
        for (String propId: sourceDefs.keySet()) {
        	PropertyDefinition pd = sourceDefs.get(propId);
        	returnProps.put(propId, pd);
        	//log.info("\t" + propId + "  = " + (null == pd.getChoices() ? "null" : pd.getChoices()));
        }
        
        // Get list of mandatory aspects applied to this type
        Set<String> aspects = AlfrescoCmisHelper.getMandatoryAspects(cmisDataSource, cmisType);
        for (String aspect : aspects) {
        	String cmisPolicy = "P:" + aspect;
        	// Add to object creation argument value (should be F:your:type[,P:some:aspect,P:another:aspect])
        	cmisTypeIdWithAspects += "," + cmisPolicy;
        	log.debug("Retrieving type definition for " + cmisPolicy);
        	try {
        		ObjectType policyDef = session.getTypeDefinition(cmisPolicy);
        		Map<String, PropertyDefinition<?>> aspectProps = policyDef.getPropertyDefinitions();
        		/**
        		 * So, guess what? If, in your Alfresco content model you have 
        		 * 	<aspect name="blah:blah"> <properties></properties>...</aspect> (i.e. no properties on the aspect)
        		 * then policyDef.getPropertyDefinitions() returns null. Fair enough, I guess. 
        		 * 	
        		 */
        		if (null == aspectProps)
        			continue;
        		for (String prop : aspectProps.keySet()) {
        			log.trace("Found property " + prop + " on policy " + cmisPolicy + ", adding to CmisContainer property list");
        			returnProps.put(prop, aspectProps.get(prop));
        		}
        	}
        	catch (Exception e) {
        		// This means we requested the type definition for an aspect that
        		// is not exposed through CMIS. This means that properties
        		// in that aspect are not available to the container (or any CMIS
        		// request). This is not an error, so continue.
        	}
        }
        AlfrescoCmisTypeInfo info = new AlfrescoCmisTypeInfo(cmisType, cmisTypeIdWithAspects, returnProps);
        typeInfo.put(cmisType, info);
        return info;
	}

}

