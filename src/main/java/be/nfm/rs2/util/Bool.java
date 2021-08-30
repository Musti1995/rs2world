package be.nfm.rs2.util;

import java.util.Objects;
import java.util.function.Supplier;

public final class Bool {

    public static final Bool TRUE = new Bool(true);
    public static final Bool FALSE = new Bool(false);

    private final boolean value;

    private Bool(boolean b) {
        this.value = b;
    }

    public static Bool of(boolean b) {
        return b ? TRUE : FALSE;
    }

    public boolean value() {
        return value;
    }

    public static Bool and(boolean a, boolean b) {
        return of(a && b);
    }

    public static Bool and(boolean... booleans) {
        boolean a = true;
        for (boolean b : booleans) a = a && b;
        return of(a);
    }

    public static Bool and(Bool a, Bool b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return a.and(b);
    }

    public Bool and(Bool b) {
        Objects.requireNonNull(b);
        return and(b.value);
    }

    public Bool and(boolean b) {
        return of(value && b);
    }

    public static Bool or(boolean a, boolean b) {
        return of(a || b);
    }

    public static Bool or(boolean... booleans) {
        boolean a = false;
        for (boolean b : booleans) a = a || b;
        return of(a);
    }

    public static Bool or(Bool a, Bool b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return a.or(b);
    }

    public Bool or(Bool b) {
        Objects.requireNonNull(b);
        return or(b.value);
    }

    public Bool or(boolean b) {
        return of(value || b);
    }

    public static Bool xor(boolean a, boolean b) {
        return of(a ^ b);
    }

    public static Bool xor(Bool a, Bool b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return a.xor(b);
    }

    public Bool xor(Bool b) {
        Objects.requireNonNull(b);
        return xor(b.value);
    }

    public Bool xor(boolean b) {
        return of(value ^ b);
    }

    public Bool computeIfTrue(Runnable block) {
        Objects.requireNonNull(block);
        if (value) block.run();
        return this;
    }

    public Bool computeIfFalse(Runnable block) {
        Objects.requireNonNull(block);
        if (!value) block.run();
        return this;
    }

    public <X extends RuntimeException> Bool throwIfTrue(Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier);
        if (value) throw exceptionSupplier.get();
        return this;
    }

    public <X extends RuntimeException> Bool throwIfFalse(Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier);
        if (!value) throw exceptionSupplier.get();
        return this;
    }

    public static <X extends RuntimeException> Bool throwIfFalse(boolean b,
                                                          Supplier<? extends X> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier);
        if (!b) throw exceptionSupplier.get();
        return of(b);
    }

    public static <X extends RuntimeException> Bool throwIfTrue(boolean b,
                                                          Supplier<? extends X> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier);
        if (b) throw exceptionSupplier.get();
        return of(b);
    }

    public <X> X returnMatrix(Supplier<X> supplyOnTrue, Supplier<X> supplyOnFalse) {
        Objects.requireNonNull(supplyOnTrue);
        Objects.requireNonNull(supplyOnFalse);
        return value ? supplyOnTrue.get() : supplyOnFalse.get();
    }

    public <X> X returnIfTrue(Supplier<X> supplier) {
        Objects.requireNonNull(supplier);
        return value ? supplier.get() : null;
    }

    public <X> X returnIfFalse(Supplier<X> supplier) {
        Objects.requireNonNull(supplier);
        return value ? null : supplier.get();
    }

    public String toString() {
        return value ? "true" : "false";
    }

    public int hashCode() {
        return value ? 1231 : 1237;
    }

    public boolean equals(Object o) {
        if (o instanceof Bool) return ((Bool) o).value == value;
        return false;
    }

}