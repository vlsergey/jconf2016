package name.vlsergey.jconf2016.browsercap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BrowsersCapParser_JavaBigPattern extends
    AbstractBrowsersCapParser {

  private static final Log log = LogFactory
      .getLog(BrowsersCapParser_JavaBigPattern.class);

  private static String toPattern(String key) {
    StringBuilder patternBuilder = new StringBuilder("^");
    for (char c : key.toCharArray()) {
      switch (c) {
      case '*':
        patternBuilder.append(".*");
        break;
      case '?':
        patternBuilder.append(".");
        break;
      default:
        if (('A' <= c && c <= 'Z')
            || ('a' <= c && c <= 'z')
            || ('0' <= c && c <= '9') || c == ' ') {
          patternBuilder.append(c);
        } else {
          patternBuilder.append("\\" + c);
        }
      }
    }

    return patternBuilder.toString();
  }

  private List<String> allPatterns;

  private Pattern pattern;

  @Override
  public BrowserInfo getBrowserInfo(String userAgent)
      throws Exception {
    return db.get(getPattern(userAgent));
  }

  @Override
  public String getPattern(String userAgent)
      throws Exception {
    final Matcher matcher = pattern.matcher(userAgent);
    if (matcher.matches())
      for (int i = 0; i < matcher.groupCount(); i++)
        if (matcher.group(i) != null)
          return this.allPatterns.get(i);
    return "*";
  }

  @PostConstruct
  public void init() throws Exception {
    super.init();

    log.info("Building patterns...");
    allPatterns = new ArrayList<>(db.keySet());
    allPatterns.sort((s1, s2) -> -Integer.compare(
        s1.length(), s2.length()));

    StringBuilder bigPattern = new StringBuilder();
    for (String pattern : allPatterns) {
      bigPattern.append("(" + toPattern(pattern) + "$)");
      bigPattern.append("|");
    }
    bigPattern.append("(^.*$)");
    allPatterns.add("*");

    this.pattern = Pattern.compile(bigPattern.toString());
    log.info("Building patterns... Done");
  }

}
