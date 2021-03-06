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
	MailUsers mailUsers;
	
	ReplyMessageLoader replySMSLoaderTemp;
	MailConfigurations mailConfigurationsTemp;
	MailMappings mailMappingsTemp;
	MailUsers mailUsersTemp;
	
	public Configurations() {
		replySMSLoader = new ReplyMessageLoader();
		DBResponseCode = new HashMap<String,String>();
		mailConfigurations = new MailConfigurations();
		mailMappings=new MailMappings();
		mailUsers = new MailUsers();
	}

	public void loadConfigurationFromDB() {
		try {
		dsConnection=new PostalServicesDS();
		replySMSLoader.getRelpyMessage(dsConnection);
		mailConfigurations.getMailConfigurations(dsConnection);
		mailMappings.getMailMappings(dsConnection);
		mailUsers.getMailUsers(dsConnection);
		}finally{
			try {
					dsConnection.con.close();
				} catch (SQLException e) {
					e.printStackTrace();
					LogWriter.LOGGER.severe(e.getMessage());
				}
		}
	}

	public Boolean reloadConf() {
		Boolean retVal = false;
		replySMSLoaderTemp = new ReplyMessageLoader();
		mailConfigurationsTemp = new MailConfigurations();
		mailMappingsTemp =new MailMappings();
		mailUsersTemp = new MailUsers();
		try {
			dsConnection=new PostalServicesDS();
			replySMSLoaderTemp.getRelpyMessage(dsConnection);
			mailConfigurationsTemp.getMailConfigurations(dsConnection);
			mailMappingsTemp.getMailMappings(dsConnection);
			mailUsersTemp.getMailUsers(dsConnection);
			retVal = true;
			}finally{
				try {
						dsConnection.con.close();
					} catch (SQLException e) {
						e.printStackTrace();
						LogWriter.LOGGER.severe(e.getMessage());
					}
			}
		return retVal;
	}
	
	public void switchConf() {
		replySMSLoader = replySMSLoaderTemp;
		mailConfigurations = mailConfigurationsTemp;
		mailMappings = mailMappingsTemp;
		mailUsers = mailUsersTemp;
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
	public HashMap<String,String> getMailUsers(){
		return  this.mailUsers.map;
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

