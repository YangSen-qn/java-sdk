package com.qiniu.storage;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Configuration 测试类
 *
 * @author yangsen
 */
public class ConfigurationTest {

    @Test
    @Tag("UnitTest")
    public void testDefault() {
        Configuration cfg = new Configuration();
        assertEquals(Configuration.ResumableUploadAPIVersion.V1, cfg.resumableUploadAPIVersion);

        cfg = new Configuration(Region.autoRegion());
        assertEquals(Configuration.ResumableUploadAPIVersion.V1, cfg.resumableUploadAPIVersion);

        cfg = Configuration.create();
        assertEquals(Configuration.ResumableUploadAPIVersion.V2, cfg.resumableUploadAPIVersion);

        cfg = Configuration.create(Region.autoRegion());
        assertEquals(Configuration.ResumableUploadAPIVersion.V2, cfg.resumableUploadAPIVersion);
    }
}
