package org.thymeleaf.dialect.springdata.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SDParameters {

	public static final String SORT = "sort";
	public static final String SIZE = "size";
	public static final String PAGE = "page";
	
	private final String prefix;
	private final int page;
	private final int size;
	private final Sort sort;
	
	private SDParameters(String prefix, Integer page, Integer size, Sort sort) {
		this.prefix = prefix;
		this.page = page;
		this.size = size;
		this.sort = sort;
	}
	
	public static SDParameters from(String prefix, Pageable page) {
		return new SDParameters(prefix, page.getPageNumber(), page.getPageSize(), page.getSort());
	}
	
	public String forPaginationSort() {
		return getSize()+Strings.AND+getPage(this.page);
	}
	
	public String forPaginationPage() {
		return getSize()+Strings.AND+getSort();
	}

	public String forPaginationSize() {
		return getSort();
	}

	private String getPage(int pageNumber) {
		return prefix + PAGE + Strings.EQ + this.page;
	}
	
	private String getSize() {
		return prefix + SIZE + Strings.EQ + this.size;
	}
	
	private String getSort() {
		StringBuilder orderBuilder = new StringBuilder();
		for (Sort.Order order : sort.toList()) {
			if (orderBuilder.length() > 0) {
				// TODO sort using more than 1 column
				break;
			}
			orderBuilder.append(order.getProperty());
			orderBuilder.append(Strings.COMMA);
			orderBuilder.append(order.getDirection().name());
		}
		return prefix + SORT + Strings.EQ + orderBuilder.toString();
	}
}
