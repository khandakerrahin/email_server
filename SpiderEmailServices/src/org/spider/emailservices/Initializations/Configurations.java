package org.spider.emailservices.Initializations;

import java.sql.SQLException;
import java.util.HashMap;

import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Logs.LogWriter;



public class Configurations {
	PostalServicesDS dsConnection;
	ReplyMessageLoader replySMSLoader;
	HashMap<String, String> DBResponseCode;
	MailConfigurations mailConfigurations;
	MailMappings mailMappings;
	public Configurations() {
		replySMSLoader = new ReplyMessageLoader();
		DBResponseCode = new HashMap<String,String>();
		mailConfigurations = new MailConfigurations();
		mailMappings=new MailMappings();
	}

	public void loadConfigurationFromDB() {
		try {
		dsConnection=new PostalServicesDS();
		replySMSLoader.getRelpyMessage(dsConnection);
		mailConfigurations.getMailConfigurations(dsConnection);
		mailMappings.getMailMappings(dsConnection);
		}finally{
			try {
					dsConnection.con.close();
				} catch (SQLException e) {
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
		}
	}

	/**
	 * @return HashMap(rawtypes) replySMSLoader.replyMessage
	 */
	@SuppressWarnings("rawtypes")
	public HashMap getReplySmsHash(){
		return  this.replySMSLoader.replyMessage;
	} 
	
	public HashMap<String,HashMap<String,Object>> getMailConfigurations(){
		return  this.mailConfigurations.properties;
	}
	public HashMap<String,String> getMailMappings(){
		return  this.mailMappings.map;
	}
	/**
	 * @return HashMap<String,String> DBResponseCode
	 */
	public HashMap<String,String> getDBResponseCodeMap(){

		DBResponseCode.put("0", "Success");
		DBResponseCode.put("1", "Already subscribed");
		DBResponseCode.put("2", "Not subscribed");
		DBResponseCode.put("3", "No permission");
		DBResponseCode.put("4", "4");

		return  this.DBResponseCode;
	}   
}

