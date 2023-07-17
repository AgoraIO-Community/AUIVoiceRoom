package io.agora.uikit.service;

import io.agora.rtm.Metadata;

public interface IService<T> {
    /**
     * Create metadata
     * 
     * @param metadata
     * @param t
     */
    void createMetadata(Metadata metadata, T t);
}
