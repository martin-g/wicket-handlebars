package de.agilecoders.wicket.mustache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.ExecutionException;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.resource.PackageResourceStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.GuavaTemplateCache;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.agilecoders.wicket.mustache.markup.html.HandlebarsPanel;
import de.agilecoders.wicket.webjars.WicketWebjars;

/**
 * Base util class.
 *
 * @author miha
 */
public final class WicketHandlebars {

    public static final String DATA_ID = "data-template";

	/**
	 * For use by Handlebars.java internally.
	 */
	private static final Cache<TemplateSource, Template> TEMPLATE_CACHE = CacheBuilder
			.newBuilder()
			.build();

	/**
	 * Handlebars.java does not cache reads of Template content from resources.
	 */
	private static final LoadingCache<Reader, Template> COMPILATION_CACHE = CacheBuilder
			.newBuilder()
			.build(new CacheLoader<Reader, Template>() {
				@Override
				public Template load(Reader input) throws Exception {
					String template = IOUtils.toString(input);
					return HANDLEBARS.compileInline(template);
				}
			});

	/**
	 */
	private static final Handlebars HANDLEBARS = new Handlebars().with(new GuavaTemplateCache(TEMPLATE_CACHE));


    /**
     * Convenience factory method to create a {@link de.agilecoders.wicket.mustache.markup.html.HandlebarsPanel} instance with a given
     * {@link IResourceStream} template resource.
     *
     * @param id               Component id
     * @param model            optional model for variable substitution.
     * @param templateResource The template resource
     * @return an instance of {@link de.agilecoders.wicket.mustache.markup.html.HandlebarsPanel}
     */
    public static HandlebarsPanel newHandlebarsTemplatePanel(final String id, final IModel<Object> model, final IResourceStream templateResource) {
        Args.notNull(templateResource, "templateResource");
        Args.notNull(model, "model");

        return new HandlebarsPanel(id, model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected IResourceStream newTemplateResourceStream() {
                return templateResource;
            }
        };
    }

    /**
     * creates a mustache javascript that renders a template with given content.
     *
     * @param component The mustache component
     * @param content   The content to render
     * @return new javascript that renders the mustache template with given content
     */
    public static CharSequence createRenderScript(final Component component, final CharSequence content) {
        final String markupId = component.getMarkupId(true);

        return String.format("var $el = $('#%s'); var tmpl = Handlebars.compile($el.attr('%s')); $el.html(tmpl(%s))",
		        markupId, WicketHandlebars.DATA_ID, content);
    }

    /**
     * appends a mustache javascript that renders a template with given content.
     *
     * @param component The mustache component
     * @param response  current header response
     * @param content   The content to render
     */
    public static void appendRenderScript(final Component component, final IHeaderResponse response, final CharSequence content) {
        response.render(OnDomReadyHeaderItem.forScript(createRenderScript(component, content)));
    }

    /**
     * Gets a new reader for the mustache template.
     *
     * @param templateName The name of the template
     * @param component    the reference component
     * @return reader for the mustache template
     */
    public static Reader newTemplateReader(final String templateName, final Component component) throws ResourceStreamNotFoundException
    {
	    PackageResourceStream resourceStream = new PackageResourceStream(component.getClass(), templateName);
	    InputStream inputStream = resourceStream.getInputStream();
	    return new InputStreamReader(inputStream);
    }

    /**
     * compiles given template without any template data. "escapeHtml" is set to false.
     *
     * @param template  The template id
     * @return compiled template
     */
    public static String compile(final Reader template) {
        return compile(template, null, false);
    }

    /**
     * compiles given template with given template data. "escapeHtml" is set to false.
     *
     * @param template       The template
     * @param data           The template data
     * @return compiled template
     */
    public static String compile(final Reader template, final Object data) {
        return compile(template, data, false);
    }

    /**
     * compiles given template with given template data.
     *
     * @param templateReader The template
     * @param data           The template data
     * @param escapeHtml     whether to escape HTML characters
     * @return compiled template
     */
    public static String compile(final Reader templateReader, final Object data, final boolean escapeHtml) {
        
        String evaluatedTemplate = "========== ERROR ===========";
        try {
	        Template template = COMPILATION_CACHE.get(templateReader);

	        if (template != null) {
		        // convert writer to string.
		        evaluatedTemplate = template.apply(data);

		        if (escapeHtml)
		        {
			        // encode the result in order to get valid html output that
			        // does not break the rest of the page
			        evaluatedTemplate = Strings.escapeMarkup(evaluatedTemplate).toString();
		        }
	        }
        } catch (ExecutionException e) {
	        e.printStackTrace();
            evaluatedTemplate = "";
        } catch (IOException e) {
	        e.printStackTrace();
            evaluatedTemplate = "";
        }

        return evaluatedTemplate;
    }

    /**
     * install all mustache configurations
     *
     * @param app current web application
     */
    public static void install(final WebApplication app) {
        WicketWebjars.install(app);
    }

    /**
     * private constructor.
     */
    private WicketHandlebars() {
        throw new UnsupportedOperationException();
    }
}
