package com.github.vmorev.crawler.utils.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.vmorev.crawler.utils.HttpHelper;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 14.02.13
 */
public class S3Service extends AmazonService {
    private AmazonS3 s3;
    private Map<String, S3Bucket> buckets = new HashMap<>();

    public AmazonS3 getS3() {
        if (s3 == null)
            s3 = new AmazonS3Client(getCredentials());
        return s3;
    }

    public <T> S3Bucket getBucket(String name, Class<T> clazz) {
        String bucketName = name+"-"+clazz.getName();
        if (buckets.get(bucketName) == null)
            buckets.put(bucketName, new S3Bucket<>(name, clazz));
        return buckets.get(bucketName);
    }

    public List<Bucket> listBuckets() {
        return getS3().listBuckets();
    }

    public class S3Bucket<T> {
        private String name;
        private Class<T> clazz;

        public S3Bucket(String name, Class<T> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public void createBucket() {
            if (!isBucketExist())
                getS3().createBucket(name);
        }

        public void deleteBucket() throws Exception {
            listObjectSummaries(new ListFunc<S3ObjectSummary>() {
                public void process(S3ObjectSummary summary) {
                    getS3().deleteObject(name, summary.getKey());
                }
            });
            getS3().deleteBucket(name);
        }

        public T getObject(String key) {
            T obj = null;
            try {
                obj = JsonHelper.parseJson(getS3().getObject(name, key).getObjectContent(), clazz);
            } catch (Exception e) {
                //do nothing and return null
            }
            return obj;
        }

        public void saveObject(String key, T obj) throws IOException {
            InputStream inStream = HttpHelper.stringToInputStream(JsonHelper.parseObject(obj));
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(inStream.available());
            getS3().putObject(name, key, inStream, metadata);
        }

        public void listObjects(final ListFunc<T> func) throws Exception {
            listObjectSummaries(new ListFunc<S3ObjectSummary>() {
                public void process(S3ObjectSummary summary) throws Exception {
                    func.process(getObject(summary.getKey()));
                }
            });
        }

        public void listObjectSummaries(ListFunc<S3ObjectSummary> func) throws Exception {
            ObjectListing objectListing = getS3().listObjects(name);
            do {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    func.process(objectSummary);
                }
                objectListing.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
        }

        protected boolean isBucketExist() {
            for (Bucket bucket : listBuckets())
                if (bucket.getName().equals(name) && bucket.getOwner() != null)
                    return true;
            return false;
        }

    }
}
