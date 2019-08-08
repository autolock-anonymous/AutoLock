package top.liebes.entityWithLock;
import java.util.*;
import java.util.AbstractList;

public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
	public ReentrantReadWriteLock modCountLock = new ReentrantReadWriteLock();
	public ReentrantReadWriteLock elementDataLock = new ReentrantReadWriteLock();
	public ReentrantReadWriteLock sizeLock = new ReentrantReadWriteLock();
	private static final long serialVersionUID = 8683452581122892189L;
	private transient Object[] elementData;
	private int size;
	public ArrayList(int initialCapacity) {
		super();
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		this.elementData = new Object[initialCapacity];
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public ArrayList() {
		this(10);
	}
	public ArrayList(Collection<? extends E> c) {
		elementData = c.toArray();
		size = elementData.length;
		if (elementData.getClass() != Object[].class)
			elementData = Arrays.copyOf(elementData, size, Object[].class);
	}
	@Perm(requires = "share(modCount) * share(elementData) * pure(size) in alive", ensures = "share(modCount) * share(elementData) * pure(size) in alive")
	public void trimToSize() {
		modCountLock.writeLock().lock();
		modCount++;
		modCountLock.writeLock().unLock();
		elementDataLock.writeLock().lock();
		int oldCapacity = elementData.length;
		sizeLock.readLock().lock();
		if (size < oldCapacity) {
			elementData = Arrays.copyOf(elementData, size);
		}
		sizeLock.readLock().unLock();
		elementDataLock.writeLock().unLock();
	}
	@Perm(requires = "share(modCount) * share(elementData) in alive", ensures = "share(modCount) * share(elementData) in alive")
	public void ensureCapacity(int minCapacity) {
		modCountLock.writeLock().lock();
		modCount++;
		modCountLock.writeLock().unLock();
		elementDataLock.writeLock().lock();
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			Object oldData[] = elementData;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			elementData = Arrays.copyOf(elementData, newCapacity);
		}
		elementDataLock.writeLock().unLock();
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	public int size() {
		sizeLock.readLock().lock();
		int tmpVar = size;
		sizeLock.readLock().unLock();
		return tmpVar;
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	public boolean isEmpty() {
		sizeLock.readLock().lock();
		boolean tmpVar = size == 0;
		sizeLock.readLock().unLock();
		return tmpVar;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}
	@Perm(requires = "pure(size) * pure(elementData) in alive", ensures = "pure(size) * pure(elementData) in alive")
	public int indexOf(Object o) {
		elementDataLock.readLock().lock();
		sizeLock.readLock().lock();
		if (o == null) {
			for (int i = 0; i < size; i++)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = 0; i < size; i++)
				if (o.equals(elementData[i]))
					return i;
		}
		sizeLock.readLock().unLock();
		elementDataLock.readLock().unLock();
		return -1;
	}
	@Perm(requires = "pure(size) * pure(elementData) in alive", ensures = "pure(size) * pure(elementData) in alive")
	public int lastIndexOf(Object o) {
		elementDataLock.readLock().lock();
		sizeLock.readLock().lock();
		if (o == null) {
			for (int i = size - 1; i >= 0; i--)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (o.equals(elementData[i]))
					return i;
		}
		sizeLock.readLock().unLock();
		elementDataLock.readLock().unLock();
		return -1;
	}
	@Perm(requires = "share(elementData) * pure(size) * pure(modCount) in alive", ensures = "share(elementData) * pure(size) * pure(modCount) in alive")
	public Object clone() {
		try {
			ArrayList<E> v = (ArrayList<E>) super.clone();
			elementDataLock.writeLock().lock();
			sizeLock.readLock().lock();
			v.elementData = Arrays.copyOf(elementData, size);
			sizeLock.readLock().unLock();
			elementDataLock.writeLock().unLock();
			modCountLock.readLock().lock();
			v.modCount = 0;
			modCountLock.readLock().unLock();
			return v;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	@Perm(requires = "pure(elementData) * unique(size) in alive", ensures = "pure(elementData) * unique(size) in alive")
	public Object[] toArray() {
		elementDataLock.readLock().lock();
		Object[] tmpVar = Arrays.copyOf(elementData, size);
		elementDataLock.readLock().unLock();
		return tmpVar;
	}
	public <T> T[] toArray(T[] a) {
		if (a.length < size)
			return (T[]) Arrays.copyOf(elementData, size, a.getClass());
		System.arraycopy(elementData, 0, a, 0, size);
		if (a.length > size)
			a[size] = null;
		return a;
	}
	@Perm(requires = "pure(elementData) in alive", ensures = "pure(elementData) in alive")
	public E get(int index) {
		RangeCheck(index);
		elementDataLock.readLock().lock();
		E tmpVar = (E) elementData[index];
		elementDataLock.readLock().unLock();
		return tmpVar;
	}
	@Perm(requires = "share(elementData) in alive", ensures = "share(elementData) in alive")
	public E set(int index, E element) {
		RangeCheck(index);
		elementDataLock.writeLock().lock();
		E oldValue = (E) elementData[index];
		elementData[index] = element;
		elementDataLock.writeLock().unLock();
		return oldValue;
	}
	@Perm(requires = "share(size) * share(elementData) in alive", ensures = "share(size) * share(elementData) in alive")
	public boolean add(E e) {
		sizeLock.writeLock().lock();
		ensureCapacity(size + 1);
		elementDataLock.writeLock().lock();
		elementData[size++] = e;
		sizeLock.writeLock().unLock();
		elementDataLock.writeLock().unLock();
		return true;
	}
	public void add(int index, E element) {
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		ensureCapacity(size + 1);
		System.arraycopy(elementData, index, elementData, index + 1, size - index);
		elementData[index] = element;
		size++;
	}
	@Perm(requires = "share(modCount) * unique(elementData) * share(size) in alive", ensures = "share(modCount) * unique(elementData) * share(size) in alive")
	public E remove(int index) {
		RangeCheck(index);
		modCountLock.writeLock().lock();
		modCount++;
		modCountLock.writeLock().unLock();
		synchronized (elementData) {
			E oldValue = (E) elementData[index];
			int numMoved = size - index - 1;
			if (numMoved > 0)
				System.arraycopy(elementData, index + 1, elementData, index, numMoved);
			elementData[--size] = null;
		}
		return oldValue;
	}
	public boolean remove(Object o) {
		if (o == null) {
			for (int index = 0; index < size; index++)
				if (elementData[index] == null) {
					fastRemove(index);
					return true;
				}
		} else {
			for (int index = 0; index < size; index++)
				if (o.equals(elementData[index])) {
					fastRemove(index);
					return true;
				}
		}
		return false;
	}
	@Perm(requires = "share(modCount) * share(size) * unique(elementData) in alive", ensures = "share(modCount) * share(size) * unique(elementData) in alive")
	private void fastRemove(int index) {
		modCountLock.writeLock().lock();
		modCount++;
		modCountLock.writeLock().unLock();
		int numMoved = size - index - 1;
		synchronized (elementData) {
			if (numMoved > 0)
				System.arraycopy(elementData, index + 1, elementData, index, numMoved);
			elementData[--size] = null;
		}
	}
	@Perm(requires = "share(modCount) * share(size) * unique(elementData) in alive", ensures = "share(modCount) * share(size) * unique(elementData) in alive")
	public void clear() {
		modCountLock.writeLock().lock();
		modCount++;
		modCountLock.writeLock().unLock();
		synchronized (elementData) {
			for (int i = 0; i < size; i++)
				elementData[i] = null;
		}
		size = 0;
	}
	@Perm(requires = "share(size) * pure(elementData) in alive", ensures = "share(size) * pure(elementData) in alive")
	public boolean addAll(Collection<? extends E> c) {
		Object[] a = c.toArray();
		int numNew = a.length;
		sizeLock.writeLock().lock();
		ensureCapacity(size + numNew);
		elementDataLock.readLock().lock();
		System.arraycopy(a, 0, elementData, size, numNew);
		elementDataLock.readLock().unLock();
		size += numNew;
		sizeLock.writeLock().unLock();
		return numNew != 0;
	}
	public boolean addAll(int index, Collection<? extends E> c) {
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacity(size + numNew);
		int numMoved = size - index;
		if (numMoved > 0)
			System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
		System.arraycopy(a, 0, elementData, index, numNew);
		size += numNew;
		return numNew != 0;
	}
	@Perm(requires = "share(modCount) * share(size) * unique(elementData) in alive", ensures = "share(modCount) * share(size) * unique(elementData) in alive")
	protected void removeRange(int fromIndex, int toIndex) {
		modCountLock.writeLock().lock();
		modCount++;
		modCountLock.writeLock().unLock();
		int numMoved = size - toIndex;
		synchronized (elementData) {
			System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
			int newSize = size - (toIndex - fromIndex);
			while (size != newSize)
				elementData[--size] = null;
		}
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	private void RangeCheck(int index) {
		sizeLock.readLock().lock();
		if (index >= size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		sizeLock.readLock().unLock();
	}
	@Perm(requires = "pure(modCount) * pure(elementData) * pure(size) in alive", ensures = "pure(modCount) * pure(elementData) * pure(size) in alive")
	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		int expectedModCount = modCount;
		s.defaultWriteObject();
		s.writeInt(elementData.length);
		for (int i = 0; i < size; i++)
			s.writeObject(elementData[i]);
		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}
	}
	@Perm(requires = "unique(elementData) * pure(size) in alive", ensures = "unique(elementData) * pure(size) in alive")
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		int arrayLength = s.readInt();
		Object[] a = elementData = new Object[arrayLength];
		for (int i = 0; i < size; i++)
			a[i] = s.readObject();
	}
}
