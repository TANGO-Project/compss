package hidro;

public class Hidro {

    private static void usage() {
        System.out.println("    Usage: hidro.Hidro");
    }

    public static void main(String[] args) throws Exception {
        // Check and get parameters
        if (args.length != 0) {
            usage();
            throw new Exception("[ERROR] Incorrect number of parameters");
        }

        // End
        System.out.println("[LOG] Main program finished.");
    }

}
