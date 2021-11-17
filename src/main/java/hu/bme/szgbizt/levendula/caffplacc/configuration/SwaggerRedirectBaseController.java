package hu.bme.szgbizt.levendula.caffplacc.configuration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/")
@ApiIgnore
public class SwaggerRedirectBaseController {
    @GetMapping
    void redirectToSwagger(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestUrl = request.getRequestURL().toString(); // Levágjuk az utolsó /-t ha van
            if (requestUrl.lastIndexOf("/") == requestUrl.length() - 1) {
                requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
            }
            response.sendRedirect(requestUrl + "/swagger-ui/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}