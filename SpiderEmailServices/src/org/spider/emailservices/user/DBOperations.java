/**
 * 
 */
package org.spider.emailservices.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.spider.emailservices.DataSources.PostalServicesDS;
import org.spider.emailservices.Initializations.SecretKey;
import org.spider.emailservices.Logs.LogWriter;



/**
 * @author hafiz
 * update information
 * delete user/organization
 */
public class DBOperations {
	PostalServicesDS fsDS;
	LogWriter logWriter;
	//private static final Logger LOGGER = LogWriter.LOGGER.getLogger(DBOperations.class.getName());
	/**
	 * 
	 */
	public DBOperations(PostalServicesDS fsDS,LogWriter logWriter) {
		this.fsDS = fsDS;
		this.logWriter=logWriter;
	}
	/**
	 * 
	 * @param userId
	 * @param oldPass
	 * @param newPass
	 * @return
	 *  0:Password set
	 *  1:User not found
	 *  2:Current password is invalid
	 *  3:Error encountered while setting password
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 */
	public String modifyPassword(String userId, String oldPass, String newPass) {
		//Modify password
		String retval="-1";
		//	check if old password matches
		//	retrieve old password, keySeed
		String keySeed="",passwd="";
		String sql="select AES_DECRYPT(passwd_enc,concat_ws('',?,key_seed,key_seed,key_seed)) as passwd, key_seed from users where user_id=?";
		try {
			fsDS.prepareStatement(sql);
			fsDS.getPreparedStatement().setString(1, SecretKey.SECRETKEY);
			fsDS.getPreparedStatement().setString(2, userId);
			fsDS.executeQuery();
			if(fsDS.getResultSet().next()) {
				passwd=fsDS.getResultSet().getString(1);
				keySeed=fsDS.getResultSet().getString(2);
			}else {
				retval="1:User not found";
			}
			fsDS.closeResultSet();
			fsDS.closePreparedStatement();

			if(oldPass.equals(passwd)) {
				//proceed to change passwd
				if(setNewPassword(userId,newPass,keySeed,"3")) {
					retval="0:Password set";
					LogWriter.LOGGER.info("New password");
					this.logWriter.setStatus(1);
					this.logWriter.appendLog("s:S");
				}else {
					retval="3:Error encountered while setting password";
					LogWriter.LOGGER.info("Error encountered while setting password.");
					this.logWriter.setStatus(0);
					this.logWriter.appendLog("s:F3");
				}
			}else {
				//password didn't match
				if(retval.startsWith("-1")) {
					retval="2:Current password is invalid";
					LogWriter.LOGGER.info("Current password did not match.");
					this.logWriter.setStatus(0);
					this.logWriter.appendLog("s:F2");
				}
			}
		} catch (SQLException e) {
			retval="-2";
			LogWriter.LOGGER.severe("modifyPassword(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					if(!fsDS.isResultSetClosed()) fsDS.closeResultSet();
					if(!fsDS.isPreparedStatementClosed()) fsDS.closePreparedStatement();
				} catch (SQLException e) {
					retval="-3";
					LogWriter.LOGGER.severe(e.getMessage());
					this.logWriter.appendLog("s:SE");
					this.logWriter.appendAdditionalInfo("UDO.modifyPassword():"+e.getMessage());
				}
			}
		}


		return retval;
	}
	private boolean setNewPassword(String uId,String newPass, String keySeed, String mode) {
		boolean retval=false;
		String userId="";
		String sqlUpdateUser="UPDATE users u set u.user_password=?,u.passwd_enc=AES_ENCRYPT(?,concat_ws('',?,key_seed,key_seed,key_seed)) WHERE <mode>=?";
		if(mode.equals("1")) {//email
			sqlUpdateUser=sqlUpdateUser.replace("<mode>", "user_email");
			userId=uId;
		}else if(mode.equals("2")) {//phone
			sqlUpdateUser=sqlUpdateUser.replace("<mode>", "phone");
//			userId=this.msisdnNormalize(uId);
		}else if(mode.equals("3")) {//userid
			sqlUpdateUser=sqlUpdateUser.replace("<mode>", "user_id");
			userId=uId;
		}else {
			retval=false;
		}
		try {
			fsDS.prepareStatement(sqlUpdateUser);
			fsDS.getPreparedStatement().setString(1, newPass);
			fsDS.getPreparedStatement().setString(2, newPass);
			fsDS.getPreparedStatement().setString(3, SecretKey.SECRETKEY);
			fsDS.getPreparedStatement().setString(4, userId);
			fsDS.execute();
			retval=true;
		} catch (SQLException e) {
			LogWriter.LOGGER.severe("setNewPassword(): "+e.getMessage());
		}finally{
			if(fsDS.getConnection() != null){
				try {
					if(!fsDS.isPreparedStatementClosed()) fsDS.closePreparedStatement();
				} catch (SQLException e) {
					retval=false;
					LogWriter.LOGGER.severe(e.getMessage());
					this.logWriter.appendLog("s:SE");
					this.logWriter.appendAdditionalInfo("UDO.setNewPassword():"+e.getMessage());
				}
			}
		}
		return retval;
	}
	public String changeKeySeed(String userId, String newKeySeed) {
		//TODO Modify keySeed
		String retval="-1";
		//	retrieve oldKeySeed, password
		//	set newKeySeed, password
		return retval;
	}
	/**
	 * 
	 * @param credential
	 * @param password
	 * @param mode
	 * @return
	 * 0:Password verified
	 * 1:User not found
	 * 2:Verificaion failed
	 * 3:Verificaion consistency error
	 * 11:Invalid mode.
	 * -1: General Error
	 * -2: SQLException
	 * -3: SQLException while closing connection
	 */
	public String validatePassword(String credential, String password, String mode) {
		String retval="-1";
		String sql="select count(*) as counter from users where <mode>=?  and passwd_enc=AES_ENCRYPT(?,concat_ws('',?,key_seed,key_seed,key_seed))";
		if(mode.equals("1")) {//email
			sql=sql.replace("<mode>", "user_email");
		}else if(mode.equals("2")) {//phone
			sql=sql.replace("<mode>", "phone");
//			credential=this.msisdnNormalize(credential);
		}else if(mode.equals("3")) {//userid
			sql=sql.replace("<mode>", "user_id");
		}else {
			retval="11:Invalid mode";
		}
		if(retval.startsWith("-1")) {
			String counter="-1";
			try {
				fsDS.prepareStatement(sql);
				fsDS.getPreparedStatement().setString(1, credential);
				fsDS.getPreparedStatement().setString(2, password);
				fsDS.getPreparedStatement().setString(3, SecretKey.SECRETKEY);				
				fsDS.executeQuery();
				if(fsDS.getResultSet().next()) {
					counter=fsDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
				fsDS.closeResultSet();
				fsDS.closePreparedStatement();
				if(counter.equals("1")) {
					retval="0:Password verified";
					this.logWriter.appendLog("vp:S");
				}else if(counter.equals("0")) {
					retval="2:Verificaion failed";
					this.logWriter.appendLog("vp:F");
					this.logWriter.setStatus(0);
				}else {
					retval="3:Verificaion consistency error";
					this.logWriter.appendLog("vp:X");
					this.logWriter.setStatus(0);
				}
			} catch (SQLException e) {
				retval="-2";
				LogWriter.LOGGER.severe("validatePassword(): "+e.getMessage());
				this.logWriter.setStatus(0);
				this.logWriter.appendLog("vp:SE");
				this.logWriter.appendAdditionalInfo("UDO.vPass():"+e.getMessage());
			}finally{
				if(fsDS.getConnection() != null){
					try {
						if(!fsDS.isResultSetClosed()) fsDS.closeResultSet();
						if(!fsDS.isPreparedStatementClosed()) fsDS.closePreparedStatement();
					} catch (SQLException e) {
						retval="-4";
						LogWriter.LOGGER.severe(e.getMessage());
						this.logWriter.appendLog("s:FSE");
						this.logWriter.appendAdditionalInfo("UDO.validatePassword():"+e.getMessage());
					}
				}
			}


		}
		return retval;
	}
	public String getUserType(String userId) {
		String retval="-1";
		String sql="select user_type from users where userId=?";
		if(retval.startsWith("-1")) {
			try {
				fsDS.prepareStatement(sql);
				fsDS.getPreparedStatement().setString(1, userId);
				fsDS.executeQuery();
				if(fsDS.getResultSet().next()) {
					retval=fsDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
			} catch (SQLException e) {
				retval="-2";
				LogWriter.LOGGER.severe("getUserType(): "+e.getMessage());
			}finally{
				if(fsDS.getConnection() != null){
					try {
						if(!fsDS.isResultSetClosed()) fsDS.closeResultSet();
						if(!fsDS.isPreparedStatementClosed()) fsDS.closePreparedStatement();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
						this.logWriter.appendLog("s:SE");
						this.logWriter.appendAdditionalInfo("UDO.getUserType():"+e.getMessage());
					}
				}
			}
		}
		return retval;
	}
	/**
	 * 
	 * @param credential
	 * @param mode
	 * @return userId in the users table
	 * <br>1:User not found, 11:Invalid mode , -1 for unknown error, -2 for SQLException
	 */
	public String getUserId(String credential,String mode) {
		String retval="-1";
		String sql="select user_id from users where <mode>=?";
		if(mode.equals("1")) {//email
			sql=sql.replace("<mode>", "user_email");
		}else if(mode.equals("2")) {//phone
			sql=sql.replace("<mode>", "phone");
//			credential=this.msisdnNormalize(credential);
		}else if(mode.equals("3")) {//userid
			sql=sql.replace("<mode>", "user_id");
		}else {
			retval="11:Invalid mode";
		}
		if(retval.startsWith("-1")) {
			try {
				fsDS.prepareStatement(sql);
				fsDS.getPreparedStatement().setString(1, credential);
				fsDS.executeQuery();
				if(fsDS.getResultSet().next()) {
					retval=fsDS.getResultSet().getString(1);
				}else {
					retval="1:User not found";
				}
			} catch (SQLException e) {
				retval="-2";
				LogWriter.LOGGER.severe("validatePassword(): "+e.getMessage());
			}finally{
				if(fsDS.getConnection() != null){
					try {
						if(!fsDS.isResultSetClosed()) fsDS.closeResultSet();
						if(!fsDS.isPreparedStatementClosed()) fsDS.closePreparedStatement();
					} catch (SQLException e) {
						retval="-3";
						LogWriter.LOGGER.severe(e.getMessage());
						this.logWriter.appendLog("s:SE");
						this.logWriter.appendAdditionalInfo("UDO.getUserId():"+e.getMessage());
					}
				}
			}
		}
		return retval;
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private String getUserId() throws SQLException {
		String retval="-1";
		ResultSet rs=fsDS.getGeneratedKeys();
		if(rs.next()) {
			retval=rs.getString(1);
		}
		rs.close();
		return retval;
	}

}
