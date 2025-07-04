package org.example.expert.domain.common.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.ServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // S3 업로드
    public String uploadFile(MultipartFile file, String dirName) {
        String fileName = createFileName(file.getOriginalFilename(), dirName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicReadWrite)); // 공개 URL
        } catch (IOException e) {
            throw new ServerException("S3 업로드 실패: " + e.getMessage());
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // S3 삭제
    public void deleteFile(String fileUrl) {
        String fileKey = extractKeyFromUrl(fileUrl);
        try {
            amazonS3.deleteObject(bucket, fileKey);
        } catch (AmazonS3Exception e) {
            throw new ServerException("S3 삭제 실패 " + e.getMessage()); // 존재하지 않으면 무시
        }
    }

    // 파일 이름 생성
    private String createFileName(String originalName, String dirName) {
        String ext = originalName.substring(originalName.lastIndexOf("."));
        return dirName + "/" + UUID.randomUUID() + ext;
    }

    // URL에서 Key 추출
    private String extractKeyFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
    }
}
