package b;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class B1 {

	public static void main(String[] args) throws SQLException {
		Connection conn = DriverManager.getConnection(null);
		conn.setAutoCommit(false);
		PreparedStatement ps = conn.prepareStatement(null);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			rs.getInt(0);
		}
		rs.close();
		ps.close();
		conn.close();

	}

}
