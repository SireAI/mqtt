package com.sire.micro.databuffer.command;

import android.support.annotation.RestrictTo;
import android.support.v4.util.SparseArrayCompat;

import com.sire.micro.databuffer.cache.LruCache;
import com.sire.micro.databuffer.command.core.ICommand;
import com.sire.micro.databuffer.command.core.IReceiver;


@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class BatchInsertCommand implements ICommand {

    private final IReceiver receiver;
    private final Result result;
    private final SparseArrayCompat<LruCache> topics;

    public BatchInsertCommand(IReceiver receiver, SparseArrayCompat<LruCache> topics, Result result) {
        this.receiver = receiver;
        this.topics = topics;
        this.result = result;
    }

    @Override
    public void execute() {
        receiver.batchInsertData(topics, result);
    }


}
