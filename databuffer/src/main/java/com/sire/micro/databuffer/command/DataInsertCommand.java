package com.sire.micro.databuffer.command;

import android.support.annotation.RestrictTo;

import com.sire.micro.databuffer.command.core.ICommand;
import com.sire.micro.databuffer.command.core.IReceiver;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DataInsertCommand implements ICommand {

    private final IReceiver receiver;
    private final String key;
    private final Object value;
    private final int topic;
    private final long cacheTime;

    public DataInsertCommand(IReceiver receiver, int topic, String key, Object value, long cacheTime) {
        this.receiver = receiver;
        this.topic = topic;
        this.key = key;
        this.value = value;
        this.cacheTime = cacheTime;
    }

    @Override
    public void execute() {
        this.receiver.insertData(topic,key,value,cacheTime);
    }
}
