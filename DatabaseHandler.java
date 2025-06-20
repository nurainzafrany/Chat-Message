package Project;

import java.sql.*;

public class DatabaseHandler {

    private static final String url = "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:fsktm"; //Oracle DB URL
    private static final String user = "B225554"; //Database username
    private static final String password = "225554"; //Database password

    private Connection connect() throws Exception {
        Class.forName("oracle.jdbc.OracleDriver"); //Load Oracle JDBC Driver
        return DriverManager.getConnection(url, user, password);
    }

    public boolean registerUser(String username, String email, String password) {
        String query = "INSERT INTO chatify (user_ID, username, email, pass_code) VALUES (chatify_seq.NEXTVAL, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Email already exists: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateUser(String email, String password) {
        String query = "SELECT * FROM chatify WHERE email = ? AND pass_code = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserProfile(String email, String username, int age, String gender, String mobileNumber, String location) {
        String query = "UPDATE chatify SET username = ?, age = ?, gender = ?, mobile_number = ?, user_loc = ? WHERE email = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, age);
            stmt.setString(3, gender);
            stmt.setString(4, mobileNumber);
            stmt.setString(5, location);
            stmt.setString(6, email);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String[] getUserProfile(String email) {
        String query = "SELECT username, age, email, gender, mobile_number, user_loc FROM chatify WHERE email = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String age = String.valueOf(rs.getInt("age"));
                String gender = rs.getString("gender");
                String mobileNumber = rs.getString("mobile_number");
                String location = rs.getString("user_loc");

                return new String[]{username, age, email, gender, mobileNumber, location};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //Return null if no user is found or an error occurs
    }
}
