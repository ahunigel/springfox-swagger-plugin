package io.pluto.springfox.swagger;

import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.ApiModel;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spring.web.DescriptionResolver;

import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

@Component
public class ApiModelLookup implements ModelBuilderPlugin {
  private final TypeResolver typeResolver;
  private final DescriptionResolver descriptions;


  public ApiModelLookup(TypeResolver typeResolver, DescriptionResolver descriptionResolver) {
    this.typeResolver = typeResolver;
    this.descriptions = descriptionResolver;
  }

  @Override
  public void apply(ModelContext context) {
    ApiModel annotation = AnnotationUtils.findAnnotation(forClass(context), ApiModel.class);
    if (annotation != null) {
      context.getBuilder().description(descriptions.resolve(annotation.description()));
    }
  }

  private Class<?> forClass(ModelContext context) {
    return typeResolver.resolve(context.getType()).getErasedType();
  }


  @Override
  public boolean supports(DocumentationType delimiter) {
    return pluginDoesApply(delimiter);
  }
}
