package name.vlsergey.jconf2016.trie;

import java.util.HashMap;
import java.util.Map;

class Node<V> {

  Map<String, Node<V>> children = new HashMap<>();
  String keyPart;
  V value;

}
  