/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.util;

import android.content.Context;
import android.os.Looper;

import com.android.launcher3.MainThreadExecutor;

import java.util.concurrent.ExecutionException;

import androidx.annotation.VisibleForTesting;

/**
 * Utility class for defining singletons which are initiated on main thread.
 */
public class MainThreadInitializedObject<T> {

    private final ObjectProvider<T> mProvider;
    private T mValue;

    public MainThreadInitializedObject(ObjectProvider<T> provider) {
        mProvider = provider;
    }

    public T get(Context context) {
        if (mValue == null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mValue = mProvider.get(context.getApplicationContext());
                onPostInit(context);
            } else {
                try {
                    return new MainThreadExecutor().submit(() -> get(context)).get();
                } catch (InterruptedException|ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return mValue;
    }

    protected void onPostInit(Context context) { }

    public T getNoCreate() {
        return mValue;
    }

    @VisibleForTesting
    public void initializeForTesting(T value) {
        mValue = value;
    }

    public interface ObjectProvider<T> {

        T get(Context context);
    }
}
