package org.spider.emailservices.Initializations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Logs.LogWriter;

public class MailMappings {

	public HashMap<String, String> map;

	public MailMappings() {
		map=new HashMap<>();
		
	}
	public void getMailMappings(PostalServicesDS dsConnection) {
		try {
			String sql="SELECT FROM_ADDRESS,CONFIGURATION_ID FROM MAIL_MAPPING WHERE STATUS=1";
			dsConnection.prepareStatement(sql);
			ResultSet rs = dsConnection.executeQuery();
			map.clear();
			while (rs.next()) {
				map.put(rs.getString("FROM_ADDRESS"),rs.getString("CONFIGURATION_ID"));
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
