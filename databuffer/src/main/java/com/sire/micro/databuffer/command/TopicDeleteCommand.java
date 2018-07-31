package com.sire.micro.databuffer.command;

import com.sire.micro.databuffer.command.core.IReceiver;

public class TopicDeleteCommand implements DeleteCommand {
    private final IReceiver receiver;
    private final int topic;

    public TopicDeleteCommand(IReceiver receiver, int topic) {
        this.receiver = receiver;
        this.topic = topic;
    }

    @Override
    public void execute() {
        receiver.deleteTopic(topic);
    }
}
