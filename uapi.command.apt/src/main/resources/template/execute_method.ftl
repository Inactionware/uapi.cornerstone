boolean isSuccess;
        try {
            isSuccess = ${userCommandField}.${runMethodName}();
        } catch (Exception ex) {
            return uapi.command.CommandResult.failure(ex);
        }
        if (isSuccess) {
            return uapi.command.CommandResult.success();
        } else {
            return uapi.command.CommandResult.failure();
        }