package org.kluthealmighty.videostreaming.service;

import org.kluthealmighty.videostreaming.dto.AuthRequest;
import org.kluthealmighty.videostreaming.dto.UpdateChannelRequest;
import org.kluthealmighty.videostreaming.dto.UserResponse;
import org.kluthealmighty.videostreaming.entity.UserEntity;
import org.kluthealmighty.videostreaming.entity.VideoEntity;
import org.kluthealmighty.videostreaming.helpers.FileOperationContext;
import org.kluthealmighty.videostreaming.repository.UserRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    public Mono<UserResponse> findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .map(this::toDomainUser);
    }

    public Mono<UserResponse> findUserByChannelTag(String tag){
        return userRepository.findByChannelTag(tag)
                .map(this::toDomainUser);
    }


    public Mono<UserResponse> createUser(AuthRequest request){
        return userRepository.existsByEmail(request.email())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new RuntimeException("User already exists with email: " + request.email())))
                .flatMap(_ -> createUserEntity(request))
                .map(this::toDomainUser);
    }

    private Mono<UserEntity> createUserEntity(AuthRequest request){
        String encryptedPassword = passwordEncoder.encode(request.password());
        String defaultChannelTag = "id"+ System.currentTimeMillis();
        String defaultChannelName = "new channel";
        UserEntity userEntity = new UserEntity(
                null,
                request.email(),
                encryptedPassword,
                defaultChannelTag,
                defaultChannelName,
                null,
                null,
                null,
                LocalDateTime.now()
        );
        return userRepository.save(userEntity);
    }


    private Mono<UserEntity> updateUserEntity(UserEntity existingUser, UpdateChannelRequest request, String newBannerPath, String newMiniaturePath ){
        UserEntity updatedUser = new UserEntity();
        updatedUser.setId(existingUser.getId());
        updatedUser.setEmail(existingUser.getEmail());
        updatedUser.setPassword(existingUser.getPassword());
        updatedUser.setChannelTag(
                (request.channelTag() !=null && !request.channelTag().isEmpty())
                ? request.channelTag()
                : existingUser.getChannelTag()
        );
        updatedUser.setChannelName(
                (request.channelName() !=null && !request.channelName().isEmpty())
                        ? request.channelName()
                        : existingUser.getChannelName()
        );
        updatedUser.setChannelDescription(
                (request.channelDescription() !=null)
                        ? request.channelDescription()
                        : existingUser.getChannelDescription()
        );
        updatedUser.setBannerPath(
                newBannerPath != null
                        ? newBannerPath
                        : existingUser.getBannerPath()
        );updatedUser.setMiniaturePath(
                newMiniaturePath != null
                        ? newMiniaturePath
                        : existingUser.getMiniaturePath()
        );
        updatedUser.setCreatedAt(existingUser.getCreatedAt());
        return userRepository.save(updatedUser);
    }

    public Mono<UserResponse> updateUser(Long userId, UpdateChannelRequest request, FilePart bannerPart, FilePart miniaturePart){
        Mono<FilePart> bannerlFilePartMono = Mono.justOrEmpty(bannerPart);
        Mono<FilePart> miniatureFilePartMono = Mono.justOrEmpty(miniaturePart);

        return Mono.usingWhen(
                //Context
                Mono.just(new UpdateContext()),
                //Action
                ctx -> Mono.empty(),
                //onSuccess
                ctx -> ctx.cleanup(fileService),
                //onError
                (ctx, _) -> ctx.rollback(fileService),
                //onCancel
                ctx -> ctx.cleanup(fileService)

        );
    }

    private static class UpdateContext implements FileOperationContext{

        String oldBannerPath;
        String newBannerPath;
        String oldMiniaturePath;
        String newMiniaturePath;


        @Override
        public Mono<Void> cleanup(FileService fileService) {
            if (!oldBannerPath.equals(newBannerPath))
                fileService.deleteFile(oldBannerPath);
            if (!oldMiniaturePath.equals(newMiniaturePath))
                fileService.deleteFile(oldMiniaturePath);
            return null;
        }

        @Override
        public Mono<Void> rollback(FileService fileService) {
            return Flux.just(newBannerPath, newMiniaturePath)
                    .map(fileService::deleteFile)
                    .then();
        }
    }


    private UserResponse toDomainUser(UserEntity userEntity){
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

}
