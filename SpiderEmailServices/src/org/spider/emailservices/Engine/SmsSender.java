/**
 * 
 */
package org.spider.emailservices.Engine;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.spider.emailservices.Logs.LogWriter;

/**
 * @author hafiz
 *
 */
public class SmsSender {
	String destinationName;
	String clientId;
	String target;
	String LoadConf;
	String reply;
	String action;
	String userId;
	LogWriter logWriter;
	/**
	 * 
	 */
	public SmsSender(LogWriter logWriter) {
		this.destinationName="bubble.fyi";
		this.clientId="fees.school";
		this.target="ENGINE";
		this.LoadConf="N";
		this.reply="true";
		this.action="sendSMS";
		this.userId="18";
		this.logWriter=logWriter;
		this.logWriter.appendLog("SmsSender"+";action:"+this.action+";userId:"+this.userId);
	}
	/**
	 * 
	 * @param msisdn
	 * @param smsText
	 * @return 0 is success in sending to SMSC. Anything else is error.
	 * 
	 */
	public String send(String msisdn, String smsText) {
		this.logWriter.appendAdditionalInfo(msisdn+":"+smsText);
		return sendMSGtoQueue(msisdn, smsText, true);
	}
	private String sendMSGtoQueue(String msisdn, String smsText, boolean responseNeeded){
		String retval="-1";
		////    destinationName=bubble.fyi&destinationType=queue&clientid=bubble.fyi&target=ENGINE&LoadConf=N&message={"id":"18","msisdn":"88019XXXX","smsText":"this is first text from bubble!!!"}&reply=true&action=sendSMS
		Destination tmpQueue = null;
		try {
			InitialContext iniCtx = new InitialContext();
			Object tmp = iniCtx.lookup("ConnectionFactory");

			QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
			QueueConnection QueueConn = qcf.createQueueConnection();
			Queue queue = (Queue) iniCtx.lookup("java:/queue/" + this.destinationName); //java:/jms/queue/bubble.fyi java:/queue/bubble.fyi
			QueueSession qSession = QueueConn.createQueueSession(false,	QueueSession.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = qSession.createProducer(queue);
			MapMessage newMsg = qSession.createMapMessage();
			MapMessage msg = generateMapMessage(msisdn, smsText, newMsg);
			//		boolean force = InhouseLogger.isTraceForced(msg);

			if (responseNeeded) {
				tmpQueue = qSession.createTemporaryQueue();
				msg.setJMSReplyTo(tmpQueue);
				LogWriter.LOGGER.info("SMS Temporary Queue is created");
			}
			QueueConn.start();

			messageProducer.send(msg);

			if (responseNeeded) {
				MessageConsumer messageConsumer = qSession.createConsumer(tmpQueue);

				msg = (MapMessage) messageConsumer.receive(30000);
				messageConsumer.close();
				if (msg != null) {
					String response=msg.getString("returnTxt");
					LogWriter.LOGGER.info("SendSMS:"+response);
					if(response.startsWith("0")) {
						retval="0";
						this.logWriter.appendLog("sendSms:"+retval);
						this.logWriter.setStatus(1);
					}else {
						retval="-2";
						this.logWriter.appendLog("sendSms:"+retval);
						this.logWriter.setStatus(0);
					}
				} else {
					retval="-3";
					LogWriter.LOGGER.severe("Timed out waiting for reply");
					this.logWriter.appendLog("sendSms:"+retval);
					this.logWriter.setStatus(0);
				}

			}else{
				//do nothing

			}
			// sessionTmp.close();
			messageProducer.close();
			qSession.close();

			QueueConn.stop();
			QueueConn.close();
			iniCtx.close();
		}catch(NamingException | JMSException ex){
			retval="E";
			LogWriter.LOGGER.severe(ex.getStackTrace().toString());
			this.logWriter.appendLog("sendSmsJMS:"+retval);
			this.logWriter.setStatus(0);
		}catch(Exception e) {
			retval="E";
			LogWriter.LOGGER.severe(e.getStackTrace().toString());
			this.logWriter.appendLog("sendSms:"+retval);
			this.logWriter.setStatus(0);
		}
		return retval;
	}

	public MapMessage generateMapMessage(String msisdn, String smsText,MapMessage msg) throws JMSException {
		String body="";
		msg.setString("body", body);
		////    destinationName=bubble.fyi&destinationType=queue&clientid=bubble.fyi&target=ENGINE&LoadConf=N&message={"id":"18","msisdn":"88019XXXX","smsText":"this is first text from bubble!!!"}&reply=true&action=sendSMS
		String message="{\"id\":\""+this.userId+"\",\"msisdn\":\""+msisdn+"\",\"smsText\":\""+smsText+"\"}";
		msg.setString("message", message);

		msg.setStringProperty("target",this.target);

		msg.setBoolean("traceON", false);

		msg.setString("destination", "httpService");
		msg.setString("action", this.action);
		msg.setString("clientid", this.clientId);
		msg.setString("reply", this.reply);
		msg.setString("LoadConf", this.LoadConf);
		return msg;
	}

}
