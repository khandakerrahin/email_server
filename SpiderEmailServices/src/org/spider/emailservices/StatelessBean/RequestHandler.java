package org.spider.emailservices.StatelessBean; 

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import com.google.gson.Gson;

import javax.annotation.PostConstruct; 
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Initializations.Configurations;
import org.spider.emailservices.Logs.LogWriter;
import org.spider.emailservices.Utilities.NullPointerExceptionHandler;
import org.spider.emailservices.user.UserOperations;



/**
 * Session Bean implementation class
 * @author hafiz
 * @implements RequestHandlerLocal session interface.
 * @see RequestHandlerLocal
 */
@Stateless
@Startup
//@Interceptors(value = org.Spider.Utility.monitor.MessageMonitor.class)
public class RequestHandler implements RequestHandlerLocal {

	public Configurations configurations;
	LogWriter logWriter;
	String replyAddr="spiderpostbox";
	String appName="SpiderPostalServices";
	String channel;
	public RequestHandler() {

	}

	@PostConstruct
	public void loadConfiguration() {
		try {

		} catch (Exception ex) {
			LogWriter.LOGGER.severe(ex.getMessage());
		}
	}

	/**
	 * Implementation method to process new request.
	 * @param msg
	 * @param configurations
	 * @param force
	 * @return The result of the processing. String value.
	 * @throws JMSException
	 * @throws Exception
	 * @see RequestHandlerLocal
	 */
	@Override
	public String processNewRequest(MapMessage msg, Configurations configurations, boolean forceLogWrite) throws JMSException, Exception  {
		HashMap<String, String> retval=new HashMap<>();
		retval.put("ErrorCode", "-1"); retval.put("ErrorMessage",null);
		//request example  
		//login https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=fees.school&destinationType=queue&clientid=fees.school&target=ENGINE&LoadConf=N&message={%20%22username%22:%22t1@sp.com%22,%20%22password%22:%22specialt1pass%22,%20%22mode%22:%221%22}&reply=true&action=login
		//school registration: https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=fees.school&destinationType=queue&clientid=fees.school&target=ENGINE&LoadConf=N&message={%20%22schoolName%22:%22Skola%201%22,%22email%22:%22spiderco@sdxb.com%22,%22phone%22:%228801912345678%22,%22password%22:%22spidercom%22,%22custodianName%22:%22SpiderCom%22,%22address%22:%2210A%20Dhanmondi%22,%22city%22:%22Dhaka%22,%22postcode%22:%221209%22}&reply=true&action=registerSchool
		this.logWriter=new LogWriter(forceLogWrite);
		String message 	=	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("message"))?"":msg.getString("message");
		String action 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("action")) ?"":msg.getString("action");
		String messageBody=	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("body"))?"":msg.getString("body");
		String isTest 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("isTest"))?"":msg.getString("isTest");
		String src   	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("src"))?"":msg.getString("src");
		String target  	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("target"))?"":msg.getString("target");
		String traceON 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("traceON"))?"":msg.getString("traceON");
		String channel 	= 	NullPointerExceptionHandler.isNullOrEmpty(msg.getString("channel"))?"default":msg.getString("channel");
		boolean isTestEnv = false;
		this.logWriter.setChannel(channel);
		this.logWriter.setTarget(target);
//		this.logWriter.setSource(src);
		this.logWriter.setAction(action);
		this.logWriter.setStatus(0);
		this.logWriter.appendInputParameters("m:"+message);
		this.logWriter.appendInputParameters("b:"+messageBody);
		this.logWriter.appendInputParameters("u:"+ (NullPointerExceptionHandler.isNullOrEmpty(msg.getString("userId"))?"":msg.getString("userId")));
		this.logWriter.setChannel(channel);
		this.logWriter.setTarget(target);
		this.configurations = configurations;
		LogWriter.LOGGER.info("Message :"+message);
		LogWriter.LOGGER.info("Message Body :"+messageBody);
		PostalServicesDS dsConn=new PostalServicesDS();
		String retVal=null;
		try {
			switch(action.toUpperCase()) {
			case "SENDEMAIL":
				retval=new UserOperations(dsConn.con,this.logWriter,this.configurations).sendEmail(message,messageBody);
				break;
			default:
				retval.replace("ErrorCode", "-9"); retval.replace("ErrorMessage","Invalid action");
				break;
			}
		}
		finally{
			if(dsConn.getConnection() != null){
				try { this.logWriter.setStatus(Integer.parseInt(retval.get("ErrorCode"))); }catch(NumberFormatException nfe) {}
				retVal=new Gson().toJson(retval);
				this.logWriter.setResponse(retVal);
				LogWriter.LOGGER.info("retVal"+retVal);
				// this.logWriter.flush(dsConn);
				this.logWriter.flush();
				try {
					dsConn.getConnection().close();
				} catch (SQLException e) {
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Gets the date today T + i days
	 * @param tPlusD
	 * @return date in yyyyMMdd format
	 */
	public String getDate(int tPlusD) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, tPlusD);
		java.util.Date dt = cal.getTime();
		SimpleDateFormat sdm = new SimpleDateFormat("yyyyMMdd");
		String datePart = sdm.format(dt);
		return  datePart;
	}
}
