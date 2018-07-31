package com.sire.micro.databuffer.command.core;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface IInvoker {
    void runCommand(ICommand command);
}
