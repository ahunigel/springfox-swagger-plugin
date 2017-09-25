package io.pluto.springfox.swagger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.spring.web.DescriptionResolver;

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
@Primary
@Order(value = SWAGGER_PLUGIN_ORDER)
public class ApiListingLookup implements ApiListingBuilderPlugin {

  private final DescriptionResolver descriptions;

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
      apiListingContext.apiListingBuilder()
          .description(descriptions.resolve(value))
          .tagNames(tagSet);
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
