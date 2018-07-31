package com.sire.micro.databuffer.command;

import com.sire.micro.databuffer.command.core.ICommand;
import com.sire.micro.databuffer.command.core.IReceiver;

public class TopicInsertCommand implements ICommand {
    private final int topic;
    private final int maxSize;
    private final long expiredTime;
    private final IReceiver receiver;

    public TopicInsertCommand(IReceiver receiver, int topic, int maxSize, long expiredTime) {
        this.receiver = receiver;
        this.topic = topic;
        this.maxSize = maxSize;
        this.expiredTime = expiredTime;
    }

    @Override
    public void execute() {
        receiver.insertTopicCache( topic,  maxSize,  expiredTime);
    }
}
