package mitm;

import java.io.*;

public class PasswordUtils {

  public static void main (String args[]) {
    
    String salt = BCrypt.gensalt();
    String hashed = BCrypt.hashpw(args[1], salt);
    
    try {
    
      File file = new File(args[0]);
      BufferedWriter bw = new BufferedWriter(new FileWriter(file));
      bw.write(hashed);
      bw.newLine();
      bw.write(salt);
      bw.newLine();
      bw.close();

    } catch (IOException e) {
      System.err.println("error");
    }
  
  }  

  public static String getHash(String name) {
    try {
      File file = new File(name);
      BufferedReader br = new BufferedReader(new FileReader(file));
      return br.readLine();
    } catch (IOException e) { return ""; }
  }

  public static String getSalt() {
    try {
      File file = new File(name);
      BufferedReader br = new BufferedReader(new FileReader(file));
      br.readLine();
      return br.readLine();
    } catch (IOException e) { return ""; }
  
  }


}
