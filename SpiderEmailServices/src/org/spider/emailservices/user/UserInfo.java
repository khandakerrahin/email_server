/**
 * 
 */
package org.spider.emailservices.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.spider.emailservices.DataSources.PostalServicesDS;
//import org.spider.emailservices.Engine.JsonEncoder;
import org.spider.emailservices.Logs.LogWriter;

/**
 * @author hafiz
 */
public class UserInfo {
	PostalServicesDS dsConn;
	LogWriter logWriter;
	/**
	 * 
	 */
	public UserInfo(PostalServicesDS dsConn,LogWriter logWriter) {
//		dsConn= new PostalServicesDS(); 
		this.dsConn=dsConn;
		this.logWriter=logWriter;
	}

	/**
	 * 
	 * @param id
	 * @param mode 1:email, 2:phone
	 * @return jsonEncoder userInfo on success
	 * <br>errorCode 0 indicated success in fetching data
	 * <br>-1:General Error
	 * <br>-2:SQLException
	 * <br>-3:Exception
	 * <br>-4:SQLException while closing
	 */
	public HashMap <String,Object> fetchUserInfo(String id, String mode) {
		HashMap <String,Object> retval=new HashMap<>(2,1.0f);
		retval.put("ErrorCode", "-1");retval.put("ErrorMessage","General Error ");
		String errorCode="-1";//default errorCode
		String sql="SELECT u.user_id, u.user_name, o.organization_name, case when u.user_email is null then '' else u.user_email end as user_email, u.user_type, u.phone, u.status, o.custodian_email,o.custodian_name,o.custodian_phone,o.organization_type,o.address,o.city,o.postcode FROM users u left join organizations o on u.user_id=o.user_id where u.<mode>=?";
		
		try {
			dsConn.prepareStatement(sql);
			dsConn.getPreparedStatement().setString(1, id);
			ResultSet rs = dsConn.executeQuery();
			if (rs.next()) {
				retval.put("id", rs.getString("user_id"));
				retval.put("username", rs.getString("user_name"));
				retval.put("email", rs.getString("user_email"));
				retval.put("phoneNumber", rs.getString("phone"));
				retval.put("userType", rs.getString("user_type"));
				retval.put("status", rs.getString("status"));
				if(rs.getString("user_type").equalsIgnoreCase("Admin")) {
					retval.put("schoolName", rs.getString("organization_name"));
					retval.put("custodianEmail", rs.getString("custodian_email"));
					retval.put("custodianName", rs.getString("custodian_name"));
					retval.put("custodianPhone", rs.getString("custodian_phone"));
					retval.put("organisationType", rs.getString("organization_type"));
					retval.put("address", rs.getString("address"));
					retval.put("city", rs.getString("city"));
					retval.put("postcode", rs.getString("postcode"));
				}
				errorCode="0";
				retval.replace("ErrorCode", errorCode);
				retval.replace("ErrorMessage","Successfully fetched.");
				this.logWriter.setStatus(1);
				this.logWriter.appendLog("fu:S");
			}else {
				errorCode="-9";
				retval.replace("ErrorCode", errorCode);
				retval.replace("ErrorMessage", "User details could not be retrieved");
				this.logWriter.setStatus(0);
				this.logWriter.appendLog("fu:F");
			}
			dsConn.closeResultSet();
			dsConn.closePreparedStatement();
		}catch(SQLException e){
			errorCode= "-2";
			retval.replace("ErrorCode", errorCode);
			retval.replace("ErrorMessage","SQLException: "+e.getMessage());
			this.logWriter.setStatus(0);
			this.logWriter.appendLog("fu:SE");
			LogWriter.LOGGER.severe(e.getMessage());
			this.logWriter.appendAdditionalInfo("UserInfo.fetchUserInfo():"+e.getMessage());
		}catch(Exception e){
			errorCode= "-3";
			retval.replace("ErrorCode", errorCode);
			retval.replace("ErrorMessage","Exception: "+e.getMessage());
			this.logWriter.setStatus(0);
			this.logWriter.appendLog("fu:E");
			LogWriter.LOGGER.severe(e.getMessage());
			this.logWriter.appendAdditionalInfo("UserInfo.fetchUserInfo():"+e.getMessage());
		}

		return retval;
	}

}
