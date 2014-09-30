package de.agilecoders.wicket.mustache.request.resource;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

/**
 * A resource reference for Handlebars JavaScript resources.
 */
public class HandlebarsJsReference extends WebjarsJavaScriptResourceReference {
    private static final long serialVersionUID = 1L;

    private static final String FILENAME = "handlebars/current/handlebars.js";

    /**
     * instance holder of {@link HandlebarsJsReference}
     */
    private static final class Holder {
        private static final HandlebarsJsReference instance = new HandlebarsJsReference();
    }

    /**
     * @return unique {@link HandlebarsJsReference} instance
     */
    public static HandlebarsJsReference instance() {
        return Holder.instance;
    }

    /**
     * Construct. Uses the recent mustache version.
     */
    public HandlebarsJsReference() {
        super(FILENAME);
    }
}
