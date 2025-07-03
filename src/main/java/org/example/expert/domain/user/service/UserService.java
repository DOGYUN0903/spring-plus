package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.service.S3Service;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserProfileChangeResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${custom.default-profile-image-url}")
    private String defaultProfileImageUrl;
    private final S3Service s3Service;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    @Transactional
    public UserProfileChangeResponse updateProfileImage(AuthUser authUser, MultipartFile file) {

        // 유저 조회
        User findUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // 기존 이미지가 기본 이미지가 아니면 S3에서 삭제
        String oldImageUrl = findUser.getProfileImageUrl();
        if (oldImageUrl != null && !oldImageUrl.equals(defaultProfileImageUrl)) {
            s3Service.deleteFile(oldImageUrl);
        }

        // 새 이미지 업로드
        String newImageUrl = s3Service.uploadFile(file, "profile");

        // db 저장
        findUser.updateProfileImage(newImageUrl);

        // 응답 생성
        return new UserProfileChangeResponse(newImageUrl);
    }

    public UserProfileChangeResponse deleteProfileImage(AuthUser authUser) {

        // 유저 찾기
        User findUser = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // 현재 유저의 프로필 url
        String currentImageUrl = findUser.getProfileImageUrl();

        if (currentImageUrl != null && !currentImageUrl.equals(defaultProfileImageUrl)) {
            s3Service.deleteFile(currentImageUrl);
        }

        findUser.updateProfileImage(defaultProfileImageUrl);

        return new UserProfileChangeResponse(defaultProfileImageUrl);
    }
}
