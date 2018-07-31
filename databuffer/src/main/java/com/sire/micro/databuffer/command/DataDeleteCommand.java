package com.sire.micro.databuffer.command;

import android.support.annotation.RestrictTo;

import com.sire.micro.databuffer.command.core.ICommand;
import com.sire.micro.databuffer.command.core.IReceiver;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DataDeleteCommand implements DeleteCommand {
    private final IReceiver receiver;
    private final String key;
    private final int topic;

    public DataDeleteCommand(IReceiver receiver, int topic, String key) {
        this.receiver = receiver;
        this.topic = topic;
        this.key = key;
    }
    @Override
    public void execute() {
        receiver.deleteData(topic,key);
    }
}
