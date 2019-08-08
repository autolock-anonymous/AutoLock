package arraylist;
import java.util.*;
public abstract class AbstractCollection<E> implements Collection<E> {
	protected AbstractCollection() {
	}
	public abstract Iterator<E> iterator();
	public abstract int size();
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean isEmpty() {
		return size() == 0;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean contains(Object o) {
		Iterator<E> e = iterator();
		if (o == null) {
			while (e.hasNext())
				if (e.next() == null)
					return true;
		} else {
			while (e.hasNext())
				if (o.equals(e.next()))
					return true;
		}
		return false;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public Object[] toArray() {
		Object[] r = new Object[size()];
		Iterator<E> it = iterator();
		for (int i = 0; i < r.length; i++) {
			if (!it.hasNext())
				return Arrays.copyOf(r, i);
			r[i] = it.next();
		}
		return it.hasNext() ? finishToArray(r, it) : r;
	}
	public <T> T[] toArray(T[] a) {
		int size = size();
		T[] r = a.length >= size ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		Iterator<E> it = iterator();
		for (int i = 0; i < r.length; i++) {
			if (!it.hasNext()) {
				if (a != r)
					return Arrays.copyOf(r, i);
				r[i] = null;
				return r;
			}
			r[i] = (T) it.next();
		}
		return it.hasNext() ? finishToArray(r, it) : r;
	}
	private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
		int i = r.length;
		while (it.hasNext()) {
			int cap = r.length;
			if (i == cap) {
				int newCap = ((cap / 2) + 1) * 3;
				if (newCap <= cap) {
					if (cap == Integer.MAX_VALUE)
						throw new OutOfMemoryError("Required array size too large");
					newCap = Integer.MAX_VALUE;
				}
				r = Arrays.copyOf(r, newCap);
			}
			r[i++] = (T) it.next();
		}
		return (i == r.length) ? r : Arrays.copyOf(r, i);
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean remove(Object o) {
		Iterator<E> e = iterator();
		if (o == null) {
			while (e.hasNext()) {
				if (e.next() == null) {
					e.remove();
					return true;
				}
			}
		} else {
			while (e.hasNext()) {
				if (o.equals(e.next())) {
					e.remove();
					return true;
				}
			}
		}
		return false;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean containsAll(Collection<?> c) {
		Iterator<?> e = c.iterator();
		while (e.hasNext())
			if (!contains(e.next()))
				return false;
		return true;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean addAll(Collection<? extends E> c) {
		boolean modified = false;
		Iterator<? extends E> e = c.iterator();
		while (e.hasNext()) {
			if (add(e.next()))
				modified = true;
		}
		return modified;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<?> e = iterator();
		while (e.hasNext()) {
			if (c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		return modified;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<E> e = iterator();
		while (e.hasNext()) {
			if (!c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		return modified;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public void clear() {
		Iterator<E> e = iterator();
		while (e.hasNext()) {
			e.next();
			e.remove();
		}
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public String toString() {
		Iterator<E> i = iterator();
		if (!i.hasNext())
			return "[]";
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;) {
			E e = i.next();
			sb.append(e == this ? "(this Collection)" : e);
			if (!i.hasNext())
				return sb.append(']').toString();
			sb.append(", ");
		}
	}
}
