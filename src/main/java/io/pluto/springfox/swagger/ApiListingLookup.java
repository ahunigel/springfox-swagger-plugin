package io.pluto.springfox.swagger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.spring.web.DescriptionResolver;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static springfox.documentation.service.Tags.emptyTags;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

@Component
@Order(value = SWAGGER_PLUGIN_ORDER + 1000)
public class ApiListingLookup implements ApiListingBuilderPlugin {

  private final DescriptionResolver descriptions;

  @Autowired
  public ApiListingLookup(DescriptionResolver descriptionResolver) {
    this.descriptions = descriptionResolver;
  }

  @Override
  public void apply(ApiListingContext apiListingContext) {
    Optional<? extends Class<?>> controller = apiListingContext.getResourceGroup().getControllerClass();
    if (controller.isPresent()) {
      Optional<Api> apiAnnotation = fromNullable(findAnnotation(controller.get(), Api.class));
      String value = emptyToNull(apiAnnotation.transform(Api::value).orNull());

      Set<String> tagSet = apiAnnotation.transform(tags())
          .or(Sets.newTreeSet())
          .stream().map(tag -> descriptions.resolve(tag))
          .collect(Collectors.toSet());
      if (tagSet.isEmpty()) {
        tagSet.add(apiListingContext.getResourceGroup().getGroupName());
      }
      ApiListingBuilder builder = apiListingContext.apiListingBuilder()
          .description(descriptions.resolve(value));
      //setTagNames(builder, tagSet);
    }
  }

  private void setTagNames(ApiListingBuilder builder, Set<String> tagSet) {
    // TODO: replace tags instead of add new tags
//    builder.tagNames(tagSet);
    try {
      Field f = builder.getClass().getDeclaredField("tagNames");
      boolean accessible = f.isAccessible();
      if (!accessible) {
        f.setAccessible(true);
      }
      f.set(builder, tagSet);
      if (!accessible) {
        f.setAccessible(false);
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

  private Function<Api, String> valueExtractor() {
    return api -> api.value();
  }

  private Function<Api, Set<String>> tags() {
    return api -> newTreeSet(from(newArrayList(api.tags())).filter(emptyTags()).toSet());
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return pluginDoesApply(delimiter);
  }
}
