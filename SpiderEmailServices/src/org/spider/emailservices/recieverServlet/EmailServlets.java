package org.spider.emailservices.recieverServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Initializations.Configurations;
import org.spider.emailservices.Logs.LogWriter;
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
	public static Boolean reloadInProgress = false;
	LogWriter logWriter;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmailServlets() {
        super();
        LOGGER.info("Loading EmailSender Configs.");
        loadConf.loadConfigurationFromDB();
        LOGGER.info("EmailSender Configs loaded.");
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
			//PostalServicesDS dsConn=new PostalServicesDS();
			LOGGER.info("Mail request recieved.");
			LOGGER.info("Initiating mail service.");
			
			String resp = processNewRequest(request,true);
			pw.println(resp);
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
		PostalServicesDS dsConn=new PostalServicesDS();
		//request example  
		
		this.logWriter=new LogWriter(forceLogWrite,dsConn);
		String message 	=	NullPointerExceptionHandler.isNullOrEmpty((String) request.getParameter("message"))?"":(String) request.getParameter("message");
		String messageBody="";//=	NullPointerExceptionHandler.isNullOrEmpty((String) request.getParameter("body"))?"":(String) request.getParameter("body");
		String action=	NullPointerExceptionHandler.isNullOrEmpty((String) request.getParameter("action"))?"":(String) request.getParameter("action");
		 
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
		//LogWriter.LOGGER.info("Message :"+message);
		//LogWriter.LOGGER.info("Message Body :"+messageBody);
		String retVal=null;
		
		try {
			switch(action.toUpperCase()) {
			case "SENDEMAIL":
				retval=new UserOperations(dsConn.con,this.logWriter,loadConf).sendEmail(message,messageBody);
				break;
			case "RELOADCONFIG":
				retval=new UserOperations(dsConn.con,this.logWriter,loadConf).verifyUser(message,messageBody);
				if(Integer.parseInt(retval.get("ErrorCode"))==0) {
					LogWriter.LOGGER.info("retVal: "+retVal);
					LogWriter.LOGGER.info("reloading configurations");
					reloadConfig();
				}
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
				this.logWriter.setAction(action);
				this.logWriter.appendInputParameters(messageBody);
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

	private void reloadConfig() {
		LOGGER.info("Reloading configuration from DB ..");
		if(loadConf.reloadConf()) {
			reloadInProgress = true;
			try{
				loadConf.switchConf();
			}finally {
				reloadInProgress = false;
			}
		}
		LOGGER.info("Reloading configuration from DB complete.");
	}
}
