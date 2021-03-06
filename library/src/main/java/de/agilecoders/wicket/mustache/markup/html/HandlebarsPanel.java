package de.agilecoders.wicket.mustache.markup.html;

import de.agilecoders.wicket.mustache.WicketHandlebars;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

/**
 * Panel that displays the result of rendering a mustache template. The template itself can be any
 * {@link IResourceStream} implementation, of which there are a number of convenient
 * implementations in the {@link org.apache.wicket.util} package. The model can be any normal
 * {@link Map}, which will be used by handlebars while rendering the template.
 */
public abstract class HandlebarsPanel extends GenericPanel<Object> implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider {
    private static final long serialVersionUID = 14121982L;

    private transient String evaluatedTemplate;

    /**
     * Construct.
     *
     * @param id    Component id
     * @param model Model with variables that can be substituted by mustache.
     */
    public HandlebarsPanel(final String id, final IModel<Object> model) {
        super(id, model);

        setEscapeModelStrings(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        // check that no components have been added in case the generated
        // markup should not be parsed
        if (size() > 0) {
            throw new WicketRuntimeException("Components cannot be added if the generated markup should not be parsed.");
        }

        if (evaluatedTemplate == null) {
            getMarkupResourceStream(null, null);
        }

        replaceComponentTagBody(markupStream, openTag, evaluatedTemplate);
    }

    /**
     * rethrow the throwable or handle it.
     *
     * @param exception the cause
     */
    protected void onException(final RuntimeException exception) {
        throw new WicketRuntimeException(exception);
    }

    /**
     * Evaluates the template and returns the result.
     *
     * @return the result of evaluating the mustache template
     */
    private String compileTemplate(Reader templateReader) {
        // evaluate and cache template data
        if (evaluatedTemplate == null) {
            try {
                evaluatedTemplate = WicketHandlebars.compile(templateReader, getModelObject(), getEscapeModelStrings());
            } catch (RuntimeException e) {
                onException(e);
            }

            return null;
        }

        return evaluatedTemplate;
    }

    /**
     * Returns the template resource passed to the constructor.
     *
     * @return The template resource
     */
    protected abstract IResourceStream newTemplateResourceStream();

    /**
     * Gets a new reader for the mustache template.
     *
     * @return reader for the mustache template
     */
    private Reader newTemplateReader() {
        final IResourceStream resource = newTemplateResourceStream();
        if (resource == null) {
            throw new IllegalArgumentException("newTemplateResourceStream must return a resource");
        }

        final String template = ResourceUtil.readString(resource);
        if (template != null) {
            return new StringReader(template);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IResourceStream getMarkupResourceStream(final MarkupContainer container, final Class<?> containerClass) {
        final Reader reader = newTemplateReader();
        if (reader == null) {
            throw new WicketRuntimeException("could not find mustache template for panel: " + this);
        }

        // evaluate the template and return a new StringResourceStream
        return new StringResourceStream("<wicket:panel>" + compileTemplate(reader) + "</wicket:panel>");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getCacheKey(final MarkupContainer container, final Class<?> containerClass) {
        // don't cache the evaluated template
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDetach() {
        super.onDetach();

        // clear cached template data
        evaluatedTemplate = null;
    }
}
