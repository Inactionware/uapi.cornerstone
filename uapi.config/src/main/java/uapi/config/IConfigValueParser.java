/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config;

/**
 * A parser used to parse configured value to specified type
 *
 * @param   <T>
 *          The parser output type
 */
public interface IConfigValueParser<T> {

    /**
     * Test the specified type is supported or not
     *
     * @param   inType
     *          The specified input type string
     * @param   outType
     *          The output type string
     * @return  true mean supported, false mean does not
     */
    boolean isSupport(String inType, String outType);

    /**
     * The parser name
     *
     * @return  Parser name
     */
    String getName();

    /**
     * Parse specified configured value to specific value type
     *
     * @param   value
     *          Configured value
     * @return  Output value
     */
    T parse(Object value);
}
