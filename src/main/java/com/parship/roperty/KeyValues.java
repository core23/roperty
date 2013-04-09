package com.parship.roperty;

import com.parship.commons.util.Ensure;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;


/**
 * @author mfinsterwalder
 * @since 2013-03-26 09:18
 */
public class KeyValues {

	private static class DomainPattern implements Comparable<DomainPattern> {
		private final String patternStr;
		private final int ordering;
		private final Object value;

		public DomainPattern(final String domainPattern, final int order, Object value) {
			Ensure.notNull(domainPattern, "domainPattern");
			Ensure.notNull(value, "value");
			this.patternStr = domainPattern;
			this.ordering = order;
			this.value = value;
		}

		@Override
		public int compareTo(final DomainPattern other) {
			int order = this.ordering - other.ordering;
			if (order == 0) {
				return patternStr.compareTo(other.patternStr);
			}
			return order;
		}

		@Override
		public String toString() {
			return "DomainPattern{" +
				"pattern=" + patternStr +
				", ordering=" + ordering +
				", value=" + value +
				'}';
		}

		private boolean matches(final String domainStr) {
			boolean equals = patternStr.equals(domainStr.substring(0, Math.min(domainStr.length(), patternStr.length())));
			if (!equals && patternStr.contains("*")) {
				String patternStr = domainStr.replaceAll("\\|", "\\\\|").replaceAll("\\*", "[^|]*") + ".*";
				return domainStr.matches(patternStr);
			}
			return equals;
		}
	}

	private Set<DomainPattern> patterns = new TreeSet<>();

	public KeyValues() {
		this("[value undefined]");
	}

	public KeyValues(Object value) {
		Ensure.notNull(value, "value");
		put(value);
	}

	public void put(Object value, String... domains) {
		Ensure.notNull(domains, "domain");
		Ensure.notNull(value, "value");
		createAndAddDomainPattern(value, domains);
	}

	private void createAndAddDomainPattern(final Object value, final String[] domains) {
		int order = 1;
		if (domains.length == 0) {
			addDomainPattern("", order, value);
			return;
		}
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String domain : domains) {
			i++;
			appendSeparatorIfNeeded(builder);
			if (!"*".equals(domain)) {
				order = order | (int) Math.pow(2, i);
			}
			builder.append(domain);
		}
		addDomainPattern(builder.toString(), order, value);
	}

	private void appendSeparatorIfNeeded(final StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append("|");
		}
	}

	private void addDomainPattern(final String pattern, final int order, final Object value) {
		DomainPattern domainPattern = new DomainPattern(pattern, order, value);
		patterns.remove(domainPattern);
		patterns.add(domainPattern);
	}

	private String buildDomain(final Iterable<String> domains, final Resolver resolver) {
		StringBuilder builder = new StringBuilder();
		for (String domain : domains) {
			appendSeparatorIfNeeded(builder);
			String domainValue = resolver.getDomainValue(domain);
			Ensure.notEmpty(domainValue, "domainValue");
			builder.append(domainValue);
		}
		return builder.toString();
	}

	public <T> T get(List<String> domains, final Resolver resolver) {
		T value = null;
		for (DomainPattern pattern : patterns) {
			String domainStr = buildDomain(domains, resolver);
			if (pattern.matches(domainStr)) {
				value = (T)pattern.value;
			}
		}
		return value;
//		if (resolver == null) {
//			return value;
//		}
//		StringBuilder builder = new StringBuilder();
//		for (String domain : domains) {
//			if (builder.length() > 0) {
//				builder.append("|");
//			} domain1|domain2|domain3
//			String domainValue = resolver.getDomainValue(domain);
//			Ensure.notEmpty(domainValue, "domainValue");
//			builder.append(domainValue);
//			T overriddenValue = (T)values.get(builder.toString());
//			if (overriddenValue != null) {
//				value = overriddenValue;
//			}
//		}
//		return value;
	}

	@Override
	public String toString() {
		return "KeyValues{" +
			"patterns=" + patterns +
			'}';
	}
}
