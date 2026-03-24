package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.dto.AuthRequest;
import org.kluthealmighty.videostreaming.dto.UpdateChannelRequest;
import org.kluthealmighty.videostreaming.dto.UserResponse;
import org.kluthealmighty.videostreaming.entity.UserEntity;
import org.kluthealmighty.videostreaming.enums.FilePartType;
import org.kluthealmighty.videostreaming.exceptions.UserNotFoundException;
import org.kluthealmighty.videostreaming.helpers.FileOperationContext;
import org.kluthealmighty.videostreaming.repository.UserRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    // region API
    public Mono<UserResponse> findUserByChannelTag(String tag) {
        return userRepository.findByChannelTag(tag)
                .switchIfEmpty(Mono.error(new UserNotFoundException("No user found by tag:" + tag)))
                .map(this::toDomainUser);
    }


    public Mono<UserResponse> createUser(AuthRequest request) {
        return userRepository.existsByEmail(request.email())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new RuntimeException("User already exists with email: " + request.email())))
                .flatMap(_ -> createUserEntity(request))
                .map(this::toDomainUser);
    }

    public Mono<UserResponse> updateUser(Long userId, UpdateChannelRequest request, FilePart bannerPart, FilePart miniaturePart) {
        Mono<FilePart> bannerlFilePartMono = Mono.justOrEmpty(bannerPart);
        Mono<FilePart> miniatureFilePartMono = Mono.justOrEmpty(miniaturePart);

        return Mono.usingWhen(
                //Context
                Mono.just(new UpdateContext()),
                //Action
                ctx -> userRepository.findById(userId)
                        .switchIfEmpty(Mono.error(new UserNotFoundException("No user found with id: " + userId)))
                        .flatMap(userEntity -> {
                            ctx.existingUserEntity = userEntity;
                            ctx.oldBannerPath = userEntity.getBannerPath();
                            ctx.oldMiniaturePath = userEntity.getMiniaturePath();

                            return bannerlFilePartMono
                                    .flatMap(filePart -> fileService.saveFile(filePart, FilePartType.BANNER))
                                    .defaultIfEmpty(ctx.oldBannerPath);
                        })
                        .flatMap(newBannerPath -> {
                            ctx.newBannerPath = newBannerPath;
                            return miniatureFilePartMono
                                    .flatMap(filePart -> fileService.saveFile(filePart, FilePartType.MINIATURE))
                                    .defaultIfEmpty(ctx.oldMiniaturePath);
                        })
                        .flatMap(newMiniaturePath -> {
                            ctx.newMiniaturePath = newMiniaturePath;
                            return updateUserEntity(ctx.existingUserEntity, request, ctx.newBannerPath, ctx.newMiniaturePath);
                        })
                        .map(this::toDomainUser)
                        .doOnSuccess(_ -> log.info("Successfully updated user with id:" + userId)),
                //onSuccess
                ctx -> ctx.cleanup(fileService),
                //onError
                (ctx, _) -> ctx.rollback(fileService),
                //onCancel
                ctx -> ctx.cleanup(fileService)
        );
    }
    // endregion

    // region HELPERS
    private static class UpdateContext implements FileOperationContext {
        String oldBannerPath;
        String newBannerPath;
        String oldMiniaturePath;
        String newMiniaturePath;
        UserEntity existingUserEntity;

        @Override
        public Mono<Void> cleanup(FileService fileService) {
            return Flux.just(
                            !oldBannerPath.equals(newBannerPath) ? oldBannerPath : "",
                            !oldMiniaturePath.equals(newMiniaturePath) ? oldMiniaturePath : ""
                    )
                    .filter(path -> !path.isEmpty())
                    .flatMap(fileService::deleteFile)
                    .then();
        }

        @Override
        public Mono<Void> rollback(FileService fileService) {
            return Flux.just(newBannerPath, newMiniaturePath)
                    .flatMap(fileService::deleteFile)
                    .then();
        }
    }
    // endregion

    // region ENTITY-DTO
    private Mono<UserEntity> createUserEntity(AuthRequest request) {
        String encryptedPassword = passwordEncoder.encode(request.password());
        String defaultChannelTag = "id" + System.currentTimeMillis();
        String defaultChannelName = "new channel";
        UserEntity userEntity = new UserEntity(
                null,
                request.email(),
                encryptedPassword,
                defaultChannelTag,
                defaultChannelName,
                "",
                "",
                "",
                LocalDateTime.now()
        );
        return userRepository.save(userEntity);
    }

    private Mono<UserEntity> updateUserEntity(UserEntity existingUser, UpdateChannelRequest request, String newBannerPath, String newMiniaturePath) {
        UserEntity updatedUser = new UserEntity();
        updatedUser.setId(existingUser.getId());
        updatedUser.setEmail(existingUser.getEmail());
        updatedUser.setPassword(existingUser.getPassword());
        updatedUser.setChannelTag(
                (request.channelTag() != null && !request.channelTag().isEmpty())
                        ? request.channelTag()
                        : existingUser.getChannelTag()
        );
        updatedUser.setChannelName(
                (request.channelName() != null && !request.channelName().isEmpty())
                        ? request.channelName()
                        : existingUser.getChannelName()
        );
        updatedUser.setChannelDescription(
                (request.channelDescription() != null)
                        ? request.channelDescription()
                        : existingUser.getChannelDescription()
        );
        updatedUser.setBannerPath(
                !newBannerPath.isEmpty()
                        ? newBannerPath
                        : existingUser.getBannerPath()
        );
        updatedUser.setMiniaturePath(
                !newMiniaturePath.isEmpty()
                        ? newMiniaturePath
                        : existingUser.getMiniaturePath()
        );
        updatedUser.setCreatedAt(existingUser.getCreatedAt());
        return userRepository.save(updatedUser);
    }

    private UserResponse toDomainUser(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getChannelTag(),
                userEntity.getChannelName(),
                userEntity.getChannelDescription(),
                userEntity.getBannerPath(),
                userEntity.getMiniaturePath(),
                userEntity.getCreatedAt()
        );
    }
    // endregion
}
