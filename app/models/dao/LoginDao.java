package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;

import play.Logger;
import play.db.DB;

/**
 * Performs basic login functionalities like creating a new account, validating login etc.
 * 
 * @author sprasa4
 *
 */
public class LoginDao {
	
	// TODO : Should use some connection pooling mechanism and get connection from that. This is
	// very a crude approach.
	private static Connection dbConn = null;

	private static String LOGIN_TABLE = "product_match_db.login";	

	public LoginDao()
	{
		DataSource ds = DB.getDataSource();
		try {
			dbConn = ds.getConnection();
		} catch (SQLException e) {
			Logger.error("Failed to get connection to database.");
			System.exit(1);
		}
	}
	
	/**
	 * Creates a new login.
	 * @param loginName
	 * @param loginPassword
	 * @return
	 */
	public boolean createLogin(String loginName, String loginPassword)
	{
		boolean doesLoginAlreadyExist = doesLoginAlreadyExist(loginName);
		if(doesLoginAlreadyExist) {
			return false;
		}
		
		// Don't store plaintext password in database. Encrypt it using MD5 or SHA algorithms.
		String md5HashPassword = DigestUtils.md5Hex(loginPassword);
		
		boolean isAccountCreationDone = true;
		String loginSql = "INSERT INTO " + LOGIN_TABLE + " (name, password) VALUES (?, ?) ";
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(loginSql);
			preparedSql.setString(1, loginName);
			preparedSql.setString(2, md5HashPassword);
			preparedSql.executeUpdate();
		} catch (SQLException e) {
			isAccountCreationDone = false;
			Logger.error("Failed to create account for user : " + loginName);
			e.printStackTrace();
		}
		
		return isAccountCreationDone;
	}
	
	/**
	 * Verifies if the user login is valid.
	 * @return
	 */
	public boolean verifyLogin(String loginName, String loginPassword)
	{
		boolean isLoginSuccess = true;
		 
		String md5LoginPassword = DigestUtils.md5Hex(loginPassword);
		String loginSql = "SELECT name, password FROM " + LOGIN_TABLE + " WHERE name = ? AND password = ?";
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(loginSql);
			preparedSql.setString(1, loginName);
			preparedSql.setString(2, md5LoginPassword);
			
			ResultSet rs = preparedSql.executeQuery();
			if(!rs.next()) {
				isLoginSuccess = false;
			}
		} catch (SQLException e) {
			isLoginSuccess = false;
			Logger.error("Failed to login the user : " + loginName);
			e.printStackTrace();
		}
		
		return isLoginSuccess;
	}
	
	/**
	 * Checks if this login name is already present in the database or not.
	 * @param loginName
	 * @return
	 */
	public boolean doesLoginAlreadyExist(String loginName)
	{
		boolean doesLoginAlreadyExist = false;
		
		String validLoginSql = "SELECT name FROM " + LOGIN_TABLE + " WHERE name = ?";
		PreparedStatement preparedSql;
		try {
			preparedSql = dbConn.prepareStatement(validLoginSql);
			preparedSql.setString(1, loginName);
			
			// If an existing login name matches with the provided login name, then it is not valid.
			// This is because login names have to be unique.
			ResultSet rs = preparedSql.executeQuery();
			
			if(rs.next()) {
				doesLoginAlreadyExist = true;
			}
		} catch (SQLException e) { 
			e.printStackTrace();
		}
		
		return doesLoginAlreadyExist;
	}
}
