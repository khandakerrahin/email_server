package org.spider.emailservices.Initializations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Logs.LogWriter;

public class MailConfigurations {

	public HashMap<String, HashMap<String,Object>> properties;
	public MailConfigurations() {
		properties=new HashMap<>(2,1.0f);
	}
	public void getMailConfigurations(PostalServicesDS dsConnection) {
		try {
			
			String sql="SELECT m.id,gateway,username, passwd,configurations "
					+ "FROM mail_configurations  m, email_gateways g where m.gateway=g.ID and m.status=1";
			
			
			// v1
			//String sql="SELECT m.id,gateway,username, cast(aes_decrypt(password, UNHEX(SHA2('"+SecretKey.SECRETKEY+"',512))) as CHAR) as password,configurations "
			//		+ "FROM `PostalServices`.mail_configurations  m, `PostalServices`.email_gateways g where m.gateway=g.ID and m.status=1";
			
			
			dsConnection.prepareStatement(sql);
			System.out.println("sql : "+sql);
			ResultSet rs = dsConnection.executeQuery();
			properties.clear();
			while (rs.next()) {
				HashMap<String,Object> property=new HashMap<>(3,1.0f);
				property.put("username", rs.getString("username"));
				property.put("password", rs.getString("passwd"));
				property.put("gateway", rs.getString("gateway"));
				property.put("configurations", rs.getString("configurations"));
				properties.put(rs.getString("id"), property );
			}
		}catch(Exception e){
			e.printStackTrace();
			LogWriter.LOGGER.severe(e.getMessage());
		}
		finally{
			if(dsConnection.con != null){
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
