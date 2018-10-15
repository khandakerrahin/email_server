package org.spider.emailservices.Engine;

public class ZmailProcessor extends EmailProcessor{
//	public ZmailProcessor(String username, String password){
//		setProperties("smtp.zoho.com",true,465,true);
//		setAdditionalProperties();
//		setCredentials(username,password);
//	}
	public ZmailProcessor(){
		setProperties("smtp.zoho.com",true,465,true);
		setAdditionalProperties();
	}
	private void setAdditionalProperties(){
      properties.put("mail.store.protocol", "pop3");
      properties.put("mail.transport.protocol", "smtp");
      properties.put("mail.smtp.ssl.enable", "true");
	}
}
