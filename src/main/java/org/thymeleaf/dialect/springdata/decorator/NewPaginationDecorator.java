package org.thymeleaf.dialect.springdata.decorator;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.springdata.Keys;
import org.thymeleaf.dialect.springdata.util.Messages;
import org.thymeleaf.dialect.springdata.util.PageUtils;
import org.thymeleaf.dialect.springdata.util.Strings;
import org.thymeleaf.model.IProcessableElementTag;

public class NewPaginationDecorator implements PaginationDecorator {
    private static final String DEFAULT_CLASS = "pagination";
    private static final String BUNDLE_NAME = NewPaginationDecorator.class.getSimpleName();
    private static final int DEFAULT_PAGE_SPLIT = 7;
	private static final String GROUP_OF_PAGES = "0";

	@Override
    public String getIdentifier() {
        return "new";
    }

	@Override
    public String decorate(final IProcessableElementTag tag, final ITemplateContext context) {

        Page<?> page = PageUtils.findPage(context);

        int pageNumber = page.getNumber();
        int totalPages = page.getTotalPages();
        int pageSplit = DEFAULT_PAGE_SPLIT;
        Object paramValue = context.getVariable(Keys.PAGINATION_SPLIT_KEY);
        if (paramValue != null) {
            pageSplit = (Integer) paramValue;
        }

        List<String> pagesLink = criarPaginacaoPara(pageNumber, totalPages, pageSplit);
        
        Locale locale = context.getLocale();
        StringBuilder builder = new StringBuilder();
        // Page links
        for (String pageLink: pagesLink) {
            int pageLinkNumber = Integer.valueOf(pageLink);
            String link = pageLinkNumber == 0 ? null : PageUtils.createPageUrl(context, pageLinkNumber - 1);
            boolean isCurrentPage = (pageLinkNumber - 1) == pageNumber;

            String msgKey = pageLinkNumber == 0 ? "page.group" : isCurrentPage ? "page.current" : "page.link";
            builder.append(Messages.getMessage(BUNDLE_NAME, msgKey, locale, pageLink, link));
        }

        boolean isUl = Strings.UL.equalsIgnoreCase(tag.getElementCompleteName());
        String currentClass = tag.getAttributeValue(Strings.CLASS);
        String clas = (isUl && !Strings.isEmpty(currentClass)) ? currentClass : DEFAULT_CLASS;

        return Messages.getMessage(BUNDLE_NAME, "pagination", locale, clas, builder.toString());
    }
	
	public List<String> criarPaginacaoPara(int paramPaginaAtual, int paramTotalDePaginas, int tamanhoPaginacao) {
		// Tratar parametros
		int totalDePaginas = paramTotalDePaginas <= 0 ? 1 : paramTotalDePaginas;
		int paginaAtual = paramPaginaAtual <= 0 ? 1 : paramPaginaAtual > totalDePaginas ? totalDePaginas : paramPaginaAtual + 1;
		int slotsPaginacao = totalDePaginas > tamanhoPaginacao ? tamanhoPaginacao : totalDePaginas;
		
		// Inicializa itens de paginacao
		String [] itensPaginacao = new String [slotsPaginacao];
		
		if (totalDePaginas <= slotsPaginacao) {
			int inicioNumeroPagina = 1;
			for (int slot=0; slot<=totalDePaginas-1; slot++) {
				itensPaginacao[slot] = String.valueOf(inicioNumeroPagina++);
			}
			return Arrays.asList(itensPaginacao);
		}
		
		// Define os extremos: primeira e última
		itensPaginacao[0] = String.valueOf("1");
		itensPaginacao[slotsPaginacao-1] = String.valueOf(totalDePaginas);
		
		// Com base na pagina atual, definir inicio
		int inicioNumeroPagina = 2;
		int inicioIndexBase0 = 1;
		int slotsEsquerdaComPaginaAtual = (slotsPaginacao/2) + 1;
		boolean inicioEtc = paginaAtual > slotsEsquerdaComPaginaAtual;
		if (inicioEtc) {
			itensPaginacao[inicioIndexBase0] = GROUP_OF_PAGES;
			inicioIndexBase0++;
			inicioNumeroPagina = paginaAtual + inicioNumeroPagina - tamanhoPaginacao/2;
		}
		
		// Com base na pagina atual, definir fim
		int slotsDireitaSemPaginaAtual = slotsPaginacao - slotsEsquerdaComPaginaAtual - 1; // -1 Página final
		int fimIndexBase0 = slotsPaginacao - 2; // -1 para base 0 e -1 para a última pagina
		int fimEtc = slotsDireitaSemPaginaAtual - (totalDePaginas - paginaAtual) + 1;
		if (fimEtc < 0) {
			itensPaginacao[fimIndexBase0] = GROUP_OF_PAGES;
			fimIndexBase0--;
		} else if (fimEtc > 0) {
			inicioNumeroPagina -= fimEtc;
			if (inicioNumeroPagina == 1) {
				inicioIndexBase0 = 0;
			}
		}

		for (int i=inicioIndexBase0; i<=fimIndexBase0; i++) {
			itensPaginacao[i] = String.valueOf(inicioNumeroPagina++);
		}
		
		return Arrays.asList(itensPaginacao);
	}
}
