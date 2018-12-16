package bdml.cache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.ChangeFileEncryption;

public class Dummy {

	public static void main(String[] args) throws SQLException {
		String dir = "./cache/";
		String db = "myaddr";
		String cipher = "AES";
		//String params = ";IFEXISTS=TRUE";
		// else: INIT (http://www.h2database.com/html/features.html)
		String params = ";CIPHER=" + cipher;
		String user = "myUser";
		String pwd = "myPassword";
		char[] filepwd = "test".toCharArray();
		String pwds = filepwd + " " + pwd;
		Connection conn = DriverManager.getConnection("jdbc:h2:" + dir + db + params, user, pwds);
		
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
//		stmt.execute("CREATE TABLE PERSON(id int primary key, name varchar(255))");
//		stmt.execute("INSERT INTO PERSON(id, name) VALUES(1, 'Hansli Secret')");
//		stmt.close();
//		conn.commit();
		
		stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM PERSON WHERE id = 1");
		if (rs.next()) {
			System.out.println(rs.getString("name"));
		}
		stmt.close();
		conn.close();
		
//		ChangeFileEncryption.execute(dir, db, cipher, "test".toCharArray(), "test".toCharArray(), true);
	}

}
