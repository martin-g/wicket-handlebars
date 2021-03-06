package de.agilecoders.wicket.mustache.markup.html;

import de.agilecoders.wicket.mustache.WicketHandlebars;
import de.agilecoders.wicket.mustache.request.resource.HandlebarsJsReference;
import de.agilecoders.wicket.mustache.util.Json;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.ResourceUtil;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * Panel that displays the result of rendering a mustache template. The template itself can be any
 * {@link IResourceStream} implementation, of which there are a number of convenient
 * implementations in the {@link org.apache.wicket.util} package. The model can be any serializable
 * object, which will be used by mustache while rendering the template. The template will be rendered
 * on client side.
 *
 * @author miha
 */
public abstract class ClientSideMustachePanel extends GenericPanel<Object> implements IMarkupResourceStreamProvider {
    private static final long serialVersionUID = 14121982L;

    private String templateData;

    /**
     * Construct.
     *
     * @param id the component id
     */
    public ClientSideMustachePanel(String id) {
        this(id, null);
    }

    /**
     * Construct.
     *
     * @param id    the component id
     * @param model the template data
     */
    public ClientSideMustachePanel(String id, IModel<Object> model) {
        super(id, model);

        setOutputMarkupId(true);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        if (size() > 0) {
            throw new WicketRuntimeException("you can't add components to a ClientSideMustachePanel");
        }

        tag.getAttributes().put(WicketHandlebars.DATA_ID, newTemplate());
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(HandlebarsJsReference.instance()));

        appendRenderScript(response);
    }

    /**
     * new javascript that renders mustache compiled content into panels body.
     */
    protected void appendRenderScript(final IHeaderResponse response) {
        WicketHandlebars.appendRenderScript(this, response, createTemplateDataAsJsonString());
    }

    /**
     * @return template data
     */
    protected CharSequence createTemplateDataAsJsonString() {
        return Json.stringify(getModelObject());
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
    protected final String newTemplate() {
        if (templateData == null) {
            final IResourceStream resource = newTemplateResourceStream();
            if (resource == null) {
                throw new IllegalArgumentException("newTemplateResourceStream must return a resource");
            }

            templateData = ResourceUtil.readString(resource);
            if (templateData == null) {
                throw new IllegalArgumentException("can't find template content on given resource.");
            }
        }

        return templateData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IResourceStream getMarkupResourceStream(final MarkupContainer container, final Class<?> containerClass) {
        // evaluate the template and return a new StringResourceStream
        return new StringResourceStream(newMarkup());
    }

    /**
     * @return new markup
     */
    protected CharSequence newMarkup() {
        return "<wicket:panel></wicket:panel>";
    }

    @Override
    public void detachModels() {
        super.detachModels();

        templateData = null;
    }
}
