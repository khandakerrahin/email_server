package org.spider.emailservices.Engine;

public class GmailProcessor extends EmailProcessor{
//	public GmailProcessor(String username, String password){
//		setProperties("smtp.gmail.com",true,587,true);
//		setCredentials(username,password);
//	}
	public GmailProcessor(){
		setProperties("smtp.gmail.com",true,587,true);
	}
	
}
