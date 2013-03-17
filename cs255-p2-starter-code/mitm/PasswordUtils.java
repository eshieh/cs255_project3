package mitm;

import java.io.*;

public class PasswordUtils {

  private static String filename = "pwdFile";

  public static void main (String args[]) {
    String salt = BCrypt.gensalt();
    String hashed = BCrypt.hashpw(args[0], salt);
    
    try {
    
      File file = new File(filename);
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      bw.write(hashed);
      bw.newLine();
      bw.write(salt);
  
    } catch (IOException e) {}
  
  }  

  public static String getPassword() {
    try {
      File file = new File(filename);
      BufferedReader br = new BufferedReader(new FileReader(file));
      return br.readLine();
    } catch (IOException e) { return ""; }
  }

  public static String getSalt() {
    try {
      File file = new File(filename);
      BufferedReader br = new BufferedReader(new FileReader(file));
      br.readLine();
      return br.readLine();
    } catch (IOException e) { return ""; }
  
  }


}
