package name.vlsergey.jconf2016.browsercap;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.basistech.tclre.HsrePattern;
import com.basistech.tclre.ReMatcher;
import com.basistech.tclre.RePattern;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

public class BrowsersCapParser_BricsBigPattern extends
    AbstractBrowsersCapParser {

  private static final Log log = LogFactory
      .getLog(BrowsersCapParser_BricsBigPattern.class);

  private static String toPattern(String key) {
    StringBuilder patternBuilder = new StringBuilder("");
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

  private RunAutomaton pattern;

  @Override
  public BrowserInfo getBrowserInfo(String userAgent)
      throws Exception {
    return db.get(getPattern(userAgent));
  }

  @Override
  public String getPattern(String userAgent)
      throws Exception {
    throw new UnsupportedOperationException();
  }

  public void superInit() throws Exception {
    super.init();
  }

  public void initImpl(int maxToLoad) throws Exception {
    log.info("Building patterns...");
    allPatterns = new ArrayList<>(db.keySet());
    allPatterns.sort((s1, s2) -> -Integer.compare(
        s1.length(), s2.length()));

    boolean complete = false;
    StringBuilder bigPattern = new StringBuilder();
    try {
      int counter = 0;
      for (String pattern : allPatterns) {
        bigPattern.append(toPattern(pattern));
        bigPattern.append("|");
        counter++;
        if (counter >= maxToLoad)
          break;
      }
      bigPattern.append(".*");
      allPatterns.add("*");

      RegExp regExp = new RegExp(bigPattern.toString());
      Automaton automaton = regExp.toAutomaton(
          (AutomatonProvider) null, false);
      this.pattern = new RunAutomaton(automaton, false);
      log.info("Building patterns... Done");

      complete = true;
    } finally {
      if (!complete)
        System.out.println(bigPattern);
    }
  }

}
