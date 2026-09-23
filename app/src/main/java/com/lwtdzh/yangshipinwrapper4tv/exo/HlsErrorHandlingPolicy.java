package com.lwtdzh.yangshipinwrapper4tv.exo;
import androidx.annotation.NonNull;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import java.io.IOException;

public class HlsErrorHandlingPolicy extends DefaultLoadErrorHandlingPolicy {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 500;

    public HlsErrorHandlingPolicy() {
        super();
    }

    @Override
    public long getRetryDelayMsFor(@NonNull LoadErrorHandlingPolicy.LoadErrorInfo loadErrorInfo) {
        if (isChunkError(loadErrorInfo)) {
            return RETRY_DELAY_MS;
        }
        return super.getRetryDelayMsFor(loadErrorInfo);
    }

    @Override
    public int getMinimumLoadableRetryCount(int dataType) {
        if (dataType == C.DATA_TYPE_MEDIA) {
            return MAX_RETRIES;
        }
        return super.getMinimumLoadableRetryCount(dataType);
    }

    @Override
    public LoadErrorHandlingPolicy.FallbackSelection getFallbackSelectionFor(
            @NonNull FallbackOptions fallbackOptions,
            @NonNull LoadErrorInfo loadErrorInfo) {
        if (isChunkError(loadErrorInfo)) {
            return null;
        }
        return super.getFallbackSelectionFor(fallbackOptions, loadErrorInfo);
    }

    private boolean isChunkError(@NonNull LoadErrorInfo loadErrorInfo) {
        Throwable error = loadErrorInfo.exception;
        return error instanceof IOException;
    }
}