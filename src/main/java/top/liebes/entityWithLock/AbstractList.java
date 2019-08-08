package arraylist;
import java.util.*;
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
	protected AbstractList() {
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean add(E e) {
		add(size(), e);
		return true;
	}
	abstract public E get(int index);
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public int indexOf(Object o) {
		ListIterator<E> e = listIterator();
		if (o == null) {
			while (e.hasNext())
				if (e.next() == null)
					return e.previousIndex();
		} else {
			while (e.hasNext())
				if (o.equals(e.next()))
					return e.previousIndex();
		}
		return -1;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public int lastIndexOf(Object o) {
		ListIterator<E> e = listIterator(size());
		if (o == null) {
			while (e.hasPrevious())
				if (e.previous() == null)
					return e.nextIndex();
		} else {
			while (e.hasPrevious())
				if (o.equals(e.previous()))
					return e.nextIndex();
		}
		return -1;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public void clear() {
		removeRange(0, size());
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean modified = false;
		Iterator<? extends E> e = c.iterator();
		while (e.hasNext()) {
			add(index++, e.next());
			modified = true;
		}
		return modified;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public Iterator<E> iterator() {
		return new Itr();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}
	public ListIterator<E> listIterator(final int index) {
		if (index < 0 || index > size())
			throw new IndexOutOfBoundsException("Index: " + index);
		return new ListItr(index);
	}
	private class Itr implements Iterator<E> {
		public ReentrantReadWriteLock lastRetLock = new ReentrantReadWriteLock();
		public ReentrantReadWriteLock cursorLock = new ReentrantReadWriteLock();
		public ReentrantReadWriteLock modCountLock = new ReentrantReadWriteLock();
		public ReentrantReadWriteLock expectedModCountLock = new ReentrantReadWriteLock();
		int cursor = 0;
		int lastRet = -1;
		int expectedModCount = modCount;
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public boolean hasNext() {
			cursorLock.readLock().lock();
			boolean tmpVar = cursor != size();
			cursorLock.readLock().unLock();
			return tmpVar;
		}
		@Perm(requires = "share(cursor) * share(lastRet) in alive", ensures = "share(cursor) * share(lastRet) in alive")
		public E next() {
			checkForComodification();
			try {
				cursorLock.writeLock().lock();
				E next = get(cursor);
				lastRetLock.writeLock().lock();
				lastRet = cursor++;
				lastRetLock.writeLock().unLock();
				cursorLock.writeLock().unLock();
				return next;
			} catch (IndexOutOfBoundsException e) {
				checkForComodification();
				throw new NoSuchElementException();
			}
		}
		@Perm(requires = "share(lastRet) * share(cursor) * share(expectedModCount) * pure(modCount) in alive", ensures = "share(lastRet) * share(cursor) * share(expectedModCount) * pure(modCount) in alive")
		public void remove() {
			lastRetLock.writeLock().lock();
			if (lastRet == -1)
				throw new IllegalStateException();
			checkForComodification();
			try {
				AbstractList.this.remove(lastRet);
				cursorLock.writeLock().lock();
				if (lastRet < cursor)
					cursor--;
				cursorLock.writeLock().unLock();
				lastRet = -1;
				expectedModCountLock.writeLock().lock();
				modCountLock.readLock().lock();
				expectedModCount = modCount;
				modCountLock.readLock().unLock();
				expectedModCountLock.writeLock().unLock();
			} catch (IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
			lastRetLock.writeLock().unLock();
		}
		@Perm(requires = "pure(modCount) * pure(expectedModCount) in alive", ensures = "pure(modCount) * pure(expectedModCount) in alive")
		final void checkForComodification() {
			expectedModCountLock.readLock().lock();
			modCountLock.readLock().lock();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			modCountLock.readLock().unLock();
			expectedModCountLock.readLock().unLock();
		}
	}
	private class ListItr extends Itr implements ListIterator<E> {
		public ReentrantReadWriteLock modCountLock = new ReentrantReadWriteLock();
		public ReentrantReadWriteLock expectedModCountLock = new ReentrantReadWriteLock();
		public ReentrantReadWriteLock lastRetLock = new ReentrantReadWriteLock();
		public ReentrantReadWriteLock cursorLock = new ReentrantReadWriteLock();
		ListItr(int index) {
			cursor = index;
		}
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public boolean hasPrevious() {
			cursorLock.readLock().lock();
			boolean tmpVar = cursor != 0;
			cursorLock.readLock().unLock();
			return tmpVar;
		}
		@Perm(requires = "share(cursor) * share(lastRet) in alive", ensures = "share(cursor) * share(lastRet) in alive")
		public E previous() {
			checkForComodification();
			try {
				cursorLock.writeLock().lock();
				int i = cursor - 1;
				E previous = get(i);
				lastRetLock.writeLock().lock();
				lastRet = cursor = i;
				lastRetLock.writeLock().unLock();
				cursorLock.writeLock().unLock();
				return previous;
			} catch (IndexOutOfBoundsException e) {
				checkForComodification();
				throw new NoSuchElementException();
			}
		}
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public int nextIndex() {
			cursorLock.readLock().lock();
			int tmpVar = cursor;
			cursorLock.readLock().unLock();
			return tmpVar;
		}
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public int previousIndex() {
			cursorLock.readLock().lock();
			int tmpVar = cursor - 1;
			cursorLock.readLock().unLock();
			return tmpVar;
		}
		@Perm(requires = "pure(lastRet) * share(expectedModCount) * pure(modCount) in alive", ensures = "pure(lastRet) * share(expectedModCount) * pure(modCount) in alive")
		public void set(E e) {
			lastRetLock.readLock().lock();
			if (lastRet == -1)
				throw new IllegalStateException();
			checkForComodification();
			try {
				AbstractList.this.set(lastRet, e);
				expectedModCountLock.writeLock().lock();
				modCountLock.readLock().lock();
				expectedModCount = modCount;
				modCountLock.readLock().unLock();
				expectedModCountLock.writeLock().unLock();
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
			lastRetLock.readLock().unLock();
		}
		@Perm(requires = "share(cursor) * share(lastRet) * share(expectedModCount) * pure(modCount) in alive", ensures = "share(cursor) * share(lastRet) * share(expectedModCount) * pure(modCount) in alive")
		public void add(E e) {
			checkForComodification();
			try {
				cursorLock.writeLock().lock();
				AbstractList.this.add(cursor++, e);
				cursorLock.writeLock().unLock();
				lastRetLock.writeLock().lock();
				lastRet = -1;
				lastRetLock.writeLock().unLock();
				expectedModCountLock.writeLock().lock();
				modCountLock.readLock().lock();
				expectedModCount = modCount;
				modCountLock.readLock().unLock();
				expectedModCountLock.writeLock().unLock();
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}
	}
	public List<E> subList(int fromIndex, int toIndex) {
		return (this instanceof RandomAccess
				? new RandomAccessSubList<E>(this, fromIndex, toIndex)
				: new SubList<E>(this, fromIndex, toIndex));
	}
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof List))
			return false;
		ListIterator<E> e1 = listIterator();
		ListIterator e2 = ((List) o).listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			E o1 = e1.next();
			Object o2 = e2.next();
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return !(e1.hasNext() || e2.hasNext());
	}
	public int hashCode() {
		int hashCode = 1;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}
	protected void removeRange(int fromIndex, int toIndex) {
		ListIterator<E> it = listIterator(fromIndex);
		for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
			it.next();
			it.remove();
		}
	}
	protected transient int modCount = 0;
}
class SubList<E> extends AbstractList<E> {
	public ReentrantReadWriteLock offsetLock = new ReentrantReadWriteLock();
	public ReentrantReadWriteLock sizeLock = new ReentrantReadWriteLock();
	public ReentrantReadWriteLock modCountLock = new ReentrantReadWriteLock();
	public ReentrantReadWriteLock lLock = new ReentrantReadWriteLock();
	public ReentrantReadWriteLock expectedModCountLock = new ReentrantReadWriteLock();
	private AbstractList<E> l;
	private int offset;
	private int size;
	private int expectedModCount;
	SubList(AbstractList<E> list, int fromIndex, int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > list.size())
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		l = list;
		offset = fromIndex;
		size = toIndex - fromIndex;
		expectedModCount = l.modCount;
	}
	@Perm(requires = "immutable(l) * pure(offset) in alive", ensures = "immutable(l) * pure(offset) in alive")
	public E set(int index, E element) {
		rangeCheck(index);
		checkForComodification();
		offsetLock.readLock().lock();
		E tmpVar = l.set(index + offset, element);
		offsetLock.readLock().unLock();
		return tmpVar;
	}
	@Perm(requires = "immutable(l) * pure(offset) in alive", ensures = "immutable(l) * pure(offset) in alive")
	public E get(int index) {
		rangeCheck(index);
		checkForComodification();
		offsetLock.readLock().lock();
		E tmpVar = l.get(index + offset);
		offsetLock.readLock().unLock();
		return tmpVar;
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	public int size() {
		checkForComodification();
		sizeLock.readLock().lock();
		int tmpVar = size;
		sizeLock.readLock().unLock();
		return tmpVar;
	}
	@Perm(requires = "share(size) * immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) in alive", ensures = "share(size) * immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) in alive")
	public void add(int index, E element) {
		sizeLock.writeLock().lock();
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException();
		checkForComodification();
		offsetLock.readLock().lock();
		l.add(index + offset, element);
		offsetLock.readLock().unLock();
		expectedModCountLock.writeLock().lock();
		modCountLock.writeLock().lock();
		expectedModCount = l.modCount;
		expectedModCountLock.writeLock().unLock();
		size++;
		sizeLock.writeLock().unLock();
		modCount++;
		modCountLock.writeLock().unLock();
	}
	@Perm(requires = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive", ensures = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive")
	public E remove(int index) {
		rangeCheck(index);
		checkForComodification();
		offsetLock.readLock().lock();
		E result = l.remove(index + offset);
		offsetLock.readLock().unLock();
		expectedModCountLock.writeLock().lock();
		modCountLock.writeLock().lock();
		expectedModCount = l.modCount;
		expectedModCountLock.writeLock().unLock();
		sizeLock.writeLock().lock();
		size--;
		sizeLock.writeLock().unLock();
		modCount++;
		modCountLock.writeLock().unLock();
		return result;
	}
	@Perm(requires = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive", ensures = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive")
	protected void removeRange(int fromIndex, int toIndex) {
		checkForComodification();
		l.removeRange(fromIndex + offset, toIndex + offset);
		modCountLock.writeLock().lock();
		expectedModCount = l.modCount;
		size -= (toIndex - fromIndex);
		modCount++;
		modCountLock.writeLock().unLock();
	}
	@Perm(requires = "share(size) in alive", ensures = "share(size) in alive")
	public boolean addAll(Collection<? extends E> c) {
		sizeLock.writeLock().lock();
		boolean tmpVar = addAll(size, c);
		sizeLock.writeLock().unLock();
		return tmpVar;
	}
	public boolean addAll(int index, Collection<? extends E> c) {
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		int cSize = c.size();
		if (cSize == 0)
			return false;
		checkForComodification();
		l.addAll(offset + index, c);
		expectedModCount = l.modCount;
		size += cSize;
		modCount++;
		return true;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public Iterator<E> iterator() {
		return listIterator();
	}
	@Perm(requires = "pure(size) * immutable(l) * pure(offset) in alive", ensures = "pure(size) * immutable(l) * pure(offset) in alive")
	public ListIterator<E> listIterator(final int index) {
		checkForComodification();
		sizeLock.readLock().lock();
		if (index < 0 || index > size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		sizeLock.readLock().unLock();
		offsetLock.readLock().lock();
		ListIterator<E> tmpVar = new ListIterator<E>() {
			private ListIterator<E> i = l.listIterator(index + offset);
			public boolean hasNext() {
				return nextIndex() < size;
			}
			public E next() {
				if (hasNext())
					return i.next();
				else
					throw new NoSuchElementException();
			}
			public boolean hasPrevious() {
				return previousIndex() >= 0;
			}
			public E previous() {
				if (hasPrevious())
					return i.previous();
				else
					throw new NoSuchElementException();
			}
			public int nextIndex() {
				return i.nextIndex() - offset;
			}
			public int previousIndex() {
				return i.previousIndex() - offset;
			}
			public void remove() {
				i.remove();
				expectedModCount = l.modCount;
				size--;
				modCount++;
			}
			public void set(E e) {
				i.set(e);
			}
			public void add(E e) {
				i.add(e);
				expectedModCount = l.modCount;
				size++;
				modCount++;
			}
		};
		offsetLock.readLock().unLock();
		return tmpVar;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public List<E> subList(int fromIndex, int toIndex) {
		return new SubList<E>(this, fromIndex, toIndex);
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	private void rangeCheck(int index) {
		sizeLock.readLock().lock();
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + size);
		sizeLock.readLock().unLock();
	}
	@Perm(requires = "immutable(l) * pure(modCount) * pure(expectedModCount) in alive", ensures = "immutable(l) * pure(modCount) * pure(expectedModCount) in alive")
	private void checkForComodification() {
		expectedModCountLock.readLock().lock();
		modCountLock.readLock().lock();
		if (l.modCount != expectedModCount)
			throw new ConcurrentModificationException();
		modCountLock.readLock().unLock();
		expectedModCountLock.readLock().unLock();
	}
}
class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
	RandomAccessSubList(AbstractList<E> list, int fromIndex, int toIndex) {
		super(list, fromIndex, toIndex);
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public List<E> subList(int fromIndex, int toIndex) {
		return new RandomAccessSubList<E>(this, fromIndex, toIndex);
	}
}
