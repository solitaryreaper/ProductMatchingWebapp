package test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import models.dao.LoginDao;

import org.junit.Test;
import static play.test.Helpers.*;

/**
 * Test cases to check the login component.
 * @see {models.dao.LoginDao}
 * @author sprasa4
 *
 */
public class LoginDaoTest {

	private static String VALID_LOGIN = "guest";
	private static String VALID_LOGIN_CORRECT_PASSWORD = "guest";
	private static String VALID_LOGIN_INCORRECT_PASSWORD = "guest1";
	
	private static String INVALID_LOGIN = "guest1";
	
	/**
	 * Tests if the login functionality works properly
	 */
	@Test
	public void testVerifyLogin() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				LoginDao loginDao = new LoginDao();
				
				boolean isLoggedIn = loginDao.verifyLogin(VALID_LOGIN, VALID_LOGIN_CORRECT_PASSWORD);
				assertTrue(isLoggedIn);
				
				isLoggedIn = loginDao.verifyLogin(VALID_LOGIN, VALID_LOGIN_INCORRECT_PASSWORD);
				assertFalse(isLoggedIn);
			}
		});
	}
	
	/**
	 * Tests if the input login name is valid
	 */
	@Test
	public void testDoesLoginAlreadyExist() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				LoginDao loginDao = new LoginDao();
				
				boolean doesLoginAlreadyExist = loginDao.doesLoginAlreadyExist(VALID_LOGIN);
				assertTrue(doesLoginAlreadyExist);
				
				doesLoginAlreadyExist = loginDao.doesLoginAlreadyExist(INVALID_LOGIN);
				assertFalse(doesLoginAlreadyExist);
			}
		});
	}
	
}
