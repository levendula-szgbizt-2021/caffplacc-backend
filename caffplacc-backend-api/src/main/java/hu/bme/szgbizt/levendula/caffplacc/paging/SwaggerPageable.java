package hu.bme.szgbizt.levendula.caffplacc.paging;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SwaggerPageable {
    @ApiModelProperty(value = "Oldalanként hány elem legyen maximum", example = "10")
    private int size;

    @ApiModelProperty(value = "A kért oldal sorszáma (0..N)", example = "0")
    private int page;

    @ApiModelProperty(value = "A rendezési feltételek formátuma: property(,asc|desc). A default rendezés növekvő (ascending). Több rendezési szempont is megadható.", example = "&sort=created,asc")
    private List<String> sort;
}
