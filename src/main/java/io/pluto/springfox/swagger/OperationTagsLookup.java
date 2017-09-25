package io.pluto.springfox.swagger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.spring.web.readers.operation.DefaultTagsProvider;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static com.google.common.collect.Sets.union;
import static springfox.documentation.service.Tags.emptyTags;

@Component
public class OperationTagsLookup implements OperationBuilderPlugin {
  private final DescriptionResolver descriptions;
  private final DefaultTagsProvider tagsProvider;

  @Autowired
  public OperationTagsLookup(DescriptionResolver descriptions, DefaultTagsProvider tagsProvider) {
    this.descriptions = descriptions;
    this.tagsProvider = tagsProvider;
  }

  @Override
  public void apply(OperationContext context) {
    Set<String> defaultTags = tagsProvider.tags(context);
    Sets.SetView<String> tags = union(operationTags(context), controllerTags(context));
    if (tags.isEmpty()) {
      context.operationBuilder().tags(defaultTags);
    } else {
      context.operationBuilder().tags(tags.stream().map(t -> descriptions.resolve(t)).collect(Collectors.toSet()));
    }
  }

  private Set<String> controllerTags(OperationContext context) {
    Optional<Api> controllerAnnotation = context.findControllerAnnotation(Api.class);
    return controllerAnnotation.transform(tagsFromController()).or(Sets.<String>newHashSet());
  }

  private Set<String> operationTags(OperationContext context) {
    Optional<ApiOperation> annotation = context.findAnnotation(ApiOperation.class);
    return annotation.transform(tagsFromOperation()).or(Sets.<String>newHashSet());
  }

  private Function<ApiOperation, Set<String>> tagsFromOperation() {
    return new Function<ApiOperation, Set<String>>() {
      @Override
      public Set<String> apply(ApiOperation input) {
        Set<String> tags = newTreeSet();
        tags.addAll(from(newArrayList(input.tags())).filter(emptyTags()).toSet());
        return tags;
      }
    };
  }

  private Function<Api, Set<String>> tagsFromController() {
    return new Function<Api, Set<String>>() {
      @Override
      public Set<String> apply(Api input) {
        Set<String> tags = newTreeSet();
        tags.addAll(from(newArrayList(input.tags())).filter(emptyTags()).toSet());
        return tags;
      }
    };
  }

  @Override
  public boolean supports(DocumentationType documentationType) {
    return true;
  }
}
