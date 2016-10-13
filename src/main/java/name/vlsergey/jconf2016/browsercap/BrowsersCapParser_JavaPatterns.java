package name.vlsergey.jconf2016.browsercap;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BrowsersCapParser_JavaPatterns extends
    AbstractBrowsersCapParser {

  private static final Log log = LogFactory
      .getLog(BrowsersCapParser_JavaPatterns.class);

  private static final Pattern PATTERN_EMPTY = Pattern
      .compile("");

  private static Pattern toPattern(String key) {
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

    String pattern = patternBuilder.toString();
    return Pattern.compile(pattern);
  }

  private TreeMap<String, Pattern> lines = new TreeMap<String, Pattern>(
      new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          int result = -Integer.compare(o1.length(),
              o2.length());
          if (result != 0) {
            return result;
          }
          return o1.compareTo(o2);
        }
      });

  @Override
  public BrowserInfo getBrowserInfo(String userAgent)
      throws Exception {
    return db.get(getPattern(userAgent));
  }

  @Override
  public String getPattern(String userAgent)
      throws Exception {
    final String useragent = userAgent;
    Matcher matcher = PATTERN_EMPTY.matcher(useragent);
    for (final Map.Entry<String, Pattern> entry : this.lines
        .entrySet()) {
      matcher.usePattern(entry.getValue());
      if (matcher.matches()) {
        return entry.getKey();
      }
    }
    return "*";
  }

  @PostConstruct
  public void init() throws Exception {
    super.init();

    log.info("Building patterns...");
    for (String pattern : db.keySet()) {
      lines.put(pattern, toPattern(pattern));
    }
    log.info("Building patterns... Done");
  }

}
