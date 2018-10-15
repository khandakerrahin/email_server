package org.spider.emailservices.recieverServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Initializations.Configurations;
import org.spider.emailservices.Logs.LogWriter;
import org.spider.emailservices.MDB.PostalServiceMDB;
import org.spider.emailservices.Utilities.NullPointerExceptionHandler;
import org.spider.emailservices.user.UserOperations;

import com.google.gson.Gson;

//import httpPoster.GenerateEmail;

/**
 * Servlet implementation class EmailServlet
 */
@WebServlet("/EmailServlets")
public class EmailServlets extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public Configurations loadConf = new Configurations();
	private static final Logger LOGGER = Logger.getLogger(EmailServlets.class.getName()); 
	private static final long TWELVE_HOURS = 12 * 10 * 60 * 1000;
	private static long confUpdateTime = System.currentTimeMillis() - TWELVE_HOURS - 1000;
	long twelveAgo = System.currentTimeMillis() - TWELVE_HOURS;
	LogWriter logWriter;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmailServlets() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
        //NiharekahS
		PrintWriter pw = response.getWriter();
		
		try{
			//GenerateEmail ge = new GenerateEmail(request);
			//ge.generate();
			PostalServicesDS dsConn=new PostalServicesDS();
			twelveAgo = System.currentTimeMillis() - TWELVE_HOURS;
			if (confUpdateTime < twelveAgo) {
				LOGGER.info("confUpdateTime is older than 12 hours");
				LOGGER.info("Loading configuration from DB ..");
				loadConf.loadConfigurationFromDB();
				confUpdateTime = System.currentTimeMillis();
				LOGGER.info("Loading configuration from DB complete.");
			}
			else{
				LOGGER.info("confUpdateTime is less than 12 hours");
			}
			LOGGER.info("initiating mail service.");
			String resp = processNewRequest(request,true);
			pw.println("request sent successfully.");
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.severe(ex.getMessage());
		}
		// http://localhost:8080/EmailTemplateGenerator/EmailServlet?name=Rahin&cardType=VISA&cardNumber=2441139&amount=237&merchantBankID=1193&transactionType=purchase&conversionRate=1.04&ipAddress=1.2.3.4&billingAddress=Spider%20Digital&phoneNumber=2441139&email=shaker@spiderdxb.com
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public String processNewRequest(HttpServletRequest request, boolean forceLogWrite){
		HashMap<String, String> retval=new HashMap<>();
		retval.put("ErrorCode", "-1"); retval.put("ErrorMessage",null);
		//request example  
		//login https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=fees.school&destinationType=queue&clientid=fees.school&target=ENGINE&LoadConf=N&message={%20%22username%22:%22t1@sp.com%22,%20%22password%22:%22specialt1pass%22,%20%22mode%22:%221%22}&reply=true&action=login
		//school registration: https://localhost:8443/HttpReceiver/HttpReceiver?destinationName=fees.school&destinationType=queue&clientid=fees.school&target=ENGINE&LoadConf=N&message={%20%22schoolName%22:%22Skola%201%22,%22email%22:%22spiderco@sdxb.com%22,%22phone%22:%228801912345678%22,%22password%22:%22spidercom%22,%22custodianName%22:%22SpiderCom%22,%22address%22:%2210A%20Dhanmondi%22,%22city%22:%22Dhaka%22,%22postcode%22:%221209%22}&reply=true&action=registerSchool
		this.logWriter=new LogWriter(forceLogWrite);
		String message 	=	NullPointerExceptionHandler.isNullOrEmpty((String) request.getParameter("message"))?"":(String) request.getParameter("message");
		String messageBody="";//=	NullPointerExceptionHandler.isNullOrEmpty((String) request.getParameter("body"))?"":(String) request.getParameter("body");
		String action=	NullPointerExceptionHandler.isNullOrEmpty((String) request.getParameter("action"))?"":(String) request.getParameter("action");
		Gson gson = new Gson();
		 
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            messageBody = sb.toString();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		LogWriter.LOGGER.info("Message :"+message);
		LogWriter.LOGGER.info("Message Body :"+messageBody);
		PostalServicesDS dsConn=new PostalServicesDS();
		String retVal=null;
		try {
			switch(action.toUpperCase()) {
			case "SENDEMAIL":
				retval=new UserOperations(dsConn.con,this.logWriter,loadConf).sendEmail(message,messageBody);
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
				this.logWriter.flush(dsConn);
				try {
					dsConn.getConnection().close();
				} catch (SQLException e) {
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}
		}
		
		return retVal;
	}

}