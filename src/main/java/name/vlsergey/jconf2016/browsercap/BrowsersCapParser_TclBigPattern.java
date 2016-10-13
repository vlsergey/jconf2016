package name.vlsergey.jconf2016.browsercap;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.basistech.tclre.HsrePattern;
import com.basistech.tclre.ReMatcher;
import com.basistech.tclre.RePattern;

public class BrowsersCapParser_TclBigPattern extends
    AbstractBrowsersCapParser {

  private static final Log log = LogFactory
      .getLog(BrowsersCapParser_TclBigPattern.class);

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
          patternBuilder.append("\\\\" + c);
        }
      }
    }

    return patternBuilder.toString();
  }

  private List<String> allPatterns;

  private RePattern pattern;

  @Override
  public BrowserInfo getBrowserInfo(String userAgent)
      throws Exception {
    return db.get(getPattern(userAgent));
  }

  @Override
  public String getPattern(String userAgent)
      throws Exception {
    final ReMatcher matcher = pattern.matcher(userAgent);
    if (matcher.matches())
      for (int i = 0; i < matcher.groupCount(); i++)
        if (matcher.group(i) != null)
          return this.allPatterns.get(i);
    return "*";
  }

  @PostConstruct
  public void init() throws Exception {
    superInit();
    initImpl(Integer.MAX_VALUE, true);
  }

  public void superInit() throws Exception {
    super.init();
  }

  public void initImpl(int maxToLoad,
      boolean wrapInParenthes) throws Exception {
    log.info("Building patterns...");
    allPatterns = new ArrayList<>(db.keySet());
    allPatterns.sort((s1, s2) -> -Integer.compare(
        s1.length(), s2.length()));

    boolean complete = false;
    StringBuilder bigPattern = new StringBuilder();
    try {
      int counter = 0;
      for (String pattern : allPatterns) {
        if (wrapInParenthes) {
          bigPattern
              .append("(" + toPattern(pattern) + "$)");
        } else {
          bigPattern.append(toPattern(pattern) + "$");
        }
        bigPattern.append("|");
        counter++;
        if (counter >= maxToLoad)
          break;
      }
      bigPattern.append("(^.*$)");
      allPatterns.add("*");

      this.pattern = HsrePattern.compile(bigPattern
          .toString());
      log.info("Building patterns... Done");

      complete = true;
    } finally {
      if (!complete)
        System.out.println(bigPattern);
    }
  }

}
