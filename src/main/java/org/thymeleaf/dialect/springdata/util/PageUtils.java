package org.thymeleaf.dialect.springdata.util;

import static org.thymeleaf.dialect.springdata.util.Strings.AND;
import static org.thymeleaf.dialect.springdata.util.Strings.COMMA;
import static org.thymeleaf.dialect.springdata.util.Strings.EMPTY;
import static org.thymeleaf.dialect.springdata.util.Strings.EQ;
import static org.thymeleaf.dialect.springdata.util.Strings.Q_MARK;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dialect.springdata.Keys;
import org.thymeleaf.dialect.springdata.exception.InvalidObjectParameterException;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.unbescape.html.HtmlEscape;

@SuppressWarnings("unchecked")
public final class PageUtils {
	
    private PageUtils() {
    }

    public static Page<?> findPage(final ITemplateContext context) {
        // 1. Get Page object from local variables (defined with sd:page-object)
        // 2. Search Page using ${page} expression
        // 3. Search Page object as request attribute

        final Object pageFromLocalVariable = context.getVariable(Keys.PAGE_VARIABLE_KEY);
        if (isPageInstance(pageFromLocalVariable)) {
            return (Page<?>) pageFromLocalVariable;
        }

        // Check if not null and Page instance available with ${page} expression
        final IEngineConfiguration configuration = context.getConfiguration();
        final IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        final IStandardExpression expression = parser.parseExpression(context, Keys.PAGE_EXPRESSION);
        final Object page = expression.execute(context);
        if (isPageInstance(page)) {
            return (Page<?>) page;
        }

        // Search for Page object, and only one instance, as request attribute
        if (context instanceof IWebContext) {
            HttpServletRequest request = ((IWebContext) context).getRequest();
            Enumeration<String> attrNames = request.getAttributeNames();
            Page<?> pageOnRequest = null;
            while (attrNames.hasMoreElements()) {
                String attrName = (String) attrNames.nextElement();
                Object attr = request.getAttribute(attrName);
                if (isPageInstance(attr)) {
                    if (pageOnRequest != null) {
                        throw new InvalidObjectParameterException("More than one Page object found on request!");
                    }

                    pageOnRequest = (Page<?>) attr;
                }
            }

            if (pageOnRequest != null) {
                return pageOnRequest;
            }
        }

        throw new InvalidObjectParameterException("Invalid or not present Page object found on request!");
    }

    public static String createPageUrl(final ITemplateContext context, int pageNumber) {
        final String baseUrl = buildBaseUrl(context);

        final String prefix = getParamPrefix(context);
        final Page<?> page = findPage(context);
        String parameters = Parameters.from(prefix, page.getPageable(), PageUtils.getFilterValue(context)).forPaginationPage();

        return buildUrl(baseUrl + Strings.Q_MARK + parameters, context).append(Parameters.PAGE).append(EQ).append(pageNumber).toString();
    }

    public static String createFilterUrl(final ITemplateContext context) {
        final String baseUrl = buildBaseUrl(context);

        final String prefix = getParamPrefix(context);
        final Page<?> page = findPage(context);
        String parameters = Parameters.from(prefix, page.getPageable(), PageUtils.getFilterValue(context)).forFilter();

        return baseUrl + Strings.Q_MARK + parameters;
    }

    /**
     * Creates an url to sort data by fieldName
     * 
     * @param context execution context
     * @param fieldName field name to sort
     * @param forcedDir optional, if specified then only this sort direction will be allowed
     * @return sort URL
     */
    public static String createSortUrl(final ITemplateContext context, final String fieldName, final Direction forcedDir) {
        // Params can be prefixed to manage multiple pagination on the same page
        final String baseUrl = buildBaseUrl(context);

        final String prefix = getParamPrefix(context);
        final Page<?> page = findPage(context);
        String parameters = Parameters.from(prefix, page.getPageable(), PageUtils.getFilterValue(context)).forPaginationSort();

        final StringBuilder sortParam = new StringBuilder();
        final Sort sort = page.getSort();
        final boolean hasPreviousOrder = sort != null && sort.getOrderFor(fieldName) != null;
        if (forcedDir != null) {
            sortParam.append(fieldName).append(COMMA).append(forcedDir.toString().toLowerCase());
        } else if (hasPreviousOrder) {
            // Sort parameters exists for this field, modify direction
            Order previousOrder = sort.getOrderFor(fieldName);
            Direction dir = previousOrder.isAscending() ? Direction.DESC : Direction.ASC;
            sortParam.append(fieldName).append(COMMA).append(dir.toString().toLowerCase());
        } else {
            sortParam.append(fieldName);
        }

        return buildUrl(baseUrl + Strings.Q_MARK + parameters, context).append(Parameters.SORT).append(EQ).append(sortParam).toString();
    }

    public static String createPageSizeUrl(final ITemplateContext context, int pageSize) {

        final String prefix = getParamPrefix(context);
        final Page<?> page = findPage(context);
        String parameters = Parameters.from(prefix, page.getPageable(), PageUtils.getFilterValue(context)).forPaginationSize();

        final String baseUrl = buildBaseUrl(context);

        return buildUrl(baseUrl + Strings.Q_MARK + parameters, context).append(Parameters.SIZE).append(EQ).append(pageSize).toString();
    }

    public static int getFirstItemInPage(final Page<?> page) {
        return page.getSize() * page.getNumber() + 1;
    }

    public static int getLatestItemInPage(final Page<?> page) {
        return page.getSize() * page.getNumber() + page.getNumberOfElements();
    }
    
    public static boolean isFirstPage(Page<?> page) {
    		if( page.getTotalPages()==0 ) {
    			return true;
    		}
    		
    		return page.isFirst();
    }
    
    public static boolean hasPrevious(Page<?> page) {
    		return page.getTotalPages()>0 && page.hasPrevious();
    }

    private static String buildBaseUrl(final ITemplateContext context) {
        // URL defined with pagination-url tag
        final String sdUrl = (String) context.getVariable(Keys.PAGINATION_URL_KEY);

        if (sdUrl == null && context instanceof IWebContext) {
            // Creates url from actual request URI and parameters
            final StringBuilder builder = new StringBuilder();
            final IWebContext webContext = (IWebContext) context;
            final HttpServletRequest request = webContext.getRequest();

            // URL base path from request
            builder.append(request.getRequestURI());

            // Escape to HTML content
            return HtmlEscape.escapeHtml4Xml(builder.toString());
        } else if (sdUrl != null) {
        	// return without parameters
        	return HtmlEscape.escapeHtml4Xml(sdUrl.split("\\?")[0]);
        }

        return sdUrl == null ? EMPTY : sdUrl;
    }

    private static boolean isPageInstance(Object page) {
        return page != null && (page instanceof Page<?>);
    }

    private static StringBuilder buildUrl(String baseUrl, final ITemplateContext context) {
        final String paramAppender = String.valueOf(baseUrl).contains(Q_MARK) ? AND : Q_MARK;
        final String prefix = getParamPrefix(context);

        return new StringBuilder(baseUrl).append(paramAppender).append(prefix);
    }

    private static String getParamPrefix(final ITemplateContext context) {
        final String prefix = (String) context.getVariable(Keys.PAGINATION_QUALIFIER_PREFIX);

        return prefix == null ? EMPTY : prefix.concat(Parameters.PREFIX_SEPARATOR);
    }

	public static String getFilterValue(final ITemplateContext context) {
        String filterValue = null;
        if (context instanceof IWebContext) {
            HttpServletRequest request = ((IWebContext) context).getRequest();
            filterValue = request.getParameter(Parameters.FILTER);
        }
		return filterValue == null? "":filterValue;
	}

}
