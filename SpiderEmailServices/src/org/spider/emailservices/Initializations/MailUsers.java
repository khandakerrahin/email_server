package org.spider.emailservices.Initializations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Logs.LogWriter;

public class MailUsers {

	public HashMap<String, String> map;

	public MailUsers() {
		map=new HashMap<>();
		
	}
	public void getMailUsers(PostalServicesDS dsConnection) {
		try {
			String sql="SELECT APP_NAME,PASSWORD FROM EMAIL_USERS WHERE STATUS=10";
			dsConnection.prepareStatement(sql);
			ResultSet rs = dsConnection.executeQuery();
			map.clear();
			while (rs.next()) {
				map.put(rs.getString("APP_NAME"),rs.getString("PASSWORD"));
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
