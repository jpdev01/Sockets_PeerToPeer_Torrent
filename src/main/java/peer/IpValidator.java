package peer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpValidator {

    public static boolean isValid(String ip) {
        String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }
}
