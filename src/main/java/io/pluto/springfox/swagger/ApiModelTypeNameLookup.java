package io.pluto.springfox.swagger;

import io.swagger.annotations.ApiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.TypeNameProviderPlugin;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.emptyToNull;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;

@Component
@Order(value = SWAGGER_PLUGIN_ORDER - 100)
public class ApiModelTypeNameLookup implements TypeNameProviderPlugin {
  private final DescriptionResolver descriptions;

  @Autowired
  public ApiModelTypeNameLookup(DescriptionResolver descriptionResolver) {
    this.descriptions = descriptionResolver;
  }

  @Override
  public String nameFor(Class<?> type) {
    ApiModel annotation = findAnnotation(type, ApiModel.class);
    String defaultTypeName = type.getSimpleName();
    if (annotation != null) {
      return fromNullable(emptyToNull(descriptions.resolve(annotation.value()))).or(defaultTypeName);
    }
    return defaultTypeName;
  }


  @Override
  public boolean supports(DocumentationType delimiter) {
    return SwaggerPluginSupport.pluginDoesApply(delimiter);
  }
}
