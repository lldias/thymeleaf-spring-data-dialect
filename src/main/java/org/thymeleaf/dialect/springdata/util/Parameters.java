package org.thymeleaf.dialect.springdata.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class Parameters {

	public static final String PREFIX_SEPARATOR = "_";
	public static final String SORT = "sort";
	public static final String SIZE = "size";
	public static final String PAGE = "page";
	public static final String FILTER = "filter";
	
	private final String prefix;
	private final int page;
	private final int size;
	private final Sort sort;
	private final String filter;
	
	private Parameters(String prefix, Integer page, Integer size, Sort sort, String filter) {
		this.prefix = prefix;
		this.page = page;
		this.size = size;
		this.sort = sort;
		this.filter = filter;
	}
	
	public static Parameters from(String prefix, Pageable page, String filter) {
		return new Parameters(prefix, page.getPageNumber(), page.getPageSize(), page.getSort(), filter);
	}
	
	/**
	 * Don't return attribute sort
	 * @return Attributes: size, page, filter 
	 */
	public String forPaginationSort() {
		return getSize()+Strings.AND+getPage(this.page)+Strings.AND+getFilter();
	}
	
	/**
	 * Don't return attribute page
	 * @return Attributes: size, sort, filter 
	 */
	public String forPaginationPage() {
		return getSize()+Strings.AND+getSort()+Strings.AND+getFilter();
	}

	/**
	 * Don't return attributes page, size
	 * @return Attributes: sort, filter 
	 */
	public String forPaginationSize() {
		return getSort()+Strings.AND+getFilter();
	}

	/**
	 * Don't return attributes page, filter
	 * @return Attributes: size, sort 
	 */
	public String forFilter() {
		return getSize()+Strings.AND+getSort();
	}
	
	private String getPage(int pageNumber) {
		return getPrefix() + PAGE + Strings.EQ + this.page;
	}
	
	private String getSize() {
		return getPrefix()  + SIZE + Strings.EQ + this.size;
	}
	
	private String getFilter() {
		return getPrefix()  + FILTER + Strings.EQ + this.filter;
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
		return getPrefix()  + SORT + Strings.EQ + orderBuilder.toString();
	}
	
	private String getPrefix() {
		return prefix.length() > 0 ? prefix + PREFIX_SEPARATOR : "";
	}
}
