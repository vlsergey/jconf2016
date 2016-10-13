package name.vlsergey.jconf2016.browsercap;

import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TCharObjectIterator;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.procedure.TCharObjectProcedure;
import gnu.trove.procedure.TCharProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TCharSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class TCharObjectEmptyMap<V> implements TCharObjectMap<V> {

    @SuppressWarnings("rawtypes")
    static final TCharObjectEmptyMap INSTANCE = new TCharObjectEmptyMap();

    @SuppressWarnings("unchecked")
    static <V> TCharObjectEmptyMap<V> instance() {
        return INSTANCE;
    }

    private TCharObjectEmptyMap() {
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean containsKey(char key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public boolean forEachEntry(TCharObjectProcedure<? super V> procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forEachKey(TCharProcedure procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forEachValue(TObjectProcedure<? super V> procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(char key) {
        return null;
    }

    @Override
    public char getNoEntryKey() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public TCharObjectIterator<V> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public char[] keys() {
        return new char[0];
    }

    @Override
    public char[] keys(char[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TCharSet keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(char key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Character, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(TCharObjectMap<? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(char key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(char key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainEntries(TCharObjectProcedure<? super V> procedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void transformValues(TObjectFunction<V, V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> valueCollection() {
        return Collections.emptyList();
    }

    @Override
    public Object[] values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V[] values(V[] array) {
        throw new UnsupportedOperationException();
    }

}
