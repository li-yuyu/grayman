package com.lyle.grayman.plugin.rocketmq.listener;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Collections;
import java.util.List;

public class MessageListenerConcurrentlyWrapper extends MessageListenerWrapper implements MessageListenerConcurrently {
    private MessageListenerConcurrently target;

    public MessageListenerConcurrentlyWrapper(MessageListenerConcurrently target) {
        this.target = target;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                if (!tagEquals(msg)) {
                    continue;
                }

                consumeMessageBefore(msg);
                ConsumeConcurrentlyStatus status = target.consumeMessage(Collections.singletonList(msg), context);

                if (status.equals(ConsumeConcurrentlyStatus.RECONSUME_LATER)) {
                    return status;
                }
            } finally {
                consumeMessageAfter();
            }
        }

        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
