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
			
			String sql="SELECT M.ID,GATEWAY,USERNAME, PASSWD,CONFIGURATIONS FROM MAIL_CONFIGURATIONS  M, EMAIL_GATEWAYS G WHERE M.GATEWAY=G.ID AND M.STATUS=1";
			
			
			// v1
			//String sql="SELECT m.id,gateway,username, cast(aes_decrypt(password, UNHEX(SHA2('"+SecretKey.SECRETKEY+"',512))) as CHAR) as password,configurations "
			//		+ "FROM `PostalServices`.mail_configurations  m, `PostalServices`.email_gateways g where m.gateway=g.ID and m.status=1";
			
			LogWriter.LOGGER.info("executing PS");
			dsConnection.prepareStatement(sql);
			LogWriter.LOGGER.info("executing Q");
			ResultSet rs = dsConnection.executeQuery();
			properties.clear();
			LogWriter.LOGGER.info("cleared properties.");
			while (rs.next()) {
				HashMap<String,Object> property=new HashMap<>(3,1.0f);
				property.put("username", rs.getString("USERNAME"));
				property.put("password", rs.getString("PASSWD"));
				property.put("gateway", rs.getString("GATEWAY"));
				property.put("configurations", rs.getString("CONFIGURATIONS"));
				properties.put(rs.getString("ID"), property );
			}
			LogWriter.LOGGER.info("Done.");
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
