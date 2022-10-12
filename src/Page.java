import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;

public class Page {

    public static StringBuilder parse(String page) throws IOException {
        StringBuilder infoboxBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new StringReader(page));

        String title = "";
        String name = "";

        String line;
        while ((line = reader.readLine()) != null) {
            // get title from page
            Matcher titleMatcher = Regex.titlePattern.matcher(line);
            if (title.isEmpty() && titleMatcher.find()) {
                title = titleMatcher.group(1);
            }

            Matcher infoboxMatcher = Regex.infoboxStartPattern.matcher(line);
            if (infoboxBuilder.isEmpty() && infoboxMatcher.find()) {
                // TODO: parse infoboxes with certain types
                String infoboxType = infoboxMatcher.group(1).toLowerCase();

//                Matcher infoboxMatcher = Regex.infoboxStartPattern.matcher(line);
//                if (infoboxType.isEmpty() && infoboxMatcher.find()) {
//                    infoboxType = infoboxMatcher.group(1);
//                }

                long stack = Regex.bracketsStartPattern.matcher(line).results().count();
                while (stack > 0) {
                    infoboxBuilder.append(line);
                    infoboxBuilder.append("\n");
                    line = reader.readLine();
                    stack += Regex.bracketsStartPattern.matcher(line).results().count();
                    stack -= Regex.bracketsEndPattern.matcher(line).results().count();
                }
//                System.out.println("infoboxType: " + infoboxType);
                infoboxBuilder.append(line);
                infoboxBuilder.append("\n");
                break;
            }
        }

        return infoboxBuilder;
    }

}
