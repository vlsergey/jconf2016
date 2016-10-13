package name.vlsergey.jconf2016.browsercap;

import java.util.Arrays;

class BitSetWithMask implements Cloneable {

  private final static int ADDRESS_BITS_PER_WORD = 6;
  private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
  private static final long WORD_MASK = 0xffffffffffffffffL;

  private static int wordIndex(int bitIndex) {
    return bitIndex >> ADDRESS_BITS_PER_WORD;
  }

  private boolean sizeIsSticky = false;

  private long[] words;

  private int wordsInUse = 0;

  public BitSetWithMask() {
    initWords(BITS_PER_WORD);
    sizeIsSticky = false;
  }

  public BitSetWithMask(int nbits) {
    // nbits can't be negative; size 0 is OK
    if (nbits < 0)
      throw new NegativeArraySizeException("nbits < 0: "
          + nbits);

    initWords(nbits);
    sizeIsSticky = true;
  }

  public void and(BitSetWithMask set) {
    if (this == set)
      return;

    while (wordsInUse > set.wordsInUse)
      words[--wordsInUse] = 0;

    for (int i = 0; i < wordsInUse; i++)
      words[i] &= set.words[i];

    recalculateWordsInUse();
  }

  public int cardinality() {
    int sum = 0;
    for (int i = 0; i < wordsInUse; i++)
      sum += Long.bitCount(words[i]);
    return sum;
  }

  public void clear(int bitIndex) {
    int wordIndex = wordIndex(bitIndex);
    if (wordIndex >= wordsInUse)
      return;

    words[wordIndex] &= ~(1L << bitIndex);
    recalculateWordsInUse();
  }

  public BitSetWithMask clone() {
    if (!sizeIsSticky)
      trimToSize();

    try {
      BitSetWithMask result = (BitSetWithMask) super
          .clone();
      result.words = words.clone();
      return result;
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }

  private void ensureCapacity(int wordsRequired) {
    if (words.length < wordsRequired) {
      int request = Math.max(2 * words.length,
          wordsRequired);
      words = Arrays.copyOf(words, request);
      sizeIsSticky = false;
    }
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof BitSetWithMask))
      return false;
    if (this == obj)
      return true;

    BitSetWithMask set = (BitSetWithMask) obj;

    if (wordsInUse != set.wordsInUse)
      return false;

    // Check words in use by both BitSets
    for (int i = 0; i < wordsInUse; i++)
      if (words[i] != set.words[i])
        return false;

    return true;
  }

  private void expandTo(int wordIndex) {
    int wordsRequired = wordIndex + 1;
    if (wordsInUse < wordsRequired) {
      ensureCapacity(wordsRequired);
      wordsInUse = wordsRequired;
    }
  }

  public int hashCode() {
    long h = 1234;
    for (int i = wordsInUse; --i >= 0;)
      h ^= words[i] * (i + 1);

    return (int) ((h >> 32) ^ h);
  }

  private void initWords(int nbits) {
    words = new long[wordIndex(nbits - 1) + 1];
  }

  public boolean isEmpty() {
    return wordsInUse == 0;
  }

  public boolean matchedMask(BitSetWithMask mask) {
    if (mask.wordsInUse > this.wordsInUse)
      return false;

    for (int i = mask.wordsInUse - 1; i >= 0; i--)
      if ((words[i] & mask.words[i]) != mask.words[i])
        return false;

    return true;
  }

  public int nextClearBit(int fromIndex) {
    int u = wordIndex(fromIndex);
    if (u >= wordsInUse)
      return fromIndex;

    long word = ~words[u] & (WORD_MASK << fromIndex);

    while (true) {
      if (word != 0)
        return (u * BITS_PER_WORD)
            + Long.numberOfTrailingZeros(word);
      if (++u == wordsInUse)
        return wordsInUse * BITS_PER_WORD;
      word = ~words[u];
    }
  }

  public int nextSetBit(int fromIndex) {
    int u = wordIndex(fromIndex);
    if (u >= wordsInUse)
      return -1;

    long word = words[u] & (WORD_MASK << fromIndex);

    while (true) {
      if (word != 0)
        return (u * BITS_PER_WORD)
            + Long.numberOfTrailingZeros(word);
      if (++u == wordsInUse)
        return -1;
      word = words[u];
    }
  }

  private void recalculateWordsInUse() {
    int i;
    for (i = wordsInUse - 1; i >= 0; i--)
      if (words[i] != 0)
        break;

    wordsInUse = i + 1;
  }

  public void set(int bitIndex) {
    int wordIndex = wordIndex(bitIndex);
    expandTo(wordIndex);

    words[wordIndex] |= (1L << bitIndex);
  }

  public void set(int bitIndex, boolean value) {
    if (value)
      set(bitIndex);
    else
      clear(bitIndex);
  }

  public void set(int fromIndex, int toIndex) {
    if (fromIndex == toIndex)
      return;

    int startWordIndex = wordIndex(fromIndex);
    int endWordIndex = wordIndex(toIndex - 1);
    expandTo(endWordIndex);

    long firstWordMask = WORD_MASK << fromIndex;
    long lastWordMask = WORD_MASK >>> -toIndex;
    if (startWordIndex == endWordIndex) {
      words[startWordIndex] |= (firstWordMask & lastWordMask);
    } else {
      words[startWordIndex] |= firstWordMask;

      for (int i = startWordIndex + 1; i < endWordIndex; i++)
        words[i] = WORD_MASK;

      words[endWordIndex] |= lastWordMask;
    }
  }

  public String toString() {

    int numBits = (wordsInUse > 128) ? cardinality()
        : wordsInUse * BITS_PER_WORD;
    StringBuilder b = new StringBuilder(6 * numBits + 2);
    b.append('{');

    int i = nextSetBit(0);
    if (i != -1) {
      b.append((char) i);
      for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
        int endOfRun = nextClearBit(i);
        do {
          b.append(", ").append((char) i);
        } while (++i < endOfRun);
      }
    }

    b.append('}');
    return b.toString();
  }

  private void trimToSize() {
    if (wordsInUse != words.length) {
      words = Arrays.copyOf(words, wordsInUse);
    }
  }

}
