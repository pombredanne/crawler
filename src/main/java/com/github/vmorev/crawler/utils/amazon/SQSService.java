package com.github.vmorev.crawler.utils.amazon;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.util.BinaryUtils;
import com.github.vmorev.crawler.utils.JsonHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Valentin_Morev
 * Date: 14.02.13
 */
public class SQSService extends AmazonService {
    private AmazonSQS sqs;
    private Map<String, Queue> queues = new HashMap<>();

    public AmazonSQS getSQS() {
        if (sqs == null) {
            sqs = new AmazonSQSClient(getCredentials(), new ClientConfiguration());
            //sqs.setEndpoint("https://sqs.us-east-1.amazonaws.com");
        }
        return sqs;
    }

    public <T> Queue getQueue(String name, Class<T> clazz) {
        //get name if url was provided instead of name
        if (name.contains("/"))
            name = name.substring(name.lastIndexOf("/") + 1, name.length());
        String queueName = name + "-" + clazz.getName();
        if (queues.get(queueName) == null)
            queues.put(queueName, new Queue<>(name, clazz));
        return queues.get(queueName);
    }

    public List<String> listQueues() {
        return getSQS().listQueues().getQueueUrls();
    }

    public class Queue<T> {
        private String name;
        private String url;
        private Class<T> clazz;

        public Queue(String name, Class<T> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public void createQueue() {
            if (!isQueueExists())
                getSQS().createQueue(new CreateQueueRequest().withQueueName(name));
        }

        public void deleteQueue() {
            getSQS().deleteQueue(new DeleteQueueRequest(getUrl()));
        }

        public void sendMessage(Object obj) throws IOException {
            getSQS().sendMessage(new SendMessageRequest(getUrl(), BinaryUtils.toBase64(JsonHelper.parseObject(obj).getBytes("UTF-8"))));
        }

        public void receiveMessages(ListFunc<T> func, int visibilityTimeout, int numberOfMessages) throws IOException {
            ReceiveMessageResult result = receiveMessage(visibilityTimeout, numberOfMessages);
            for (Message m : result.getMessages()) {
                T obj = decodeMessage(m);
                try {
                    func.process(obj);
                } catch (Exception e) {
                    deleteMessage(m.getReceiptHandle());
                }
            }
        }

        protected ReceiveMessageResult receiveMessage(int visibilityTimeout, int numberOfMessages) {
            ReceiveMessageRequest request = new ReceiveMessageRequest(getUrl());
            if (visibilityTimeout >= 0)
                request.setVisibilityTimeout(visibilityTimeout);
            if (numberOfMessages > 0)
                request.setMaxNumberOfMessages(numberOfMessages);
            return getSQS().receiveMessage(request);
        }

        protected void deleteMessage(String receiptHandle) {
            getSQS().deleteMessage(new DeleteMessageRequest(getUrl(), receiptHandle));
        }

        protected T decodeMessage(Message m) throws IOException {
            String mBody = m.getBody();
            if (!mBody.startsWith("{")) {
                mBody = new String(BinaryUtils.fromBase64(mBody));
            }
            return JsonHelper.parseJson(mBody, clazz);
        }

        protected String getUrl() {
            if (url == null)
                url = getSQS().getQueueUrl(new GetQueueUrlRequest(name)).getQueueUrl();
            return url;
        }

        protected boolean isQueueExists() {
            List<String> urls = getSQS().listQueues().getQueueUrls();
            for (String queueUrl : urls)
                if (queueUrl.equals(url))
                    return true;
            return false;
        }

    }
}
