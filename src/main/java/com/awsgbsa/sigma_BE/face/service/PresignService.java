package com.awsgbsa.sigma_BE.face.service;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PresignService {
    private final S3Presigner presigner;

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
}
