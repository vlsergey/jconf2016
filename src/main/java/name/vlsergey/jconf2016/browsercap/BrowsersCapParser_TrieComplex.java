package name.vlsergey.jconf2016.browsercap;

import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;

import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BrowsersCapParser_TrieComplex extends
    AbstractBrowsersCapParser {

  static abstract class AbstractNode {

    protected String leaf = null;

    protected short minLengthOfUserAgentSuffix;

    protected final char nodeChar;

    protected BitSetWithMask requiredCharacters;

    public AbstractNode(final char c) {
      this.nodeChar = c;
    }

    public abstract List<String> getLeafs();

    public abstract boolean hasChildren();

    public abstract void populateNextCheckNodes(char c,
        Collection<AbstractNode> nextToCheck);
  }

  static class Node extends AbstractNode {

    private AbstractNode asterixNode = null;

    private TCharObjectMap<AbstractNode> children = new TCharObjectHashMap<>();

    private AbstractNode questionNode = null;

    public Node(final char c) {
      super(c);
    }

    private char calcMaxChar() {
      char max = this.nodeChar;
      for (AbstractNode child : children.valueCollection()) {
        max = (char) Math.max(max,
            ((Node) child).calcMaxChar());
      }
      return max;
    }

    private short calcMinLengthOfUserAgentSuffix() {
      if (leaf != null) {
        this.minLengthOfUserAgentSuffix = 0;
        return 0;
      }
      short min = Short.MAX_VALUE;
      for (AbstractNode child : children.valueCollection()) {
        short childValue = ((Node) child)
            .calcMinLengthOfUserAgentSuffix();
        if (this.nodeChar != ASTERIX) {
          childValue++;
        }
        min = (short) Math.min(childValue, min);
      }
      this.minLengthOfUserAgentSuffix = min;
      return min;
    }

    private BitSetWithMask calcRequiredCharacters() {
      if (leaf != null) {
        // still need to calc required chars for children
        for (AbstractNode child : children
            .valueCollection()) {
          ((Node) child).calcRequiredCharacters();
        }

        return requiredCharacters = new BitSetWithMask();
      }
      BitSetWithMask result = null;
      for (AbstractNode child : children.valueCollection()) {
        BitSetWithMask childResult = ((Node) child)
            .calcRequiredCharacters().clone();
        if (child.nodeChar != ASTERIX
            && child.nodeChar != QUESTION) {
          childResult.set(child.nodeChar);
        }
        if (result == null) {
          result = childResult;
        } else {
          result.and(childResult);
        }
      }

      return this.requiredCharacters = result;
    }

    public List<String> getLeafs() {
      List<String> result = new ArrayList<>(2);
      if (StringUtils.isNotBlank(this.leaf)) {
        result.add(leaf);
      }
      if (this.asterixNode != null) {
        result.addAll(this.asterixNode.getLeafs());
      }
      return result;
    }

    public int getMinLengthOfUserAgentSuffix() {
      return minLengthOfUserAgentSuffix;
    }

    @Override
    public boolean hasChildren() {
      return !children.isEmpty();
    }

    public void insertPattern(String pattern, char[] cs,
        int i) {
      if (i == cs.length) {
        // this is the end of pattern
        if (this.leaf != null) {
          throw new IllegalArgumentException(
              "Duplicate pattern: '" + pattern + "'");
        }
        this.leaf = pattern;
        return;
      }

      final char c = cs[i];

      Node charNode = (Node) children.get(c);
      if (charNode == null) {
        charNode = new Node(c);
        children.put(c, charNode);
      }
      charNode.insertPattern(pattern, cs, i + 1);

      this.asterixNode = (Node) children.get(ASTERIX);
      this.questionNode = (Node) children.get(QUESTION);
    }

    private void optimize() {
      log.info("Optimize patterns tree...");
      Map<BitSetWithMask, BitSetWithMask> bitSets = new HashMap<>(
          1 << 18);
      optimizeImpl(bitSets);
    }

    public void optimizeImpl(
        Map<BitSetWithMask, BitSetWithMask> bitSets) {
      for (AbstractNode child : children.valueCollection()) {
        ((Node) child).optimizeImpl(bitSets);
      }

      for (char c : children.keys()) {
        Node child = (Node) children.get(c);
        if (child.children.size() == 1) {
          SingleChildNode singleChildNode = new SingleChildNode(
              child);
          children.put(c, singleChildNode);
          if (c == '*') {
            this.asterixNode = singleChildNode;
          } else if (c == '?') {
            this.questionNode = singleChildNode;
          }
        }
      }

      if (children.size() == 1) {
        this.children = new TCharObjectSinglentonMap<AbstractNode>(
            children.keys()[0],
            (AbstractNode) children.values()[0]);
      } else if (children.size() == 0) {
        this.children = TCharObjectEmptyMap.instance();
      } else {
        this.children = new TCharObjectHashMap<AbstractNode>(
            this.children);
      }
      if (bitSets.containsKey(this.requiredCharacters)) {
        this.requiredCharacters = bitSets
            .get(this.requiredCharacters);
      } else {
        bitSets.put(this.requiredCharacters,
            this.requiredCharacters);
      }
    }

    public void populateNextCheckNodes(char c,
        Collection<AbstractNode> nextToCheck) {
      final AbstractNode byChar = children.get(c);
      if (byChar != null) {
        nextToCheck.add(byChar);
      }

      if (asterixNode != null) {
        asterixNode.populateNextCheckNodes(c, nextToCheck);
      }

      if (questionNode != null) {
        nextToCheck.add(questionNode);
      }

      if (nodeChar == '*') {
        nextToCheck.add(this);
      }
    }

    @Override
    public String toString() {
      return this.nodeChar + "=>["
          + new String(this.children.keys()) + "]; "
          + this.leaf;
    }

  }

  static class SingleChildNode extends AbstractNode {

    private final AbstractNode child;

    public SingleChildNode(Node src) {
      super(src.nodeChar);
      this.leaf = src.leaf;
      this.minLengthOfUserAgentSuffix = src.minLengthOfUserAgentSuffix;
      this.requiredCharacters = src.requiredCharacters;
      this.child = (AbstractNode) src.children.values()[0];
    }

    public List<String> getLeafs() {
      List<String> result = new ArrayList<>(2);
      if (StringUtils.isNotBlank(this.leaf)) {
        result.add(leaf);
      }
      if (this.child.nodeChar == '*') {
        result.addAll(this.child.getLeafs());
      }
      return result;
    }

    @Override
    public boolean hasChildren() {
      return true;
    }

    public void populateNextCheckNodes(char c,
        Collection<AbstractNode> nextToCheck) {
      if (this.child.nodeChar == c
          || this.child.nodeChar == '?') {
        nextToCheck.add(this.child);
      }

      if (this.child.nodeChar == '*') {
        this.child.populateNextCheckNodes(c, nextToCheck);
      }

      if (nodeChar == '*') {
        nextToCheck.add(this);
      }
    }

  }

  static class Trie {

    private int maxPatternChar = 127;

    private final Node root = new Node((char) 0);

    public List<String> getMatchedPatterns(String userAgent) {
      final int userAgentLength = userAgent.length();
      if (userAgentLength >= Short.MAX_VALUE) {
        return Collections.singletonList("*");
      }

      final short[] charCounters = new short[maxPatternChar + 1];
      final BitSetWithMask charPresence = new BitSetWithMask(
          maxPatternChar);
      for (int i = 0; i < userAgentLength; i++) {
        char c = userAgent.charAt(i);
        if (c <= maxPatternChar) {
          charCounters[c]++;
          charPresence.set(c);
        }
      }

      final List<String> leafs = new ArrayList<>();

      final List<AbstractNode> toCheck = new ArrayList<>();
      toCheck.add(this.root);
      int currentChar = -1;
      final List<AbstractNode> nextToCheck = new ArrayList<>();

      while (!toCheck.isEmpty()) {
        currentChar++;
        final int uaCharsLeft = userAgentLength
            - currentChar;

        if (0 == uaCharsLeft) {
          for (AbstractNode node : toCheck) {
            leafs.addAll(node.getLeafs());
          }
          break;
        }

        char c = userAgent.charAt(currentChar);
        final int toCheckSize = toCheck.size();
        for (int i = 0; i < toCheckSize; i++) {
          toCheck.get(i).populateNextCheckNodes(c,
              nextToCheck);
        }

        if (nextToCheck.isEmpty()) {
          break;
        }

        toCheck.clear();

        if (c <= maxPatternChar) {
          if (--charCounters[c] == 0) {
            charPresence.clear(c);
          }
        }

        final int nextToCheckSize = nextToCheck.size();
        for (int i = 0; i < nextToCheckSize; i++) {
          AbstractNode node = nextToCheck.get(i);
          if (node.nodeChar == ASTERIX
              && !node.hasChildren()) {
            leafs.addAll(node.getLeafs());
          } else if (uaCharsLeft >= node.minLengthOfUserAgentSuffix
              && charPresence
                  .matchedMask(node.requiredCharacters)) {
            toCheck.add(node);
          }
        }
        nextToCheck.clear();
      }

      return leafs;
    }

    public void makeTrie(Collection<String> patterns) {
      for (String pattern : patterns) {
        this.root.insertPattern(pattern,
            pattern.toCharArray(), 0);
      }
      this.root.calcMinLengthOfUserAgentSuffix();
      this.maxPatternChar = this.root.calcMaxChar();
      this.root.calcRequiredCharacters();
      this.root.optimize();
    }
  }

  private static final char ASTERIX = '*';

  private static final Log log = LogFactory
      .getLog(BrowsersCapParser_TrieComplex.class);

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
