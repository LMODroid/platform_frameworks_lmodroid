/**
 * Copyright (C) 2023 The LibeMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.libremobileos.context;

import com.libremobileos.app.ParallelSpaceManager;
import com.libremobileos.applock.AppLockManager;

/**
 * @hide
 * TODO: We need to somehow make these managers accessible via getSystemService
 */
public final class LMOContextConstants {

    /**
     * @hide
     */
    private LMOContextConstants() {
        // Empty constructor
    }

    /**
     * {@link AppLockManager}.
     *
     * @see #getSystemService(String)
     * @hide
     */
    public static final String APP_LOCK_SERVICE = "app_lock";

    /**
     * {@link ParallelSpaceManager}.
     *
     * @see #getSystemService(String)
     * @hide
     */
    public static final String PARALLEL_SERVICE = "parallel";

}
