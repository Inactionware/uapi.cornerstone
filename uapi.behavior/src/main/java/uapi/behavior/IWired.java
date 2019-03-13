/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

public interface IWired {

    IOutputReference toOutput(String actionLabel, String outputName);

    IOutputReference toOutput(String actionLabel, int actionIndex);
}
