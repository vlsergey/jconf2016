package name.vlsergey.jconf2016.browsercap.jwh;

import java.util.concurrent.TimeUnit;

import name.vlsergey.jconf2016.browsercap.BrowsersCapParser_JavaBigPattern;
import name.vlsergey.jconf2016.browsercap.BrowsersCapParser_JavaPatterns;
import name.vlsergey.jconf2016.browsercap.BrowsersCapParser_TrieComplex;
import name.vlsergey.jconf2016.browsercap.BrowsersCapParser_TrieSimple;

import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

//@Threads(Threads.MAX)
@Warmup(iterations = 2)
@Fork(value = 3)
@Measurement(iterations = 10, time = 10, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class Benchmark {

  private BrowsersCapParser_JavaPatterns parserJavaPatterns;
  private BrowsersCapParser_JavaBigPattern parserRegexpBigPattern;
  // private BrowsersCapParser_TclBigPattern parserTclBigPattern;
  private BrowsersCapParser_TrieComplex parserTrieComplex;
  private BrowsersCapParser_TrieSimple parserTrieSimple;

  private String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";

  @Setup
  public void setup() throws Exception {
    parserJavaPatterns = new BrowsersCapParser_JavaPatterns();
    parserJavaPatterns.init();

    parserRegexpBigPattern = new BrowsersCapParser_JavaBigPattern();
    parserRegexpBigPattern.init();

    // parserTcl = new BrowsersCapParser_Tcl();
    // parserTcl.init();

    parserTrieSimple = new BrowsersCapParser_TrieSimple();
    parserTrieSimple.init();

    parserTrieComplex = new BrowsersCapParser_TrieComplex();
    parserTrieComplex.init();

    // parser2_Plain = new BrowsersCapParser2_Plain();
    // parser2_Plain.init();
  }

  @org.openjdk.jmh.annotations.Benchmark
  public String testRegexpBigPattern() throws Exception {
    return parserRegexpBigPattern.getPattern(userAgent);
  }

  // @org.openjdk.jmh.annotations.Benchmark
  // public String testTcl() throws Exception {
  // return parserBigPattern.getPattern(userAgent);
  // }

  @org.openjdk.jmh.annotations.Benchmark
  public String testJavaPatterns() throws Exception {
    return parserJavaPatterns.getPattern(userAgent);
  }

  @org.openjdk.jmh.annotations.Benchmark
  public String testTrieComplex() throws Exception {
    return parserTrieComplex.getPattern(userAgent);
  }

  @org.openjdk.jmh.annotations.Benchmark
  public String testTrieSimple() throws Exception {
    return parserTrieSimple.getPattern(userAgent);
  }

}
