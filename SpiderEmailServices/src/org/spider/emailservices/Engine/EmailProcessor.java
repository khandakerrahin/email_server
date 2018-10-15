package org.spider.emailservices.Engine;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.spider.emailservices.Logs.LogWriter;

public class EmailProcessor {
	protected Properties properties;
	Session session;
	String username,password;
	public EmailProcessor() {
		session=null;
		properties = new Properties();
	}
	public void setProperties(String smtpHost,boolean starttlsEnable, int port, boolean needAuthentication) {
        properties.put("mail.smtp.starttls.enable", starttlsEnable?"true":"false");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", port+"");
        properties.put("mail.smtp.auth", needAuthentication?"true":"false");
        //mail.mime.charset
        properties.put("mail.debug", "true");
        properties.put("mail.debug.auth", "true");
	}
	public Session authenticateSession() {
		this.session= Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
            	return new PasswordAuthentication(username, password);
            }
        });
		return session;
	}
	public Session getActiveSession() {
		if(this.session==null) {
			authenticateSession();
		}
		/* this.session.getStore;
		 * Throws:
    		NoSuchProviderException - If a provider for the given protocol is not found.
    	*/
		return this.session;
	}
	/**
	 * almost useless. use getActiveSession() instead
	 * @return session
	 */
	public Session getSession() {
		return this.session;
	}
	public void setCredentials(String username, String password) {
		this.username=username;
		this.password=password;
	}
	public HashMap<String,String> send(InternetAddress from, InternetAddress[] to, InternetAddress[] cc,
			InternetAddress[] bcc, String subject, String mailBody,boolean isUnicode) {
		HashMap<String, String> retval=new HashMap<>();
		retval.put("ErrorCode", "-1"); retval.put("ErrorMessage",null);
		try {
            //Creating a Message object to set the email content
            MimeMessage msg = new MimeMessage(this.getActiveSession());
            msg.setFrom(from);
            msg.setSender(from);
            msg.setReplyTo(InternetAddress.parse(from.getAddress(), true));
            //Setting the recepients
            msg.setRecipients(Message.RecipientType.TO, to);
            msg.setRecipients(Message.RecipientType.CC, cc);
            msg.setRecipients(Message.RecipientType.BCC, bcc);
//            String timeStamp = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());
            msg.setSentDate(new Date());
            
            if(isUnicode) {
            	msg.setSubject(subject,"UTF-8");
            	msg.setContent(mailBody,"text/html; charset=utf-8");
            }
            else{
            	msg.setSubject(subject);
            	msg.setText(mailBody);
            }
            
            msg.setHeader("XPriority", "1");
            Transport.send(msg);
            
            retval.replace("ErrorCode", "0");
            retval.replace("ErrorMessage", "Mail sent to mail server "+properties.getProperty("mail.smtp.host")+" successfully");
            System.out.println("Mail has been sent successfully");
        } catch (MessagingException mex) {
            LogWriter.LOGGER.severe("Unable to send an email" + mex);
        } 
		return retval;
	}
	@SuppressWarnings("unused")
	private void setAttachment(MimeMessage message) throws MessagingException {
		//TODO Allow attachments in mail body
		BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("This is message body");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new MimeBodyPart();
        String filename = "filepath";
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);
	}
}
