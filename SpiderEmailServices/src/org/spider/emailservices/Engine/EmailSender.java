package org.spider.emailservices.Engine;

import java.sql.Connection;
import java.util.HashMap;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.spider.emailservices.Initializations.Configurations;
import org.spider.emailservices.Logs.LogWriter;
import org.spider.emailservices.Utilities.NullPointerExceptionHandler;
import org.spider.emailservices.recieverServlet.EmailServlets;

public class EmailSender {
	public final String GMAIL_KEY="GMAIL";
	public final String ZOHO_KEY="ZOHO";
//	GmailProcessor gmailProcessor;
//	ZmailProcessor zmailProcessor;
	Configurations configurations;
	Connection con;
	LogWriter logWriter;
	public EmailSender(Connection con, LogWriter logWriter, Configurations configurations) {
		this.configurations=configurations;
		this.logWriter=logWriter;
		this.con=con;
//		if(this.configurations.getMailConfigurations().containsKey("GMAIL"))
//			gmailProcessor=new GmailProcessor();
//		else
//			gmailProcessor=null;
//		if(this.configurations.getMailConfigurations().containsKey("ZOHO"))
//			zmailProcessor=new ZmailProcessor();
//		else
//			zmailProcessor=null;
	}
	public HashMap<String, String> send(String from,String to, String cc, String bcc,String subject,String mailBody, boolean isUnicode) {
		HashMap<String, String> retval=new HashMap<>();
		retval.put("ErrorCode", "-1"); retval.put("ErrorMessage",null);
		InternetAddress fromEmail=null;
		InternetAddress[] toEmails=null;
		InternetAddress[] ccEmails=null;
		InternetAddress[] bccEmails=null;
		boolean recipientExists=false;
		try {
			fromEmail=new InternetAddress(from);
		}catch(AddressException e) {
			e.printStackTrace();
			retval.replace("ErrorCode", "-2");
			retval.merge("ErrorMessage", "Invalid from email address format.", String::concat);
		}
		try {
			if(!NullPointerExceptionHandler.isNullOrEmpty(to)) {
				recipientExists=true;
				toEmails=InternetAddress.parse(to, true);
			}
		}catch(AddressException e) {
			e.printStackTrace();
			retval.replace("ErrorCode", "-3");
			retval.merge("ErrorMessage", "Invalid email address format in 'to' list", String::concat);
		}
		try {
			if(!NullPointerExceptionHandler.isNullOrEmpty(cc)) {
				recipientExists=true;
				ccEmails=InternetAddress.parse(cc, true);
			}
		}catch(AddressException e) {
			e.printStackTrace();
			retval.replace("ErrorCode", "-4");
			retval.merge("ErrorMessage", "Invalid email address format in 'cc' list", String::concat);
		}
		try {
			if(!NullPointerExceptionHandler.isNullOrEmpty(bcc)) {
				recipientExists=true;
				bccEmails=InternetAddress.parse(bcc, true);
			}
		}catch(AddressException e) {
			e.printStackTrace();
			retval.replace("ErrorCode", "-5");
			retval.merge("ErrorMessage", "Invalid email address format in 'bcc' list", String::concat);
		}
		if(retval.get("ErrorCode")=="-1" && recipientExists && !NullPointerExceptionHandler.isNullOrEmpty(from))
			retval.putAll(send(fromEmail,toEmails,ccEmails,bccEmails,subject,mailBody, isUnicode));
		return retval;
	}
	public HashMap<String, String> send(InternetAddress from, InternetAddress[] to, InternetAddress[] cc,
			InternetAddress[] bcc, String subject, String mailBody, boolean isUnicode) {
		HashMap<String, String> retval=new HashMap<>(2,1.0f);
		retval.put("ErrorCode", "-1"); retval.put("ErrorMessage",null);
		boolean recipientExists=false;
		if(to!=null) {
			recipientExists=true;
		}
		if(cc!=null) {
			recipientExists=true;
		}
		if(bcc!=null) {
			recipientExists=true;
		}
		if(retval.get("ErrorCode")=="-1" && recipientExists && from!=null) {
			
			//waiting for reload
			while(EmailServlets.reloadInProgress) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			String configurationId=this.configurations.getMailMappings().containsKey(from.getAddress())? this.configurations.getMailMappings().get(from.getAddress()):"";
			String gateway= (this.configurations.getMailConfigurations().containsKey( configurationId )?
				((String) this.configurations.getMailConfigurations().get(configurationId).get("gateway")) :"");
//			String gateway=this.configurations.getMailMappings().containsKey(from.getAddress())? 
//				     (this.configurations.getMailConfigurations().containsKey( this.configurations.getMailMappings().get(from.getAddress()) )?
//				((String) this.configurations.getMailConfigurations().get(this.configurations.getMailMappings().get(from.getAddress())).get("gateway")) :"")
//		       :"";
			LogWriter.LOGGER.info("Configuration ID: "+ configurationId);
			LogWriter.LOGGER.info("Gateway: "+gateway);
			switch(gateway) {
			case GMAIL_KEY: 
				LogWriter.LOGGER.info(GMAIL_KEY+" Sender");
				GmailProcessor gmailProcessor=new GmailProcessor();
				
				// waiting for reload
				while(EmailServlets.reloadInProgress) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				gmailProcessor.setCredentials((String)this.configurations.getMailConfigurations().get(configurationId).get("username"), (String)this.configurations.getMailConfigurations().get(configurationId).get("password"));
				retval=gmailProcessor.send(from, to, cc, bcc, subject, mailBody, isUnicode);
				break;
			case ZOHO_KEY:
				LogWriter.LOGGER.info(ZOHO_KEY+" Sender");
				ZmailProcessor zmailProcessor=new ZmailProcessor();
				// waiting for reload
				while(EmailServlets.reloadInProgress) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				zmailProcessor.setCredentials((String)this.configurations.getMailConfigurations().get(this.configurations.getMailMappings().get(from.getAddress())).get("username"), (String)this.configurations.getMailConfigurations().get(this.configurations.getMailMappings().get(from.getAddress())).get("password"));
				retval=zmailProcessor.send(from, to, cc, bcc, subject, mailBody, isUnicode);
				break;
			default:
				LogWriter.LOGGER.info("From email address is not in mail server mapping list");
				retval.replace("ErrorCode", "-6"); retval.replace("ErrorMessage", "From email address is not in mail server mapping list");
				break;
			}
		}
		return retval;
	}
}
