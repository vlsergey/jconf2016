package name.vlsergey.jconf2016.browsercap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BrowsersCapParser_TrieSimple extends
    AbstractBrowsersCapParser {

  static class Node {

    private Map<Character, Node> children = new HashMap<Character, Node>();

    protected String leaf = null;

    protected final char nodeChar;

    public Node(final char c) {
      this.nodeChar = c;
    }

    public List<String> getLeafs() {
      List<String> result = new ArrayList<>(2);
      if (StringUtils.isNotBlank(this.leaf)) {
        result.add(leaf);
      }
      if (children.containsKey('*')) {
        result.addAll(children.get('*').getLeafs());
      }
      return result;
    }

    public boolean hasChildren() {
      return !children.isEmpty();
    }

    public void insertPattern(String pattern, int i) {
      if (i == pattern.length()) {
        this.leaf = pattern;
        return;
      }

      final char c = pattern.charAt(i);
      Node charNode = (Node) children.get(c);
      if (charNode == null) {
        charNode = new Node(c);
        children.put(c, charNode);
      }
      charNode.insertPattern(pattern, i + 1);
    }

    public void populateNextCheckNodes(char c,
        Collection<Node> nextToCheck) {
      final Node byChar = children.get(c);
      if (byChar != null)
        nextToCheck.add(byChar);

      if (children.containsKey('*'))
        children.get('*').populateNextCheckNodes(c,
            nextToCheck);

      if (children.containsKey('?'))
        nextToCheck.add(children.get('?'));

      if (nodeChar == '*')
        nextToCheck.add(this);
    }

    @Override
    public String toString() {
      return this.nodeChar + "=>[" + this.children.keySet()
          + "]; " + this.leaf;
    }

  }

  static class Trie {

    private final Node root = new Node((char) 0);

    public List<String> getMatchedPatterns(String userAgent) {
      final int userAgentLength = userAgent.length();
      if (userAgentLength >= Short.MAX_VALUE) {
        return Collections.singletonList("*");
      }

      final List<String> leafs = new ArrayList<>();

      final List<Node> toCheck = new ArrayList<>();
      toCheck.add(this.root);
      int currentChar = -1;
      final List<Node> nextToCheck = new ArrayList<>();

      while (!toCheck.isEmpty()) {
        currentChar++;
        final int uaCharsLeft = userAgentLength
            - currentChar;
        if (0 == uaCharsLeft) {
          for (Node node : toCheck)
            leafs.addAll(node.getLeafs());
          break;
        }

        char c = userAgent.charAt(currentChar);
        for (Node toCheckNode : toCheck) {
          toCheckNode
              .populateNextCheckNodes(c, nextToCheck);
        }

        if (nextToCheck.isEmpty())
          break;

        toCheck.clear();
        for (Node node : nextToCheck) {
          if (node.nodeChar == ASTERIX
              && !node.hasChildren()) {
            leafs.addAll(node.getLeafs());
          } else {
            toCheck.add(node);
          }
        }
        nextToCheck.clear();
      }

      return leafs;
    }

    public void makeTrie(Collection<String> patterns) {
      for (String pattern : patterns) {
        this.root.insertPattern(pattern, 0);
      }
    }
  }

  private static final char ASTERIX = '*';

  private static final Log log = LogFactory
      .getLog(BrowsersCapParser_TrieSimple.class);

  private static final char QUESTION = '?';

  final Trie tree = new Trie();

  @Override
  public BrowserInfo getBrowserInfo(String userAgent)
      throws Exception {
    return db.get(getPattern(userAgent));
  }

  @Override
  public String getPattern(String userAgent)
      throws Exception {
    List<String> patterns = tree
        .getMatchedPatterns(userAgent);
    Collections.sort(patterns);

    String longest = patterns.get(0);
    for (String str : patterns) {
      if (str.length() > longest.length()) {
        longest = str;
      }
    }

    return longest;
  }

  @PostConstruct
  public void init() throws Exception {
    super.init();

    log.info("Creating patterns tree...");
    this.tree.makeTrie(db.keySet());
    log.info("Creating patterns tree... Done");

  }
}
