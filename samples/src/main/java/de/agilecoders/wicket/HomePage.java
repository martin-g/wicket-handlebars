package de.agilecoders.wicket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.agilecoders.wicket.mustache.markup.html.ClientSideMustachePanel;
import de.agilecoders.wicket.mustache.markup.html.HandlebarsPanel;
import de.agilecoders.wicket.mustache.markup.html.LazyLoadingClientSideMustachePanel;
import org.apache.wicket.core.util.resource.PackageResourceStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Duration;

import java.util.List;
import java.util.Map;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super(parameters);

        IModel<Object> scopeModel = new LoadableDetachableModel<Object>() {
            @Override
            public Object load() {
                final List<Object> data = Lists.newArrayList();

                Map<String, Object> item1 = Maps.newHashMap();
                item1.put("name", "Item 1");
                item1.put("price", "$19.99");
                item1.put("features", Lists.newArrayList("New!", "Awesome!"));
                data.add(item1);

                Map<String, Object> item2 = Maps.newHashMap();
                item2.put("name", "Item 2");
                item2.put("price", "$29.99");
                item2.put("features", Lists.newArrayList("Old!", "Ugly!"));
                data.add(item2);

                Map<String, Object> item3 = Maps.newHashMap();
                item3.put("name", "Item 3");
                item3.put("price", "$0.99");
                item3.put("features", false);
                data.add(item3);

                Map<String, Object> map = Maps.newHashMap();
                map.put("items", data);

                return map;
            }
        };

        add(new HandlebarsPanel("template", scopeModel) {
            @Override
            protected IResourceStream newTemplateResourceStream() {
                return new PackageResourceStream(HomePage.class, "template.hbs");
            }
        });

        add(new ClientSideMustachePanel("template-client", scopeModel) {
            @Override
            protected IResourceStream newTemplateResourceStream() {
                return new PackageResourceStream(HomePage.class, "template.hbs");
            }
        });

        add(new LazyLoadingClientSideMustachePanel("template-lazy", scopeModel) {
            @Override
            protected IResourceStream newTemplateResourceStream() {
                return new PackageResourceStream(HomePage.class, "template.hbs");
            }

            @Override
            protected Duration delay() {
                return Duration.seconds(5);
            }
        });
    }

}
