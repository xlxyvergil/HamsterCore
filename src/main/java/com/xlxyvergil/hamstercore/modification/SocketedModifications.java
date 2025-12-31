package com.xlxyvergil.hamstercore.modification;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Stream;

/**
 * Live instance of socketed modifications on an item. The size of the list is equal to the number of sockets on the object.
 * Additionally, this list may contain empty or invalid modification instances.
 */
public record SocketedModifications(ImmutableList<ModificationInstance> modifications) implements List<ModificationInstance> {

    public static final SocketedModifications EMPTY = new SocketedModifications(ImmutableList.of());

    public SocketedModifications(List<ModificationInstance> modifications) {
        this(ImmutableList.copyOf(modifications));
    }

    /**
     * Returns a stream of all socketed modification instances that are {@link ModificationInstance#isValid()}.
     */
    public Stream<ModificationInstance> streamValidModifications() {
        return this.modifications.stream().filter(ModificationInstance::isValid);
    }

    /**
     * Applies all valid modifications to the given item stack using AffixAPI.
     * @param stack The item stack to apply modifications to
     */
    public void applyAllModifications(ItemStack stack) {
        this.streamValidModifications().forEach(inst -> inst.applyAffixes(stack));
    }

    // List interface methods below this line

    @Override
    public int size() {
        return this.modifications.size();
    }

    @Override
    public boolean isEmpty() {
        return this.modifications.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.modifications.contains(o);
    }

    @Override
    public Iterator<ModificationInstance> iterator() {
        return this.modifications.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.modifications.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.modifications.toArray(a);
    }

    @Override
    @Deprecated
    public boolean add(ModificationInstance e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.modifications.containsAll(c);
    }

    @Override
    @Deprecated
    public boolean addAll(Collection<? extends ModificationInstance> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends ModificationInstance> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModificationInstance get(int index) {
        return this.modifications.get(index);
    }

    @Override
    @Deprecated
    public ModificationInstance set(int index, ModificationInstance element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void add(int index, ModificationInstance element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public ModificationInstance remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return this.modifications.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.modifications.lastIndexOf(o);
    }

    @Override
    public ListIterator<ModificationInstance> listIterator() {
        return this.modifications.listIterator();
    }

    @Override
    public ListIterator<ModificationInstance> listIterator(int index) {
        return this.modifications.listIterator(index);
    }

    @Override
    public List<ModificationInstance> subList(int fromIndex, int toIndex) {
        return this.modifications.subList(fromIndex, toIndex);
    }
}
