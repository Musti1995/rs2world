package be.nfm.rs2.util;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class LocalRepository<K, E> {

    private final HashMap<K, E> elementMap;
    private final Function<E, K> getIdFunction; //in lieu of @Entity & @Id
    private final BiConsumer<K, E> setIdFunction;

    public LocalRepository(Function<E, K> getIdFunction, BiConsumer<K, E> setIdFunction) {
        this.elementMap = new HashMap<>();
        this.getIdFunction = getIdFunction;
        this.setIdFunction = setIdFunction;
    }




}
