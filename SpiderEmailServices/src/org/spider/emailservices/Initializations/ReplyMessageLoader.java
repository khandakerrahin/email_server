package org.spider.emailservices.Initializations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Logs.LogWriter;

//import org.Banglalink.InhouseUtility.dataSource.DSInfo;
//import org.Banglalink.InhouseUtility.dataSource.InhouseDataFiller;
//import org.Banglalink.InhouseUtility.dataSource.MultiDataSource;
// 


public class ReplyMessageLoader{
	public HashMap<String,String> replyMessage;
	public ReplyMessageLoader() {
		replyMessage = new HashMap<String, String>();
	}

	/**
	 * getRelpyMessage:: Upload reply SMS table in HASH during application UP.
	 */
	public void getRelpyMessage(PostalServicesDS dsConnection) {
		try {
			String sql="SELECT ID,SMS_TEXT,SCHOOL_ID,STATUS,DESCRIPTION FROM SMS_TEXTS";
			dsConnection.prepareStatement(sql);
			ResultSet rs = dsConnection.executeQuery();
			replyMessage.clear();
			while (rs.next()) {
				replyMessage.put(rs.getString("ID")+"-"+rs.getString("SCHOOL_ID"), rs.getString("SMS_TEXT"));
			}
		}catch(Exception e){
			LogWriter.LOGGER.severe(e.getMessage());
		}
		finally{
			if(dsConnection.getConnection() != null){
				try {
					dsConnection.closeResultSet();
				} catch (SQLException e) {
					LogWriter.LOGGER.severe(e.getMessage());
				}
				try {
					dsConnection.closePreparedStatement();
				} catch (SQLException e) {
					LogWriter.LOGGER.severe(e.getMessage());
				}
			}      
		}
	}

}