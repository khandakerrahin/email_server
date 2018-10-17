package org.spider.emailservices.user;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.spider.emailservices.Engine.EmailSender;
import org.spider.emailservices.Initializations.Configurations;
import org.spider.emailservices.Logs.LogWriter;
import org.spider.emailservices.Utilities.NullPointerExceptionHandler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class UserOperations {
	private LogWriter logWriter;
	private Connection con;
	private Configurations configurations;
	public UserOperations(Connection con, LogWriter logWriter, Configurations configurations) {
		this.con=con;
		this.logWriter=logWriter;
		this.configurations=configurations;
	}
	
	public HashMap<String, String> verifyUser(String message, String messageBody) {
		Map<String, Object> mb;
		if(NullPointerExceptionHandler.isNullOrEmpty(messageBody))
			mb = new Gson().fromJson((String)message, new TypeToken<HashMap<String, Object>>() {}.getType());
		else {
			try {
				mb = new Gson().fromJson((String)messageBody, new TypeToken<HashMap<String, Object>>() {}.getType());
			}catch(com.google.gson.JsonSyntaxException e) {

				try {
					mb = new Gson().fromJson((String)URLDecoder.decode(messageBody, "UTF-8"), new TypeToken<HashMap<String, Object>>() {}.getType());
				} catch (JsonSyntaxException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-13"); retval.put("ErrorMessage","JSON syntax error. JsonSyntaxException");
					return retval;
				} catch (UnsupportedEncodingException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-14"); retval.put("ErrorMessage","JSON syntax error. JsonSyntaxException & UnsupportedEncodingException");
					return retval;
				}

			}catch(java.lang.IllegalStateException e) {
				try {
					mb = new Gson().fromJson((String)URLDecoder.decode(messageBody, "UTF-8"), new TypeToken<HashMap<String, Object>>() {}.getType());
				} catch (JsonSyntaxException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-13"); retval.put("ErrorMessage","JSON syntax error. JsonSyntaxException");
					return retval;
				} catch (UnsupportedEncodingException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-14"); retval.put("ErrorMessage","JSON syntax error. IllegalStateException & UnsupportedEncodingException");
					return retval;
				}
			}
		}
		
		String uname = mb.containsKey("uname")?(String)mb.get("uname"):"";
		String upass = mb.containsKey("upass")?(String)mb.get("upass"):"";
		
		String appPass = this.configurations.getMailUsers().containsKey(uname)? this.configurations.getMailUsers().get(uname):"";
		
		if(upass.equals(appPass) && appPass!="") {
			System.out.println("Application authentication successful.");
			HashMap<String, String> retval=new HashMap<>(2,1.0f);
			
			try {
				retval.put("ErrorCode", "0");
				retval.put("ErrorMessage","Configuration reloaded");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return retval;
		}
		else {
			HashMap<String, String> retval=new HashMap<>(2,1.0f);
			retval.put("ErrorCode", "-5"); retval.put("ErrorMessage","SpiderEmailServices : User is not authorized to perform this action.");
			return retval;
		}
	}

	public HashMap<String, String> sendEmail(String message, String messageBody) {
		Map<String, Object> mb;
		if(NullPointerExceptionHandler.isNullOrEmpty(messageBody))
			mb = new Gson().fromJson((String)message, new TypeToken<HashMap<String, Object>>() {}.getType());
		else {
			try {
				mb = new Gson().fromJson((String)messageBody, new TypeToken<HashMap<String, Object>>() {}.getType());
			}catch(com.google.gson.JsonSyntaxException e) {

				try {
					mb = new Gson().fromJson((String)URLDecoder.decode(messageBody, "UTF-8"), new TypeToken<HashMap<String, Object>>() {}.getType());
				} catch (JsonSyntaxException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-13"); retval.put("ErrorMessage","JSON syntax error. JsonSyntaxException");
					return retval;
				} catch (UnsupportedEncodingException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-14"); retval.put("ErrorMessage","JSON syntax error. JsonSyntaxException & UnsupportedEncodingException");
					return retval;
				}

			}catch(java.lang.IllegalStateException e) {
				try {
					mb = new Gson().fromJson((String)URLDecoder.decode(messageBody, "UTF-8"), new TypeToken<HashMap<String, Object>>() {}.getType());
				} catch (JsonSyntaxException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-13"); retval.put("ErrorMessage","JSON syntax error. JsonSyntaxException");
					return retval;
				} catch (UnsupportedEncodingException e1) {
					HashMap<String, String> retval=new HashMap<>();
					retval.put("ErrorCode", "-14"); retval.put("ErrorMessage","JSON syntax error. IllegalStateException & UnsupportedEncodingException");
					return retval;
				}
			}
		}
		boolean isUnicode;
		//		isUnicode=mb.containsKey("isUnicode")?(mb.get("isUnicode").equalsIgnoreCase("true") || mb.get("isUnicode").equalsIgnoreCase("y") || mb.get("isUnicode").equalsIgnoreCase("yes") || mb.get("isUnicode").equals("1")  ):false;
		if(mb.containsKey("isUnicode")) {
			try {
				if(mb.get("isUnicode") instanceof String) {
					if(((String)mb.get("isUnicode")).equalsIgnoreCase("true") || ((String)mb.get("isUnicode")).equalsIgnoreCase("y") || ((String)mb.get("isUnicode")).equalsIgnoreCase("yes") || mb.get("isUnicode").equals("1")  )
						isUnicode=true;
					else isUnicode=false;
				}else {
					try {
						isUnicode=mb.get("isUnicode")!=null?(boolean) mb.get("isUnicode"):false;
					}catch (ClassCastException e) {
						isUnicode=false;
					}
				}
			}catch(Exception e) {
				isUnicode=false;
			}
		}else
			isUnicode=false;
		String uname = mb.containsKey("uname")?(String)mb.get("uname"):"";
		String upass = mb.containsKey("upass")?(String)mb.get("upass"):"";
		
		String appPass = this.configurations.getMailUsers().containsKey(uname)? this.configurations.getMailUsers().get(uname):"";
		
		if(upass.equals(appPass) && appPass!="") {
			System.out.println("Application authentication successful.");
			return (new EmailSender(con,logWriter,configurations)).send(mb.containsKey("from")?(String)mb.get("from"):"", mb.containsKey("to")?(String)mb.get("to"):"", mb.containsKey("cc")?(String)mb.get("cc"):"", mb.containsKey("bcc")?(String)mb.get("bcc"):"", mb.containsKey("subject")?(String)mb.get("subject"):"", mb.containsKey("mailBody")?(String)mb.get("mailBody"):"",isUnicode );
		}
		else {
			System.out.println("Application authentication failed.");
			HashMap<String, String> retval=new HashMap<>(2,1.0f);
			retval.put("ErrorCode", "-5"); retval.put("ErrorMessage","SpiderEmailServices : Application authentication failed.");
			return retval;
		}
	}
}
