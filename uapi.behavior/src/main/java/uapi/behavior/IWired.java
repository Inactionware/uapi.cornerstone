/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

/**
 * The interface is used wire to a specific Action output
 */
public interface IWired {

    /**
     * Wire to specific Action output by its name
     *
     * @param   actionLabel
     *          The label of Action which will be wired
     * @param   outputName
     *          The output name
     * @return  The reference object
     */
    IOutputReference toOutput(String actionLabel, String outputName);

    /**
     * Wire to specific Action output by its index
     *
     * @param   actionLabel
     *          The label of Action which will be wired
     * @param   outputIndex
     *          The output index
     * @return  The reference object
     */
    IOutputReference toOutput(String actionLabel, int outputIndex);
}
