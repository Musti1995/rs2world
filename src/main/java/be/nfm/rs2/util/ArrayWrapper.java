package be.nfm.rs2.util;

import java.util.Objects;

/**
 * @author Musa Kapan
 */
public class ArrayWrapper<E> {

    private final E[] array;

    private ArrayWrapper(E[] array) {
        this.array = array;
    }

    public static <E> ArrayWrapper<E> wrap(E[] array) {
        return new ArrayWrapper<>(array);
    }

    public int indexOf(E e) {
        if (e == null) {
            for (int i = 0; i < array.length; i++) if (array[i] == null) return i;
        } else {
            for (int i = 0; i < array.length; i++) if (array[i].equals(e)) return i;
        }
        return -1;
    }

    public int add(E e) {
        Objects.requireNonNull(e);
        int emptyIndex = indexOf(null);
        if (emptyIndex >= 0) array[emptyIndex] = e;
        return emptyIndex;
    }

    public E get(int index) {
        Objects.checkIndex(index, array.length);
        return array[index];
    }

    public E remove(int index) {
        Objects.checkIndex(index, array.length);
        E oldObj = array[index];
        array[index] = null;
        return oldObj;
    }

    public int remove(E e) {
        Objects.requireNonNull(e);
        int index = indexOf(e);
        if (index >= 0) array[index] = null;
        return index;
    }

    public E set(int index, E e) {
        Objects.checkIndex(index, array.length);
        E oldObj = array[index];
        array[index] = e;
        return oldObj;
    }

    public E[] array() {
        return array;
    }


}
