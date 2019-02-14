/*
 * Copyright (C) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior;

import uapi.common.ArgumentChecker;

/**
 * The meta class hold bass information for action input argument.
 */
public final class ActionInputMeta {

//    private static final String SEP = ".";

//    public static Pair<String, String> parse(final String inputString) {
//        ArgumentChecker.required(inputString, "inputString");
//        String[] inputRef = inputString.split(".");
//        if (inputRef.length != 2) {
//            throw BehaviorException.builder()
//                    .errorCode(BehaviorErrors.INVALIDE_ACTION_INPUT_REF)
//                    .variables(new BehaviorErrors.InvalidActionInputRef().inputReference(inputString))
//                    .build();
//        }
//        return new Pair<>(inputRef[0], inputRef[1]);
//    }

    private final Class<?> _type;

    public ActionInputMeta(
            final Class<?> type
    ) {
        ArgumentChecker.required(type, "type");
        this._type = type;
    }

    /**
     * Return type of Action input.
     *
     * @return  The type of Action input
     */
    public Class<?> type() {
        return this._type;
    }
}
