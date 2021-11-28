package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.animation.AnimationDetailedResponse;
import hu.bme.szgbizt.levendula.caffplacc.animation.AnimationResponse;
import hu.bme.szgbizt.levendula.caffplacc.animation.CommentResponse;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AnimationResponseMapper {
    AnimationResponse map(Animation animation);

    AnimationDetailedResponse detailedMap(Animation animationById);

    default String map(UUID value) {
        return value.toString();
    }

    CommentResponse map(Comment comment);
}
