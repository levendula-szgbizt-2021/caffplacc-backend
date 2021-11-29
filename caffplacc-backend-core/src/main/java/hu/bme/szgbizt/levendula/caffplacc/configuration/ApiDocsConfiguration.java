package hu.bme.szgbizt.levendula.caffplacc.configuration;

import hu.bme.szgbizt.levendula.caffplacc.paging.SwaggerPageable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@Import({SwaggerRedirectBaseController.class})
public class ApiDocsConfiguration {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .directModelSubstitute(Pageable.class, SwaggerPageable.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("hu.bme.szgbizt.levendula.caffplacc"))
                .paths(PathSelectors.any())
                .build();
    }
}