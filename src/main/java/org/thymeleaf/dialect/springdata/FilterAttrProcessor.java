package org.thymeleaf.dialect.springdata;

import java.util.Locale;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.springdata.util.Messages;
import org.thymeleaf.dialect.springdata.util.PageUtils;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

final class FilterAttrProcessor extends AbstractAttributeTagProcessor {
    private static final String ATTR_NAME = "filter";
    private static final String BUNDLE_NAME = "Filter";
    private static final int PRECEDENCE = 900;

    protected FilterAttrProcessor(final String dialectPrefix) {
        super(TemplateMode.HTML, dialectPrefix, null, false, ATTR_NAME, true, PRECEDENCE, true);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName,
            String attributeValue, IElementTagStructureHandler structureHandler) {

    	// Compose message with parameters:
        // {0} existing filter value
        // {1} page link

        Locale locale = context.getLocale();
        String messageKey = "filter";

        String filterValue = PageUtils.getFilterValue(context);

        String message = Messages.getMessage(BUNDLE_NAME, messageKey, locale, filterValue, PageUtils.createFilterUrl(context));
        structureHandler.setBody(message, false);
    }
}
