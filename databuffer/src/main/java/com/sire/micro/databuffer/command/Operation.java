package com.sire.micro.databuffer.command;

import android.support.annotation.RestrictTo;

import com.sire.micro.databuffer.Log;
import com.sire.micro.databuffer.command.core.ICommand;
import com.sire.micro.databuffer.command.core.IInvoker;

import static com.sire.micro.databuffer.DataBuffer.AUTOMATIC;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Operation implements IInvoker {
    private static final String TAG = "Operation";
    private final int strategy;

    public Operation(int strategy) {
        this.strategy = strategy;
    }


    @Override
    public void runCommand(ICommand command) {
        //为保证数据同步，手动模式时删除操作需要同步
        if(strategy == AUTOMATIC || command instanceof DeleteCommand){
            command.execute();
        }else {
            Log.w(TAG,"current strategy in not automatic , command will not be excuted!");
        }
    }
}
