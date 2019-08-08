package top.liebes.entityWithLock;
import java.util.Iterator;
public class Main {
	public static void main(String args[]) {
		ArrayList<String> list1 = new ArrayList<String>(2);
		ArrayList<String> list = new ArrayList<String>();
		list.ensureCapacity(3);
		list.add("a");
		list.add("b");
		list.trimToSize();
		System.out.println(list.size());
		System.out.println(list.isEmpty());
		System.out.println(list.contains("a"));
		System.out.println(list.indexOf("a"));
		System.out.println(list.lastIndexOf("a"));
		list1 = (ArrayList<String>) list.clone();
		String[] s = (String[]) list.toArray();
		String tmp = list.get(0);
		list.set(1, "c");
		list.add("d");
		list.add(0, "d");
		list.remove(0);
		list.remove("d");
		list.addAll(list1);
		list.addAll(0, list1);
		list.removeAll(list1);
		list.retainAll(list1);
		Iterator<String> it = list.listIterator();
		it = list.listIterator(1);
		it = list.iterator();
		list1 = (ArrayList<String>) list.subList(0, 1);
		list.clear();
	}
}
