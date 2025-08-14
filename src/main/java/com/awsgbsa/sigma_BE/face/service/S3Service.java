package com.awsgbsa.sigma_BE.face.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner presigner;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.default.ttl-seconds:600}")
    private long defaultTtlSeconds;

    public URL presignPut(String objectKey, String contentType) {
        Duration ttl = Duration.ofSeconds(defaultTtlSeconds);

        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                //.serverSideEncryption(ServerSideEncryption.AES256)
                .build();

        PresignedPutObjectRequest req = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(ttl)
                        .putObjectRequest(por)
                        .build()
        );

        return req.url();
    }

    public void deleteObject(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        s3Client.deleteObject(request);
    }

    public boolean existsObject(String authKey) {
        try {
            s3Client.headObject(b -> b.bucket(bucket).key(authKey));
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
