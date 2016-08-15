package storage.tools_i18n.util;

/**
 * 
 */
public class HashUtil {

	public static boolean isEqual(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}

	public static int calHash(Object... args) {
		int k = 23;
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				int c;
				if (args[i] instanceof Object[]) {
					c = calHash((Object[]) args[i]);
				} else {
					c = args[i] == null ? 0 : args[i].hashCode();
				}
				k = k * 31 + c;
			}
		}
		return k;
	}
}
