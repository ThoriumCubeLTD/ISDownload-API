package com.infernalsuite.isdownloadapi.util;

import org.jetbrains.annotations.UnknownNullability;

import java.time.Instant;

public interface TimeSource {
    @UnknownNullability Instant time();
}
