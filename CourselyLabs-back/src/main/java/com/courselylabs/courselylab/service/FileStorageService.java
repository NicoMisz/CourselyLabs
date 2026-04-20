package com.courselylabs.courselylab.service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.courselylabs.courselylab.exception.BadRequestException;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;

@Service
public class FileStorageService {

    @Value("${storage.endpoint}")
    private String endpoint;

    @Value("${storage.access-key}")
    private String accessKey;

    @Value("${storage.secret-key}")
    private String secretKey;

    @Value("${storage.bucket}")
    private String bucket;

    @Value("${storage.presigned-url-expiry-minutes}")
    private int presignedExpiryMinutes;

    private MinioClient client;

    @PostConstruct
    public void init() {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            System.err.println("[FileStorage] Warning: could not verify/create bucket: " + e.getMessage());
        }
    }

    /**
     * Uploads a file and returns the storage key (not the URL).
     * Use getPresignedUrl() later to generate temporary download URLs.
     */
    public String upload(String objectKey, InputStream data, long size, String contentType) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(data, size, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
            return objectKey;
        } catch (Exception e) {
            throw new BadRequestException("Error al subir archivo: " + e.getMessage());
        }
    }

    public String getPresignedUrl(String storageKey) {
        try {
            return client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(storageKey)
                            .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new BadRequestException("Error al generar URL: " + e.getMessage());
        }
    }

    public void delete(String storageKey) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(storageKey)
                    .build());
        } catch (Exception e) {
            System.err.println("[FileStorage] Failed to delete " + storageKey + ": " + e.getMessage());
        }
    }
}
