package com.lyle.grayman.plugin.rocketmq.listener;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Collections;
import java.util.List;

public class MessageListenerOrderlyWrapper extends MessageListenerWrapper implements MessageListenerOrderly {
    private MessageListenerOrderly target;

    public MessageListenerOrderlyWrapper(MessageListenerOrderly target) {
        this.target = target;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        for (MessageExt msg : msgs) {
            try {
                if (!tagEquals(msg)) {
                    continue;
                }

                consumeMessageBefore(msg);
                ConsumeOrderlyStatus status = target.consumeMessage(Collections.singletonList(msg), context);

                if (status.equals(ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT)) {
                    return status;
                }
            } finally {
                consumeMessageAfter();
            }

        }

        return ConsumeOrderlyStatus.SUCCESS;
    }


}
