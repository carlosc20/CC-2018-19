import java.util.Scanner;

public class CmdLineApp {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);

        System.out.println("Boas mano, manda ai um comando");

        String cmd = s.next();

        System.out.print("Fixe");

        s.close();
    }
}
